package com.ideaxen.hr.ideasms;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Bundle;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ideaxen.hr.ideasms.api.ApiManager;
import com.ideaxen.hr.ideasms.model.ApprovalDetailItem;
import com.ideaxen.hr.ideasms.model.Login;
import com.ideaxen.hr.ideasms.model.Visit;
import com.ideaxen.hr.ideasms.model.VisitReport;
import com.ideaxen.hr.ideasms.model.VisitUpdateData;
import com.ideaxen.hr.ideasms.util.ApprovalDetailItemAdapter;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmResults;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ApprovalDetailActivity extends AppCompatActivity implements Callback<List<ApprovalDetailItem>>, SwipeRefreshLayout.OnRefreshListener{

    private static final String TAG = "ApprovalDetailActivity";

    protected View dialogView;
    protected AlertDialog alertDialog;
    protected AlertDialog confirmDialog;

    protected List<ApprovalDetailItem> visitDetails;
    protected VisitReport visitReport;
    protected Login loginUser;
    protected Visit approval;

    protected Context thisContext;
    protected Activity thisActivity;

    protected Button btnShowVisitProgressApprover;
    protected Button btnDeclineVisit;
    protected Button btnApproveVisit;

    protected View mainView;

    protected TextView txtViewVisiteeCompanyName;
    protected TextView txtViewVisiteeCompanyAddress;
    protected View rateViews[] = new View[5];
    protected View rateViewsConfirm[] = new View[5];
    protected int newPriority = 0;

    protected int visitType;//visited=>1/due=>-1/normal=>0

    protected static boolean VISIT_STARTED  = false;
    protected static boolean VISIT_STARTED_FROM_OFFICE  = true;
    protected static boolean VISIT_STARTED_AFTER_ANOTHER_VISIT  = false;
    protected static int VISIT_STATUS  = R.integer.VISIT_STATUS_NOT_STARTED;
    protected static int CURR_ACTION  = -10;
    protected static int LAST_ACTION_TAKEN;

    protected ListView visitDetailList;

    protected LinearLayout panelVisitDetailBottom;
    protected LinearLayout panelVisitDetailBottom0;

    SwipeRefreshLayout mSwipeRefreshLayout;

    private DatePickerDialog datePickerDialog;
    private SimpleDateFormat dateFormatter;

    private int positionInList;
    private boolean isRecentlyVisited;
    private boolean isAdjournedVisit;
    private boolean directReport;
    protected static final int DEFAULT_LAST_ACTION_TAKEN  = -10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = this.getLayoutInflater();
        mainView = inflater.inflate(R.layout.activity_approval_detail, null);
        setContentView(mainView);

        findViewsById();

        //Add back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Get the application context
        thisContext = getApplicationContext();

        // Get the activity
        thisActivity = ApprovalDetailActivity.this;

        LAST_ACTION_TAKEN = DEFAULT_LAST_ACTION_TAKEN;

        dateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

        Intent in = getIntent();
        positionInList = getIntent().getIntExtra("com.ideaxen.client.visitapp.int.POSITION_IN_LIST", -1);
        approval = getIntent().getParcelableExtra("com.ideaxen.client.visitapp.model.APPROVAL");
        loginUser = getIntent().getParcelableExtra("com.ideaxen.client.visitapp.model.LOGIN");
        isRecentlyVisited = getIntent().getBooleanExtra("com.ideaxen.client.visitapp.bool.RECENTLY_VISITED", false);
        isAdjournedVisit = getIntent().getBooleanExtra("com.ideaxen.client.visitapp.bool.ADJOURNED_VISIT", false);
        directReport = getIntent().getBooleanExtra("com.ideaxen.client.visitapp.bool.DIRECT_REPORT", false);
        if(approval.getId().equals(MyApplication.isRunningVisit) && (visitReport == null || visitReport.getVisitStatus()==0)){
            getVisitReport(approval.getId());
        }
        mSwipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        String FILTER_TYPE = getIntent().getStringExtra("FILTER_TYPE");

        if(FILTER_TYPE.equals("7") || FILTER_TYPE.equals("8") || FILTER_TYPE.equals("9")){
            manipulateUIComponents(0);
        }

        mSwipeRefreshLayout.setOnRefreshListener(this);

        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                getVisitDetail(false);
            }
        });

        btnDeclineVisit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAppDecConfirmDialog(0);
            }
        });
        btnApproveVisit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAppDecConfirmDialog(1);
            }
        });

    }

    public void showToast(String message){
        Toast.makeText(thisActivity, message,Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //do something
        if(visitReport == null){
            Realm realm = Realm.getDefaultInstance();
            visitReport = realm.where(VisitReport.class)
                    .equalTo("id", approval.getId())
                    .findFirst();
        }
        if(approval.getId().equals(MyApplication.isRunningVisit) && (visitReport == null || visitReport.getVisitStatus()==0)){
            getVisitReport(approval.getId());
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
                if(directReport){
                    directReport = false;
                    showVisitProgressApprover(null);
                }
            }

            @Override
            public void onFailure(Call<VisitReport> call, Throwable t) {
                Log.d("Failed:", t.getMessage());
            }
        });
    }

    public void getVisitDetail(boolean force_load_from_server){
//        Toast.makeText(getApplicationContext(), visitFilterPref+"", Toast.LENGTH_SHORT);
        mSwipeRefreshLayout.setRefreshing(true);
        Realm realm = Realm.getDefaultInstance();
        if(!force_load_from_server) {
            visitDetails = realm.where(ApprovalDetailItem.class)
                    .equalTo("id", approval.getId())
                    .findAll();
        }else{
            final RealmResults<ApprovalDetailItem> results = realm.where(ApprovalDetailItem.class)
                    .equalTo("id", approval.getId())
                    .findAll();
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    // Delete all matches
                    results.deleteAllFromRealm();
                }
            });
        }

        if(force_load_from_server || visitDetails.size()<=0) {//not available locally
            ApiManager apiManager = ApiManager.getInstance();
            apiManager.getApprovalDetail(loginUser.getToken(), loginUser.getUsername(), loginUser.getUser().getEmpId(), MyApplication.getAppUniqueID(getApplicationContext()), approval.getId(), this);
        }else{
            setDetailAdapter();
        }
    }

    private void findViewsById() {
        txtViewVisiteeCompanyName = findViewById(R.id.txtViewApproverCompanyName);
        txtViewVisiteeCompanyAddress = findViewById(R.id.txtViewApproverCompanyAddress);
        visitDetailList = findViewById(R.id.visitDetailList);
        btnDeclineVisit = findViewById(R.id.btnDeclineVisit);
        btnApproveVisit = findViewById(R.id.btnApproveVisit);
        btnShowVisitProgressApprover = findViewById(R.id.btnShowVisitProgressApprover);

        rateViews[0] = findViewById(R.id.rate1);
        rateViews[1] = findViewById(R.id.rate2);
        rateViews[2] = findViewById(R.id.rate3);
        rateViews[3] = findViewById(R.id.rate4);
        rateViews[4] = findViewById(R.id.rate5);
    }

    // handle menu item activities
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int optionItem = item.getItemId();
        switch (optionItem) {
            case android.R.id.home:
                onBackPressed();
                break;
            default:
                break;
        }

        return true;
    }

    public void openAppDecConfirmDialog(final int flag){
        String alertTitle = "";
        if(flag==1){
            alertTitle = "Approve this visit?";
        }
        else if(flag==0){
            alertTitle = "Decline this visit?";
        }
        AlertDialog.Builder confirmDialogBuilder = new AlertDialog.Builder(this)
                .setTitle(alertTitle);
        LayoutInflater inflater = this.getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        final View confirmDialogView = inflater.inflate(R.layout.confirm_dialog, null);
        confirmDialogBuilder.setView(confirmDialogView);
        LinearLayout layout_rates = confirmDialogView.findViewById(R.id.layout_rates);
        LinearLayout layout_text = confirmDialogView.findViewById(R.id.layout_text);
        if(flag == 0){
            layout_rates.setVisibility(View.GONE);
            layout_text.setVisibility(View.GONE);
        }
        else {
            layout_rates.setVisibility(View.VISIBLE);
            layout_text.setVisibility(View.VISIBLE);
            rateViewsConfirm[0] = confirmDialogView.findViewById(R.id.confirmRate1);
            rateViewsConfirm[1] = confirmDialogView.findViewById(R.id.confirmRate2);
            rateViewsConfirm[2] = confirmDialogView.findViewById(R.id.confirmRate3);
            rateViewsConfirm[3] = confirmDialogView.findViewById(R.id.confirmRate4);
            rateViewsConfirm[4] = confirmDialogView.findViewById(R.id.confirmRate5);
            TextView tv_priority = confirmDialogView.findViewById(R.id.tv_priority);

            int priority = Integer.valueOf(approval.getVisitPriority());
            if(priority > 0){
                for(int i = 0; i < priority; i++){
                    rateViewsConfirm[i].setBackground(ContextCompat.getDrawable(thisContext, R.drawable.rating_circle_full));
                    if(priority == 1){
                        tv_priority.setText("Very Low");
                    }
                    else if(priority == 2){
                        tv_priority.setText("Low");
                    }
                    else if(priority == 3){
                        tv_priority.setText("Normal");
                    }
                    else if(priority == 4){
                        tv_priority.setText("High");
                    }
                    else if(priority == 5){
                        tv_priority.setText("Very high");
                    }
                }
            }

            rateViewsConfirm[0].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    changePriority(1, confirmDialogView);
                    newPriority = 1;
                }
            });
            rateViewsConfirm[1].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    changePriority(2, confirmDialogView);
                    newPriority = 2;
                }
            });
            rateViewsConfirm[2].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    changePriority(3, confirmDialogView);
                    newPriority = 3;
                }
            });
            rateViewsConfirm[3].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    changePriority(4, confirmDialogView);
                    newPriority = 4;
                }
            });
            rateViewsConfirm[4].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    changePriority(5, confirmDialogView);
                    newPriority = 5;
                }
            });
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
                if(flag==1){
                    ApiManager apiManager = ApiManager.getInstance();
                    Call<VisitUpdateData> visits = apiManager.getApprovalUpdate(loginUser.getToken(), loginUser.getUsername(), loginUser.getUser().getEmpId(), MyApplication.getAppUniqueID(getApplicationContext()), approval.getId(), String.valueOf(newPriority));
                    visits.enqueue(new Callback<VisitUpdateData>() {
                        @Override
                        public void onResponse(Call<VisitUpdateData> call, Response<VisitUpdateData> response) {
                            VisitUpdateData visitUpdateData = response.body();
                            if (visitUpdateData != null) {
                                showToast(visitUpdateData.getMessage());
                            }
                            manipulateUIComponents(0);
                        }

                        @Override
                        public void onFailure(Call<VisitUpdateData> call, Throwable t) {
                            Log.d("Failed", t.getMessage());
                        }
                    });
                }
                else if(flag==0){
                    ApiManager apiManager = ApiManager.getInstance();
                    Call<VisitUpdateData> visits = apiManager.getDeclineUpdate(loginUser.getToken(), loginUser.getUsername(), loginUser.getUser().getEmpId(), MyApplication.getAppUniqueID(getApplicationContext()), approval.getId());
                    visits.enqueue(new Callback<VisitUpdateData>() {
                        @Override
                        public void onResponse(Call<VisitUpdateData> call, Response<VisitUpdateData> response) {
                            VisitUpdateData visitUpdateData = response.body();
                            if (visitUpdateData != null) {
                                showToast(visitUpdateData.getMessage());
                            }
                            manipulateUIComponents(0);
                        }

                        @Override
                        public void onFailure(Call<VisitUpdateData> call, Throwable t) {
                            Log.d("Failed", t.getMessage());
                        }
                    });
                }
            }
        });

        confirmDialog.show();
    }

    private void changePriority(int priority, View confirmDialogView){
//        Log.d(TAG, "changePriority: " + priority);
        rateViewsConfirm[0] = confirmDialogView.findViewById(R.id.confirmRate1);
        rateViewsConfirm[1] = confirmDialogView.findViewById(R.id.confirmRate2);
        rateViewsConfirm[2] = confirmDialogView.findViewById(R.id.confirmRate3);
        rateViewsConfirm[3] = confirmDialogView.findViewById(R.id.confirmRate4);
        rateViewsConfirm[4] = confirmDialogView.findViewById(R.id.confirmRate5);
        TextView tv_priority = confirmDialogView.findViewById(R.id.tv_priority);
        if(priority == 1){
            tv_priority.setText("Very Low");
        }
        else if(priority == 2){
            tv_priority.setText("Low");
        }
        else if(priority == 3){
            tv_priority.setText("Normal");
        }
        else if(priority == 4){
            tv_priority.setText("High");
        }
        else if(priority == 5){
            tv_priority.setText("Very High");
        }

        for(int i = 0; i < priority; i++){
            rateViewsConfirm[i].setBackground(ContextCompat.getDrawable(thisContext, R.drawable.rating_circle_full));
        }
        for(int i = priority; i < 5; i++){
            rateViewsConfirm[i].setBackground(ContextCompat.getDrawable(thisContext, R.drawable.rating_circle_empty));
        }
    }

    private void manipulateUIComponents(int t){
        if(t==0){
            btnApproveVisit.setVisibility(View.GONE);
            btnDeclineVisit.setVisibility(View.GONE);
            btnShowVisitProgressApprover.setVisibility(View.VISIBLE);
        }
        else {
            btnApproveVisit.setVisibility(View.VISIBLE);
            btnDeclineVisit.setVisibility(View.VISIBLE);
            btnShowVisitProgressApprover.setVisibility(View.GONE);
        }
    }

    public void showVisitProgressApprover(View view) {
        Intent intent = new Intent(thisActivity, VisitProgressActivity.class);
        intent.putExtra("com.ideaxen.client.visitapp.bool.RECENTLY_VISITED", isRecentlyVisited);
        intent.putExtra("com.ideaxen.client.visitapp.model.VISIT", approval);
        intent.putExtra("com.ideaxen.client.visitapp.model.LOGIN", loginUser);
        intent.putExtra("com.ideaxen.client.visitapp.model.VISIT_REPORT", visitReport);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == 1) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                visitReport = data.getParcelableExtra("com.ideaxen.client.visitapp.model.VISIT_REPORT");
                VisitDetailActivity.LAST_ACTION_TAKEN = data.getIntExtra("com.ideaxen.client.visitapp.model.LAST_ACTION",-10);
                boolean backToList = data.getBooleanExtra("com.ideaxen.client.visitapp.bool.BACK_TO_LIST", false);
                if(backToList)
                    backToListPage();
            }
        }
    }
    public void openVisitStartDialog(View view) {
        openDialog(R.layout.visit_from_out_of_office);
    }

    @Override
    public void onResponse(Call<List<ApprovalDetailItem>> call, Response<List<ApprovalDetailItem>> response) {
        if (response.body() != null) {
            Log.d(TAG, "Visit Detail Response" + response.body().toString());
        }
        visitDetails = response.body();

        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.insert(visitDetails);
        realm.commitTransaction();

        setDetailAdapter();

    }

    @Override
    public void onFailure(Call<List<ApprovalDetailItem>> call, Throwable t) {
        Log.d("Failed", "Request Failed: " + t.getMessage());
    }
    private void setDetailAdapter(){
        txtViewVisiteeCompanyName.setText(approval.getCustomer());
        txtViewVisiteeCompanyAddress.setText(approval.getAddress());
        int priority = Integer.valueOf(approval.getVisitPriority());
        if(priority > 0){
            for(int i = 0; i < priority; i++){
                rateViews[i].setBackground(ContextCompat.getDrawable(thisContext, R.drawable.rating_circle_full));
            }
        }

        ApprovalDetailItemAdapter approvalDetailItemAdapter = new ApprovalDetailItemAdapter(this, visitDetails);
        visitDetailList.setAdapter(approvalDetailItemAdapter);
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onRefresh() {
        getVisitDetail(true);
    }

    private void setDatePickerField(final EditText editTextForDatePicker) {

//        editTextForDatePicker.setInputType(InputType.TYPE_NULL);
        Calendar newCalendar = Calendar.getInstance();
        datePickerDialog = new DatePickerDialog(this, DatePickerDialog.THEME_DEVICE_DEFAULT_DARK, new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
//                ToastUtils.showShort("Set FROM Date");
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                editTextForDatePicker.setText(dateFormatter.format(newDate.getTime()));
            }

        },newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));

        editTextForDatePicker.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
//                ToastUtils.showShort("FROM Date Picker");
                if(hasFocus)
                    datePickerDialog.show();
            }
        });
    }
    //==================----------------------------=====================
    @Override
    public void onBackPressed() {
        backToListPage();
    }

    private void backToListPage(){
//        Intent ret = new Intent();
//        ret.putExtra("com.ideaxen.client.visitapp.model.LAST_ACTION", ApprovalDetailActivity.LAST_ACTION_TAKEN);
//        ret.putExtra("com.ideaxen.client.visitapp.int.POSITION_IN_LIST", positionInList);
//        setResult(RESULT_OK,ret);
//        LAST_ACTION_TAKEN = DEFAULT_LAST_ACTION_TAKEN;
        Intent intent = new Intent(this, MyApprovalActivity.class);
        startActivity(intent);
        finish();
    }

    public void openDialog(int dialog_layout){
        switch (dialog_layout){
            case R.layout.visit_cancelled:
                VisitDetailActivity.CURR_ACTION = R.integer.ACTION_VISIT_CANCEL;
                break;
            case R.layout.visit_reschedule:
                VisitDetailActivity.CURR_ACTION = R.integer.ACTION_VISIT_RESCHEDULE;
        }
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        // Get the layout inflater
        LayoutInflater inflater = this.getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        dialogView = inflater.inflate(dialog_layout, null);
        alertDialogBuilder.setView(dialogView);
        if(dialog_layout == R.layout.visit_reschedule){
            EditText datePickerField = dialogView.findViewById(R.id.edtTextNewVisitingDate);
            setDatePickerField(datePickerField);
        }
        if(dialog_layout == R.layout.visit_from_out_of_office){
            Spinner spinner = (Spinner) dialogView.findViewById(R.id.spinnerOutOfOfficePurpose);
            MyApplication.bindLovToSpinner(thisContext, spinner, "VISIT_FROM_OTHER_REASON", "Select reason");
        }
        else if(dialog_layout == R.layout.visit_cancelled){
            Spinner spinner = (Spinner) dialogView.findViewById(R.id.spinnerVisitCancelPurpose);
            MyApplication.bindLovToSpinner(thisContext, spinner, "VISIT_CANCEL_REASON", "Select reason");
        }
        else if(dialog_layout == R.layout.visit_reschedule){
            Spinner spinner = (Spinner) dialogView.findViewById(R.id.spinnerVisitReschedulePurpose);
            MyApplication.bindLovToSpinner(thisContext, spinner, "Visit_RESCHEDULE_REASON", "Select reason");
        }
        alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public void updateVisitStatus(int visitStatus, List<String> serverResponse) throws UnsupportedEncodingException {
        int visitStatusValue = Integer.parseInt(serverResponse.get(0).toString());
        switch(visitStatus){
            case R.integer.VISIT_STATUS_STARTED_FROM_OFFICE:
                visitReport.setVisitDate(serverResponse.get(1).toString());
                visitReport.setStartTime(serverResponse.get(2).toString());
                visitReport.setOutOfficeTime(visitReport.getStartTime());
                visitReport.setStartedFrom(URLDecode(serverResponse.get(3).toString()));
                MyApplication.isRunningVisit = approval.getId();

                break;
            case R.integer.VISIT_STATUS_STARTED_FROM_OTHER:
                visitReport.setVisitDate(serverResponse.get(1).toString());
                visitReport.setStartTime(serverResponse.get(2).toString());
                visitReport.setStartedFrom(URLDecode(serverResponse.get(3).toString()));
                visitReport.setStartedFromOtherReason(URLDecode(serverResponse.get(4).toString()));
                visitReport.setAfterAnotherVisit(serverResponse.get(5).toString());
                MyApplication.isRunningVisit = approval.getId();

                break;
            case R.integer.VISIT_STATUS_REACHED_CUSTOMER:
                visitReport.setInCustomerTime(serverResponse.get(2).toString());
                break;
            case R.integer.VISIT_STATUS_FINISHED:
                visitReport.setEndTime(serverResponse.get(2).toString());
                if(visitReport.getInCustomerTime() != null){
                    visitReport.setOutCustomerTime(visitReport.getEndTime());
                }
                visitReport.setFeedbackNote(URLDecode(serverResponse.get(3).toString()));
                MyApplication.isRunningVisit = null;
                break;
            case R.integer.VISIT_STATUS_CANCELED_BEFORE_START:
                visitReport = new VisitReport();
                visitReport.setId(approval.getId());
                visitReport.setVisitDate(serverResponse.get(1).toString());
                visitReport.setCancelTime(serverResponse.get(2).toString());
                visitReport.setCancelReason(URLDecode(serverResponse.get(3).toString()));

                break;
            case R.integer.VISIT_STATUS_CANCELED:
                visitReport.setEndTime(serverResponse.get(2).toString());
                visitReport.setCancelTime(serverResponse.get(2).toString());
                visitReport.setCancelReason(URLDecode(serverResponse.get(3).toString()));
                MyApplication.isRunningVisit = null;
                break;
            case R.integer.VISIT_STATUS_RESCHEDULED_BEFORE_START:
                visitReport = new VisitReport();
                visitReport.setId(approval.getId());
                visitReport.setVisitDate(serverResponse.get(1).toString());
                visitReport.setRescheduleTime(serverResponse.get(2).toString());
                visitReport.setRescheduleReason(URLDecode(serverResponse.get(3).toString()));
                visitReport.setRescheduledVisitingDate(serverResponse.get(4).toString());

                break;
            case R.integer.VISIT_STATUS_RESCHEDULED:
                visitReport.setEndTime(serverResponse.get(2).toString());
                visitReport.setRescheduleTime(serverResponse.get(2).toString());
                visitReport.setRescheduleReason(URLDecode(serverResponse.get(3).toString()));
                visitReport.setRescheduledVisitingDate(serverResponse.get(4).toString());
                MyApplication.isRunningVisit = null;
                break;
        }
        VisitDetailActivity.LAST_ACTION_TAKEN = visitStatus;
        visitReport.setVisitStatus(visitStatusValue);
        Realm realm = Realm.getDefaultInstance();
        // Copy elements from Retrofit to Realm to persist them.
        realm.beginTransaction();
        visitReport = realm.copyToRealmOrUpdate(visitReport);
        realm.commitTransaction();

        showVisitProgressApprover(null);
    }
    private String URLDecode(String string) throws UnsupportedEncodingException {
        return URLDecoder.decode(string, "UTF-8");
    }
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
        Call<List<String>> visits = apiManager.updateVisitReport(loginUser.getToken(), loginUser.getUsername(), loginUser.getUser().getEmpId(), MyApplication.getAppUniqueID(getApplicationContext()), approval.getId(), res.getInteger(visitStatus)+"", msg1, msg2, msg3, msg4);
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
