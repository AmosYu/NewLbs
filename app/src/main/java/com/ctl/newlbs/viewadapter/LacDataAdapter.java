package com.ctl.newlbs.viewadapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ctl.newlbs.R;
import com.ctl.newlbs.utils.LuceCellInfo;

import java.util.List;


/**
 * Created by on 16/5/15.
 */
public class LacDataAdapter extends CommonAdapter<LuceCellInfo> {

    private List<LuceCellInfo> datas;
    private int mode=0;
    public LacDataAdapter(Context context, List<LuceCellInfo> datas, int layoutId) {
        super(context, datas, layoutId);
        this.datas=datas;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = ViewHolder.get(mContext, convertView, parent, R.layout.cell_listview_item, position);
        LuceCellInfo cellInfo = datas.get(position);
        if (mode==2){
            (holder.getView(R.id.dilog_cell_bid)).setVisibility(View.VISIBLE);
        }else {
            (holder.getView(R.id.dilog_cell_bid)).setVisibility(View.GONE);
        }
        ((TextView)holder.getView(R.id.dilog_cell_lac)).setText(String.valueOf(cellInfo.getLac_sid()));
        ((TextView)holder.getView(R.id.dilog_cell_ci)).setText(" , "+ String.valueOf(cellInfo.getCi_nid()));
        ((TextView)holder.getView(R.id.dilog_cell_bid)).setText(" , "+ String.valueOf(cellInfo.getBid()));

        ((TextView)holder.getView(R.id.number)).setText(" , "+ String.valueOf(cellInfo.getSize()));
        ((TextView)holder.getView(R.id.dilog_cell_lac)).setTextColor(datas.get(position).getColor());
        ((TextView)holder.getView(R.id.dilog_cell_ci)).setTextColor(datas.get(position).getColor());
        ((TextView)holder.getView(R.id.dilog_cell_bid)).setTextColor(datas.get(position).getColor());
        return holder.getConvertView();
    }

    @Override
    public void convert(ViewHolder holder, LuceCellInfo cellInfo) {

        holder.setText(R.id.dilog_cell_lac, String.valueOf(cellInfo.getLac_sid()))
                .setText(R.id.dilog_cell_ci, String.valueOf(cellInfo.getCi_nid()))
                .setText(R.id.dilog_cell_bid, String.valueOf(cellInfo.getBid()))
                .setText(R.id.number, "");
    }
}
