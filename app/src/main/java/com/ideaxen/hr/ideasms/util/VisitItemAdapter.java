package com.ideaxen.hr.ideasms.util;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.ideaxen.hr.ideasms.MyApplication;
import com.ideaxen.hr.ideasms.R;
import com.ideaxen.hr.ideasms.model.Visit;

import java.util.List;

public class VisitItemAdapter extends BaseAdapter {
    private static final String TAG = "VisitItemAdapter";
    LayoutInflater itemLayoutInflater;

    List<Visit> visitItems;
    Context c;

    public VisitItemAdapter(Context c, List<Visit> visitItems){
        this.visitItems = visitItems;
        this.c = c;
        itemLayoutInflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        if(visitItems == null)return 0;
        return visitItems.size();
    }

    @Override
    public Visit getItem(int position) {
        return visitItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View listItemView = itemLayoutInflater.inflate(R.layout.visit_list_item, null);
        TextView txtViewVisiteeName = listItemView.findViewById(R.id.txtViewVisiteeName);
        TextView txtViewVisiteeAddress = listItemView.findViewById(R.id.txtViewVisiteeAddress);
        TextView txtViewVisitingDate = listItemView.findViewById(R.id.txtViewVisitingDate);


        View rateViews[] = new View[5];
        rateViews[0] = listItemView.findViewById(R.id.rate1);
        rateViews[1] = listItemView.findViewById(R.id.rate2);
        rateViews[2] = listItemView.findViewById(R.id.rate3);
        rateViews[3] = listItemView.findViewById(R.id.rate4);
        rateViews[4] = listItemView.findViewById(R.id.rate5);

        Visit currentVisitItem = getItem(position);
        Log.d(TAG, "currentVisitItem: " + currentVisitItem.toString());
        if(currentVisitItem.getId().equals(MyApplication.isRunningVisit)) {
            listItemView.setBackgroundColor(ContextCompat.getColor(c, R.color.colorListItemBg));
        }

        if(currentVisitItem.getVisitStatus().equals("ADJOURNED")) {
            listItemView.setBackgroundColor(ContextCompat.getColor(c, R.color.colorListAdjournedItemBg));
        }

        if(currentVisitItem.getIsInstant().equals("Yes")) {
            listItemView.setBackgroundColor(ContextCompat.getColor(c, R.color.colorListInstantItemBg));
        }

        txtViewVisiteeName.setText(currentVisitItem.getCustomer());
        txtViewVisiteeAddress.setText(currentVisitItem.getAddress());
        txtViewVisitingDate.setText(currentVisitItem.getVisitDate());
        return listItemView;
    }
}
