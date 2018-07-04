package com.ctl.newlbs.database;

import android.os.Environment;
import android.util.Log;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;
import com.ctl.newlbs.cell.BtsType;
import com.ctl.newlbs.cell.LuceCellInfo;
import com.ctl.newlbs.utils.Gps2BaiDu;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;

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
        ArrayList<LuceCellInfo> dataList = new ArrayList<>();

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            boolean gpsEffective = false;
            boolean upload = false;
            LatLng lastPoint = null;
            double latitude=0,longitude=0;
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                String revMsgSplit[] = line.split(":");
                if(line.startsWith("GX1")){
                    String gpsArray[]=revMsgSplit[1].split(",");
                    if("GPRMC".equals(gpsArray[0])){
                        if("A".equals(gpsArray[2])){
                            gpsEffective = true;
                            if (gpsArray[3].length() >= 8 && gpsArray[5].length() >= 9) {
                                double latDu = Double.parseDouble(gpsArray[3].substring(0, 2));
                                double latFen = Double.parseDouble(gpsArray[3].substring(2)) / 60;
                                double lngDu = Double.parseDouble(gpsArray[5].substring(0, 3));
                                double lngFen = Double.parseDouble(gpsArray[5].substring(3)) / 60;
                                BigDecimal lat = new BigDecimal(latDu + latFen);
                                latitude = lat.setScale(6, BigDecimal.ROUND_HALF_UP).doubleValue();
                                BigDecimal lng = new BigDecimal(lngDu + lngFen);
                                longitude = lng.setScale(6, BigDecimal.ROUND_HALF_UP).doubleValue();
                                LatLng latLng = Gps2BaiDu.gpsToBaidu(latitude,longitude);

                                if(DistanceUtil.getDistance(latLng,lastPoint)>20){
                                    lastPoint = latLng;
                                    upload = true;
                                }
                            }
                        }
                        else{
                            gpsEffective = false;
                            upload = true;
                        }
                    }
                }
                else if(line.startsWith("GX2")){

                }

                if(gpsEffective&&upload){
                    if(line.startsWith("MX1")){//移动G
                        String btsMsg[] = revMsgSplit[1].split(",");
                        LuceCellInfo  lbsCellInfo = getLbsCellInfo(BtsType.GSM_MOBILE,btsMsg,latitude,longitude);
                        dataList.add(lbsCellInfo);
                    }
                    else if(line.startsWith("MX2")){//联通G
                        String btsMsg[] = revMsgSplit[1].split(",");
                        LuceCellInfo  lbsCellInfo = getLbsCellInfo(BtsType.GSM_UNICOM,btsMsg,latitude,longitude);
                        dataList.add(lbsCellInfo);
                    }
                    else if(line.startsWith("MX3")){//mobile lte
                        String btsMsg[] = revMsgSplit[1].split(",");
                        LuceCellInfo  lbsCellInfo = getLbsCellInfo(BtsType.LTE_MOBILE,btsMsg,latitude,longitude);
                        dataList.add(lbsCellInfo);
                    }
                    else if(line.startsWith("MX4")){//wcdma
                        String btsMsg[] = revMsgSplit[1].split(",");
                        LuceCellInfo  lbsCellInfo = getLbsCellInfo(BtsType.WCDMA,btsMsg,latitude,longitude);
                        dataList.add(lbsCellInfo);
                    }
                    else if(line.startsWith("MX5")){//tdscdma
                        String btsMsg[] = revMsgSplit[1].split(",");
                        LuceCellInfo  lbsCellInfo = getLbsCellInfo(BtsType.TDSCDMA,btsMsg,latitude,longitude);
                        dataList.add(lbsCellInfo);
                    }
                    else if(line.startsWith("MX6")){//unicom lte
                        String btsMsg[] = revMsgSplit[1].split(",");
                        LuceCellInfo  lbsCellInfo = getLbsCellInfo(BtsType.LTE_UNICOM,btsMsg,latitude,longitude);
                        dataList.add(lbsCellInfo);
                    }
                    else if(line.startsWith("MX7")){//telecom lte
                        String btsMsg[] = revMsgSplit[1].split(",");
                        LuceCellInfo  lbsCellInfo = getLbsCellInfo(BtsType.LTE_TELECOM,btsMsg,latitude,longitude);
                        dataList.add(lbsCellInfo);
                    }
                    else if(line.startsWith("MX8")){//cdma
                        String btsMsg[] = revMsgSplit[1].split(",");
                        LuceCellInfo lbsCellInfoCdma = getCdmaCellInfo(btsMsg,latitude,longitude);
                        dataList.add(lbsCellInfoCdma);
                    }
                }

                if(dataList.size()>199){


                }
            }
        }
        catch (FileNotFoundException noFile){

        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }



    private LuceCellInfo getLbsCellInfo(BtsType btsType, String[] btsMsg,double latitude,double logitude){

        try {
            int lac = 0;
            int ci = 0;
            if (btsMsg[4].equals("FFFF")) {
                lac = 0;
            } else {

                lac = Integer.parseInt(btsMsg[4], 16);
            }

            if (btsMsg[5].equals("FFFFFFFF")) {
                ci = 0;
            } else {
                ci = Integer.parseInt(btsMsg[5], 16);
            }

            int arfcnA = Integer.parseInt(btsMsg[6]);
            if (arfcnA == 65535) arfcnA = 0;

            int arfcnB = Integer.parseInt(btsMsg[7]);

            int rssi = Integer.parseInt(btsMsg[8]);

            LuceCellInfo lbsCellInfo = new LuceCellInfo();
            lbsCellInfo.setCellType(Integer.parseInt(btsMsg[0]));
            lbsCellInfo.setLac(lac);
            lbsCellInfo.setCellId(ci);
            lbsCellInfo.setArfcnA(arfcnA);
            lbsCellInfo.setArfcnB(arfcnB);
            lbsCellInfo.setRssi(rssi);
            lbsCellInfo.setBtsType(btsType);
            lbsCellInfo.setTime(LuceCellInfo.getCurrentTime());
            lbsCellInfo.setLatitudeGps(latitude);
            lbsCellInfo.setLongitudeGps(logitude);
            return lbsCellInfo;
        }catch(Exception ex){
            return null;
        }

    }

    private LuceCellInfo getCdmaCellInfo(String btsMsg[],double latitude,double logitude){
        try {

            int sid = Integer.parseInt(btsMsg[2]);

            int nid = Integer.parseInt(btsMsg[3]);

            int bid = Integer.parseInt(btsMsg[4]);

            int arfcnA = Integer.parseInt(btsMsg[5]);

            int arfcnB = Integer.parseInt(btsMsg[6]);

            int rssi = Integer.parseInt(btsMsg[7]);

            LuceCellInfo lbsCellInfo = new LuceCellInfo();
            lbsCellInfo.setCellType(Integer.parseInt(btsMsg[0]));
            lbsCellInfo.setSid(sid);
            lbsCellInfo.setNid(nid);
            lbsCellInfo.setBid(bid);
            lbsCellInfo.setArfcnA(arfcnA);
            lbsCellInfo.setArfcnB(arfcnB);
            lbsCellInfo.setRssi(rssi);
            lbsCellInfo.setBtsType(BtsType.CDMA);
            lbsCellInfo.setLatitudeGps(latitude);
            lbsCellInfo.setLongitudeGps(logitude);

            return lbsCellInfo;
        }catch (Exception ex){
            Log.e("CDMA","异常");
            return null;
        }
    }
}
