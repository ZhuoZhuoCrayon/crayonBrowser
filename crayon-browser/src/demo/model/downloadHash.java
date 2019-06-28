package demo.model;

import demo.util.JaxbUtil;

import java.io.File;

public class downloadHash extends emailCash{
    public downloadHash(){
        file = new File(JaxbUtil.downloadFile);
    }
}
