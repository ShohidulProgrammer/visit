package com.ideaxen.hr.ideasms;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.ideaxen.hr.ideasms.api.ApiManager;
import com.ideaxen.hr.ideasms.model.LoVItem;
import com.ideaxen.hr.ideasms.model.Login;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DialogController {

    private static final String TAG = "DialogController";

    public static void showToast(AppCompatActivity thisActivity, String message){
        Toast.makeText(thisActivity, message, Toast.LENGTH_SHORT).show();
    }

    public static void openNewInstantVisitDialog(final View v, final AppCompatActivity thisActivity){
        final View dialogView;
        final AlertDialog alertDialog;
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(thisActivity);
        // Get the layout inflater
        LayoutInflater inflater = thisActivity.getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        dialogView = inflater.inflate(R.layout.new_instant_visit, null);
        //=======---------------

        Spinner spinner1 = dialogView.findViewById(R.id.spinnerVisiteeCompany);
        MyApplication.bindLovToSpinner2(thisActivity, spinner1, "MY_CUSTOMER", "Select customer");

        Spinner spinner2 = dialogView.findViewById(R.id.spinnerWithPermissionFrom);
        MyApplication.bindLovToSpinner2(thisActivity, spinner2, "MY_LEADER", "Informed to");

        Spinner spinner3 = dialogView.findViewById(R.id.spinnerInstantVisitPurpose);
        MyApplication.bindLovToSpinner2(thisActivity, spinner3, "INSTANT_VISIT_REASON", "Select reason");

        Button btnCancel = dialogView.findViewById(R.id.btnCancelNewInstantVisit);
        Button btnCreate = dialogView.findViewById(R.id.btnCreateAndStartNewInstantVisit);
        alertDialogBuilder.setView(dialogView);

        alertDialog = alertDialogBuilder.create();
        alertDialog.show();

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });
        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createInstantVisit(v, dialogView, alertDialog, thisActivity);
            }
        });
    }

    private static void createInstantVisit(View v, final View dialogView, final AlertDialog alertDialog, final AppCompatActivity thisActivity){
        Spinner spinner1 = dialogView.findViewById(R.id.spinnerVisiteeCompany);
        Spinner spinner2 = dialogView.findViewById(R.id.spinnerWithPermissionFrom);
        Spinner spinner3 = dialogView.findViewById(R.id.spinnerInstantVisitPurpose);
        EditText fromField = dialogView.findViewById(R.id.edtTextInstantVisitFromWhere);

        final LoVItem visitCustomer = (LoVItem)spinner1.getSelectedItem();
        final LoVItem informedTo = (LoVItem)spinner2.getSelectedItem();
        final LoVItem visitReason = (LoVItem)spinner3.getSelectedItem();
        final String from = fromField.getText().toString().trim();

        if(visitCustomer.getValue().equals("Select customer")){
            showToast(thisActivity, "You must select Customer");
        }
        else if(informedTo.getValue().equals("Informed to")){
            showToast(thisActivity, "You must select whom you have to inform");
        }
        else if(visitReason.getValue().equals("Select reason")){
            showToast(thisActivity, "You must select a valid reason");
        }
        else if(from.isEmpty()){
            showToast(thisActivity, "You must select a starting place");
        }
        else{
            final AlertDialog confirmDialog;
            AlertDialog.Builder confirmDialogBuilder = new AlertDialog.Builder(thisActivity).setTitle("Confirmation Required");
            LayoutInflater inflater = thisActivity.getLayoutInflater();

            // Inflate and set the layout for the dialog
            // Pass null as the parent view because its going in the dialog layout
            View confirmDialogView = inflater.inflate(R.layout.confirm_dialog, null);
            confirmDialogBuilder.setView(confirmDialogView);

            Button btnNegative = confirmDialogView.findViewById(R.id.btnNegative);
            confirmDialog = confirmDialogBuilder.create();
            btnNegative.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    confirmDialog.dismiss();
                }
            });
            final boolean instantVisitAfterAnother;
            if(v != null){
                instantVisitAfterAnother = (v.getId() == R.id.btnStartInstantVisitAfterAnotherVisit);
            }
            else{
                instantVisitAfterAnother = false;
            }
            Button btnPositive = confirmDialogView.findViewById(R.id.btnPositive);
            btnPositive.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Spinner spinner1 = dialogView.findViewById(R.id.spinnerVisiteeCompany);
                    Spinner spinner2 = dialogView.findViewById(R.id.spinnerWithPermissionFrom);
                    Spinner spinner3 = dialogView.findViewById(R.id.spinnerInstantVisitPurpose);
                    EditText fromField = dialogView.findViewById(R.id.edtTextInstantVisitFromWhere);

                    final LoVItem visitCustomer = (LoVItem)spinner1.getSelectedItem();
                    final LoVItem informedTo = (LoVItem)spinner2.getSelectedItem();
                    final LoVItem visitReason = (LoVItem)spinner3.getSelectedItem();
                    final String from = fromField.getText().toString().trim();

                    if(visitCustomer.getValue().equals("Select customer")){
                        showToast(thisActivity, "You must select Customer");
                    }
                    else if(informedTo.getValue().equals("Informed to")){
                        showToast(thisActivity, "You must select whom you have to inform");
                    }
                    else if(visitReason.getValue().equals("Select reason")){
                        showToast(thisActivity, "You must select a valid reason");
                    }
                    else if(from.isEmpty()){
                        showToast(thisActivity, "You must select a starting place");
                    }
                    else{
                        newInstantVisitConfirmed(visitCustomer, informedTo, visitReason, from, instantVisitAfterAnother?"1":"0", thisActivity, confirmDialog);
                        alertDialog.dismiss();
                        confirmDialog.dismiss();
                    }
                }
            });
            confirmDialog.show();
        }
    }

    private static void newInstantVisitConfirmed(LoVItem visiteeCompany, LoVItem informedTo, LoVItem visitReason, String from, String instantVisitAfterAnother, final AppCompatActivity thisActivity, final AlertDialog confirmDialog){
        Login loginUser = MyApplication.isLoggedin();
        ApiManager apiManager = ApiManager.getInstance();
        Call<List<String>> visitDetails = apiManager.createInstantVisit(loginUser.getToken(), loginUser.getUsername(), loginUser.getUser().getEmpId(), MyApplication.getAppUniqueID(thisActivity),
                visiteeCompany.getId(), visiteeCompany.getValue(), informedTo.getId(), informedTo.getValue(), visitReason.getKey(), from, instantVisitAfterAnother);
        visitDetails.enqueue(new Callback<List<String>>() {
            @Override
            public void onResponse(Call<List<String>> call, Response<List<String>> response) {
                List<String> responseData = response.body();
                Log.d(TAG, "onResponse: " + responseData);
//                Visit visit = new Visit(responseData.get(0), responseData.get(1), responseData.get(2),
//                        responseData.get(3), responseData.get(4), responseData.get(0));
//                visits.add(0, visit);
//                visitItemAdapter.notifyDataSetChanged();
//                MyApplication.isRunningVisit=responseData.get(0);
//                moveToDetailView(visit, 0, true);
                showToast(thisActivity,"Your instant visit is waiting for approval!");
//                if(response.body()!=null);
//                locationSaver.startLocationProcess(thisContext, "0","An instant visit was created");
            }

            @Override
            public void onFailure(Call<List<String>> call, Throwable t) {
                Log.d("ERROR", t.getMessage());
            }
        });

        confirmDialog.dismiss();
    }
}
