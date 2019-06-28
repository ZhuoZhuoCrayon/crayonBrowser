package demo.download;

import demo.util.JaxbUtil;

import java.io.*;
import java.net.HttpURLConnection;
import javafx.scene.control.ProgressBar;

/**
 * 文件保存规则：
 * 断点临时文件的保存：相对路径 tmpFile/tmpFile_name.txt
 * 断点临时文件的命名规则：file_downloadPath-threadId
 *
 * 下载文件/图片的保存：相对路径 downloadFile/file_downloadPath
 */

/**
 * 继承基础的下载类
 * 实现断点续传功能
 */
public class URLdownload_backpoint extends URLdownload{
    public ProgressBar bar;
    //protected  String tmpFilePath="src/tmpFile/";   //断点续传标记文件存储路径
    protected  int finishDownload_threadNum;        //已完成的下载线程数
    public URLdownload_backpoint(String fileName, String address, int threadNum) {
        super(fileName, address, threadNum);
        this.finishDownload_threadNum=0;
    }
    public URLdownload_backpoint(String fileName, String address,String save_path, int threadNum) {
        super(fileName, address, save_path, threadNum);
        this.finishDownload_threadNum=0;
        bar=new ProgressBar();
    }

    /**
     * 内部下载线程类，继承父类的内部类
     * 嵌入断点文件更新功能
     */
    class downloadThread_n extends downloadThread{

        downloadThread_n(int _threadId, int _beginIndex, int _endIndex) {
            super(_threadId, _beginIndex, _endIndex);
        }
        @Override
        public void run(){
            HttpURLConnection request= Request(address);
            try {
                int hadRead=0;
                File tmpfile=new File(JaxbUtil.BREAK_POINT +fileName+threadId+".txt");

                /*
                    如果所下载文件的断点文件存在，表示未下载完
                    读取断点，从断点处下载
                    bug：断点文件命名漏洞，下载同个文件但设置的线程通道不同
                        会造成误读，导致下载文件内容重叠或缺失
                 */
                if(tmpfile.exists()){
                    FileInputStream fis=new FileInputStream(tmpfile);
                    BufferedReader bufr=new BufferedReader(new InputStreamReader(fis));
                    hadRead=Integer.parseInt(bufr.readLine());
                    beginIndex+=hadRead;
                    fis.close();
                }
                request.setRequestProperty("Range","bytes="+beginIndex+"-"+endIndex);
                //System.out.println(threadId+" "+beginIndex+" "+ endIndex);
                if (request.getResponseCode() == 200||request.getResponseCode() == 206) {

                    InputStream ins=request.getInputStream();
                    File file=new File(file_downloadPath);
                    RandomAccessFile raf=new RandomAccessFile(file,"rwd");
                    raf.seek(beginIndex);


                    byte[] bytes=new byte[bytesLength];
                    int len=0;
                    int byteCount=+hadRead;
                    while((len=ins.read(bytes))!=-1){
                        if(STOP){
                            raf.close();
                        }

                        raf.write(bytes,0,len);
                        byteCount+=len;
                        Percents[threadId]=byteCount;
                        //bar.setProgress(Percents[0]*1.0/partWorks[0]);
                        //创建临时文件，更新断点
                        RandomAccessFile tmpraf=new RandomAccessFile(tmpfile,"rwd");
                        tmpraf.write((byteCount+"").getBytes());
                        tmpraf.close();
                    }
                    raf.close();
                    delTmp_file();  //下载完毕，删除对应文件
                }
            }catch (Exception e){
                currentThread().interrupt();
            }
        }

        /**
         * 下载完毕后，用于删除断点文件
         * 设置锁，防止读写冲突
         */
        public synchronized void delTmp_file() {
            finishDownload_threadNum++;
            //System.out.println(finishDownload_threadNum);
            if (finishDownload_threadNum == threadNum) {
                for (int i = 0; i < threadNum; i++) {
                    File finishFile = new File(JaxbUtil.BREAK_POINT + fileName + i + ".txt");
                    finishFile.delete();
                }
                finishDownload_threadNum = 0;
            }
        }
    }

    @Override
    public void Download(){
        System.out.println(address);
        HttpURLConnection request=Request(address);
        try {
            if (request.getResponseCode() == 200) {
                int len=request.getContentLength();
                //System.out.println(len);
                File file=new File(file_downloadPath);
                RandomAccessFile raf=new RandomAccessFile(file,"rwd");
                raf.setLength(len);
                raf.close();

                int size=len/threadNum;

                for(int i=0;i<threadNum;i++){
                    int beginIndex=i*size;
                    int endIndex=(i+1)*size-1;
                    if(i==threadNum-1){
                        endIndex=len-1;
                    }
                    partWorks[i]=endIndex-beginIndex+1;
                    new downloadThread_n(i,beginIndex,endIndex).start();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
