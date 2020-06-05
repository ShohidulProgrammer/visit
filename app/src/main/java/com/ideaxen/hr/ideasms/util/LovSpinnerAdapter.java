package com.ideaxen.hr.ideasms.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.ideaxen.hr.ideasms.R;
import com.ideaxen.hr.ideasms.model.LoVItem;

import java.util.List;

public class LovSpinnerAdapter extends BaseAdapter {

    LayoutInflater itemLayoutInflater;

    List<LoVItem> lovItems;

    public LovSpinnerAdapter(Context c, List<LoVItem> lovItems){
        this.lovItems = lovItems;

        itemLayoutInflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return lovItems.size();
    }

    @Override
    public LoVItem getItem(int position) {
        return lovItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LoVItem lovItem = getItem(position);

        View rowview = itemLayoutInflater.inflate(R.layout.simple_spinner_item,null,true);

        TextView txtTitle = (TextView) rowview.findViewById(R.id.txtSpinnerItem);
        txtTitle.setText(lovItem.getValue());


        return rowview;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        if(convertView == null){
            convertView = itemLayoutInflater.inflate(R.layout.simple_spinner_dropdown_item,parent, false);
        }
        LoVItem lovItem = getItem(position);
        TextView txtTitle = (TextView) convertView.findViewById(R.id.txtSpinnerDropdownItem);
        txtTitle.setText(lovItem.getValue());
        return convertView;
    }
}