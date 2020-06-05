package com.ideaxen.hr.ideasms;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Bundle;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.internal.LinkedTreeMap;
import com.ideaxen.hr.ideasms.api.ApiManager;
import com.ideaxen.hr.ideasms.model.Visit;
import com.ideaxen.hr.ideasms.model.VisitUpdateData;
import com.ideaxen.hr.ideasms.services.FirebaseMessagingService;
import com.ideaxen.hr.ideasms.model.Login;
import com.ideaxen.hr.ideasms.util.ApprovalItemAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import io.realm.Realm;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyApprovalActivity extends AppCompatActivity implements Callback<List<Object>>, SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = "MyApprovalActivity";

    Login loginUser;
    ListView myApprovalList;
    ApprovalItemAdapter approvalItemAdapter;
    List<Visit> approvals;
    LinearLayout filterSection;
    String isFCM = null;
    boolean isDetail = false;

    EditText edtTextFromDate;
    EditText edtTextToDate;

    private DatePickerDialog fromDatePickerDialog;
    private DatePickerDialog toDatePickerDialog;

    private SimpleDateFormat dateFormatter;

    AlertDialog confirmDialog;

    String fromDate = "";
    String toDate = "";
    boolean appliedFilter;

    protected String visitFilterPref;
    protected String approvalFilterPref;

    private Context thisContext;
    private Activity thisActivity;

    SwipeRefreshLayout mSwipeRefreshLayout;
    private View rateViewsConfirm[] = new View[5];
    private int newPriority = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_approval);

        dateFormatter = new SimpleDateFormat("dd-MM-yyyy", Locale.US);

        // Get the application context
        thisContext = getApplicationContext();

        // Get the activity
        thisActivity = MyApprovalActivity.this;

        mSwipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        appliedFilter = false;

        findViewsById();
        setDateTimeField();

        cancelFilter(null);

        loginUser = MyApplication.isLoggedin();

        FirebaseMessaging.getInstance().subscribeToTopic(loginUser.getUser().getUsername())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Log.d(TAG, "Subscribed To Topic: " + loginUser.getUser().getUsername());
                        }
                    }
                });

        visitFilterPref = MyApplication.getVisitPreference(getApplicationContext());

        mSwipeRefreshLayout.setOnRefreshListener(this);

        mSwipeRefreshLayout.post(new Runnable() {
             @Override
             public void run() {
                 getApprovals();
             }
         }
        );

        myApprovalList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Visit approval = (Visit) myApprovalList.getItemAtPosition(position);
                Log.d(TAG, "onItemClick: " + approval);
                moveToDetailView(approval, position, false);
//                ActivityUtils.startActivity(VisitDetailActivity.class);
            }
        });

        if(getIntent().hasExtra("FROM_FCM")){
            String from_fcm = getIntent().getStringExtra("FROM_FCM");
            if(from_fcm != null && from_fcm.equals("FCM")){

                String fcm_action = getIntent().getStringExtra("ACTION");
                String fcm_visitType = getIntent().getStringExtra("VISIT_TYPE");
                String fcm_visitId = getIntent().getStringExtra("VISIT_ID");
                String fcm_visitPriority = getIntent().getStringExtra("VISIT_PRIORITY");

                isFCM = fcm_visitId;

                Log.d(TAG, "onCreate: ACTION: " + fcm_action + " ID: " + fcm_visitId);

                NotificationManager notificationManager = (NotificationManager) thisContext.getSystemService(Context.NOTIFICATION_SERVICE);
                if (notificationManager != null) {
                    notificationManager.cancel(FirebaseMessagingService.NOTIFICATION_ID);
                }

                switch (fcm_action) {
                    case "APPROVE":
                        openAppDecConfirmDialog(1, fcm_visitId, fcm_visitPriority);
                        break;
                    case "DECLINE":
                        openAppDecConfirmDialog(0, fcm_visitId, fcm_visitPriority);
                        break;
                    case "DETAIL":
                        if(fcm_visitType.equals("Yes")){
                            filterVisitsByMenu(R.string.FILTER_INSTANT_VISITS);
                        }
                        else{
                            filterVisitsByMenu(R.string.FILTER_ALL_VISITS);
                        }
                        isDetail = true;
                        break;
                }
            }
        }
    }

    public void showToast(String message){
        Toast.makeText(thisActivity, message,Toast.LENGTH_SHORT).show();
    }

    private void moveToDetailView(Visit approval, int position, boolean directReport){
        Intent intent = new Intent(thisActivity, ApprovalDetailActivity.class);
        intent.putExtra("FILTER_TYPE", visitFilterPref);
        startActivity(intent);
        intent.putExtra("com.ideaxen.client.visitapp.bool.DIRECT_REPORT", directReport);
        intent.putExtra("com.ideaxen.client.visitapp.bool.RECENTLY_VISITED", visitFilterPref.equals(thisContext.getResources().getString(R.string.FILTER_RECENTLY_VISITED)));
        intent.putExtra("com.ideaxen.client.visitapp.bool.ADJOURNED_VISIT", visitFilterPref.equals(thisContext.getResources().getString(R.string.FILTER_ADJOURNED_VISITS)));
        intent.putExtra("com.ideaxen.client.visitapp.int.POSITION_IN_LIST", position);
        intent.putExtra("com.ideaxen.client.visitapp.model.APPROVAL", approval);
        intent.putExtra("com.ideaxen.client.visitapp.model.LOGIN", loginUser);
        startActivityForResult(intent, 2);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == 2) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                int last_action = data.getIntExtra("com.ideaxen.client.visitapp.model.LAST_ACTION", -10);
                String visit_status = data.getStringExtra("com.ideaxen.client.visitapp.string.VISIT_REPORT");
                if(visit_status == null)return;
                int visit_status_val =Integer.parseInt(visit_status);
                int positin_in_list = data.getIntExtra("com.ideaxen.client.visitapp.int.POSITION_IN_LIST", -1);
                Resources res = getResources();
                if(last_action == R.integer.VISIT_STATUS_CANCELED||
                        last_action == R.integer.VISIT_STATUS_ADJOURNED ||
                        last_action == R.integer.VISIT_STATUS_CANCELED_BEFORE_START ||
                        last_action == R.integer.VISIT_STATUS_RESCHEDULED ||
                        last_action == R.integer.VISIT_STATUS_RESCHEDULED_BEFORE_START ||
                        last_action == R.integer.VISIT_STATUS_FINISHED){
                    approvals.remove(positin_in_list);
                    approvalItemAdapter.notifyDataSetChanged();
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void getApprovals(){
        ApiManager apiManager = ApiManager.getInstance();
        mSwipeRefreshLayout.setRefreshing(true);
        if(appliedFilter)
            apiManager.getApprovals(loginUser.getToken(), loginUser.getUsername(), loginUser.getLoginType(), loginUser.getUser().getEmpId(), MyApplication.getAppUniqueID(getApplicationContext()), visitFilterPref, fromDate, toDate,this);
        else
            apiManager.getApprovals(loginUser.getToken(), loginUser.getUsername(), loginUser.getLoginType(), loginUser.getUser().getEmpId(), MyApplication.getAppUniqueID(getApplicationContext()), visitFilterPref, "", "",this);

    }

    private void findViewsById() {
        myApprovalList = findViewById(R.id.myApprovalList);
        filterSection = findViewById(R.id.filterSection);
        edtTextFromDate = findViewById(R.id.edtTextFromDate);
        edtTextToDate = findViewById(R.id.edtTextToDate);

        edtTextFromDate.setInputType(InputType.TYPE_NULL);
        edtTextToDate.setInputType(InputType.TYPE_NULL);
    }

    private void setDateTimeField() {
        Calendar newCalendar = Calendar.getInstance();
        fromDatePickerDialog = new DatePickerDialog(this, DatePickerDialog.THEME_DEVICE_DEFAULT_DARK, new DatePickerDialog.OnDateSetListener() {
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                edtTextFromDate.setText(dateFormatter.format(newDate.getTime()));
            }

        },newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));

        toDatePickerDialog = new DatePickerDialog(this, DatePickerDialog.THEME_DEVICE_DEFAULT_DARK, new DatePickerDialog.OnDateSetListener() {
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                edtTextToDate.setText(dateFormatter.format(newDate.getTime()));
            }

        },newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));

        edtTextFromDate.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus)
                    fromDatePickerDialog.show();
            }
        });
        edtTextToDate.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {

                if(hasFocus)
                    toDatePickerDialog.show();
            }
        });
    }
    // create an action bar button
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actionbar_menu_approver, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // handle button activities
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int optionItem = item.getItemId();
        switch (optionItem) {
            case R.id.action_filter:
                if(filterSection.isShown())
                    cancelFilter(null);
                else
                    showFilter();
                break;
            case R.id.action_day_visits:
                filterVisitsByMenu(R.string.FILTER_TODAY_VISITS);
                break;
            case R.id.action_week_visits:
                filterVisitsByMenu(R.string.FILTER_WEEKS_VISITS);
                break;
            case R.id.action_month_visits:
                filterVisitsByMenu(R.string.FILTER_MONTHS_VISITS);
                break;
            case R.id.action_all_visits:
                filterVisitsByMenu(R.string.FILTER_ALL_VISITS);
                break;
            case R.id.action_approved_visits:
                filterVisitsByMenu(R.string.FILTER_APPROVED_VISITS);
                break;
            case R.id.action_declined_visits:
                filterVisitsByMenu(R.string.FILTER_DECLINED_VISITS);
                break;
            case R.id.action_adjourned_visits:
                filterVisitsByMenu(R.string.FILTER_ADJOURNED_VISITS);
                break;
            case R.id.action_logout:
                confirmLogoutDialog();
                break;


            default:
                break;
        }

        return true;
    }
    private void filterVisitsByMenu(int filter){
        appliedFilter = false;
        Resources res = getResources();
        String lastFilter = MyApplication.getVisitPreference(getApplicationContext());
        String filterValue = res.getString(filter);
        if(!lastFilter.equals(filterValue)){
            visitFilterPref = MyApplication.setVisitPreference(getApplicationContext(), filterValue);
        }
        getApprovals();
    }
    public void confirmLogoutDialog(){
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
//                cancelInstantVisitDialog(v);
            }
        });

        Button btnPositive = confirmDialogView.findViewById(R.id.btnPositive);
        btnPositive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmDialog.dismiss();
                FirebaseMessaging.getInstance().unsubscribeFromTopic(loginUser.getUser().getUsername());
                MyApplication.logout();
                Realm realm = Realm.getDefaultInstance();
                realm.beginTransaction();
                realm.deleteAll();
                realm.commitTransaction();
                startActivity(new Intent(thisContext, LoginActivity.class));
                finish();
            }
        });

        confirmDialog.show();
    }
    public void applyFilter(View view) {
        appliedFilter = true;
        fromDate = edtTextFromDate.getText().toString();
        toDate = edtTextToDate.getText().toString();
        visitFilterPref = MyApplication.setVisitPreference(thisContext, "0");
        getApprovals();
        cancelFilter(null);
    }
    public void clearFilter(View view) {
        edtTextFromDate.setText("");
        edtTextToDate.setText("");
        fromDate = "";
        toDate = "";
    }
    public void cancelFilter(View v) {
        edtTextFromDate.setText(fromDate);
        edtTextToDate.setText(toDate);
        filterSection.setVisibility(View.GONE);
    }
    public void showFilter() {
        filterSection.setVisibility(View.VISIBLE);
    }

    public void openAppDecConfirmDialog(final int flag, final String approvalId, String visitPriority){
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

        if(flag == 1){
            layout_rates.setVisibility(View.VISIBLE);
            layout_text.setVisibility(View.VISIBLE);
            rateViewsConfirm[0] = confirmDialogView.findViewById(R.id.confirmRate1);
            rateViewsConfirm[1] = confirmDialogView.findViewById(R.id.confirmRate2);
            rateViewsConfirm[2] = confirmDialogView.findViewById(R.id.confirmRate3);
            rateViewsConfirm[3] = confirmDialogView.findViewById(R.id.confirmRate4);
            rateViewsConfirm[4] = confirmDialogView.findViewById(R.id.confirmRate5);
            TextView tv_priority = confirmDialogView.findViewById(R.id.tv_priority);

            int priority = Integer.valueOf(visitPriority);
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

        newPriority = Integer.valueOf(visitPriority);

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
                    Call<VisitUpdateData> visits = apiManager.getApprovalUpdate(loginUser.getToken(), loginUser.getUsername(), loginUser.getUser().getEmpId(), MyApplication.getAppUniqueID(getApplicationContext()), approvalId, String.valueOf(newPriority));
                    visits.enqueue(new Callback<VisitUpdateData>() {
                        @Override
                        public void onResponse(Call<VisitUpdateData> call, Response<VisitUpdateData> response) {
                            VisitUpdateData visitUpdateData = response.body();
                            if (visitUpdateData != null) {
                                Log.d(TAG, "Approve Response: " + visitUpdateData);
                                showToast(visitUpdateData.getMessage());
                            }
                            getApprovals();
                        }

                        @Override
                        public void onFailure(Call<VisitUpdateData> call, Throwable t) {
                            Log.d("Failed", t.getMessage());
                        }
                    });
                }
                else if(flag==0){
                    ApiManager apiManager = ApiManager.getInstance();
                    Call<VisitUpdateData> visits = apiManager.getDeclineUpdate(loginUser.getToken(), loginUser.getUsername(), loginUser.getUser().getEmpId(), MyApplication.getAppUniqueID(getApplicationContext()), approvalId);
                    visits.enqueue(new Callback<VisitUpdateData>() {
                        @Override
                        public void onResponse(Call<VisitUpdateData> call, Response<VisitUpdateData> response) {
                            VisitUpdateData visitUpdateData = response.body();
                            if (visitUpdateData != null) {
                                Log.d(TAG, "Decline Response: " + visitUpdateData);
                                showToast(visitUpdateData.getMessage());
                            }
                            getApprovals();
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

    @Override
    public void onRefresh() {
        getApprovals();
    }

    @Override
    public void onResponse(Call<List<Object>> call, Response<List<Object>> response) {
        if(response.body() != null) {
            List<LinkedTreeMap> tempVisits = (List<LinkedTreeMap>)response.body().get(1);
            approvals = new ArrayList<>();
            for (LinkedTreeMap ltm : tempVisits) {
                approvals.add(
                        new Visit(ltm.get("id").toString(),
                        (ltm.get("customer") == null ? "" : ltm.get("customer").toString()),
                        (ltm.get("address") == null ? "" : ltm.get("address").toString()),
                        (ltm.get("contact_person") == null ? "" : ltm.get("contact_person").toString()),
                        (ltm.get("contact_number") == null ? "" : ltm.get("contact_number").toString()),
                        (ltm.get("visit_date") == null ? "" : ltm.get("visit_date").toString()),
                        (ltm.get("visit_priority") == null ? "0" : ltm.get("visit_priority").toString()),
                        (ltm.get("is_instant") == null ? "" : ltm.get("is_instant").toString()),
                        (ltm.get("visit_status") == null ? "" : ltm.get("visit_status").toString()))
                );
            }
            approvalItemAdapter = new ApprovalItemAdapter(this, approvals, visitFilterPref);
            myApprovalList.setAdapter(approvalItemAdapter);
        }
        TextView txtViewApprovalListTitle = findViewById(R.id.txtViewApprovalListTitle);
        Resources res = getResources();
        if (appliedFilter){
            txtViewApprovalListTitle.setText(res.getString(R.string.custom_filter_visit_title)+" "+fromDate+" to "+toDate);
        }
        else if (visitFilterPref.equals(res.getString(R.string.FILTER_TODAY_VISITS))){
            txtViewApprovalListTitle.setText(res.getString(R.string.todays_visit_title));
        }
        else if (visitFilterPref.equals(res.getString(R.string.FILTER_WEEKS_VISITS))){
            txtViewApprovalListTitle.setText(res.getString(R.string.weeks_visit_title));
        }
        else if (visitFilterPref.equals(res.getString(R.string.FILTER_MONTHS_VISITS))){
            txtViewApprovalListTitle.setText(res.getString(R.string.months_visit_title));
        }
        else if (visitFilterPref.equals(res.getString(R.string.FILTER_ALL_VISITS))){
            txtViewApprovalListTitle.setText(res.getString(R.string.all_pending_visit_title));
        }
        else if (visitFilterPref.equals(res.getString(R.string.FILTER_INSTANT_VISITS))){
            txtViewApprovalListTitle.setText(res.getString(R.string.pending_instant_visit_title));
        }
        else if (visitFilterPref.equals(res.getString(R.string.FILTER_APPROVED_VISITS))){
            txtViewApprovalListTitle.setText(res.getString(R.string.approved_visit_title));
        }
        else if (visitFilterPref.equals(res.getString(R.string.FILTER_DECLINED_VISITS))){
            txtViewApprovalListTitle.setText(res.getString(R.string.declined_visit_title));
        }
        else if (visitFilterPref.equals(res.getString(R.string.FILTER_ADJOURNED_VISITS))){
            txtViewApprovalListTitle.setText(res.getString(R.string.adjourned_visit_title));
        }
        mSwipeRefreshLayout.setRefreshing(false);

        if(isFCM != null && isDetail){
            int position = -1;
            for(int pos = 0; pos < approvalItemAdapter.getCount(); pos++){
                if(approvalItemAdapter.getItemId(pos) == Long.valueOf(isFCM)){
                    position = (int) approvalItemAdapter.getItemId(pos);
                    break;
                }
            }
            if(position != -1){
                Visit approval = (Visit) myApprovalList.getItemAtPosition(position);
                moveToDetailView(approval, position, false);
            }
        }
    }

    @Override
    public void onFailure(Call<List<Object>> call, Throwable t) {
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onBackPressed() {
        confirmExitDialog();
    }

    public void confirmExitDialog(){
        AlertDialog.Builder confirmDialogBuilder = new AlertDialog.Builder(this).setTitle("Confirmation Required");
        LayoutInflater inflater = this.getLayoutInflater();

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
                finish();
            }
        });
        confirmDialog.show();
    }
}
