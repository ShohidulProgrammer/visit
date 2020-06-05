package com.ideaxen.hr.ideasms;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.ideaxen.hr.ideasms.api.ApiManager;
import com.ideaxen.hr.ideasms.model.CustomerInfo;
import com.ideaxen.hr.ideasms.model.DashboardData;
import com.ideaxen.hr.ideasms.model.Login;
import com.ideaxen.hr.ideasms.model.Visit;
import com.ideaxen.hr.ideasms.util.CustomerInfoAdapter;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import io.realm.Realm;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardActivity extends AppCompatActivity implements Callback<List<DashboardData>>, SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = "DashboardActivity";
    public static final String BROADCAST_ACTION = "com.ideaxen.broadcastreceiver";
    private static final int REQUEST_LOCATION_CODE = 101;

    public Context thisContext;
    public AppCompatActivity thisActivity;

    IntentFilter intentFilter;

    AlertDialog confirmDialog;
    Login loginUser;
    Visit runningVisit;

    private SwipeRefreshLayout dashboard_swipe_refresh;
    private LinearLayout linearLayout_running, linearLayout_today, linearLayout_assigned, linearLayout_canceled, linearLayout_completed, linearLayout_missed_week, linearLayout_missed_month, linearLayout_pending, linearLayout_adjourned;
    private TextView textView_running_name, textView_running_address, textView_today, textView_assigned, textView_canceled, textView_completed, textView_missed_week, textView_missed_month, textView_pending, textView_adjourned;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

        thisContext = getApplicationContext();
        thisActivity = DashboardActivity.this;
        dashboard_swipe_refresh = findViewById(R.id.dashboard_swipe_refresh);

        loginUser = MyApplication.isLoggedin();

        findViewsById();

        intentFilter = new IntentFilter();
        intentFilter.addAction(BROADCAST_ACTION);

        FirebaseMessaging.getInstance().subscribeToTopic(loginUser.getUser().getUsername()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.d(TAG, "Subscribed To Topic: " + loginUser.getUser().getUsername());
            }
        });

        dashboard_swipe_refresh.setOnRefreshListener(this);

        dashboard_swipe_refresh.post(new Runnable() {
                 @Override
                 public void run() {
                     getDashboardData();
                 }
             }
        );

        if(MyApplication.isRunningVisit == null){
            textView_running_name.setText("None");
            textView_running_address.setVisibility(View.GONE);
        }

        linearLayout_running.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(runningVisit != null){
                    Intent intent = new Intent(thisActivity, VisitDetailActivity.class);
                    intent.putExtra("com.ideaxen.client.visitapp.model.VISIT", runningVisit);
                    intent.putExtra("com.ideaxen.client.visitapp.model.LOGIN", loginUser);
                    startActivityForResult(intent, 2);
                }
            }
        });
        linearLayout_today.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToVisitList("today");
            }
        });
        linearLayout_assigned.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToVisitList("assigned");
            }
        });
        linearLayout_canceled.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToVisitList("canceled");
            }
        });
        linearLayout_completed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToVisitList("completed");
            }
        });
        linearLayout_missed_week.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToVisitList("missed_week");
            }
        });
        linearLayout_missed_month.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToVisitList("missed_month");
            }
        });
        linearLayout_pending.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToVisitList("pending");
            }
        });
        linearLayout_adjourned.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToVisitList("adjourned");
            }
        });
    }

    public void showToast(String message){
        Toast.makeText(thisActivity, message, Toast.LENGTH_SHORT).show();
    }

    private void findViewsById(){
        linearLayout_running = findViewById(R.id.linear_layout_running_visit);
        linearLayout_today = findViewById(R.id.linear_layout_scheduled_today);
        linearLayout_assigned = findViewById(R.id.linear_layout_assigned);
        linearLayout_canceled = findViewById(R.id.linear_layout_canceled);
        linearLayout_completed = findViewById(R.id.linear_layout_completed);
        linearLayout_missed_week = findViewById(R.id.linear_layout_missed_last_week);
        linearLayout_missed_month = findViewById(R.id.linear_layout_missed_this_month);
        linearLayout_pending = findViewById(R.id.linear_layout_pending_visit);
        linearLayout_adjourned = findViewById(R.id.linear_layout_adjourned_visit);

        textView_running_name = findViewById(R.id.tv_running_visit_name);
        textView_running_address = findViewById(R.id.tv_running_visit_address);
        textView_today = findViewById(R.id.tv_scheduled_today);
        textView_assigned = findViewById(R.id.tv_assigned);
        textView_canceled = findViewById(R.id.tv_canceled);
        textView_completed = findViewById(R.id.tv_completed);
        textView_missed_week = findViewById(R.id.tv_missed_last_week);
        textView_missed_month = findViewById(R.id.tv_missed_this_month);
        textView_pending = findViewById(R.id.tv_pending);
        textView_adjourned = findViewById(R.id.tv_adjourned);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getDashboardData();
    }

    public void getDashboardData(){
        ApiManager apiManager = ApiManager.getInstance();
        dashboard_swipe_refresh.setRefreshing(true);
        if(!loginUser.getUser().getEmpId().equals("0")){
            apiManager.getDashboardData(loginUser.getToken(), loginUser.getUsername(), loginUser.getLoginType(), loginUser.getUser().getEmpId(), MyApplication.getAppUniqueID(getApplicationContext()), this);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actionbar_menu_dashboard, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int optionItem = item.getItemId();
        switch (optionItem) {
            case R.id.action_new_instant_visit:
                DialogController.openNewInstantVisitDialog(null, thisActivity);
                break;
            case R.id.action_visit_list:
                goToVisitList("all");
                break;
            case R.id.action_customer_contacts:
                Intent intent = new Intent(DashboardActivity.this, ContactPersonActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.action_customer_address:
                updateCustomerAddress();
                break;
            case R.id.action_logout:
                confirmLogoutDialog();
                break;
            default:
                break;
        }
        return true;
    }

    private void goToVisitList(String filter){
        String filterValue;
        String filterTitle;
        switch (filter) {
            case "today":
                filterValue = "20";
                filterTitle = "Today's assigned visits";
                break;
            case "assigned":
                filterValue = "5";
                filterTitle = "";
                break;
            case "canceled":
                filterValue = "15";
                filterTitle = "Canceled visits";
                break;
            case "completed":
                filterValue = "16";
                filterTitle = "Completed visits";
                break;
            case "missed_week":
                filterValue = "17";
                filterTitle = "This week missed visits";
                break;
            case "missed_month":
                filterValue = "18";
                filterTitle = "This month missed visits";
                break;
            case "pending":
                filterValue = "19";
                filterTitle = "Pending visits";
                break;
            case "adjourned":
                filterValue = "7";
                filterTitle = "Adjourned visits";
                break;
            default:
                filterValue = "0";
                filterTitle = "";
                break;
        }
        Intent intent = new Intent(thisContext, MyVisitsActivity.class);
        intent.putExtra("DASHBOARD_FILTER", filterValue);
        intent.putExtra("DASHBOARD_FILTER_TITLE", filterTitle);
        startActivity(intent);
        finish();
    }

    public void updateCustomerAddress(){
        AlertDialog.Builder updateCustomerDialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();

        View updateCustomerDialogView = inflater.inflate(R.layout.customer_address_dialog, null);
        updateCustomerDialogBuilder.setView(updateCustomerDialogView);

        final ListView updateCustomerCustList = updateCustomerDialogView.findViewById(R.id.custList);

        confirmDialog = updateCustomerDialogBuilder.create();

        ApiManager apiManager = ApiManager.getInstance();
        Call<List<CustomerInfo>> updateCust = apiManager.getCustomerAddresses(loginUser.getToken(), loginUser.getUsername(), loginUser.getLoginType(), loginUser.getUser().getEmpId(), MyApplication.getAppUniqueID(getApplicationContext()));
        updateCust.enqueue(new Callback<List<CustomerInfo>>() {
            @Override
            public void onResponse(Call<List<CustomerInfo>> call, Response<List<CustomerInfo>> response) {

                if (response.body() != null) {
                    List<CustomerInfo> responseData = response.body();
                    CustomerInfoAdapter customerInfoAdapter = new CustomerInfoAdapter(thisContext, responseData);
                    updateCustomerCustList.setAdapter(customerInfoAdapter);

                    updateCustomerCustList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            CustomerInfo customerInfo = (CustomerInfo) parent.getAdapter().getItem(position);
                            String cust_id = customerInfo.getId();
                            String cust_name = customerInfo.getCustomerName();
                            String cust_address = customerInfo.getCustomerAddress();
                            newAddress(cust_id, cust_name, cust_address);
                            confirmDialog.dismiss();
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<List<CustomerInfo>> call, Throwable t) {
                Log.d(TAG, "onFailure: " + t.getMessage());
            }
        });

        confirmDialog.show();
    }

    private void newAddress(final String id, final String name, String address){
        AlertDialog.Builder newAddressDialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();

        View updateCustomerDialogView = inflater.inflate(R.layout.new_cust_address, null);
        newAddressDialogBuilder.setView(updateCustomerDialogView);

        TextView tv_id = updateCustomerDialogView.findViewById(R.id.tv_id);
        TextView tv_name = updateCustomerDialogView.findViewById(R.id.tv_name);
        TextView tv_old_address = updateCustomerDialogView.findViewById(R.id.tv_old_address);
        final EditText et_new_address = updateCustomerDialogView.findViewById(R.id.et_new_address);

        Button btnNegative = updateCustomerDialogView.findViewById(R.id.btnNegative);
        Button btnPositive = updateCustomerDialogView.findViewById(R.id.btnPositive);

        final AlertDialog newAddressDialog = newAddressDialogBuilder.create();

        tv_id.setText(id);
        tv_name.setText(name);
        tv_old_address.setText(address);

        btnNegative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newAddressDialog.dismiss();
            }
        });
        btnPositive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String new_address = et_new_address.getText().toString();

                if(new_address.equals("")){
                    et_new_address.setError("Empty Address");
                }
                else{
                    ApiManager apiManager = ApiManager.getInstance();
                    Call<String> customerAddresses = apiManager.updateCustomerAddress(loginUser.getToken(), loginUser.getUsername(), loginUser.getLoginType(), loginUser.getUser().getEmpId(), MyApplication.getAppUniqueID(getApplicationContext()), id, name, new_address);
                    customerAddresses.enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(Call<String> call, Response<String> response) {
                            if (response.body() != null) {
                                Log.d(TAG, "onResponse: " + response.body());
                                if(response.body().equals("true")){
                                    showToast("New address updated!");
                                }
                                else {
                                    showToast("There was a problem");
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<String> call, Throwable t) {
                            Log.d(TAG, "onFailure: " + t.getMessage());
                        }
                    });
                    newAddressDialog.dismiss();
                }
            }
        });

        newAddressDialog.show();
    }

    public void confirmLogoutDialog(){
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



    @Override
    public void onRefresh() {
        getDashboardData();
    }

    @Override
    public void onResponse(Call<List<DashboardData>> call, Response<List<DashboardData>> response) {
        if (response.body() != null) {
            Log.d(TAG, "Dashboard Body: " + response.body().toString());
            dashboard_swipe_refresh.setRefreshing(false);
            DashboardData dashboardData = response.body().get(0);
            runningVisit = dashboardData.getVisit();
            if(runningVisit != null){
                MyApplication.isRunningVisit = runningVisit.getId();
                textView_running_address.setVisibility(View.VISIBLE);
                textView_running_name.setText(runningVisit.getCustomer());
                textView_running_address.setText(runningVisit.getAddress());
            }
            else{
                textView_running_name.setText("None");
                textView_running_address.setVisibility(View.GONE);
            }
            textView_today.setText(dashboardData.getToday());
            textView_assigned.setText(dashboardData.getAssigned());
            textView_canceled.setText(dashboardData.getCanceled());
            textView_completed.setText(dashboardData.getCompleted());
            textView_missed_week.setText(dashboardData.getMissedWeek());
            textView_missed_month.setText(dashboardData.getMissedMonth());
            textView_pending.setText(dashboardData.getPending());
            textView_adjourned.setText(dashboardData.getAdjourned());
        }
    }

    @Override
    public void onFailure(Call<List<DashboardData>> call, Throwable t) {
        dashboard_swipe_refresh.setRefreshing(false);
    }

    @Override
    public void onBackPressed() {
        confirmExitDialog();
    }
}
