package com.ideaxen.hr.ideasms.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.ideaxen.hr.ideasms.R;
import com.ideaxen.hr.ideasms.model.ApprovalDetailItem;

import java.util.List;

public class ApprovalDetailItemAdapter extends BaseAdapter {
    LayoutInflater itemLayoutInflater;

    List<ApprovalDetailItem> approvalDetails;

    public ApprovalDetailItemAdapter(Context c, List<ApprovalDetailItem> approvalDetails){
        this.approvalDetails = approvalDetails;
        itemLayoutInflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return approvalDetails.size();
    }

    @Override
    public ApprovalDetailItem getItem(int position) {
        return approvalDetails.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View listItemView = itemLayoutInflater.inflate(R.layout.approval_detail_item, null);
        TextView txtViewItemDetailName = listItemView.findViewById(R.id.txtViewItemDetailName);
        TextView txtViewItemDetailValue = listItemView.findViewById(R.id.txtViewItemDetailValue);
        ApprovalDetailItem item = getItem(position);
        txtViewItemDetailName.setText(item.getColDisplayName());
        txtViewItemDetailValue.setText(item.getValue());

        return listItemView;
    }
}
