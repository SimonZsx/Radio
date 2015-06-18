package io.yunba.androiddemo.radio;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.util.UUID;
import android.util.Log;

/**
 * Created by Zhao on 2015/6/18.
 */

public class UploadUtil {
    private static final String TAG = "uploadFile";
    private static final int TIME_OUT = 10*1000;
   // private static final String CHARSET = "utf-8";

    public static String uploadFile(File file,String RequestURL)
    {
        String result = null;
        String  BOUNDARY =  UUID.randomUUID().toString();
        String PREFIX = "--" , LINE_END = "\r\n";
        String CONTENT_TYPE = "multipart/form-data";

        try {
            URL url = new URL(RequestURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(TIME_OUT);
            conn.setConnectTimeout(TIME_OUT);
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Host","f2.yunba.io:8888");
            //conn.setRequestProperty("Charset", CHARSET);
            conn.setRequestProperty("Connection", "keep-alive");
            conn.setRequestProperty("Origin","f2.yunba.io:8888");
            conn.setRequestProperty("User-Agent","Mozilla/5.0 (Windows NT 5.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2272.101 Safari/537.36");
            conn.setRequestProperty("Content-Type", CONTENT_TYPE + "; boundary=" + BOUNDARY);
            conn.setRequestProperty("Accept","*/*");
            conn.setRequestProperty("DNT","1");
            conn.setRequestProperty("Referer","http://f2.yunba.io:8888/files/example.html");
            conn.setRequestProperty("Accept-Encoding","gzip, deflate");
            conn.setRequestProperty("Accept-Language","zh-CN,zh;q=0.8,en;q=0.6");
            if(file!=null)
            {

                DataOutputStream dos = new DataOutputStream( conn.getOutputStream());
                StringBuffer sb = new StringBuffer();

                sb.append(PREFIX);
                sb.append(BOUNDARY);
                sb.append(LINE_END);

                sb.append("Content-Disposition: form-data; name=\"path\""+LINE_END);
                sb.append(LINE_END);
                sb.append(file.getName()+LINE_END);

                sb.append(PREFIX);
                sb.append(BOUNDARY);
                sb.append(LINE_END);

                sb.append("Content-Disposition: form-data; name=\"file\"; filename=\""+file.getName()+"\""+LINE_END);
                sb.append("Content-Type: application/octet-stream;" + LINE_END);//charset="+CHARSET+LINE_END);
                sb.append(LINE_END);


                dos.write(sb.toString().getBytes());

                MessageDigest digest = null;
                InputStream is = new FileInputStream(file);


                byte[] bytes = new byte[1024];
                int len = 0;

                try {
                    digest = MessageDigest.getInstance("MD5");
                    while ((len = is.read(bytes)) != -1) {
                        digest.update(bytes,0,len);
                        dos.write(bytes, 0, len);
                    }
                }
                catch(Exception e){
                    e.printStackTrace();
                    return null;
                }
                is.close();

                BigInteger bigInt = new BigInteger(1, digest.digest());
                String signature= bigInt.toString(16);

                dos.write(LINE_END.getBytes());


                StringBuffer sbend=new StringBuffer();
                sbend.append(PREFIX+BOUNDARY+LINE_END);

                sbend.append("Content-Disposition: form-data; name=\"signature\""+LINE_END);
                sbend.append(LINE_END);
                sbend.append(signature+LINE_END);

                sbend.append(PREFIX+BOUNDARY+PREFIX+LINE_END);

                dos.write(sbend.toString().getBytes());
                dos.flush();

                int res = conn.getResponseCode();
                Log.e(TAG, "response code:"+res);
                InputStream input =  conn.getInputStream();
                StringBuffer sb1= new StringBuffer();
                int ss ;
                while((ss=input.read())!=-1)
                {
                    sb1.append((char)ss);
                }
                result = sb1.toString();
                Log.e(TAG, "result : "+ result);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
}