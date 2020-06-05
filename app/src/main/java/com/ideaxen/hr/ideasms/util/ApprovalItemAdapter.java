package com.ideaxen.hr.ideasms.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.ideaxen.hr.ideasms.MyApplication;
import com.ideaxen.hr.ideasms.MyApprovalActivity;
import com.ideaxen.hr.ideasms.R;
import com.ideaxen.hr.ideasms.model.Visit;

import java.util.List;

public class ApprovalItemAdapter extends BaseAdapter {

    private LayoutInflater itemLayoutInflater;

    private List<Visit> approvalItems;
    private Context c;
    private String approvalFilterPref;

    public ApprovalItemAdapter(Context c, List<Visit> approvalItems, String filterPref){
        this.approvalItems = approvalItems;
        this.c = c;
        this.approvalFilterPref = filterPref;
        itemLayoutInflater = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        if(approvalItems == null)return 0;
        return approvalItems.size();
    }

    @Override
    public Visit getItem(int position) {
        return approvalItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View listItemView = itemLayoutInflater.inflate(R.layout.approval_list_item, null);
        TextView txtViewApproverName = listItemView.findViewById(R.id.txtViewApproverName);
        TextView txtViewApproverAddress = listItemView.findViewById(R.id.txtViewApproverAddress);
        TextView txtViewApprovingDate = listItemView.findViewById(R.id.txtViewApprovingDate);

        View rateViews[] = new View[5];
        rateViews[0] = listItemView.findViewById(R.id.rate1);
        rateViews[1] = listItemView.findViewById(R.id.rate2);
        rateViews[2] = listItemView.findViewById(R.id.rate3);
        rateViews[3] = listItemView.findViewById(R.id.rate4);
        rateViews[4] = listItemView.findViewById(R.id.rate5);

        Button btnDecline = listItemView.findViewById(R.id.btnDecline);
        Button btnApprove = listItemView.findViewById(R.id.btnApprove);

        if(approvalFilterPref.equals("7") || approvalFilterPref.equals("8") || approvalFilterPref.equals("9")){
            btnApprove.setVisibility(View.GONE);
            btnDecline.setVisibility(View.GONE);
        }

        final Visit currentApprovalItem = getItem(position);
        if(currentApprovalItem.getId().equals(MyApplication.isRunningVisit)) {
            listItemView.setBackgroundColor(ContextCompat.getColor(c, R.color.colorListItemBg));
        }

        if(currentApprovalItem.getIsInstant().equals("Yes")) {
            listItemView.setBackgroundColor(ContextCompat.getColor(c, R.color.colorListInstantItemBg));
        }

        if(currentApprovalItem.getVisitStatus().equals("ADJOURNED")) {
            listItemView.setBackgroundColor(ContextCompat.getColor(c, R.color.colorListAdjournedItemBg));
        }

        txtViewApproverName.setText(currentApprovalItem.getCustomer());
        txtViewApproverAddress.setText(currentApprovalItem.getAddress());
        txtViewApprovingDate.setText(currentApprovalItem.getVisitDate());
        int priority = Integer.valueOf(currentApprovalItem.getVisitPriority());
        if(priority > 0){
            for(int i = 0; i < priority; i++){
                rateViews[i].setBackground(ContextCompat.getDrawable(c, R.drawable.rating_circle_full));
            }
        }

        btnApprove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MyApprovalActivity)c).openAppDecConfirmDialog(1, currentApprovalItem.getId(), currentApprovalItem.getVisitPriority());
            }
        });

        btnDecline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MyApprovalActivity)c).openAppDecConfirmDialog(0, currentApprovalItem.getId(), currentApprovalItem.getVisitPriority());
            }
        });

        return listItemView;
    }
}
