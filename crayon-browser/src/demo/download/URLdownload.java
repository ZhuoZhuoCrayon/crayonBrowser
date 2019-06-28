package demo.download;

import demo.util.JaxbUtil;
import javafx.scene.paint.Stop;

import javax.swing.*;
import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 文件保存规则：
 * 断点临时文件的保存：相对路径 tmpFile/tmpFile_name.txt
 * 断点临时文件的命名规则：file_downloadPath-threadId
 *
 * 下载文件/图片的保存：相对路径 downloadFile/file_downloadPath
 */
public class URLdownload {
    protected static int bytesLength=1024;    //读取字长
    protected static int maxSupportNum=10;      //单文件-多线程下载模式线程最大支持数
    protected static int Timeout=5000;          //访问时间限制
    protected int threadNum;            //下载进程数
    protected  String file_downloadPath="src/downloadFile/";    //下载文件默认保存路径
    protected String fileName;      //文件名-name.type
    protected  String address;      //url
    public int[] partWorks;         //每个下载线程的工作量
    public int[] Percents;          //每个下载线程的下载进度
    protected boolean STOP;         //下载状态-false暂停|true正在下

    /**
     * @name 构造方法
     * @param fileName  文件名，包括文件保存名及文件保存类型
     * @param address   文件下载的url地址
     * @param threadNum 请求的下载进程数
     */
    public URLdownload(String fileName,String address,int threadNum){
        if(threadNum>maxSupportNum){
            threadNum=maxSupportNum;
        }
        this.threadNum=threadNum;
        this.address=new String(address);
        this.fileName=fileName;
        this.file_downloadPath+=fileName;
        this.STOP=false;
        partWorks=new int[threadNum];
        Percents=new int[threadNum];
    }

    public URLdownload(String fileName,String url,String save_path,int threadNum){
        if(threadNum>maxSupportNum){
            threadNum=maxSupportNum;
        }
        this.threadNum=threadNum;
        this.address=new String(url);
        this.fileName=fileName;
        this.file_downloadPath = JaxbUtil.DOWNLOAD_PATH + this.fileName;
        this.STOP=false;
        partWorks=new int[threadNum];
        Percents=new int[threadNum];
    }

    /**
     * 内部下载线程类
     */
    class downloadThread extends Thread{
        int beginIndex;
        int endIndex;
        int threadId;

        /**
         *
         * @param _threadId 下载线程id
         * @param _beginIndex   下载索引起始
         * @param _endIndex
         */
        downloadThread(int _threadId,int _beginIndex,int _endIndex){
            super();
            beginIndex=_beginIndex;
            endIndex=_endIndex;
            threadId=_threadId;
        }
        @Override
        public void run(){
            //请求url
            HttpURLConnection request=URLdownload.Request(address);
            request.setRequestProperty("Range","bytes="+beginIndex+"-"+endIndex);
            try {
                System.out.println(threadId+":"+beginIndex+"-"+endIndex);
                //响应码为200表示连接正常
                //响应码206表示从指定位置连接
                if (request.getResponseCode() == 200||request.getResponseCode() == 206) {

                    //获取输入流
                    InputStream ins=request.getInputStream();
                    File file=new File(file_downloadPath);

                    //RandomAccessFile类支持文件定点写入
                    //作为支持断点续传和多线程上传的基础
                    RandomAccessFile raf=new RandomAccessFile(file,"rwd");
                    raf.seek(beginIndex);   //从begin位置才是

                    byte[] bytes=new byte[URLdownload.bytesLength];
                    int len=0;
                    int byteCount=0;
                    while((len=ins.read(bytes))!=-1){

                        //收到结束信号，关闭
                        //手动滑稽
                        if(STOP){
                            raf.close();
                        }
                        raf.write(bytes,0,len);
                        byteCount+=len;
                        //更新进度
                        Percents[threadId]=byteCount;
                    }
                    //关闭流
                    raf.close();
                }
            }catch (Exception e){
                //关闭流的处理，直接结束线程
                Thread.currentThread().interrupt();
                //e.printStackTrace();
            }
        }
    }

    /**
     * 下载方法，由用户端调用
     */
    public void Download(){
        //建立url连接
        HttpURLConnection request=Request(address);
        try {
            if (request.getResponseCode() == 200) {
                //获取下载内容长度
                int len=request.getContentLength();
                System.out.println(len);
                //建立下载文件及下载流
                File file=new File(file_downloadPath);
                RandomAccessFile raf=new RandomAccessFile(file,"rwd");
                raf.setLength(len);
                raf.close();

                //为每个下载线程分配任务
                int size=len/threadNum;
                for(int i=0;i<threadNum;i++){
                    int beginIndex=i*size;
                    int endIndex=(i+1)*size+1;
                    if(i==threadNum-1){
                        endIndex=len-1;
                    }
                    //打表记录
                    partWorks[i]=endIndex-beginIndex+1;
                    new downloadThread(i,beginIndex,endIndex).start();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    /**暂停下载，用户端调用*/
    public void STOP(){
        STOP=true;
    }

    /**
     * @param address_path  下载文件的url
     * @return  返回建立连接的HttpURLConnection
     */
    protected static HttpURLConnection Request(String address_path){
        try {
            URL url = new URL(address_path);
            HttpURLConnection connection=(HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            //设置响应时间阈值
            connection.setConnectTimeout(Timeout);
            connection.setReadTimeout(Timeout);
            return connection;
        }catch (Exception e){
            e.printStackTrace();
            //下载出错处理，直接退出
            //由于有断点保存，由用户选择是否下载，无需服务端浪费资源重连重下
            return null;
        }
    }
}