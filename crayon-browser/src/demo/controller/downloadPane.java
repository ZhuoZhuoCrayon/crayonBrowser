package demo.controller;

import demo.download.URLdownload_backpoint;
import demo.util.JaxbUtil;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

/**
 * @author caixiaoxin
 * @TODO 下载管理，更新进度条
 */
public class downloadPane {
    public String download_info;    //下载存储信息
    public HBox mainPane;           //主面板
    public URLdownload_backpoint downloadWork;  //断点下载任务
    public ProgressBar bar;     //下载进度
    public Label label;         //下载名称标签
    public Button startButton = new Button("Start");
    public Button stopButton = new Button("Stop");
    public int threadNum;   //下载线程数，默认为4
    Task copyWorker;        //监控进度条事件

    downloadPane(String name,String address,String path,int threadNum){
        this.download_info = name+"@#@"+address+"@#@"+path; //构造存储信息

        this.threadNum=threadNum;
        mainPane=new HBox();
        label = new Label("  " + name);
        label.setMinWidth(100);
        bar = new ProgressBar();
        mainPane.getChildren().addAll(label,bar,startButton,stopButton);

        startButton.setDisable(false);
        stopButton.setDisable(true);

        /**
         * 添加开始按钮事件，设置进度条监控
         * 开始下载
         */
        startButton.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                startButton.setDisable(true);
                stopButton.setDisable(false);

                bar.progressProperty().unbind();
                bar.setProgress(0);
                downloadWork = new URLdownload_backpoint(name,address,path,threadNum);
                copyWorker = createWorker();
                bar.progressProperty().bind(copyWorker.progressProperty());
                downloadWork.Download();
                copyWorker.messageProperty().addListener(new ChangeListener<String>() {
                    public void changed(ObservableValue<? extends String> observable, String oldValue,
                                        String newValue) {
                        //System.out.println(newValue);
                    }
                });
                new Thread(copyWorker).start();
            }
        });

        /**
         * 添加暂停按钮事件
         * 结束加载
         */
        stopButton.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                startButton.setDisable(false);
                stopButton.setDisable(true);
                downloadWork.STOP();
                copyWorker.cancel(true);
                bar.progressProperty().unbind();
                bar.setProgress(0);
                System.out.println("cancelled.");
            }
        });
    }

    /**
     * 监控事件，负责更新进度条
     * @return
     */
    public Task createWorker() {
        return new Task() {
            @Override
            protected Object call() throws Exception {
                while (true){
                    Thread.sleep(100);
                    updateMessage("100 milliseconds");
                    int tot=0;
                    int fenmu=0;

                    for(int i=0;i<threadNum;i++){
                        tot+=downloadWork.Percents[i];
                        fenmu+=downloadWork.partWorks[i];
                    }
                    //System.out.println(tot);
                    updateProgress(tot, fenmu); //更新进度

                    //下载完成,写入下载完成文件，锁定按钮
                    if(tot==fenmu){
                        try {
                            File file = new File(JaxbUtil.FINISH_DOWNLOAD);
                            BufferedWriter bufw= new BufferedWriter(new FileWriter(file,true));
                            bufw.write(download_info);
                            bufw.newLine();
                            bufw.close();
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                        break;
                    }
                }
                startButton.setDisable(true);
                stopButton.setDisable(true);
                //mainPane.getChildren().removeAll(bar,startButton,stopButton);
                return true;
            }
        };
    }
}
