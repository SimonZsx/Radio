package io.yunba.androiddemo.radio;

/**
 * Created by Zhao on 2015/6/18.
 */
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class RandomFilenameUtil {

    /**
     * Implement random filename
     *
     * @return filename
     */
    public static String getRandomFileName() {

        SimpleDateFormat simpleDateFormat;

        simpleDateFormat = new SimpleDateFormat("yyyyMMdd");

        Date date = new Date();

        String str = simpleDateFormat.format(date);

        Random random = new Random();

        int rannum = (int) (random.nextDouble() * (99999 - 10000 + 1)) + 10000;// Random number

        return rannum + str;// current time
    }

}
