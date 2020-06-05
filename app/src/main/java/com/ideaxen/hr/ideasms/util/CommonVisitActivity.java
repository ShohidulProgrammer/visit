package com.ideaxen.hr.ideasms.util;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.ideaxen.hr.ideasms.R;
import com.ideaxen.hr.ideasms.model.Login;
import com.ideaxen.hr.ideasms.model.Visit;
import com.ideaxen.hr.ideasms.model.VisitDetailItem;
import com.ideaxen.hr.ideasms.model.VisitReport;

import java.util.List;

import io.realm.Realm;

public class CommonVisitActivity extends AppCompatActivity {

    protected View dialogView;
    protected AlertDialog alertDialog;
    protected AlertDialog confirmDialog;

    protected List<VisitDetailItem> visitDetails;
    protected VisitReport visitReport;
    protected Login loginUser;
    protected Visit visit;

    protected Context thisContext;
    protected Activity thisActivity;

    protected Button btnShowVisitProgress;
    protected Button btnReachedCustomer;
    protected Button btnUpdateVisitStatus;
    protected Button btnStartVisit;
    protected Button btnStartVisitFromOffice;

    protected View mainView;

    protected Spinner spinnerAfterAnotherVisit;
    protected Spinner spinnerOutOfOfficePurpose;
    protected Spinner spinnerVisitCancelPurpose;

//    protected MenuItem menuItemLogout;
//    protected MenuItem menuItemVisitSummary;
//    protected MenuItem menuItemVisitDetail;

    protected TextView txtViewVisiteeCompanyName;

    protected static boolean VISIT_STARTED  = false;
    protected static boolean VISIT_STARTED_FROM_OFFICE  = true;
    protected static boolean VISIT_STARTED_AFTER_ANOTHER_VISIT  = false;
    protected static int VISIT_STATUS  = R.integer.VISIT_STATUS_NOT_STARTED;
    protected static int CURR_ACTION  = -1;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actionbar_menu, menu);
//        menuItemLogout = menu.findItem(R.id.action_logout);
//        menuItemVisitSummary = menu.findItem(R.id.action_visit_summary);
//        menuItemVisitDetail = menu.findItem(R.id.action_visit_detail);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public void onBackPressed() {
        Toast.makeText(thisContext, this.getClass().getSimpleName(), Toast.LENGTH_SHORT).show();
        if(CommonVisitActivity.CURR_ACTION == R.integer.ACTION_VISIT_CANCEL)
            super.onBackPressed();
        else if(CommonVisitActivity.CURR_ACTION == R.integer.ACTION_VISIT_RESCHEDULE)
            super.onBackPressed();
        else if(!CommonVisitActivity.VISIT_STARTED)
            super.onBackPressed();
    }

    public void openDialog(int dialog_layout){
        switch (dialog_layout){
            case R.layout.visit_cancelled:
                CommonVisitActivity.CURR_ACTION = R.integer.ACTION_VISIT_CANCEL;
                break;
            case R.layout.visit_reschedule:
                CommonVisitActivity.CURR_ACTION = R.integer.ACTION_VISIT_RESCHEDULE;
        }
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        // Get the layout inflater
        LayoutInflater inflater = this.getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        dialogView = inflater.inflate(dialog_layout, null);
        alertDialogBuilder.setView(dialogView);

        alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public void cancelDialog(View view) {
        alertDialog.dismiss();
    }
    public void openCancelVisitDialog(View view) {
        openDialog(R.layout.visit_cancelled);
    }
    public void openRescheduleVisitDialog(View view) {
        openDialog(R.layout.visit_reschedule);
    }
    public void openVisitStatusDialog(View view) {
        openDialog(R.layout.visit_status);
    }

    public void cancelVisitDialog(View v){
        if(alertDialog.isShowing()){
            alertDialog.dismiss();
        }

        Toast.makeText(thisActivity,"You have disagreed to proceed",Toast.LENGTH_SHORT).show();
    }

    public void proceedVisitCancel(View v){
        //CANCELLATION PROCESS HERE
        alertDialog.dismiss();
        onBackPressed();
    }
    public void proceedVisitRescedule(View v){
        //RESCHEDULE PROCESS HERE
        alertDialog.dismiss();
        onBackPressed();
    }

    public void rescheduledVisit(String reason, String newVisitngDate){
        visitReport.setRescheduleReason(reason);
        visitReport.setRescheduledVisitingDate(newVisitngDate);
        updateVisitStatus(R.integer.VISIT_STATUS_RESCHEDULED);
    }
    public void canceledVisit(String reason){
        visitReport.setCancelReason(reason);
        updateVisitStatus(R.integer.VISIT_STATUS_CANCELED);
    }
    public void startedVisitAfterAnotherVisit(String from){
        visitReport.setAfterAnotherVisit("Yes");
        startedVisitFromOutOfOffice(from, "Consequent Visit");
    }
    public void startedVisitFromOutOfOffice(String from, String reason){
        visitReport.setStartedFrom(from);
        updateVisitStatus(R.integer.VISIT_STATUS_STARTED_FROM_OTHER);
    }
    public void startedVisitFromOffice(){
        visitReport.setStartedFrom("Office");//(R.string.text_started_visit_from_office);
        updateVisitStatus(R.integer.VISIT_STATUS_STARTED_FROM_OTHER);
    }
    public void updateVisitStatus(int visitStatus){
        Resources res = getResources();
        visitStatus = res.getInteger(visitStatus);
        visitReport.setVisitStatus(visitStatus);
        Realm realm = Realm.getDefaultInstance();
        // Copy elements from Retrofit to Realm to persist them.
        realm.beginTransaction();
        visitReport = realm.copyToRealmOrUpdate(visitReport);
        realm.commitTransaction();
    }
}
