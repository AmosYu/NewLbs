/*
 * @Title CommonAdapter.java
 * @Copyright Copyright 2010-2015 Yann Software Co,.Ltd All Rights Reserved.
 * @Description��
 * @author Yann
 * @date 2015-8-5 ����10:39:05
 * @version 1.0
 */
package com.ctl.newlbs.viewadapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;


public abstract class CommonAdapter<T> extends BaseAdapter
{
	protected Context mContext;
	protected List<T> mDatas;
	protected LayoutInflater mInflater;
	protected int mlayoutId;

	public CommonAdapter(Context context, List<T> datas, int layoutId)
	{
		this.mContext = context;
		this.mDatas = datas;
		this.mlayoutId = layoutId;
		mInflater = LayoutInflater.from(context);
	}


	@Override
	public int getCount()
	{
		return mDatas.size();
	}


	@Override
	public T getItem(int position)
	{
		return mDatas.get(position);
	}


	@Override
	public long getItemId(int position)
	{
		return position;
	}


	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		ViewHolder holder = ViewHolder.get(mContext, convertView, parent, mlayoutId, position);

		convert(holder, getItem(position));

		return holder.getConvertView();
	}

	public abstract void convert(ViewHolder holder, T t);
}
