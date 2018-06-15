package com.ctl.newlbs.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;
import com.ctl.newlbs.viewadapter.InfoWindowHolder;
import com.ctl.newlbs.R;
import com.ctl.newlbs.cell.BtsType;
import com.ctl.newlbs.cell.LuceCellInfo;
import com.ctl.newlbs.cell.ProcessBtsData;
import com.ctl.newlbs.database.DbAcessImpl;


import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;

public class TrackActivity extends Activity {

    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track);
        context = this;
        initMap();
        initMapTopView();
    }

    private MapView mMapView = null;
    private BaiduMap mBaiduMap=null;
    private InfoWindow mInfoWindow;
    private LinearLayout baidumap_infowindow;
    private MarkerOnInfoWindowClickListener markerListener;
    private void initMap() {
        mMapView = (MapView)findViewById(R.id.track_mapView);
        baidumap_infowindow = (LinearLayout) LayoutInflater.from (context).inflate (R.layout.baidu_map_infowindow, null);
        mBaiduMap = mMapView.getMap();
        markerListener = new MarkerOnInfoWindowClickListener();
        //普通地图
        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        mBaiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                if(showBtsType.equals("全部")){
                    return false;
                }
                createInfoWindow(baidumap_infowindow,(LuceCellInfo)  marker.getExtraInfo ().get ("marker"));
                final LatLng ll = marker.getPosition();
                mInfoWindow = new InfoWindow(BitmapDescriptorFactory.fromView (baidumap_infowindow), ll, -47, markerListener);
                mBaiduMap.showInfoWindow(mInfoWindow);
                return true;
            }
        });
    }
    private final class  MarkerOnInfoWindowClickListener implements InfoWindow.OnInfoWindowClickListener {
        @Override
        public void onInfoWindowClick(){
            mBaiduMap.hideInfoWindow();
        }
    }

    private Button timeSelect,modeSelect,loadBtn;
    private String showBtsType = BtsType.GSM_MOBILE.toString();
    private String timeEnd = getPastTime(times[0]);

    private RadioGroup timeSelectGroup;
    private void initMapTopView() {

        modeSelect=(Button) findViewById(R.id.spinner_marker_type);
        modeSelect.setText("基站类型："+showBtsType);
//        timeSelect=(Button) findViewById(R.id.time_select_btn);
//        timeSelect.setText(times[0]);
        timeSelectGroup = (RadioGroup)findViewById(R.id.track_select_time_group);
        timeSelectGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int id) {
                if(id==R.id.track_toady_radio){
                    timeEnd = getPastTime("一天内");
                }
                else if(id==R.id.track_three_day_radio){
                    timeEnd = getPastTime("三天内");
                }
                else if(id==R.id.track_seven_day_radio){
                    timeEnd = getPastTime("七天内");
                }
                else if(id==R.id.track_one_month_radio){
                    timeEnd = getPastTime("一月内");
                }
            }
        });

//        initBts(showBtsType,timeEnd);
//        timeSelect.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                showTimeSelectDialog();
//            }
//        });
        modeSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showModeSelectDialog();
            }
        });

        loadBtn = (Button)findViewById(R.id.load_btn);
        loadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initBts(showBtsType,timeEnd);
                loadBtn.setEnabled(false);
            }
        });
    }
    private void showModeSelectDialog() {
        Resources res = getResources ();
        final String[] modes = BtsType.getAllBtsTypeStr();
        final AlertDialog.Builder listDialog = new AlertDialog.Builder(TrackActivity.this);
        listDialog.setTitle("选择基站类型");
        listDialog.setItems(modes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                modeSelect.setText("基站类型："+modes[which]);
                showBtsType = modes[which];
//                initBts(showBtsType,timeEnd);
            }
        });
        listDialog.show();
    }
    public final static String[] times = {"一天内","三天内","七天内","一月内"};
    private void showTimeSelectDialog() {
        final AlertDialog.Builder listDialog = new AlertDialog.Builder(TrackActivity.this);
        listDialog.setTitle("选择时间段");
        listDialog.setItems(times, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                timeSelect.setText(times[which]);
                timeEnd = getPastTime(times[which]);
//                initBts(showBtsType,timeEnd);
            }
        });
        listDialog.show();
    }

    /**
     * 任务下的所有制定制式的数据，内层集合中每个List中是同LAC的基站
     */
    private LinkedList<ArrayList<LuceCellInfo>> btsInfoList = new LinkedList<>();

    /**
     * 将基站数据添加至区分LAC的btsInfoList中
     * @param luceCellInfo
     */
    private void addBsInfoToList(LuceCellInfo luceCellInfo){
        for(ArrayList<LuceCellInfo> sameLacList:btsInfoList){
            if(luceCellInfo.getBtsType()== BtsType.CDMA){
                if(sameLacList.get(0).getBid()==luceCellInfo.getBid()){
                    sameLacList.add(luceCellInfo);
                    return;
                }
            }
            else{
                if(sameLacList.get(0).getLac()==luceCellInfo.getLac()){
                    sameLacList.add(luceCellInfo);
                    return;
                }
            }
        }
        ArrayList<LuceCellInfo> newList = new ArrayList<>();
        newList.add(luceCellInfo);
        btsInfoList.add(newList);
    }

    private void  addBtsInfoToMap(){
        int i = 0;
        for(ArrayList<LuceCellInfo> luceCellInfos:btsInfoList){
            for(LuceCellInfo luceCellInfo : luceCellInfos){
                if(!addPointToTrackList(luceCellInfo.getBaiduPoint())){
                    continue;
                }
                Message msg = new Message();
                msg.obj  = luceCellInfo;
                msg.arg1 = 101010;
                msg.arg2 = i;
                handler.sendMessage(msg);
            }
            i++;
        }

        Message msg = new Message();
        msg.obj  = 123;
        msg.arg1 = 2222;
        msg.arg2 = 2222;
        handler.sendMessage(msg);
    }

    private void initTack(){
        new Thread(){
            @Override
            public void run() {
                btsInfoList.clear();
                trackList.clear();
                mBaiduMap.clear();
                initTrackList(ProcessBtsData.mark);
            }
        }.start();
    }
    private void initTrackList(String taskName){

        DbAcessImpl db= DbAcessImpl.getDbInstance(context);

        LinkedList<LatLng> list = new LinkedList<>();

        list.addAll(db.selectByFileGetPoint(taskName));

        for(LatLng latLng:list){
            addPointToMap(latLng);
        }
    }
    private void initBts(final String btsType,final String time ){
        new Thread(){
            public void run() {
                DbAcessImpl db=DbAcessImpl.getDbInstance(context);
                LinkedList<LuceCellInfo> luceCellInfos = new LinkedList<LuceCellInfo>();
                luceCellInfos.addAll(db.selectByNameAndType(time,btsType));
                btsInfoList.clear();
                trackList.clear();
                mBaiduMap.clear();
                for(LuceCellInfo luceCellInfo:luceCellInfos){
                    addBsInfoToList(luceCellInfo);
                }
                addBtsInfoToMap();
            }
        }.start();
    }
    private LinkedList<LatLng>  trackList = new LinkedList<>();
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.arg1 == 101010){
//                progressDialog.dismiss();
                LuceCellInfo luceCellInfo =(LuceCellInfo) msg.obj;

                LatLng latLng = luceCellInfo.getBaiduPoint();
                String title = luceCellInfo.getLac()+","+luceCellInfo.getCellId()+","+luceCellInfo.getBid();
                BitmapDescriptor bitmap = getBitmap(msg.arg2);
                //构建MarkerOption，用于在地图上添加Marker
                OverlayOptions option = new MarkerOptions().position(latLng).icon(bitmap).title(title);
                //定义地图状态
                MapStatus mMapStatus = new MapStatus.Builder().target(latLng).zoom(18).build();
                //定义MapStatusUpdate对象，以便描述地图状态将要发生的变化
                MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
                //改变地图状态
                mBaiduMap.setMapStatus(mMapStatusUpdate);
                //在地图上添加Marker，并显示
                Marker marker = (Marker)mBaiduMap.addOverlay(option);
                // 将信息保存
                Bundle bundle = new Bundle ();
                bundle.putSerializable ("marker", luceCellInfo);
                marker.setExtraInfo (bundle);
            }
            else if(msg.arg1==1010){

                LatLng latLng = (LatLng)msg.obj;
                BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.drawable.iconmarka);
                //构建MarkerOption，用于在地图上添加Marker
                OverlayOptions option = new MarkerOptions().position(latLng).icon(bitmap);
                //定义地图状态
                MapStatus mMapStatus = new MapStatus.Builder().target(latLng).zoom(18).build();
                //定义MapStatusUpdate对象，以便描述地图状态将要发生的变化
                MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
                //改变地图状态
                mBaiduMap.setMapStatus(mMapStatusUpdate);
                //在地图上添加Marker，并显示
                mBaiduMap.addOverlay(option);
            }
            else if(msg.arg1==2222&&msg.arg2==2222){
                loadBtn.setEnabled(true);
            }
        }
    };


    private BitmapDescriptor getBitmap(int id){
        BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.drawable.p1);
        switch (id%10){
            case 0:
                bitmap = BitmapDescriptorFactory.fromResource(R.drawable.p1);
                break;
            case 1:
                bitmap = BitmapDescriptorFactory.fromResource(R.drawable.p2);
                break;
            case 2:
                bitmap = BitmapDescriptorFactory.fromResource(R.drawable.p3);
                break;
            case 3:
                bitmap = BitmapDescriptorFactory.fromResource(R.drawable.p4);
                break;
            case 4:
                bitmap = BitmapDescriptorFactory.fromResource(R.drawable.p5);
                break;
            case 5:
                bitmap = BitmapDescriptorFactory.fromResource(R.drawable.p6);
                break;
            case 6:
                bitmap = BitmapDescriptorFactory.fromResource(R.drawable.p7);
                break;
            case 7:
                bitmap = BitmapDescriptorFactory.fromResource(R.drawable.p8);
                break;
            case 8:
                bitmap = BitmapDescriptorFactory.fromResource(R.drawable.p9);
                break;
            case 9:
                bitmap = BitmapDescriptorFactory.fromResource(R.drawable.p10);
                break;
        }
        return bitmap;
    }

    private void createInfoWindow(LinearLayout baidumap_infowindow,LuceCellInfo luceCellInfo){
        InfoWindowHolder holder = null;
//        if(baidumap_infowindow.getTag () == null){
        holder = new InfoWindowHolder ();
        holder.tv_title = (TextView) baidumap_infowindow.findViewById (R.id.map_window_title);
        holder.tv_content = (TextView) baidumap_infowindow.findViewById (R.id.map_window_content);

//        }
//        holder = (InfoWindowHolder) baidumap_infowindow.getTag ();
        String title = null;
        StringBuffer sb = new StringBuffer();
        sb.append("邻区\n");
        if(luceCellInfo.getBtsType()==BtsType.CDMA){
            title = luceCellInfo.getBtsType()+"\n大区号：("+luceCellInfo.getLac()+","+luceCellInfo.getCellId()+")"+"\n小区号："+luceCellInfo.getBid()+"，场强："+luceCellInfo.getRssi();
        }
        else{
            title = luceCellInfo.getBtsType()+"\n大区号："+luceCellInfo.getLac()+"\n" +
                    "小区号："+luceCellInfo.getCellId()+"，场强："+luceCellInfo.getRssi();
            DbAcessImpl dbAcess = DbAcessImpl.getDbInstance(context);
            ArrayList<LuceCellInfo> list = new ArrayList<>();
            list.addAll(dbAcess.findOnlySameLacBts(String.valueOf(luceCellInfo.getLac()),luceCellInfo.getBtsType().toString(),luceCellInfo.getLatitudeGps(),luceCellInfo.getLongitudeGps(),ProcessBtsData.mark));
            for(LuceCellInfo nCellinfo:list){
                if(nCellinfo.getCellId()!=luceCellInfo.getCellId()){
                    sb.append("小区号："+nCellinfo.getCellId()+",场强："+nCellinfo.getRssi()+"\n");
                }
            }
            holder.tv_content.setText (sb.toString());
        }
        holder.tv_title.setText (title);
    }

    private boolean addPointToTrackList(LatLng point){

        if(trackList.size()==0){
            trackList.add(point);
            return true;
        }
        else{
            if(isEnableAdd(point)) {
                trackList.add(point);
                return true;
            }
        }
        return false;
    }
    /**
     * 判断是否能将坐标点添加至轨迹集合，20米内不添加，20米外添加
     * @param latLng
     * @return
     */
    private boolean isEnableAdd(LatLng latLng){
        for(LatLng latLngInList:trackList){
            if(DistanceUtil.getDistance(latLng,latLngInList)<10){
                return false;
            }
        }
        return true;
    }

    /**
     * 添加坐标点到地图上
     * @param latLng
     */
    private void addPointToMap(LatLng latLng){
        boolean add = addPointToTrackList(latLng);
        if(add) {
            Message msg = new Message();
            msg.arg1 = 1010;
            msg.obj = latLng;
            handler.sendMessage(msg);
        }
    }




    @SuppressLint("WrongConstant")
    public static String getPastTime(String timeStr) {

        long time=System.currentTimeMillis();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        Calendar calendar = Calendar.getInstance();
        Date date = new Date(time);
        calendar.setTime(date);
        if(timeStr.equals(times[0])){
            calendar.add(Calendar.HOUR, -24);
            return simpleDateFormat.format(calendar.getTime());
        }
        else if(timeStr.equals(times[1])){
            calendar.add(Calendar.HOUR, -24*3);
            return simpleDateFormat.format(calendar.getTime());
        }
        else if(timeStr.equals(times[2])){
            calendar.add(Calendar.HOUR, -24*7);
            return simpleDateFormat.format(calendar.getTime());
        }
        else if(timeStr.equals(times[3])){
            calendar.add(Calendar.HOUR, -24*7);
            return simpleDateFormat.format(calendar.getTime());
        }
        return null;
    }
}
