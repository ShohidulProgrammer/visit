package com.ideaxen.hr.ideasms.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.ideaxen.hr.ideasms.R;
import com.ideaxen.hr.ideasms.model.CustomerInfo;

import java.util.List;

public class CustomerInfoAdapter extends BaseAdapter {

    private Context context;
    private List<CustomerInfo> customerInfoList;

    public CustomerInfoAdapter(Context context, List<CustomerInfo> customerInfoList) {
        this.context = context;
        this.customerInfoList = customerInfoList;
    }

    @Override
    public int getCount() {
        return customerInfoList.size();
    }

    @Override
    public Object getItem(int position) {
        return customerInfoList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = LayoutInflater.from(context).inflate(R.layout.cust_info_item, parent, false);

        TextView cust_info_name = convertView.findViewById(R.id.cust_info_name);

        final CustomerInfo customerInfo = customerInfoList.get(position);
        String id = customerInfo.getId();
        String name = customerInfo.getCustomerName();
        cust_info_name.setText(name);

        return convertView;
    }
}
