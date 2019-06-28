package demo.controller;

import demo.model.Bookmark;
import demo.model.Setting;
import demo.model.downloadHash;
import demo.model.emailCash;
import demo.node.MyTab;
import demo.util.JaxbUtil;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import demo.myMail.SendMailText_Picture_Enclosure;

/**
 * FXML Controller class
 *
 * @author caixiaoxin
 */
public class DemoController implements Initializable {


    @FXML
    private TabPane tabPane;
    @FXML
    private Button addTabBtn;

    //书签组件
    @FXML
    private TextField bookmarkName;
    @FXML
    private TextField bookmarkAddress;
    @FXML
    private AnchorPane bookmarkPane;
    @FXML
    private CheckMenuItem bookmarkMenu;
    @FXML
    private HBox bookmarkBox;   //书签栏

    //下载框组件
    @FXML
    private TextField file_name;
    @FXML
    private TextField save_path;
    @FXML
    private TextField source_path;
    @FXML
    private AnchorPane downloadPane;
    @FXML
    private AnchorPane downloadContent;
    @FXML
    private CheckMenuItem checkDown;
    @FXML
    private VBox downloadBox;   //下载栏/已下载/正在下载

    //邮件组件
    @FXML
    private AnchorPane emailPane;
    @FXML
    private TextField To;
    @FXML
    private TextField Title;
    @FXML
    private TextField attachPic;
    @FXML
    private TextField attachFile;
    @FXML
    private TextArea Text;
    @FXML
    private Button send;
    @FXML
    private VBox email_list;




    public static Setting userSettiing;  //用户设置
    private static ObservableList<Node> bookmarks;  //书签
    //邮件存档管理
    private static emailCash email_file= new emailCash();
    //下载存档管理
    private static downloadHash download_file = new downloadHash();
    private final Double tabHeaderWidth = 200D;

    //----------------浏览器设置、版本-------------------------------------------------
    @FXML   //关闭浏览器
    private void closeAction(ActionEvent event) {//函数名必需与fxml中的一致
        System.exit(0);
    }
    @FXML   //显示设置
    private void showSetting() {
        MyTab mt = new MyTab("", this);
        if (userSettiing == null) {
            mt.load("file:///" + JaxbUtil.SETTING_HTML.replaceAll("\\\\", "/"));
        } else {
            System.out.println(userSettiing.toString());
            mt.load("file:///" + JaxbUtil.SETTING_HTML.replaceAll("\\\\", "/") + "?" + userSettiing.toString());
        }
        mt.setType(MyTab.Type.SETTING);
        mt.hideAddressBox();
        tabPane.getTabs().add(mt);
        tabPane.getSelectionModel().select(mt);
    }
    @FXML   //显示版本信息
    private void showAbout() {
        MyTab mt = new MyTab("", this);
        mt.load("file:///" + JaxbUtil.ABOUT_FILE.replaceAll("\\\\", "/"));
        //mt.setType(MyTab.Type.SETTING);
        mt.hideAddressBox();
        mt.setType(MyTab.Type.ABOUT);
        tabPane.getTabs().add(mt);
        tabPane.getSelectionModel().select(mt);
    }

    //---------------书签控制-------------------------------------------------------------
    /**
     * 显示书签栏*
     * @param event
     */
    @FXML
    private void showBookmark(ActionEvent event) {//函数名必需与fxml中的一致
        tabPane.getTabs().forEach((t) -> {
            MyTab mt = (MyTab) t;
            mt.changeBookmarkBarVisible(bookmarkMenu.isSelected());
        });
        bookmarkBox.setVisible(bookmarkMenu.isSelected());
        //userSettiing.setShowBookmark(bookmarkMenu.isSelected());
        //JaxbUtil.saveToXML(userSettiing, JaxbUtil.SETTING_FILE);
    }

    /**
     * 添加书签
     * @param name
     * @param url
     */
    public void addBookmark(String name, String url) {
        Object data = bookmarkPane.getUserData();
        String tooltip = "右键点击修改\n左键单击新标签页打开书签\n" +
                        "CTRL+左键单击当前页打开书签\n" + name + "\n" + url;
        if (data != null) {//修改书签
            Label node = (Label) data;
            node.setText(name);
            node.setUserData(url);
            node.setTooltip(new Tooltip(tooltip));
        } else {//添加书签
            Label node = new Label(name+"|");
            node.setMaxWidth(150);
            node.setUserData(url);
            node.setTooltip(new Tooltip(tooltip));
            node.setCursor(Cursor.HAND);
            node.setOnMouseClicked((event) -> {//书签点击事件处理
                String ul = node.getUserData().toString();
                if (MouseButton.SECONDARY.equals(event.getButton())) {
                    showBookmarkPane(node.getText(), ul, node);
                } else if (event.isControlDown()) {//CTRL+单击事件
                    MyTab mt = (MyTab) tabPane.getSelectionModel().getSelectedItem();
                    mt.load(ul);
                } else {
                    MyTab mt = new MyTab(ul, this);
                    tabPane.getTabs().add(mt);
                    tabPane.getSelectionModel().select(mt);
                }
            });
            bookmarks.add(node);
        }
    }

    /**
     * 移除书签
     * @param node
     */
    public static void removeBookmark(Node node) {
        bookmarks.add(node);
    }

    /**保存书签
     */
    @FXML
    private void saveBookmark() {
        addBookmark(bookmarkName.getText(), bookmarkAddress.getText());
        hideBookmark();
        Bookmark bm = new Bookmark();
        bookmarks.forEach((t) -> {
            Label l = (Label) t;
            bm.addBookmark(l.getText(), l.getUserData().toString());
        });
        JaxbUtil.saveToXML(bm, JaxbUtil.BOOKMARK_FILE);//将书签存储到文件
    }

    /**
     * 隐藏书签管理窗口
     */
    @FXML
    public void hideBookmark() {
        bookmarkPane.setVisible(false);
    }

    /**
     * 显示书签管理窗口
     *
     * @param name
     * @param url
     * @param userData 编辑传需要修改的节点
     */
    public void showBookmarkPane(String name, String url, Object userData) {
        //System.out.println("yes");
        Double width = bookmarkPane.getScene().getWidth();
        AnchorPane.setLeftAnchor(bookmarkPane, (width - 344d) / 2);
        bookmarkAddress.setText(url);
        bookmarkName.setText(name);
        bookmarkPane.setVisible(true);
        bookmarkPane.setUserData(userData);
    }


    //---------------------------邮件控件---------------------------------------------------------
    @FXML   //显示邮件面板
    public void showEmailPane(Object userData){
        AnchorPane.setRightAnchor(emailPane, 0D);
        emailPane.setVisible(true);
        emailPane.setUserData(userData);
    }

    @FXML   //隐藏邮件面板
    public void hideMailPane(ActionEvent event){
        emailPane.setVisible(false);
        To.setText("收件人邮箱");
        Title.setText("");
        attachFile.setText("null");
        attachPic.setText("null");
        send.setDisable(false);
    }

    @FXML   //发送邮件
    private void send_email(ActionEvent event){
        //TODO

        boolean email_status = true;
        String to = To.getText();
        String title = Title.getText();
        String attachpic = attachPic.getText();
        String attachfile = attachFile.getText();
        String text = Text.getText();
        send.setDisable(true);
        //尝试发送并记录邮件发送状态
        try{
            new SendMailText_Picture_Enclosure(title,to,text,attachpic,attachfile);
        }catch (Exception e){
            send.setDisable(false);
            email_status=false;
        }
        String email_info = "Title:" + title + "\nTo:"+to;
        if(email_status==false){
            email_info+="\nStatus:send fail";
        }else{
            email_info+="\nStatus:send succeed";
        }
        //邮件信息整合存储
        String sended_info = email_info+"@#@"+to+"@#@"+title+"@#@"+
                            attachpic+"@#@"+attachfile+"@#@"+text;
        add_email(sended_info);
        email_file.add(sended_info);
    }

    /**
     * @TODO:将邮件信息存储
     * @param info：加工后的邮件信息   --@#@--@#@----
     */
    public void add_email(String info){
        String infos[] = info.split("@#@");
        Button button = new Button(infos[0]);
        button.setMinWidth(350);
        button.setMinHeight(100);
        //设置单击事件，用于加载邮件到发送框
        button.setOnMouseClicked((event1 -> {
            To.setText(infos[1]);
            Title.setText(infos[2]);
            attachFile.setText(infos[3]);
            attachPic.setText(infos[4]);
            Text.setText(infos[5]);
        }));
        email_list.getChildren().add(button);
    }


    //---------------------下载控件-------------------------------------
    @FXML   //显示下载进度
    private void showDownload(ActionEvent event){
        AnchorPane.setRightAnchor(downloadContent, 0D);
        downloadContent.setVisible(checkDown.isSelected());
    }
    /**显示下载信息窗口
     * @param name:下载文件名
     * @param url：下载链接
     * @param userData
     */
    public void showDownloadPane(String name,String url,Object userData){
        Double width = downloadPane.getScene().getWidth();
        AnchorPane.setLeftAnchor(downloadPane, (width - 344d) / 2);
        file_name.setText(name);
        save_path.setText(JaxbUtil.DOWNLOAD_PATH);
        source_path.setText(url);
        downloadPane.setVisible(true);
        downloadPane.setUserData(userData);
    }

    /**
     *  开始下载，将下载信息载入download_file
     *  新建下载面板类，构造进度条，按钮等控件
     */
    @FXML
    public void download_start(){
        String name = file_name.getText();
        String path = save_path.getText();
        String address = source_path.getText();


        String download_info = name+"@#@"+address+"@#@"+path;
        download_file.add(download_info);
        add_download(download_info,false);
        hideDownloadPane();
    }

    /**
     * 新建下载任务
     * @param download_info：下载信息，和邮件信息一样的加工方式
     * @param isfinish:下载状态，主要是在初始化浏览器时锁定已经下载完成的项目
     */
    public void add_download(String download_info,boolean isfinish){
        String infos[] = download_info.split("@#@");
        downloadPane downPane = new downloadPane(infos[0],infos[1],infos[2],4);
        //下载完成的项目锁定开始和暂停键，进度100
        if(isfinish){
            downPane.bar.setProgress(1.0);
            downPane.stopButton.setDisable(true);
            downPane.startButton.setDisable(true);
        }
        downloadBox.getChildren().add(downPane.mainPane);
    }

    @FXML   //隐藏下载进度面板
    public void hideDownloadPane(){
        downloadPane.setVisible(false);
    }


    public void initSetting(boolean openURLS) {
        if (userSettiing == null) {
            return;
        }
        MyTab.HOME_URL = userSettiing.getHome();
        MyTab.SEARCH = userSettiing.getSearchEngines();
        bookmarkMenu.setSelected(userSettiing.getShowBookmark());
        if (userSettiing.getShowBookmark()) {
            MyTab.currentWebViewTop = 54D;
        } else {
            MyTab.currentWebViewTop = 28D;
        }
        showBookmark(null);
        if (openURLS) {
            if ("newtab".equals(userSettiing.getInitType())) {
                tabPane.getTabs().add(new MyTab("", this));
            } else {
                for (String s1 : userSettiing.getURLS()) {
                    tabPane.getTabs().add(new MyTab(s1, this));
                }
            }
        }
    }

    /**
     * Initializes the controller class.
     * 初始化浏览器配置
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        /*File setting = new File(JaxbUtil.SETTING_HTML);
        if (!setting.exists()) {
            JaxbUtil.saveFile(JaxbUtil.readFile(getClass().getResourceAsStream("/resources/html/default.html")), JaxbUtil.SETTING_HTML);
        }
        userSettiing = JaxbUtil.converyToJavaBean(JaxbUtil.SETTING_FILE, Setting.class);
        if (userSettiing != null) {
            initSetting(true);
        } else {
            tabPane.getTabs().add(new MyTab(this));
        }*/

        //TODO:加载书签
        tabPane.getTabs().add(new MyTab(this));
        bookmarks = bookmarkBox.getChildren();
        Bookmark bm = JaxbUtil.converyToJavaBean(JaxbUtil.BOOKMARK_FILE, Bookmark.class);
        if (bm != null) {
            bm.getBookmarks().forEach((t) -> {
                addBookmark(t.getName(), t.getUrl());
            });
        }
        bookmarkPane.setVisible(false);
        addTabBtn.setOnAction((event) -> {
            MyTab mt = new MyTab("", this);
            tabPane.getTabs().add(mt);
            tabPane.getSelectionModel().select(mt);
        });
        tabPane.getTabs().addListener((ListChangeListener.Change<? extends Tab> c) -> {
            int size = c.getList().size();
            if (size == 0) {
                Platform.exit();
            }
            Double width = tabPane.getScene().getWidth();
            Double left = tabHeaderWidth * size + (size - 1) * 12D + 20D;
            if (left - 80D > width) {
                left = width - 80D;
                tabPane.setTabMinWidth(5D);
            } else if (left < width - tabHeaderWidth) {
                tabPane.setTabMinWidth(tabHeaderWidth);
            }
            AnchorPane.setLeftAnchor(addTabBtn, left);
        });
        tabPane.setTabMaxWidth(tabHeaderWidth);

        //TODO:加载邮件
        ArrayList<String> email_infos = email_file.readAll();
        for(String info:email_infos){
            add_email(info);
        }



        //TODO:加载下载项,分读已下载文件和下载日志，在已下载文件中的文件锁定下载暂停和开始按钮
        ArrayList<String> list = new ArrayList<>();
        try {
            File file = new File(JaxbUtil.FINISH_DOWNLOAD);

            BufferedReader bufr = new BufferedReader(new FileReader(file));
            String s = null;
            while ((s = bufr.readLine()) != null) {
                //System.out.println(s);
                if(s.length()>1){
                    list.add(s.replace("\n",""));
                    System.out.println(s);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        ArrayList<String> download_infos = download_file.readAll();
        for(String info:download_infos){
            info = info.replace("\n","");
            System.out.println(info);
            System.out.println(list.contains(info));
            add_download(info,list.contains(info));
        }
    }

}
