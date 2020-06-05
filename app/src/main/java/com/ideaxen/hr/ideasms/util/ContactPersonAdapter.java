package com.ideaxen.hr.ideasms.util;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;

import com.ideaxen.hr.ideasms.R;
import com.ideaxen.hr.ideasms.model.ContactPerson;

import java.util.List;

public class ContactPersonAdapter extends BaseAdapter {

    private static final String TAG = "ContactPersonAdapter";

    private LayoutInflater itemLayoutInflater;

    private Context context;
    private List<ContactPerson> contactPersonList;


    public ContactPersonAdapter(Context context, List<ContactPerson> contactPersonList) {

        this.context = context;
        this.contactPersonList = contactPersonList;
        itemLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        if (contactPersonList == null) return 0;
        return contactPersonList.size();
    }

    @Override
    public ContactPerson getItem(int position) {
        return contactPersonList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final View listItemView = itemLayoutInflater.inflate(R.layout.contact_person_list_item, null);
        TextView tv_contact_name = listItemView.findViewById(R.id.tv_contact_name);
        TextView tv_contact_designation = listItemView.findViewById(R.id.tv_contact_designation);
        TextView tv_contact_number = listItemView.findViewById(R.id.tv_contact_number);
        TextView tv_customer_name = listItemView.findViewById(R.id.tv_customer_name);
        Button btn_copy = listItemView.findViewById(R.id.btn_copy);
        Button btn_call = listItemView.findViewById(R.id.btn_call);

        final ContactPerson contactPerson = contactPersonList.get(position);

        String contact_name = "", contact_designation = "", contact_number = "", customer_name = "";
        if (contactPerson.getContactName() != null) {
            contact_name = contactPerson.getContactName();
        }

        if (contactPerson.getContactDesignation() != null) {
            contact_designation = contactPerson.getContactDesignation();
        }

        if (contactPerson.getContactNumber() != null) {
            contact_number = contactPerson.getContactNumber();
        }

        if (contactPerson.getCustomerName() != null) {
            customer_name = contactPerson.getCustomerName();
        }

        tv_contact_name.setText(contact_name);
        tv_contact_designation.setText(contact_designation);
        tv_contact_number.setText(contact_number);
        tv_customer_name.setText(customer_name);

        btn_copy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (contactPerson.getContactNumber() != null) {
                    copyNumber(contactPerson.getContactNumber());
                }
            }
        });

        btn_call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (contactPerson.getContactNumber() != null) {
                    String name = contactPerson.getContactName();
                    String number = contactPerson.getContactNumber();
                    if(!number.equals("")){
                        makeCall(listItemView, name, number);
                    }
                    else{
                        Toast.makeText(context, "No number!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        return listItemView;
    }

    private void copyNumber(String number) {
        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        android.content.ClipData clip = android.content.ClipData.newPlainText("Number Copied", number);
        if (clipboard != null) {
            clipboard.setPrimaryClip(clip);
            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show();
        }
    }

    private void makeCall(View v , String name, final String number){
        AlertDialog.Builder alertbox = new AlertDialog.Builder(v.getRootView().getContext());
        alertbox.setTitle("Do you want to call " + name + "?");

        LayoutInflater inflater = LayoutInflater.from(v.getRootView().getContext());
        View confirmDialogView = inflater.inflate(R.layout.confirm_dialog, null);
        alertbox.setView(confirmDialogView);

        Button btnNegative = confirmDialogView.findViewById(R.id.btnNegative);
        final AlertDialog confirmDialog = alertbox.create();
        btnNegative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmDialog.dismiss();
            }
        });

        Button btnPositive = confirmDialogView.findViewById(R.id.btnPositive);
        btnPositive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmDialog.dismiss();
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + number));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                context.startActivity(intent);
            }
        });

        confirmDialog.show();
    }
}
