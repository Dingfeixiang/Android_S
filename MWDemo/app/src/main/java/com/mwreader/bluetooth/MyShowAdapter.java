package com.mwreader.bluetooth;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class MyShowAdapter extends BaseAdapter {  
	  
    private Context context;  
    private List list;  
    public MyShowAdapter(Context context, List list) {  
        this.context = context;  
        this.list = list;  
    }  
  
    @Override  
    public int getCount() {  
        // TODO Auto-generated method stub  
        return list.size();  
    }  
  
    @Override  
    public Object getItem(int position) {  
        // TODO Auto-generated method stub  
        return position;  
    }  
  
    @Override  
    public long getItemId(int position) {  
        // TODO Auto-generated method stub  
        return position;  
    }  
  
    @Override  
    public View getView(int position, View convertView, ViewGroup parent) {  
        // TODO Auto-generated method stub  
        if(convertView==null){  
           // convertView=LayoutInflater.from(context).inflate(R.layout.adater_line, null);  
        }  
        return convertView;  
    }


  
}  