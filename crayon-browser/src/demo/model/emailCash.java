package demo.model;

import demo.util.JaxbUtil;

import java.io.*;
import java.util.ArrayList;

public class emailCash {
    private BufferedWriter bufw;
    private BufferedReader bufr;
    File file;
    public emailCash(){
            file = new File(JaxbUtil.emailFile);
    }
    public void add(String email_info){
        try{
            bufw = new BufferedWriter(new FileWriter(file,true));
            bufw.write(email_info+"\n");
            bufw.write("@@@@@@@@@@\n");
            bufw.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public ArrayList<String> readAll(){
        ArrayList<String> list = new ArrayList<>();
        try {
            bufr = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            String s = null;
            String res="";
            while ((s = bufr.readLine()) != null) {
                //System.out.println(s);
                if(s.equals("@@@@@@@@@@")){
                    //System.out.println(res);
                    list.add(res);
                    res="";
                }else{
                    res+=s+"\n";
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return list;
    }
}
