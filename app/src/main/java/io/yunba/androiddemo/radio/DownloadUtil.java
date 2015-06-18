package io.yunba.androiddemo.radio;

import android.os.Environment;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * Created by Zhao on 2015/6/18.
 */
public class DownloadUtil {


    public static String downloadFile(String RequestURL) {

        String downloadPath = Environment.getExternalStorageDirectory().getPath() + "/download_cache";
        String url = RequestURL;
        File file = new File(downloadPath);

        if (!file.exists())
            file.mkdir();

        File file1 = new File(downloadPath+"/received.amr");
        if(file1.exists()){
            file1.delete();
            Log.e("download", "Delete cache sucessfully");
        }else{System.out.println("Didn't find the cache file"); }

        HttpGet httpGet = new HttpGet(url);
        try {
            HttpResponse httpResponse = new DefaultHttpClient().execute(httpGet);
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                InputStream is = httpResponse.getEntity().getContent();
                // start downloading
                FileOutputStream fos = new FileOutputStream(downloadPath + "/received.amr");
                byte[] buffer = new byte[1024];
                int len = 0;
                while ((len = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
                is.close();
            }
        } catch (Exception e) {}

        return downloadPath+"/received.amr";
    }

}
