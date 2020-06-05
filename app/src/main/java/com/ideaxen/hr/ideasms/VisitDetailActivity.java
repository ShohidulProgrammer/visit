package com.ideaxen.hr.ideasms;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Bundle;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.text.InputType;
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
import com.ideaxen.hr.ideasms.model.Visit;
import com.ideaxen.hr.ideasms.services.LocationSaver;
import com.ideaxen.hr.ideasms.model.Login;
import com.ideaxen.hr.ideasms.model.VisitDetailItem;
import com.ideaxen.hr.ideasms.model.VisitReport;
import com.ideaxen.hr.ideasms.services.OfflineDataService;
import com.ideaxen.hr.ideasms.util.VisitDetailItemAdapter;

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

public class VisitDetailActivity extends AppCompatActivity implements Callback<List<VisitDetailItem>>, SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = "VisitDetailActivity";
    public static final String BROADCAST_ACTION = "com.ideaxen.broadcastreceiver";

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
    protected Button btnStartVisit;
    protected Button btnStartVisitFromOffice;

    protected View mainView;

    protected Spinner spinnerOutOfOfficePurpose;

    protected TextView txtViewVisiteeCompanyName;
    protected TextView txtViewVisiteeCompanyAddress;
    protected View rateViews[] = new View[5];
    protected static int CURR_ACTION = -10;
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
    protected static final int DEFAULT_LAST_ACTION_TAKEN = -10;

    LocationSaver locationSaver;
    IntentFilter intentFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = this.getLayoutInflater();
        mainView = inflater.inflate(R.layout.activity_visit_detail, null);
        setContentView(mainView);

        findViewsById();

        //Add back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Get the application context
        thisContext = getApplicationContext();

        // Get the activity
        thisActivity = VisitDetailActivity.this;

        locationSaver = new LocationSaver();

        LAST_ACTION_TAKEN = DEFAULT_LAST_ACTION_TAKEN;

        dateFormatter = new SimpleDateFormat("dd-MM-yyyy", Locale.US);

        Intent in = getIntent();
        positionInList = getIntent().getIntExtra("com.ideaxen.client.visitapp.int.POSITION_IN_LIST", -1);
        visit = getIntent().getParcelableExtra("com.ideaxen.client.visitapp.model.VISIT");
        loginUser = getIntent().getParcelableExtra("com.ideaxen.client.visitapp.model.LOGIN");
        isRecentlyVisited = getIntent().getBooleanExtra("com.ideaxen.client.visitapp.bool.RECENTLY_VISITED", false);
        isAdjournedVisit = getIntent().getBooleanExtra("com.ideaxen.client.visitapp.bool.ADJOURNED_VISIT", false);
        directReport = getIntent().getBooleanExtra("com.ideaxen.client.visitapp.bool.DIRECT_REPORT", false);
        getVisitReport(visit.getId());
        mSwipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        intentFilter = new IntentFilter();
        intentFilter.addAction(BROADCAST_ACTION);

        mSwipeRefreshLayout.setOnRefreshListener(this);

        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                getVisitDetail(false);
            }
        });

//        locationSaver.startLocationProcess(thisContext, visit.getId(), "Visit Details");
    }

    public void showToast(String message) {
        Toast.makeText(thisActivity, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();

        stopService(new Intent(getApplicationContext(), OfflineDataService.class));
        if (visitReport == null) {
            Realm realm = Realm.getDefaultInstance();
            visitReport = realm.where(VisitReport.class)
                    .equalTo("id", visit.getId())
                    .findFirst();
        }
        if (visit.getId().equals(MyApplication.isRunningVisit) && (visitReport == null || visitReport.getVisitStatus() == 0)) {
            getVisitReport(visit.getId());
        }
        else {
            setVisitProgressMilestones();
        }
        try {
            registerReceiver(myBroadCastReceiver, intentFilter);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            Log.d(TAG, "registerReceiver: " + e.getMessage());
        }
    }

    private BroadcastReceiver myBroadCastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra("auto")) {
                Log.d(TAG, "auto" + intent.getStringExtra("auto"));
//                locationSaver.autoLocationProcess(thisContext);
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(myBroadCastReceiver);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            Log.d(TAG, "unregisterReceiver: " + e.getMessage());
        }
    }

    public void getVisitReport(String visitId) {
        Resources res = getResources();
        ApiManager apiManager = ApiManager.getInstance();
        final Call<VisitReport> thisVisitReport = apiManager.getVisitReport(loginUser.getToken(), loginUser.getUsername(), loginUser.getUser().getEmpId(), MyApplication.getAppUniqueID(getApplicationContext()), visitId);
        thisVisitReport.enqueue(new Callback<VisitReport>() {
            @Override
            public void onResponse(Call<VisitReport> call, Response<VisitReport> response) {
                visitReport = response.body();
                if (directReport) {
                    directReport = false;
                    showVisitProgress(null);
                }
                else {
                    setVisitProgressMilestones();
                }
            }

            @Override
            public void onFailure(Call<VisitReport> call, Throwable t) {
                Log.d("Failed:", t.getMessage());
            }
        });
    }

    public void getVisitDetail(boolean force_load_from_server) {
//        Toast.makeText(getApplicationContext(), visitFilterPref+"", Toast.LENGTH_SHORT);
        mSwipeRefreshLayout.setRefreshing(true);
        Realm realm = Realm.getDefaultInstance();
        if (!force_load_from_server) {
            visitDetails = realm.where(VisitDetailItem.class)
                    .equalTo("id", visit.getId())
                    .findAll();
        } else {
            final RealmResults<VisitDetailItem> results = realm.where(VisitDetailItem.class)
                    .equalTo("id", visit.getId())
                    .findAll();
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    // Delete all matches
                    results.deleteAllFromRealm();
                }
            });
        }

        if (force_load_from_server || visitDetails.size() <= 0) {//not available locally
            ApiManager apiManager = ApiManager.getInstance();
            apiManager.getVisitDetail(loginUser.getToken(), loginUser.getUsername(), loginUser.getUser().getEmpId(), MyApplication.getAppUniqueID(getApplicationContext()), visit.getId(), this);
        } else {
            setDetailAdapter();
        }
    }

    public void setVisitProgressMilestones() {
        if (isRecentlyVisited || isAdjournedVisit) {
            manipulateVisitFinishUIComponents();
            return;
        }
        if (MyApplication.isRunningVisit != null && !MyApplication.isRunningVisit.isEmpty() && !visit.getId().equals(MyApplication.isRunningVisit)) {
            panelVisitDetailBottom0.setVisibility(View.GONE);
            panelVisitDetailBottom.setVisibility(View.GONE);
            return;
        }
        else if (MyApplication.isRunningVisit != null && visit.getId().equals(MyApplication.isRunningVisit)) {//this is the running visit

            Resources res = getResources();
            if (visitReport.getVisitStatus() == res.getInteger(R.integer.VISIT_STATUS_CANCELED) ||
                    visitReport.getVisitStatus() == res.getInteger(R.integer.VISIT_STATUS_CANCELED_BEFORE_START) ||
                    visitReport.getVisitStatus() == res.getInteger(R.integer.VISIT_STATUS_RESCHEDULED) ||
                    visitReport.getVisitStatus() == res.getInteger(R.integer.VISIT_STATUS_RESCHEDULED_BEFORE_START) ||
                    visitReport.getVisitStatus() == res.getInteger(R.integer.VISIT_STATUS_FINISHED)) {
                manipulateVisitFinishUIComponents();
                return;
            } else {
                manipulateVisitStartUIComponents();
                return;
            }
        } else if (visitReport != null) {
            Resources res = getResources();
            if (visitReport.getVisitStatus() == res.getInteger(R.integer.VISIT_STATUS_CANCELED) ||
                    visitReport.getVisitStatus() == res.getInteger(R.integer.VISIT_STATUS_CANCELED_BEFORE_START) ||
                    visitReport.getVisitStatus() == res.getInteger(R.integer.VISIT_STATUS_RESCHEDULED) ||
                    visitReport.getVisitStatus() == res.getInteger(R.integer.VISIT_STATUS_RESCHEDULED_BEFORE_START) ||
                    visitReport.getVisitStatus() == res.getInteger(R.integer.VISIT_STATUS_FINISHED) ||
                    visitReport.getVisitStatus() == res.getInteger(R.integer.VISIT_STATUS_ADJOURNED)
            ) {
                manipulateVisitFinishUIComponents();
                return;
            }
        }
        manipulateVisitNewUIComponents();
    }

    private void findViewsById() {
        txtViewVisiteeCompanyName = findViewById(R.id.txtViewVisiteeCompanyName);
        txtViewVisiteeCompanyAddress = findViewById(R.id.txtViewVisiteeCompanyAddress);

        rateViews[0] = findViewById(R.id.rate1);
        rateViews[1] = findViewById(R.id.rate2);
        rateViews[2] = findViewById(R.id.rate3);
        rateViews[3] = findViewById(R.id.rate4);
        rateViews[4] = findViewById(R.id.rate5);

        visitDetailList = findViewById(R.id.visitDetailList);
        btnStartVisit = findViewById(R.id.btnStartVisit);
        btnShowVisitProgress = findViewById(R.id.btnShowVisitProgress);
        btnStartVisitFromOffice = findViewById(R.id.btnStartVisitFromOffice);
        panelVisitDetailBottom = findViewById(R.id.panelVisitDetailBottom);
        panelVisitDetailBottom0 = findViewById(R.id.panelVisitDetailBottom0);
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

    public void makeCall(View v , String name, final String number){
        AlertDialog.Builder confirmDialogBuilder = new AlertDialog.Builder(v.getRootView().getContext());
        confirmDialogBuilder.setTitle("Do you want to call " + name + "?");

        LayoutInflater inflater = LayoutInflater.from(v.getRootView().getContext());
        View confirmDialogView = inflater.inflate(R.layout.confirm_dialog, null);
        confirmDialogBuilder.setView(confirmDialogView);

        Button btnNegative = confirmDialogView.findViewById(R.id.btnNegative);
        final AlertDialog confirmDialog = confirmDialogBuilder.create();
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
                if (ActivityCompat.checkSelfPermission(thisContext, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                startActivity(intent);
            }
        });
        confirmDialog.show();
    }

    public void openConfirmDialog(View v){
        AlertDialog.Builder confirmDialogBuilder = new AlertDialog.Builder(this).setTitle("Confirmation Required");
        LayoutInflater inflater = this.getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        View confirmDialogView = inflater.inflate(R.layout.confirm_dialog, null);
        confirmDialogBuilder.setView(confirmDialogView);

        Button btnNegative = confirmDialogView.findViewById(R.id.btnNegative);
        confirmDialog = confirmDialogBuilder.create();

        if(v.getId() == R.id.btnStartAfterAnotherVisit){
            VisitDetailActivity.CURR_ACTION = R.integer.ACTION_VISIT_CONFIRM_AFTER_ANOTHER;
        }
        else if(v.getId() == R.id.btnStartVisitFromOffice){
            VisitDetailActivity.CURR_ACTION = R.integer.ACTION_VISIT_CONFIRM_FROM_OFFICE;
        }

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
                switch (VisitDetailActivity.CURR_ACTION) {
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
                        Spinner spinnerVisitReschedulePurpose = dialogView.findViewById(R.id.spinnerVisitReschedulePurpose);
                        if(spinnerVisitReschedulePurpose.getSelectedItem().toString().equals("Select reason")){
//                            Toast.makeText(thisContext, "You must select a valid reason", Toast.LENGTH_SHORT);
                            showToast("You must select a valid reason");
                            break;
                        }
                        EditText edtTextNewVisitingDate = dialogView.findViewById(R.id.edtTextNewVisitingDate);
//                        if(edtTextNewVisitingDate.getText() == null){
//                            Toast.makeText(thisContext, "You must select a valid visiting date", Toast.LENGTH_SHORT);
//                            break;
//                        }
                        proceedVisitRescedule(v, spinnerVisitReschedulePurpose.getSelectedItem().toString(), edtTextNewVisitingDate.getText().toString());
                        break;
                    default:
                        startVisitConfirmed(v, VisitDetailActivity.CURR_ACTION == R.integer.ACTION_VISIT_CONFIRM_AFTER_ANOTHER);
                }
            }
        });

        confirmDialog.show();
    }

    public void startVisitConfirmed(View v, boolean afterAnotherVisit){
        visitReport = new VisitReport();
        visitReport.setId(visit.getId());
        if(alertDialog != null && alertDialog.isShowing()){//visit started from out of office
            EditText edtTextFromWhere = dialogView.findViewById(R.id.edtTextFromWhere);
            spinnerOutOfOfficePurpose = dialogView.findViewById(R.id.spinnerOutOfOfficePurpose);
            String reason = spinnerOutOfOfficePurpose.getSelectedItem().toString();
            if(reason.equals("Select reason"))
                reason = "";
            if(afterAnotherVisit){
//                    Toast.makeText(thisActivity,"After Another Visit",Toast.LENGTH_SHORT).show();
                startedVisitAfterAnotherVisit(edtTextFromWhere.getText().toString().isEmpty()?"-":edtTextFromWhere.getText().toString(),
                        reason.equals("")?"After Another Visit":reason);
                alertDialog.dismiss();
            }else{//not after another visit
                if(edtTextFromWhere.getText().toString().isEmpty()) {
//                Toast.makeText(thisContext, "You must enter a valid place", Toast.LENGTH_SHORT);
                    showToast("You must enter a valid place");
                }
                else if(reason.equals("")){
//                        Toast.makeText(thisContext, "You must select a valid reason", Toast.LENGTH_SHORT);
                    showToast("You must select a valid reason");
                }else {
                    startedVisitFromOutOfOffice(edtTextFromWhere.getText().toString(), spinnerOutOfOfficePurpose.getSelectedItem().toString());
//                    locationSaver.startLocationProcess(thisContext, visit.getId(), "Visit Started");
                    alertDialog.dismiss();
                }
            }

//            VisitDetailActivity.VISIT_STARTED_FROM_OFFICE = false;
        }else{//visit started from office
            startedVisitFromOffice();
        }
//        VisitDetailActivity.VISIT_STARTED = true;

//        showVisitProgress(null);



//        menuItemLogout.setVisible(false);
//        menuItemVisitSummary.setVisible(true);

//        Toast.makeText(thisActivity,"You have confirmed to proceed",Toast.LENGTH_SHORT).show();
    }
    public void showVisitProgress(View view) {
        Intent intent = new Intent(thisActivity, VisitProgressActivity.class);
        intent.putExtra("com.ideaxen.client.visitapp.bool.RECENTLY_VISITED", isRecentlyVisited);
        intent.putExtra("com.ideaxen.client.visitapp.model.VISIT", visit);
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
    public void onResponse(Call<List<VisitDetailItem>> call, Response<List<VisitDetailItem>> response) {
        visitDetails = response.body();

        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.insert(visitDetails);
        realm.commitTransaction();

        setDetailAdapter();

    }

    @Override
    public void onFailure(Call<List<VisitDetailItem>> call, Throwable t) {
        Log.d("Failed", "Request Failed: "+t.getMessage().toString());
    }
    private void setDetailAdapter(){
        txtViewVisiteeCompanyName.setText(visit.getCustomer());
        txtViewVisiteeCompanyAddress.setText(visit.getAddress());
        int priority = Integer.valueOf(visit.getVisitPriority());
        if(priority > 0){
            for(int i = 0; i < priority; i++){
                rateViews[i].setBackground(ContextCompat.getDrawable(thisContext, R.drawable.rating_circle_full));
            }
        }

        VisitDetailItemAdapter visitDetailItemAdapter = new VisitDetailItemAdapter(this, visitDetails);
        visitDetailList.setAdapter(visitDetailItemAdapter);
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
    //==================----------------------------=====================
    @Override
    public void onBackPressed() {
        backToListPage();
    }

    private void backToListPage(){
        Intent ret = new Intent();
        ret.putExtra("com.ideaxen.client.visitapp.model.LAST_ACTION", VisitDetailActivity.LAST_ACTION_TAKEN);
        ret.putExtra("com.ideaxen.client.visitapp.int.POSITION_IN_LIST", positionInList);
        ret.putExtra("com.ideaxen.client.visitapp.string.VISIT_REPORT", (visitReport==null?null:visitReport.getVisitStatus()+""));
        setResult(RESULT_OK,ret);
        LAST_ACTION_TAKEN = DEFAULT_LAST_ACTION_TAKEN;
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
            datePickerField.setInputType(InputType.TYPE_NULL);
            setDatePickerField(datePickerField);
        }
        if(dialog_layout == R.layout.visit_from_out_of_office){
            Spinner spinner = dialogView.findViewById(R.id.spinnerOutOfOfficePurpose);
            MyApplication.bindLovToSpinner(thisActivity, spinner, "VISIT_FROM_OTHER_REASON", "Select reason");
        }
        else if(dialog_layout == R.layout.visit_cancelled){
            Spinner spinner = dialogView.findViewById(R.id.spinnerVisitCancelPurpose);
            MyApplication.bindLovToSpinner(thisActivity, spinner, "VISIT_CANCEL_REASON", "Select reason");
        }
        else if(dialog_layout == R.layout.visit_reschedule){
            Spinner spinner = dialogView.findViewById(R.id.spinnerVisitReschedulePurpose);
            MyApplication.bindLovToSpinner(thisActivity, spinner, "VISIT_RESCHEDULE_REASON", "Select reason");
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

    public void rescheduledVisit(String reason, String newVisitngDate){
        if(btnShowVisitProgress.isShown())
            updateOnServer(R.integer.VISIT_STATUS_RESCHEDULED, reason, newVisitngDate);
        else
            updateOnServer(R.integer.VISIT_STATUS_RESCHEDULED_BEFORE_START, reason, newVisitngDate);
    }
    public void canceledVisit(String reason){
        if(btnShowVisitProgress.isShown())
            updateOnServer(R.integer.VISIT_STATUS_CANCELED, reason);
        else
            updateOnServer(R.integer.VISIT_STATUS_CANCELED_BEFORE_START, reason);
    }
    public void startedVisitAfterAnotherVisit(String from, String reason){
        updateOnServer(R.integer.VISIT_STATUS_STARTED_FROM_OTHER, from, reason, "Yes");
    }
    public void startedVisitFromOutOfOffice(String from, String reason){
        updateOnServer(R.integer.VISIT_STATUS_STARTED_FROM_OTHER, from, reason);
    }
    public void startedVisitFromOffice(){
        updateOnServer(R.integer.VISIT_STATUS_STARTED_FROM_OFFICE, "Office");
    }

    private void manipulateVisitNewUIComponents(){
        btnShowVisitProgress.setVisibility(View.GONE);
        btnStartVisit.setVisibility(View.VISIBLE);
        btnStartVisitFromOffice.setVisibility(View.VISIBLE);
        panelVisitDetailBottom.setVisibility(View.VISIBLE);

        if(visit.getIsInstant().equals("Yes")){
            btnStartVisit.setText("Start Visit");
            btnStartVisitFromOffice.setVisibility(View.GONE);
        }
    }
    private void manipulateVisitFinishUIComponents(){
        manipulateVisitCancelUIComponents();
    }
    private void manipulateVisitRescheduleUIComponents(){
        manipulateVisitCancelUIComponents();
    }
    private void manipulateVisitCancelUIComponents(){
        manipulateVisitStartUIComponents();
        panelVisitDetailBottom.setVisibility(View.GONE);
    }

    private void manipulateVisitStartUIComponents(){
        btnShowVisitProgress.setVisibility(View.VISIBLE);
        btnStartVisit.setVisibility(View.GONE);
        btnStartVisitFromOffice.setVisibility(View.GONE);
        panelVisitDetailBottom.setVisibility(View.GONE);
    }
    public void updateVisitStatus(int visitStatus, List<String> serverResponse) throws UnsupportedEncodingException {
        int visitStatusValue = Integer.parseInt(serverResponse.get(0));
        switch(visitStatus){
            case R.integer.VISIT_STATUS_STARTED_FROM_OFFICE:
                visitReport.setVisitDate(serverResponse.get(1));
                visitReport.setStartTime(serverResponse.get(2));
                visitReport.setOutOfficeTime(visitReport.getStartTime());
                visitReport.setStartedFrom(URLDecode(serverResponse.get(3)));
                MyApplication.isRunningVisit = visit.getId();
                manipulateVisitStartUIComponents();

                break;
            case R.integer.VISIT_STATUS_STARTED_FROM_OTHER:
                visitReport.setVisitDate(serverResponse.get(1));
                visitReport.setStartTime(serverResponse.get(2));
                visitReport.setStartedFrom(URLDecode(serverResponse.get(3)));
                visitReport.setStartedFromOtherReason(URLDecode(serverResponse.get(4)));
                visitReport.setAfterAnotherVisit(serverResponse.get(5));
                MyApplication.isRunningVisit = visit.getId();
                manipulateVisitStartUIComponents();

                break;
            case R.integer.VISIT_STATUS_REACHED_CUSTOMER:
                visitReport.setInCustomerTime(serverResponse.get(2));
                break;
            case R.integer.VISIT_STATUS_FINISHED:
                visitReport.setEndTime(serverResponse.get(2));
                if(visitReport.getInCustomerTime() != null){
                    visitReport.setOutCustomerTime(visitReport.getEndTime());
                }
                visitReport.setFeedbackNote(URLDecode(serverResponse.get(3)));
                MyApplication.isRunningVisit = null;
                manipulateVisitFinishUIComponents();
                break;
            case R.integer.VISIT_STATUS_CANCELED_BEFORE_START:
                visitReport = new VisitReport();
                visitReport.setId(visit.getId());
                visitReport.setVisitDate(serverResponse.get(1));
                visitReport.setCancelTime(serverResponse.get(2));
                visitReport.setCancelReason(URLDecode(serverResponse.get(3)));

                manipulateVisitCancelUIComponents();
                break;
            case R.integer.VISIT_STATUS_CANCELED:
                visitReport.setEndTime(serverResponse.get(2));
                visitReport.setCancelTime(serverResponse.get(2));
                visitReport.setCancelReason(URLDecode(serverResponse.get(3)));
                MyApplication.isRunningVisit = null;
                manipulateVisitCancelUIComponents();
                break;
            case R.integer.VISIT_STATUS_RESCHEDULED_BEFORE_START:
                visitReport = new VisitReport();
                visitReport.setId(visit.getId());
                visitReport.setVisitDate(serverResponse.get(1));
                visitReport.setRescheduleTime(serverResponse.get(2));
                visitReport.setRescheduleReason(URLDecode(serverResponse.get(3)));
                visitReport.setRescheduledVisitingDate(serverResponse.get(4));

                manipulateVisitRescheduleUIComponents();
                break;
            case R.integer.VISIT_STATUS_RESCHEDULED:
                visitReport.setEndTime(serverResponse.get(2));
                visitReport.setRescheduleTime(serverResponse.get(2));
                visitReport.setRescheduleReason(URLDecode(serverResponse.get(3)));
                visitReport.setRescheduledVisitingDate(serverResponse.get(4));
                MyApplication.isRunningVisit = null;
                manipulateVisitRescheduleUIComponents();
                break;
        }
        VisitDetailActivity.LAST_ACTION_TAKEN = visitStatus;
        visitReport.setVisitStatus(visitStatusValue);
        Realm realm = Realm.getDefaultInstance();
        // Copy elements from Retrofit to Realm to persist them.
        realm.beginTransaction();
        visitReport = realm.copyToRealmOrUpdate(visitReport);
        realm.commitTransaction();

        showVisitProgress(null);
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
