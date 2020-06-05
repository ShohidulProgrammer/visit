package com.ideaxen.hr.ideasms.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.ideaxen.hr.ideasms.R;
import com.ideaxen.hr.ideasms.VisitDetailActivity;
import com.ideaxen.hr.ideasms.model.VisitDetailItem;

import java.util.List;

public class VisitDetailItemAdapter extends BaseAdapter {

    private static final String TAG = "VisitDetailItemAdapter";
    LayoutInflater itemLayoutInflater;
    private Context c;
    List<VisitDetailItem> visitDetails;
    private String name = "", number = "";

    public VisitDetailItemAdapter(Context c, List<VisitDetailItem> visitDetails){
        this.c = c;
        this.visitDetails = visitDetails;
        itemLayoutInflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return visitDetails.size();
    }

    @Override
    public VisitDetailItem getItem(int position) {
        return visitDetails.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final View listItemView = itemLayoutInflater.inflate(R.layout.visit_detail_item, null);
        TextView txtViewItemDetailName = listItemView.findViewById(R.id.txtViewItemDetailName);
        TextView txtViewItemDetailValue = listItemView.findViewById(R.id.txtViewItemDetailValue);
        VisitDetailItem item = getItem(position);
        txtViewItemDetailName.setText(item.getColDisplayName());
        if(item.getColDisplayName().equals("Contact Person")) {
            name = item.getValue();
        }
        if(item.getColDisplayName().equals("Cont Person Mob")){
            number = item.getValue();
            txtViewItemDetailValue.setText(number);
            if(!number.equals("")){
                txtViewItemDetailValue.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_call, 0);
                txtViewItemDetailValue.setBackgroundResource(R.drawable.call_section_style);
                txtViewItemDetailValue.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ((VisitDetailActivity)c).makeCall(listItemView, name, number);
                    }
                });
            }
        }
        else{
            txtViewItemDetailValue.setText(item.getValue());
        }

        return listItemView;
    }
}
