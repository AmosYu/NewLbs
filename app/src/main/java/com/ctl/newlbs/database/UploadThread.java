package com.ctl.newlbs.database;

import android.os.Environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by yuxingyu on 2018/7/3.
 */

public class UploadThread implements Runnable {

    public static String myDataPath = Environment.getExternalStorageDirectory().getPath()
            + File.separator +"mydata.txt";


    public static void deleteMyDataFile(){
        File file  = new File(myDataPath);
        if(file!=null){
            file.delete();
        }
    }

    @Override
    public void run() {
        File file = new File(myDataPath);
        try {
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));

            for (String line = br.readLine(); line != null; line = br.readLine()) {
                if(line.startsWith("GX1")){

                }
                else if(line.startsWith("MX1")){//移动G

                }
                else if(line.startsWith("MX2")){//联通G

                }
                else if(line.startsWith("MX3")){//mobile lte

                }
                else if(line.startsWith("MX4")){//wcdma

                }
                else if(line.startsWith("MX5")){//tdscdma

                }
                else if(line.startsWith("MX6")){//unicom lte

                }
                else if(line.startsWith("MX7")){//telecom lte

                }
                else if(line.startsWith("MX8")){//cdma

                }







            }
        }
        catch (FileNotFoundException noFile){

        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
