package com.ctl.newlbs.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.ctl.newlbs.R;
import com.ctl.newlbs.cell.LuceCellInfo;
import com.ctl.newlbs.database.DbAcessImpl;
import com.ctl.newlbs.database.SdCardOperate;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * 文件导入，文件导出，文件上传全部使用此界面，区分功能
 * 使用intent.getStringExtra("类型")
 * 文件导入功能：导入
 * 文件导出功能：导出
 * 文件上传功能：上传
 */
public class FileImportActivity extends Activity {
    private final String CHECK = "CHECK";
    private final String DIR_NAME ="DIR_NAME";

    private SimpleAdapter fileNameAdapter;
    private SimpleAdapter logAdapter;
    private ArrayList<HashMap<String,Object>> dirNameList;
//    private ReadExterSdFileSdFile readExterSdFile;
    private ListView dirListView,logListView;
    private CheckBox allCheckBox;
    private Button actionBtn;
    private Context context;
    /**
     * 无论导入、导出、上传都需要选择文件或目录，此列表存储已选择的文件或目录名称
     */
    private ArrayList<String> selectDirNameList = new ArrayList<>();

    //点击action按钮后弹出对话框title是对话框标题
    private String title = "";
    //点击action按钮后弹出对话框leftBtnStr是对话框左按钮的功能描述
    private String leftBtnStr = "";
    //点击action按钮后弹出对话框rightBtnStr是对话框右按钮的功能描述
    private String rightBtnStr= "";

    /**
     * 进度提示列表的标题
     */
    private TextView logTvTitle;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_file_import);
        MyApplication.getInstance().addActivity(this);
        context = this;
        dirNameList = new ArrayList<>();
        selectDirNameList = new ArrayList<>();
        Intent intent= getIntent();
        final String type = intent.getStringExtra("类型");
        actionBtn = (Button)findViewById(R.id.file_action_btn);
        selectDirNameList.add("test");
        actionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SdCardOperate.deleteTestFile();
                DbAcessImpl dbAcess = DbAcessImpl.getDbInstance(context);
                dbAcess.startExportDb(LuceCellInfo.getYMD()+"导出"+endTimeStr,endTime,false,handler);
                actionBtn.setEnabled(false);
            }
        });
        logList= new ArrayList<>();
        logListView = (ListView)findViewById(R.id.file_log_listview);
        logAdapter = new SimpleAdapter(this,logList,R.layout.file_log_item,
                new String[]{DIR_NAME},new int[]{R.id.file_log_list});
        logListView.setAdapter(logAdapter);

    }

    /**
     * 文件或目录列表的点击监听
     */
    AdapterView.OnItemClickListener dirItemLis = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            HashMap<String,Object> map = dirNameList.get((int) id);
            boolean select = !((boolean)map.get(CHECK));
            map.put(CHECK,select);
            fileNameAdapter.notifyDataSetChanged();
            selectDir(map.get(DIR_NAME).toString(),select);
        }
    };


    private void selectDir(String dirName, boolean select){
        if(select){
            for(String name:selectDirNameList){
                if(name.equals(dirName)){
                    return;
                }
            }
            selectDirNameList.add(dirName);
        }else {
            for(int i=0;i<selectDirNameList.size();i++){
                if(dirName.equals(selectDirNameList.get(i))){
                    selectDirNameList.remove(i);
                    return;
                }
            }
        }
    }
    private ArrayList<HashMap<String,Object>> logList;
    /**
     * 数据导出 arg2 == 1000  档arg1==1001时表示导出完成
     * 数据导入 arg2 == 2000  档arg1==2001时表示导入完成
     * 数据上传 arg2 == 3000  档arg1==3001时表示上传完成或因其他问题导致上传终止
     */
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            HashMap<String,Object> map = new HashMap<>();
            map.put(DIR_NAME,(String)msg.obj);
            logList.add(0,map);
            if(logList.size()>30) logList.remove(logList.size()-1);
            logAdapter.notifyDataSetChanged();
//            if(msg.arg2 == 1000&&msg.arg1==1001){
//                actionBtn.setEnabled(true);
//                fileNameAdapter.notifyDataSetChanged();
//            }
//            else if(msg.arg2 == 2000&&msg.arg1==2001){
//                actionBtn.setEnabled(true);
//                initImport();
//                fileNameAdapter.notifyDataSetChanged();
//            }
//            else if(msg.arg2 == 3000&&msg.arg1==3001){
//                initUpload();
//                actionBtn.setEnabled(true);
//                fileNameAdapter.notifyDataSetChanged();
//            }
        }
    };

    private void initUpload(){
        title = "数据上传";
        leftBtnStr = "开始上传";
        rightBtnStr = "取消";
        actionBtn.setText("开始上传");
        dirNameList.clear();
        DbAcessImpl dbAcess = DbAcessImpl.getDbInstance(context);
        ArrayList<String> markList = dbAcess.loadUploadMark();
        markList.add("test");
        for(String mark:markList){
            HashMap<String,Object> map = new HashMap<>();
            map.put(CHECK,false);
            map.put(DIR_NAME,mark);
            dirNameList.add(map);
        }
        allCheckBox.setText("本机所有未上传的文件");
        logTvTitle.setText("数据上传进度");
    }
    @Override
    protected void onStart() {
        super.onStart();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
    private RadioGroup radioGroup;
    private RadioButton todayRadio,threeDayRadio,sevenDayRadio,oneMonthRadio;
    private String endTime = TrackActivity.getPastTime("一天内");
    private String endTimeStr = "一天内";
    private void initRadioGroup(){
        todayRadio = (RadioButton)findViewById(R.id.toady_radio);
        threeDayRadio = (RadioButton)findViewById(R.id.three_day_radio);
        sevenDayRadio = (RadioButton)findViewById(R.id.seven_day_radio);
        oneMonthRadio = (RadioButton)findViewById(R.id.one_month_radio);
        radioGroup = (RadioGroup)findViewById(R.id.select_time_group);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int id) {
                if(id==R.id.toady_radio){
                    endTime = TrackActivity.getPastTime("一天内");
                    endTimeStr = "一天内";
                }
                else if(id==R.id.three_day_radio){
                    endTime = TrackActivity.getPastTime("三天内");
                    endTimeStr = "三天内";
                }
                else if(id==R.id.seven_day_radio){
                    endTime = TrackActivity.getPastTime("七天内");
                    endTimeStr = "七天内";
                }
                else if(id==R.id.one_month_radio){
                    endTime = TrackActivity.getPastTime("一月内");
                    endTimeStr = "一月内";
                }
            }
        });
    }
}
