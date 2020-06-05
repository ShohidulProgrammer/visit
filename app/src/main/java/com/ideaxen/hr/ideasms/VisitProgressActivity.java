package com.ideaxen.hr.ideasms;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ideaxen.hr.ideasms.api.ApiManager;
import com.ideaxen.hr.ideasms.model.Visit;
import com.ideaxen.hr.ideasms.model.LoVItem;
import com.ideaxen.hr.ideasms.model.Login;
import com.ideaxen.hr.ideasms.model.VisitDetailItem;
import com.ideaxen.hr.ideasms.model.VisitReport;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import io.realm.Realm;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VisitProgressActivity extends AppCompatActivity {

    private static final String TAG = "VisitProgressActivity";

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

    protected TextView txtViewVisiteeCompanyName;

    protected static boolean VISIT_STARTED  = false;
    protected static boolean VISIT_STARTED_FROM_OFFICE  = true;
    protected static boolean VISIT_STARTED_AFTER_ANOTHER_VISIT  = false;
    protected static int VISIT_STATUS  = R.integer.VISIT_STATUS_NOT_STARTED;
    protected static int CURR_ACTION  = -10;
    protected static int LAST_ACTION_TAKEN;

    //====================-----------------------=============================

    LinearLayout containerVisitProgressItemStartedAt;
    LinearLayout containerVisitProgressItemCancelledAt;
    LinearLayout containerVisitProgressItemCancelledReason;
    LinearLayout containerVisitProgressItemRescheduledAt;
    LinearLayout containerVisitProgressItemRescheduledReason;
    LinearLayout containerVisitProgressItemRescheduledDate;
    LinearLayout containerVisitProgressItemStartedFrom;
    LinearLayout containerVisitProgressItemStartAfterAnotherVisit;
    LinearLayout containerVisitProgressItemReachedCustomer;
    LinearLayout containerVisitProgressItemFinishedVisit;
    LinearLayout containerVisitProgressItemFinishedVisitFeedbackNote;
    LinearLayout containerVisitProgressItemFinishedVisitStatus;
    LinearLayout containerVisitProgressItemVisitingDate;
    LinearLayout containerVisitProgressItemVisitStatusTitle;
    LinearLayout containerVisitProgressItemAdjournedTime;
    LinearLayout containerVisitProgressItemAdjournedReason;
    LinearLayout containerVisitProgressItemAdjournInstructionFrom;
    LinearLayout containerVisitProgressItemResumeVisit;
    LinearLayout containerVisitProgressItemAdjournResumeDate;
    LinearLayout containerVisitProgressItemAdjournResumeTime;
    LinearLayout panelVisitProgressBottom;
    LinearLayout panelVisitProgressBottom0;


    TextView txtViewVisiteeCompanyAddress;

    TextView txtViewVisitProgressItemStartingTime;
    TextView txtViewVisitProgressItemStartedFromText;
    TextView txtViewReachedAtCustomerTime;
    TextView txtViewVisitFinishedTime;
    TextView txtViewVisitCancelledTime;
    TextView txtViewVisitCancelledReason;
    TextView txtViewVisitRescheduledTime;
    TextView txtViewVisitRescheduledReason;
    TextView txtViewVisitRescheduledVisitingDate;
    TextView txtViewVisitFinishedFeedbackNote;
    TextView txtViewVisitFinishedStatus;
    TextView txtViewVisitProgressItemVisitingDate;
    TextView txtViewVisitProgressItemVisitStatusTitle;
    TextView txtViewAdjournedAt;
    TextView txtViewAdjournReason;
    TextView txtViewAdjournInstructionBy;
    TextView txtViewAdjournResumeDate;
    TextView txtViewAdjournResumeTime;

    private DatePickerDialog datePickerDialog;
    private SimpleDateFormat dateFormatter;

    private boolean isRecentlyVisited;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = this.getLayoutInflater();
        mainView = inflater.inflate(R.layout.activity_visit_progress, null);
        setContentView(mainView);

        findViewsById();

        //Add back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Get the application context
        thisContext = getApplicationContext();

        // Get the activity
        thisActivity = VisitProgressActivity.this;

        LAST_ACTION_TAKEN = VisitDetailActivity.DEFAULT_LAST_ACTION_TAKEN;

        Intent in = getIntent();
        visit = getIntent().getParcelableExtra("com.ideaxen.client.visitapp.model.VISIT");
        loginUser = getIntent().getParcelableExtra("com.ideaxen.client.visitapp.model.LOGIN");
        visitReport = getIntent().getParcelableExtra("com.ideaxen.client.visitapp.model.VISIT_REPORT");
        isRecentlyVisited = getIntent().getBooleanExtra("com.ideaxen.client.visitapp.bool.RECENTLY_VISITED", false);

        txtViewVisiteeCompanyName.setText(visit.getCustomer());
        txtViewVisiteeCompanyAddress.setText(visit.getAddress());

        dateFormatter = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
    }

    public void showToast(String message){
        Toast.makeText(thisActivity, message, Toast.LENGTH_SHORT).show();
    }

    public void openConfirmDialog(View v){
        AlertDialog.Builder confirmDialogBuilder = new AlertDialog.Builder(this).setTitle("Confirmation Required");
        LayoutInflater inflater = this.getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        View confirmDialogView = inflater.inflate(R.layout.confirm_dialog, null);
        confirmDialogBuilder.setView(confirmDialogView);

        if(v.getId() == R.id.btnResumeVisit){
            VisitProgressActivity.CURR_ACTION = R.integer.ACTION_RESUME_VISIT;
        }

        Button btnNegative = confirmDialogView.findViewById(R.id.btnNegative);
        confirmDialog = confirmDialogBuilder.create();
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
                switch (VisitProgressActivity.CURR_ACTION) {
                    case R.integer.ACTION_VISIT_CANCEL:
                        Spinner spinnerVisitCancelPurpose = alertDialog.findViewById(R.id.spinnerVisitCancelPurpose);
                        if(spinnerVisitCancelPurpose.getSelectedItem().toString().equals("Select reason")){
//                            Toast.makeText(thisContext, "You must select a valid reason", Toast.LENGTH_SHORT);
                            showToast("You must select a valid reason");
                            break;
                        }
                        proceedVisitCancel(v, spinnerVisitCancelPurpose.getSelectedItem().toString());
                        break;
                    case R.integer.ACTION_VISIT_RESCHEDULE:
                        Spinner spinnerVisitReschedulePurpose = alertDialog.findViewById(R.id.spinnerVisitReschedulePurpose);
                        if(spinnerVisitReschedulePurpose.getSelectedItem().toString().equals("Select reason")){
                            showToast("You must select a valid reason");
                            break;
                        }
                        EditText edtTextNewVisitingDate = alertDialog.findViewById(R.id.edtTextNewVisitingDate);
                        if (edtTextNewVisitingDate != null) {
                            edtTextNewVisitingDate.setInputType(InputType.TYPE_NULL);
                        }
//                        if(edtTextNewVisitingDate.getText() == null){
//                            Toast.makeText(thisContext, "You must select a valid visiting date", Toast.LENGTH_SHORT);
//                            break;
//                        }
                        proceedVisitRescedule(v, spinnerVisitReschedulePurpose.getSelectedItem().toString(), edtTextNewVisitingDate.getText().toString());
                        break;
                    case R.integer.ACTION_VISIT_ADJOURN:
                        Spinner spinnerAdjournVisitPurpose = alertDialog.findViewById(R.id.spinnerAdjournVisitPurpose);
                        if(spinnerAdjournVisitPurpose.getSelectedItem().toString().equals("Select reason")){
                            showToast("You must select a valid reason");
                            break;
                        }
                        Spinner spinnerWithInstructionFrom = alertDialog.findViewById(R.id.spinnerWithInstructionFrom);
                        if(spinnerWithInstructionFrom.getSelectedItem().toString().equals("With Instruction From")){
                            showToast("You must select the instructor");
                            break;
                        }
                        LoVItem loVItem = (LoVItem)spinnerWithInstructionFrom.getSelectedItem();
                        proceedVisitAdjourn(v, spinnerAdjournVisitPurpose.getSelectedItem().toString(), loVItem.getValue(), loVItem.getId());
                        break;
                    case R.integer.ACTION_RESUME_VISIT:
                        proceedResumeVisit(v);
                        break;
                }
            }
        });

        confirmDialog.show();
    }
    public void reachedCustomer(View v){
        AlertDialog.Builder confirmDialogBuilder = new AlertDialog.Builder(this).setTitle("Confirmation Required");
        LayoutInflater inflater = this.getLayoutInflater();

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

        Button btnPositive = confirmDialogView.findViewById(R.id.btnPositive);
        btnPositive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmDialog.dismiss();
//                updateVisitStatus(R.integer.VISIT_STATUS_REACHED_CUSTOMER);
                updateOnServer(R.integer.VISIT_STATUS_REACHED_CUSTOMER);
            }
        });

        confirmDialog.show();

        confirmDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
//                Toast.makeText(thisActivity, "Dialog gone", Toast.LENGTH_SHORT);
                if(VisitProgressActivity.VISIT_STATUS == R.integer.VISIT_STATUS_REACHED_CUSTOMER){
                    containerVisitProgressItemReachedCustomer.setVisibility(View.VISIBLE);
                }
            }
        });
    }
    public void finishVisit(View v){
        AlertDialog.Builder confirmDialogBuilder = new AlertDialog.Builder(this).setTitle("Confirmation Required");
        LayoutInflater inflater = this.getLayoutInflater();

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

        Button btnPositive = confirmDialogView.findViewById(R.id.btnPositive);
        btnPositive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Spinner visitStatus = (Spinner) dialogView.findViewById(R.id.spinnerVisitStatus);
                if(visitStatus.getSelectedItem().toString().equals("Select visit status")){
//                    Toast.makeText(thisContext, "You must select a valid status", Toast.LENGTH_SHORT);
                    showToast("You must select a valid status");
                }
                else {
                    EditText feedbackNote = dialogView.findViewById(R.id.edtTextVisitFeedback);
                    updateOnServer(R.integer.VISIT_STATUS_FINISHED, visitStatus.getSelectedItem().toString(), feedbackNote.getText().toString());
                    confirmDialog.dismiss();
                    alertDialog.dismiss();
                }
            }
        });

        confirmDialog.show();

    }
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actionbar_button, menu);
        return super.onCreateOptionsMenu(menu);
//        boolean menuCreated = super.onCreateOptionsMenu(menu);
//        menuItemLogout.setVisible(false);
//        menuItemVisitSummary.setVisible(false);
//        menuItemVisitDetail.setVisible(true);
//        return menuCreated;
    }

    private void findViewsById() {
        containerVisitProgressItemStartedAt = (LinearLayout) findViewById(R.id.containerVisitProgressItemStartedAt);
        containerVisitProgressItemStartedFrom = (LinearLayout) findViewById(R.id.containerVisitProgressItemStartedFrom);
        containerVisitProgressItemStartAfterAnotherVisit = (LinearLayout) findViewById(R.id.containerVisitProgressItemStartAfterAnotherVisit);
        containerVisitProgressItemReachedCustomer = (LinearLayout) findViewById(R.id.containerVisitProgressItemReachedCustomer);
        containerVisitProgressItemFinishedVisit = (LinearLayout) findViewById(R.id.containerVisitProgressItemFinishedVisit);
        containerVisitProgressItemCancelledAt = (LinearLayout) findViewById(R.id.containerVisitProgressItemCancelledAt);
        containerVisitProgressItemCancelledReason = (LinearLayout) findViewById(R.id.containerVisitProgressItemCancelledReason);
        containerVisitProgressItemRescheduledAt = (LinearLayout) findViewById(R.id.containerVisitProgressItemRescheduledAt);
        containerVisitProgressItemRescheduledReason = (LinearLayout) findViewById(R.id.containerVisitProgressItemRescheduledReason);
        containerVisitProgressItemRescheduledDate = (LinearLayout) findViewById(R.id.containerVisitProgressItemRescheduledDate);
        containerVisitProgressItemFinishedVisitFeedbackNote = (LinearLayout) findViewById(R.id.containerVisitProgressItemFinishedVisitFeedbackNote);
        containerVisitProgressItemFinishedVisitStatus = (LinearLayout) findViewById(R.id.containerVisitProgressItemFinishedVisitStatus);
        containerVisitProgressItemVisitingDate = (LinearLayout) findViewById(R.id.containerVisitProgressItemVisitingDate);
        containerVisitProgressItemVisitStatusTitle = (LinearLayout) findViewById(R.id.containerVisitProgressItemVisitStatusTitle);
        containerVisitProgressItemAdjournedTime = (LinearLayout) findViewById(R.id.containerVisitProgressItemAdjournedTime);
        containerVisitProgressItemAdjournedReason = (LinearLayout) findViewById(R.id.containerVisitProgressItemAdjournedReason);
        containerVisitProgressItemAdjournInstructionFrom = (LinearLayout) findViewById(R.id.containerVisitProgressItemAdjournInstructionFrom);
        containerVisitProgressItemResumeVisit = (LinearLayout) findViewById(R.id.containerVisitProgressItemResumeVisit);
        containerVisitProgressItemAdjournResumeDate = (LinearLayout) findViewById(R.id.containerVisitProgressItemAdjournResumeDate);
        containerVisitProgressItemAdjournResumeTime = (LinearLayout) findViewById(R.id.containerVisitProgressItemAdjournResumeTime);
        panelVisitProgressBottom = (LinearLayout) findViewById(R.id.panelVisitProgressBottom);
        panelVisitProgressBottom0 = (LinearLayout) findViewById(R.id.panelVisitProgressBottom0);


        txtViewVisiteeCompanyName = (TextView) findViewById(R.id.txtViewVisiteeCompanyName);
        txtViewVisiteeCompanyAddress = (TextView) findViewById(R.id.txtViewVisiteeCompanyAddress);

        txtViewVisitProgressItemStartingTime = (TextView) findViewById(R.id.txtViewVisitProgressItemStartingTime);
        txtViewVisitProgressItemStartedFromText = (TextView) findViewById(R.id.txtViewVisitProgressItemStartedFromText);
        txtViewReachedAtCustomerTime = (TextView) findViewById(R.id.txtViewReachedAtCustomerTime);
        txtViewVisitFinishedTime = (TextView) findViewById(R.id.txtViewVisitFinishedTime);
        txtViewVisitCancelledTime = (TextView) findViewById(R.id.txtViewVisitCancelledTime);
        txtViewVisitCancelledReason = (TextView) findViewById(R.id.txtViewVisitCancelledReason);
        txtViewVisitRescheduledTime = (TextView) findViewById(R.id.txtViewVisitRescheduledTime);
        txtViewVisitRescheduledReason = (TextView) findViewById(R.id.txtViewVisitRescheduledReason);
        txtViewVisitRescheduledVisitingDate = (TextView) findViewById(R.id.txtViewVisitRescheduledVisitingDate);
        txtViewVisitFinishedFeedbackNote = (TextView) findViewById(R.id.txtViewVisitFinishedFeedbackNote);
        txtViewVisitFinishedStatus = (TextView) findViewById(R.id.txtViewVisitFinishedStatus);
        txtViewVisitProgressItemVisitingDate = (TextView) findViewById(R.id.txtViewVisitProgressItemVisitingDate);
        txtViewVisitProgressItemVisitStatusTitle = (TextView) findViewById(R.id.txtViewVisitProgressItemVisitStatusTitle);
        txtViewAdjournedAt = (TextView) findViewById(R.id.txtViewAdjournedAt);
        txtViewAdjournReason = (TextView) findViewById(R.id.txtViewAdjournReason);
        txtViewAdjournInstructionBy = (TextView) findViewById(R.id.txtViewAdjournInstructionBy);
        txtViewAdjournResumeDate = (TextView) findViewById(R.id.txtViewAdjournResumeDate);
        txtViewAdjournResumeTime = (TextView) findViewById(R.id.txtViewAdjournResumeTime);

        btnReachedCustomer = (Button) findViewById(R.id.btnReachedCustomer);
        btnUpdateVisitStatus = (Button) findViewById(R.id.btnUpdateVisitStatus);

    }
    @Override
    protected void onResume() {
        super.onResume();
        //do something
        if(visitReport == null){
            Realm realm = Realm.getDefaultInstance();
            visitReport = realm.where(VisitReport.class)
                    .equalTo("id", visit.getId())
                    .findFirst();
        }
        if(visitReport == null){
            getVisitReport(visit.getId());
        }else {
            setVisitProgressMilestones();
        }
    }
    public void getVisitReport(String visitId){
        Resources res = getResources();
        ApiManager apiManager = ApiManager.getInstance();
        final Call<VisitReport> thisVisitReport = apiManager.getVisitReport(loginUser.getToken(), loginUser.getUsername(), loginUser.getUser().getEmpId(), MyApplication.getAppUniqueID(getApplicationContext()), visitId);
        thisVisitReport.enqueue(new Callback<VisitReport>() {
            @Override
            public void onResponse(Call<VisitReport> call, Response<VisitReport> response) {
                visitReport = response.body();
                setVisitProgressMilestones();
            }

            @Override
            public void onFailure(Call<VisitReport> call, Throwable t) {
                Log.d("Failed:", t.getMessage());
            }
        });
    }
    private void showVisitCancelInfo(){
        containerVisitProgressItemCancelledAt.setVisibility(View.VISIBLE);
        txtViewVisitCancelledTime.setText(visitReport.getCancelTime());
        containerVisitProgressItemCancelledReason.setVisibility(View.VISIBLE);
        txtViewVisitCancelledReason.setText(visitReport.getCancelReason());
        containerVisitProgressItemVisitStatusTitle.setVisibility(View.VISIBLE);
        txtViewVisitProgressItemVisitStatusTitle.setText("Visit Cancelled");

    }
    private void showVisitRescheduleInfo(){
        containerVisitProgressItemRescheduledAt.setVisibility(View.VISIBLE);
        txtViewVisitRescheduledTime.setText(visitReport.getRescheduleTime());
        containerVisitProgressItemRescheduledReason.setVisibility(View.VISIBLE);
        txtViewVisitRescheduledReason.setText(visitReport.getRescheduleReason());
        containerVisitProgressItemRescheduledDate.setVisibility(View.VISIBLE);
        txtViewVisitRescheduledVisitingDate.setText(visitReport.getRescheduledVisitingDate());
        containerVisitProgressItemVisitStatusTitle.setVisibility(View.VISIBLE);
        txtViewVisitProgressItemVisitStatusTitle.setText("Visit Rescheduled");
    }
    private void showVisitFinishInfo(){
        containerVisitProgressItemFinishedVisit.setVisibility(View.VISIBLE);
        txtViewVisitFinishedTime.setText(visitReport.getEndTime());
        containerVisitProgressItemFinishedVisitFeedbackNote.setVisibility(View.VISIBLE);
        txtViewVisitFinishedFeedbackNote.setText(visitReport.getFeedbackNote());
        containerVisitProgressItemFinishedVisitStatus.setVisibility(View.VISIBLE);
        txtViewVisitFinishedStatus.setText(visitReport.getFinishedStatus());
        containerVisitProgressItemVisitStatusTitle.setVisibility(View.VISIBLE);
        txtViewVisitProgressItemVisitStatusTitle.setText("Visit Completed");
    }
    private void showVisitAdjournInfo(){
        containerVisitProgressItemVisitStatusTitle.setVisibility(View.VISIBLE);
        txtViewVisitProgressItemVisitStatusTitle.setText("Visit Adjourned");
        if(MyApplication.isRunningVisit == null)
            containerVisitProgressItemResumeVisit.setVisibility(View.VISIBLE);
    }
    public void setVisitProgressMilestones(){

        containerVisitProgressItemVisitingDate.setVisibility(View.VISIBLE);
        txtViewVisitProgressItemVisitingDate.setText(visitReport.getVisitDate());
        Resources res = getResources();
        if(visitReport.getVisitStatus() == res.getInteger(R.integer.VISIT_STATUS_CANCELED_BEFORE_START)) {
            showVisitCancelInfo();
        }
        else if(visitReport.getVisitStatus() == res.getInteger(R.integer.VISIT_STATUS_RESCHEDULED_BEFORE_START)){//rescheduled
            showVisitRescheduleInfo();
        }else {
            containerVisitProgressItemStartedAt.setVisibility(View.VISIBLE);
            txtViewVisitProgressItemStartingTime.setText(visitReport.getStartTime());

            containerVisitProgressItemStartedFrom.setVisibility(View.VISIBLE);
            txtViewVisitProgressItemStartedFromText.setText(visitReport.getStartedFrom());

            if(!(visitReport.getAfterAnotherVisit() == null || visitReport.getAfterAnotherVisit().isEmpty())){
                containerVisitProgressItemStartAfterAnotherVisit.setVisibility(View.VISIBLE);
            }
            if(visitReport.getInCustomerTime() != null){
                containerVisitProgressItemReachedCustomer.setVisibility(View.VISIBLE);
            }

            if(visitReport.getAdjournedTime() != null){//Adjourned
                containerVisitProgressItemAdjournedTime.setVisibility(View.VISIBLE);
                txtViewAdjournedAt.setText(visitReport.getAdjournedTime());
                containerVisitProgressItemAdjournedReason.setVisibility(View.VISIBLE);
                txtViewAdjournReason.setText(visitReport.getAdjournReason());
                containerVisitProgressItemAdjournInstructionFrom.setVisibility(View.VISIBLE);
                txtViewAdjournInstructionBy.setText(visitReport.getAdjournInstructionFrom());
//                showVisitAdjournInfo();
            }
            if(visitReport.getAdjournResumeTime() != null){
                containerVisitProgressItemAdjournResumeDate.setVisibility(View.VISIBLE);
                txtViewAdjournResumeDate.setText(visitReport.getAdjournResumeDate());
                containerVisitProgressItemAdjournResumeTime.setVisibility(View.VISIBLE);
                txtViewAdjournResumeTime.setText(visitReport.getAdjournReason());
            }

            if(visitReport.getCancelTime() != null){
                showVisitCancelInfo();
            }
            else if(visitReport.getRescheduleTime() != null){
                showVisitRescheduleInfo();
            }
            else if(visitReport.getAdjournedTime() != null && visitReport.getAdjournResumeTime() == null){
                manipulateVisitAdjournUIComponents();
            }
            else if(visitReport.getFeedbackNote() != null){//Finished
                showVisitFinishInfo();
            }else if(!isRecentlyVisited){
                if(visitReport.getInCustomerTime() == null){
                    btnReachedCustomer.setVisibility(View.VISIBLE);
                    panelVisitProgressBottom0.setVisibility(View.VISIBLE);
                }else {
                    btnUpdateVisitStatus.setVisibility(View.VISIBLE);
                }
//                containerVisitProgressItemResumeVisit.setVisibility(View.GONE);
                panelVisitProgressBottom.setVisibility(View.VISIBLE);
            }
        }
    }
    // handle menu item activities
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int optionItem = item.getItemId();
        switch (optionItem) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.action_button_home:
                backToListPage();
                break;

            default:
                break;
        }

        return true;
    }
    private void setDatePickerField(final EditText editTextForDatePicker) {

//        editTextForDatePicker.setInputType(InputType.TYPE_NULL);
        Calendar newCalendar = Calendar.getInstance();
        datePickerDialog = new DatePickerDialog(this, DatePickerDialog.THEME_DEVICE_DEFAULT_DARK, new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
//                showToast("Set FROM Date");
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                editTextForDatePicker.setText(dateFormatter.format(newDate.getTime()));
            }

        },newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));

        editTextForDatePicker.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
//                showToast("FROM Date Picker");
                if(hasFocus)
                    datePickerDialog.show();
            }
        });
    }
    //====================---------------------------------------==========================
    @Override
    public void onBackPressed() {
//        showToast(this.getClass().getSimpleName().toString());
        backToDetailPage();
//        super.onBackPressed();
    }

    private void backToDetailPage(){
        Intent ret = new Intent();
        ret.putExtra("com.ideaxen.client.visitapp.model.LAST_ACTION", VisitProgressActivity.LAST_ACTION_TAKEN);
        ret.putExtra("com.ideaxen.client.visitapp.model.VISIT_REPORT", visitReport);
        ret.putExtra("com.ideaxen.client.visitapp.bool.BACK_TO_LIST", false);
        setResult(RESULT_OK,ret);
        VisitProgressActivity.LAST_ACTION_TAKEN = VisitDetailActivity.DEFAULT_LAST_ACTION_TAKEN;
        finish();
    }

    private void backToListPage(){
        Intent ret = new Intent();
        ret.putExtra("com.ideaxen.client.visitapp.model.LAST_ACTION", VisitProgressActivity.LAST_ACTION_TAKEN);
        ret.putExtra("com.ideaxen.client.visitapp.model.VISIT_REPORT", visitReport);
        ret.putExtra("com.ideaxen.client.visitapp.bool.BACK_TO_LIST", true);
        setResult(RESULT_OK,ret);
        VisitProgressActivity.LAST_ACTION_TAKEN = VisitDetailActivity.DEFAULT_LAST_ACTION_TAKEN;
        finish();
    }

    public void openDialog(int dialog_layout){
        switch (dialog_layout){
            case R.layout.visit_cancelled:
                VisitProgressActivity.CURR_ACTION = R.integer.ACTION_VISIT_CANCEL;
                break;
            case R.layout.visit_reschedule:
                VisitProgressActivity.CURR_ACTION = R.integer.ACTION_VISIT_RESCHEDULE;
                break;
            case R.layout.visit_adjourn:
                VisitProgressActivity.CURR_ACTION = R.integer.ACTION_VISIT_ADJOURN;
        }
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        // Get the layout inflater
        LayoutInflater inflater = this.getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        dialogView = inflater.inflate(dialog_layout, null);
        alertDialogBuilder.setView(dialogView);
        if(dialog_layout == R.layout.visit_status){
            Spinner spinner = dialogView.findViewById(R.id.spinnerVisitStatus);
            MyApplication.bindLovToSpinner(thisActivity, spinner, "VISIT_COMPLETION_STATUS", "Select visit status");
        }
        else if(dialog_layout == R.layout.visit_cancelled){
            Spinner spinner = dialogView.findViewById(R.id.spinnerVisitCancelPurpose);
            MyApplication.bindLovToSpinner(thisActivity, spinner, "VISIT_CANCEL_REASON", "Select reason");
        }
        else if(dialog_layout == R.layout.visit_reschedule){
            EditText datePickerField = dialogView.findViewById(R.id.edtTextNewVisitingDate);
            setDatePickerField(datePickerField);

            Spinner spinner = dialogView.findViewById(R.id.spinnerVisitReschedulePurpose);
            MyApplication.bindLovToSpinner(thisActivity, spinner, "VISIT_RESCHEDULE_REASON", "Select reason");
        }
        else if(dialog_layout == R.layout.visit_adjourn){
            Spinner spinner1 = dialogView.findViewById(R.id.spinnerWithInstructionFrom);
            MyApplication.bindLovToSpinner2(thisActivity, spinner1, "MY_LEADER", "With Instruction From");

            Spinner spinner2 = dialogView.findViewById(R.id.spinnerAdjournVisitPurpose);
            MyApplication.bindLovToSpinner(thisActivity, spinner2, "VISIT_ADJOURN_REASON", "Select reason");
        }

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
    public void openAdjournVisitDialog(View view) {
        openDialog(R.layout.visit_adjourn);
    }
    public void openVisitStatusDialog(View view) {
        openDialog(R.layout.visit_status);
    }

    public void cancelVisitDialog(View v){
        if(alertDialog.isShowing()){
            alertDialog.dismiss();
        }

//        Toast.makeText(thisActivity,"You have disagreed to proceed",Toast.LENGTH_SHORT).show();
    }

    public void proceedVisitCancel(View v, String reason){
        //CANCELLATION PROCESS HERE
        alertDialog.dismiss();
        canceledVisit(reason);
//        onBackPressed();
    }
    public void proceedVisitRescedule(View v, String reason, String newDate){
        //RESCHEDULE PROCESS HERE
        alertDialog.dismiss();
        rescheduledVisit(reason, newDate);
//        onBackPressed();
    }
    public void proceedVisitAdjourn(View v, String reason, String instructor, String instructorId){
        //RESCHEDULE PROCESS HERE
        alertDialog.dismiss();
        updateOnServer(R.integer.VISIT_STATUS_ADJOURNED, reason, instructor, instructorId);
    }
    public void proceedResumeVisit(View v){
        updateOnServer(R.integer.VISIT_STATUS_RESUME);
    }

    public void rescheduledVisit(String reason, String newVisitngDate){
        updateOnServer(R.integer.VISIT_STATUS_RESCHEDULED, reason, newVisitngDate);
    }
    public void canceledVisit(String reason){
        updateOnServer(R.integer.VISIT_STATUS_CANCELED, reason);
    }
    private void manipulateVisitFinishUIComponents(){
        panelVisitProgressBottom.setVisibility(View.GONE);
        panelVisitProgressBottom0.setVisibility(View.GONE);
        btnUpdateVisitStatus.setVisibility(View.GONE);
        btnReachedCustomer.setVisibility(View.GONE);
        containerVisitProgressItemResumeVisit.setVisibility(View.GONE);
        showVisitFinishInfo();
    }
    private void manipulateVisitAdjournUIComponents(){
        btnReachedCustomer.setVisibility(View.GONE);
        btnUpdateVisitStatus.setVisibility(View.GONE);
        panelVisitProgressBottom.setVisibility(View.GONE);
        panelVisitProgressBottom0.setVisibility(View.GONE);
        showVisitAdjournInfo();
    }
    private void manipulateVisitResumeUIComponents(){
        containerVisitProgressItemAdjournResumeDate.setVisibility(View.VISIBLE);
        txtViewAdjournResumeDate.setText(visitReport.getAdjournResumeDate());
        containerVisitProgressItemAdjournResumeTime.setVisibility(View.VISIBLE);
        txtViewAdjournResumeTime.setText(visitReport.getAdjournReason());

        panelVisitProgressBottom.setVisibility(View.VISIBLE);
        panelVisitProgressBottom0.setVisibility(View.GONE);
        btnUpdateVisitStatus.setVisibility(View.GONE);
        btnReachedCustomer.setVisibility(View.VISIBLE);
        containerVisitProgressItemVisitStatusTitle.setVisibility(View.GONE);
        containerVisitProgressItemResumeVisit.setVisibility(View.GONE);
//        showVisitAdjournInfo();
    }
    private void manipulateVisitRescheduleUIComponents(){
        panelVisitProgressBottom.setVisibility(View.GONE);
        panelVisitProgressBottom0.setVisibility(View.GONE);
        btnUpdateVisitStatus.setVisibility(View.GONE);
        btnReachedCustomer.setVisibility(View.GONE);
        containerVisitProgressItemResumeVisit.setVisibility(View.GONE);
        showVisitRescheduleInfo();
    }
    private void manipulateVisitCancelUIComponents(){
        panelVisitProgressBottom.setVisibility(View.GONE);
        panelVisitProgressBottom0.setVisibility(View.GONE);
        btnUpdateVisitStatus.setVisibility(View.GONE);
        btnReachedCustomer.setVisibility(View.GONE);
        containerVisitProgressItemResumeVisit.setVisibility(View.GONE);
        showVisitCancelInfo();
    }

    public void updateVisitStatus(int visitStatus, List<String> serverResponse) throws UnsupportedEncodingException {
        int visitStatusValue = Integer.parseInt(serverResponse.get(0));
        switch(visitStatus){
            case R.integer.VISIT_STATUS_REACHED_CUSTOMER:
                visitReport.setInCustomerTime(serverResponse.get(2));
                btnReachedCustomer.setVisibility(View.GONE);
                btnUpdateVisitStatus.setVisibility(View.VISIBLE);
                containerVisitProgressItemReachedCustomer.setVisibility(View.VISIBLE);
                txtViewReachedAtCustomerTime.setText(visitReport.getInCustomerTime());
                break;
            case R.integer.VISIT_STATUS_FINISHED:
                visitReport.setEndTime(serverResponse.get(2));
                if(visitReport.getInCustomerTime() != null){
                    visitReport.setOutCustomerTime(visitReport.getEndTime());
                }
                visitReport.setFinishedStatus(serverResponse.get(3));
                visitReport.setFeedbackNote(serverResponse.get(4));
                MyApplication.isRunningVisit = null;
                manipulateVisitFinishUIComponents();
                break;
            case R.integer.VISIT_STATUS_CANCELED:
                visitReport.setCancelTime(serverResponse.get(2));
                visitReport.setCancelReason(serverResponse.get(3));
                visitReport.setEndTime(visitReport.getCancelTime());
                MyApplication.isRunningVisit = null;
                manipulateVisitCancelUIComponents();
                break;
            case R.integer.VISIT_STATUS_RESCHEDULED:
                visitReport.setEndTime(serverResponse.get(2));
                visitReport.setRescheduleTime(visitReport.getEndTime());
                visitReport.setRescheduleReason(serverResponse.get(3));
                visitReport.setRescheduledVisitingDate(serverResponse.get(4));
                MyApplication.isRunningVisit = null;
                manipulateVisitRescheduleUIComponents();
                break;
            case R.integer.VISIT_STATUS_ADJOURNED:
                visitReport.setAdjournedTime(serverResponse.get(2));
                visitReport.setAdjournReason(serverResponse.get(3));
                visitReport.setAdjournInstructionFrom(serverResponse.get(4));
                MyApplication.isRunningVisit = null;
                containerVisitProgressItemAdjournedTime.setVisibility(View.VISIBLE);
                txtViewAdjournedAt.setText(visitReport.getAdjournedTime());
                containerVisitProgressItemAdjournedReason.setVisibility(View.VISIBLE);
                txtViewAdjournReason.setText(visitReport.getAdjournReason());
                containerVisitProgressItemAdjournInstructionFrom.setVisibility(View.VISIBLE);
                txtViewAdjournInstructionBy.setText(visitReport.getAdjournInstructionFrom());

                manipulateVisitAdjournUIComponents();
                break;
            case R.integer.VISIT_STATUS_RESUME:
                visitReport.setAdjournResumeDate(serverResponse.get(1));
                visitReport.setAdjournResumeTime(serverResponse.get(2));
                MyApplication.isRunningVisit = visitReport.getId();
                manipulateVisitResumeUIComponents();
                break;
        }
        VisitProgressActivity.LAST_ACTION_TAKEN = visitStatus;

        visitReport.setVisitStatus(visitStatusValue);
        Realm realm = Realm.getDefaultInstance();
        // Copy elements from Retrofit to Realm to persist them.
        realm.beginTransaction();
        realm.insertOrUpdate(visitReport);
        realm.commitTransaction();

//        visitReport = realm.where(VisitReport.class)
//                .equalTo("id", visit.getId())
//                .findFirst();
    }

    //    private String URLDecode(String string) throws UnsupportedEncodingException {
//        return URLDecoder.decode(string, "UTF-8");
//    }
    public boolean updateOnServer(int visitStatus){
        return updateOnServer(visitStatus, "", "", "");
    }
    public boolean updateOnServer(int visitStatus, String msg1){
        return updateOnServer(visitStatus, msg1, "", "");
    }
    public boolean updateOnServer(int visitStatus, String msg1, String msg2){
        return updateOnServer(visitStatus, msg1, msg2, "");
    }
    public boolean updateOnServer(int visitStatus, String msg1, String msg2, String msg3){
        return updateOnServer(visitStatus, msg1, msg2, msg3, "");
    }
    public boolean updateOnServer(final int visitStatus, String msg1, String msg2, String msg3, String msg4){
        Resources res = getResources();
        ApiManager apiManager = ApiManager.getInstance();
        Call<List<String>> visits = apiManager.updateVisitReport(loginUser.getToken(), loginUser.getUsername(), loginUser.getUser().getEmpId(), MyApplication.getAppUniqueID(getApplicationContext()), visit.getId(), res.getInteger(visitStatus)+"", msg1, msg2, msg3, msg4);
        visits.enqueue(new Callback<List<String>>() {
            @Override
            public void onResponse(Call<List<String>> call, Response<List<String>> response) {
                List<String> responseData = response.body();
                try {
                    updateVisitStatus(visitStatus, responseData);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<List<String>> call, Throwable t) {
                Log.d("Failed:", t.getMessage());
            }
        });
        return true;
    }
}
