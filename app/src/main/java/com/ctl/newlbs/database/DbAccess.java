package com.ctl.newlbs.database;



import com.ctl.newlbs.cell.LuceCellInfo;
import com.ctl.newlbs.cell.WifiInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * Created by yuxingyu on 16/5/24.
 */
public interface  DbAccess {

    /**
     * 程序内部数据自动存储
     * @param cellMapInfo
     */
    void insertCellMapInfo(LuceCellInfo cellMapInfo);

    void insertWifiInfo(WifiInfo wifiInfo);

    void deleteAllData();

    /**
     * 导出用户选择的数据
     * @param markList 用户选择的数据; 用list中的每个mark
     * @param isDelete 导出后是否删除原有数据
     * @return
     */
    int startExportDb(ArrayList<String> markList, boolean isDelete);
    boolean cellInDbBackup(LuceCellInfo cellMapInfo, boolean isCdma);
    void deleteCellMapInfo(LuceCellInfo cellMapInfo, boolean isCdma, int type);

    /**
     * @param mark
     */
    void insertMark(String mark);

    ArrayList<String> loadMark();

    /**
     * 根据文件名查询数据(数据库中存储的是用户地址)
     * @param filename
     * @return
     */
    List<Map<String,Object>> selectByFile(String filename);
    public List<LuceCellInfo> selectByNameAndType(String filename, String type);
    //根据LAC值和CELL值 查询出对应的基站经纬度
    ArrayList<String> FindPos(String mnc, String lac, String cellid);


    public final static String CELL_TYPE = "CELL_TYPE";
    public final static String LAC = "LAC";
    public final static String SID = "SID";
    public final static String NID = "NID";
    public final static String CI = "CI";
    public final static String BID = "BID";
    public final static String LATITUDE = "latitude";
    public final static String LONGITUDE = "longitude";
    public final static String RSSI = "rssi";
    public final static String ARFCN = "arfcn";
    public final static String PCI = "PCI";

    public final static String TIME = "time";
    public final static String MARK = "mark";
    public final static String BTS_TYPE = "btsType";
    public final static String BACKUP_DATA="backupData";
    public final static String USER_MARK="userMark";

    public final static String WIFI_DATA = "wifiData";

    public final static String TYPE = "TYPE";
    public final static String MAC  = "MAC";

    public final static String BAIDULATITUDE = "baiduLatitude";
    public final static String BAIDULONGITUDE = "baiduLongitude";


}
