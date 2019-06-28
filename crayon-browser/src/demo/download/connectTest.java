package demo.download;

import java.net.HttpURLConnection;
import java.net.URL;

public class connectTest {
    protected static int Timeout = 2000;
    public static int Request(String request){
        try{
            URL url = new URL(request);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(Timeout);
            connection.setReadTimeout(Timeout);
            return connection.getResponseCode();
        }catch (Exception e){
            return 404;
        }
    }
}
