package com.ideaxen.hr.ideasms;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Bundle;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.DatePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.provider.Settings;
import android.text.InputType;
import android.text.TextUtils;
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
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.internal.LinkedTreeMap;
import com.ideaxen.hr.ideasms.api.ApiManager;
import com.ideaxen.hr.ideasms.model.Visit;
import com.ideaxen.hr.ideasms.services.LocationSaver;
import com.ideaxen.hr.ideasms.model.CustomerInfo;
import com.ideaxen.hr.ideasms.model.LoVItem;
import com.ideaxen.hr.ideasms.model.Login;
import com.ideaxen.hr.ideasms.services.OfflineDataService;
import com.ideaxen.hr.ideasms.util.CustomerInfoAdapter;
import com.ideaxen.hr.ideasms.util.VisitItemAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import io.realm.Realm;
import io.realm.RealmResults;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyVisitsActivity extends AppCompatActivity implements Callback<List<Object>>, SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = "MyVisitsActivity";
    public static final String BROADCAST_ACTION = "com.ideaxen.broadcastreceiver";
    private static final int REQUEST_LOCATION_CODE = 101;

    Login loginUser;
    ListView myVisitList;
    VisitItemAdapter visitItemAdapter;
    List<Visit> visits;
    LinearLayout filterSection;

    EditText edtTextFromDate;
    EditText edtTextToDate;
    TextView visitListTitle;

    private DatePickerDialog fromDatePickerDialog;
    private DatePickerDialog toDatePickerDialog;

    private SimpleDateFormat dateFormatter;
    private Button btnNewInstantVisit;


    View dialogView;
    AlertDialog alertDialog;
    AlertDialog confirmDialog;

    String sCustomer = "";

    String fromDate = "";
    String toDate = "";
    boolean appliedFilter;

    protected String visitFilterPref;
    private String dashboardFilterTitle = "";

    public Context thisContext;
    public AppCompatActivity thisActivity;

    SwipeRefreshLayout mSwipeRefreshLayout;
    LocationSaver locationSaver;
    Intent autoLocatorServ;
    IntentFilter intentFilter;
    Intent offlineDataService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_visits);

        dateFormatter = new SimpleDateFormat("dd-MM-yyyy", Locale.US);

        // Get the application context
        thisContext = getApplicationContext();

        // Get the activity
        thisActivity = MyVisitsActivity.this;

        //Check if GPS is Enabled
        gpsEnabled();

        locationSaver = new LocationSaver();

        mSwipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        appliedFilter = false;

        findViewsById();

        setDateTimeField();

        cancelFilter(null);

        loginUser = MyApplication.isLoggedin();
        offlineDataService = new Intent(getApplicationContext(), OfflineDataService.class);

        intentFilter = new IntentFilter();
        intentFilter.addAction(BROADCAST_ACTION);

//        if(checkAndRequestPermissions()){
//            autoLocatorServ = new Intent(getApplicationContext(), AutoLocatorService.class);
//            startService(autoLocatorServ);
//        }

        FirebaseMessaging.getInstance().subscribeToTopic(loginUser.getUser().getUsername()).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.d(TAG, "Subscribed To Topic: " + loginUser.getUser().getUsername());
            }
        });

        if(getIntent().hasExtra("FILTER_MENU")){
            visitFilterPref = getIntent().getStringExtra("FILTER_MENU");
        }
        else if(getIntent().hasExtra("DASHBOARD_FILTER")){
            visitFilterPref = getIntent().getStringExtra("DASHBOARD_FILTER");
            if(getIntent().hasExtra("DASHBOARD_FILTER_TITLE")) {
                dashboardFilterTitle = getIntent().getStringExtra("DASHBOARD_FILTER_TITLE");
            }
        }
        else {
            visitFilterPref = MyApplication.getVisitPreference(getApplicationContext());
        }

        mSwipeRefreshLayout.setOnRefreshListener(this);

        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                getVisits();
            }
        });

//        locationSaver.startLocationProcess(thisContext, "0","Dashboard");

        myVisitList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Visit visit = (Visit) myVisitList.getItemAtPosition(position);
                moveToDetailView(visit, position, false);
            }
        });
    }

    public void showToast(String message){
        Toast.makeText(thisActivity, message, Toast.LENGTH_SHORT).show();
    }

    private void moveToDetailView(Visit visit, int position, boolean directReport){
        Intent intent = new Intent(thisActivity, VisitDetailActivity.class);
        intent.putExtra("com.ideaxen.client.visitapp.bool.DIRECT_REPORT", directReport);
        intent.putExtra("com.ideaxen.client.visitapp.bool.RECENTLY_VISITED", visitFilterPref.equals(thisContext.getResources().getString(R.string.FILTER_RECENTLY_VISITED)));
        intent.putExtra("com.ideaxen.client.visitapp.bool.ADJOURNED_VISIT", visitFilterPref.equals(thisContext.getResources().getString(R.string.FILTER_ADJOURNED_VISITS)));
        intent.putExtra("com.ideaxen.client.visitapp.int.POSITION_IN_LIST", position);
        intent.putExtra("com.ideaxen.client.visitapp.model.VISIT", visit);
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
                int positin_in_list = data.getIntExtra("com.ideaxen.client.visitapp.int.POSITION_IN_LIST", -1);
                if(last_action == R.integer.VISIT_STATUS_CANCELED||
                        last_action == R.integer.VISIT_STATUS_ADJOURNED ||
                        last_action == R.integer.VISIT_STATUS_CANCELED_BEFORE_START ||
                        last_action == R.integer.VISIT_STATUS_RESCHEDULED ||
                        last_action == R.integer.VISIT_STATUS_RESCHEDULED_BEFORE_START ||
                        last_action == R.integer.VISIT_STATUS_FINISHED){
                    visits.remove(positin_in_list);
                    visitItemAdapter.notifyDataSetChanged();
                    return;
                }
            }
        }
    }

    public void gpsEnabled(){
        LocationManager locationManager = (LocationManager) thisContext.getSystemService(LOCATION_SERVICE);
        if (locationManager != null){
            if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
                alertDialog.setTitle("GPS is not Enabled!")
                        .setMessage("Do you want to turn on GPS?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivityForResult(intent,0);
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).create().show();
            }
            else{
                Log.d(TAG, "gpsEnabled: GPS is ready");
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "isRunningVisit: " + MyApplication.isRunningVisit);
        if(MyApplication.isRunningVisit != null){
            btnNewInstantVisit.setVisibility(View.GONE);
        }
        else{
            btnNewInstantVisit.setVisibility(View.VISIBLE);
        }
        gpsEnabled();
        getVisits();
        stopService(offlineDataService);
        try {
            registerReceiver(myBroadCastReceiver, intentFilter);
            Log.d(TAG, "registerReceiver success:");
        } catch(IllegalArgumentException e) {
            e.printStackTrace();
            Log.d(TAG, "registerReceiver failed: " + e.getMessage());
        }
    }

    private BroadcastReceiver myBroadCastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.hasExtra("auto")){
                Log.d(TAG, "auto" + intent.getStringExtra("auto"));
                locationSaver.autoLocationProcess(thisContext);
            }
        }
    };

    @Override
    protected void onStop() {
        super.onStop();
        startService(offlineDataService);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(myBroadCastReceiver);
        } catch(IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    public void getVisits(){
        if(!isNetworkAvailable()){
//      Offline Starts -----------------------------------
            View parentLayout = findViewById(android.R.id.content);
            final Snackbar snackbar = Snackbar.make(parentLayout, "No Internet, showing local data", Snackbar.LENGTH_LONG);
            snackbar.setAction("OK", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    snackbar.dismiss();
                }
            }).setActionTextColor(getResources().getColor(android.R.color.holo_red_light)).show();
            Realm realm = Realm.getDefaultInstance();
            RealmResults<Visit> results = realm.where(Visit.class).findAllAsync();
            results.load();
//            Log.d(TAG, "RealmResults: " + results.toString());
            List<Visit> offlineVisits = new ArrayList<>();
            for(Visit visit:results){
                offlineVisits.add(new Visit(visit.getId(), visit.getCustomer(), visit.getAddress(),
                        visit.getContactPerson(), visit.getContactNumber(), visit.getVisitDate(),
                        visit.getVisitPriority(), visit.getIsInstant(), visit.getVisitStatus()));
            }
//            Log.d(TAG, "SHOWING FROM local");
            visitItemAdapter = new VisitItemAdapter(this, offlineVisits);
            myVisitList.setAdapter(visitItemAdapter);

            listTitle();
//      Offline Ends -----------------------------------
        }
        else{
            ApiManager apiManager = ApiManager.getInstance();
            mSwipeRefreshLayout.setRefreshing(true);
            if(appliedFilter) {
                apiManager.getVisits(loginUser.getToken(), loginUser.getUsername(), loginUser.getLoginType(), loginUser.getUser().getEmpId(), MyApplication.getAppUniqueID(getApplicationContext()), visitFilterPref, fromDate, toDate, this);
            }
            else {
                apiManager.getVisits(loginUser.getToken(), loginUser.getUsername(), loginUser.getLoginType(), loginUser.getUser().getEmpId(), MyApplication.getAppUniqueID(getApplicationContext()), visitFilterPref, "", "", this);
            }
        }
    }

    private void findViewsById() {

        visitListTitle = findViewById(R.id.txtViewVisitListTitle);

        myVisitList = findViewById(R.id.myVisitList);
        filterSection = findViewById(R.id.filterSection);

        edtTextFromDate = findViewById(R.id.edtTextFromDate);
        edtTextToDate = findViewById(R.id.edtTextToDate);

        btnNewInstantVisit = findViewById(R.id.btnNewInstantVisit);

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
        getMenuInflater().inflate(R.menu.actionbar_menu_visitor, menu);
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
            case R.id.action_nextday_visits:
                filterVisitsByMenu(R.string.FILTER_NEXT_DAY_VISITS);
                break;
            case R.id.action_week_visits:
                filterVisitsByMenu(R.string.FILTER_WEEKS_VISITS);
                break;
            case R.id.action_month_visits:
                filterVisitsByMenu(R.string.FILTER_MONTHS_VISITS);
                break;
            case R.id.action_completed_visits:
                filterVisitsByMenu(R.string.FILTER_COMPLETED_VISITS);
                break;
            case R.id.action_past_visits:
                filterVisitsByMenu(R.string.FILTER_PAST_VISITS);
                break;
            case R.id.action_all_visits:
                filterVisitsByMenu(R.string.FILTER_ALL_VISITS);
                break;
            case R.id.action_recently_visited:
                filterVisitsByMenu(R.string.FILTER_RECENTLY_VISITED);
                break;
            case R.id.action_adjourned_visits:
                filterVisitsByMenu(R.string.FILTER_ADJOURNED_VISITS);
                break;
            case R.id.action_reload_lovs:
                reloadLoVs();
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
//        String lastFilter = MyApplication.getVisitPreference(getApplicationContext());
        String filterValue = res.getString(filter);
//        if(!lastFilter.equals(filterValue)){
            visitFilterPref = MyApplication.setVisitPreference(getApplicationContext(), filterValue);
            getVisits();
//        }
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
        getVisits();
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

    public void createNewInstantVisitDialog(View v) {
        DialogController.openNewInstantVisitDialog(v, thisActivity);
    }

    public void newCustomerDialog(View view) {
        AlertDialog.Builder custDialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater custInflater = this.getLayoutInflater();
        final View custDialogView = custInflater.inflate(R.layout.new_customer_dialog, null);
        custDialogBuilder.setView(custDialogView);

        Button btnNegative = custDialogView.findViewById(R.id.btnNegative);
        Button btnPositive = custDialogView.findViewById(R.id.btnPositive);
        final AlertDialog custDialog = custDialogBuilder.create();

        btnNegative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                custDialog.dismiss();
            }
        });
        btnPositive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView editTextCustName = custDialogView.findViewById(R.id.editTextCustName);
                TextView editTextCustAddress = custDialogView.findViewById(R.id.editTextCustAddress);
                TextView editTextConName = custDialogView.findViewById(R.id.editTextConName);
                TextView editTextConNumber = custDialogView.findViewById(R.id.editTextConNumber);

                String custName = editTextCustName.getText().toString();
                if(TextUtils.isEmpty(custName)) {
                    editTextCustName.setError("No Customer Name!");
                    return;
                }
                String custAddress = editTextCustAddress.getText().toString();
                if(TextUtils.isEmpty(custAddress)) {
                    editTextCustAddress.setError("No Customer Address!");
                    return;
                }
                String conName = editTextConName.getText().toString();
                if(TextUtils.isEmpty(custName)) {
                    editTextCustName.setError("No Contact Person!");
                    return;
                }
                String conNumber = editTextConNumber.getText().toString();
                if(TextUtils.isEmpty(custAddress)) {
                    editTextCustAddress.setError("No Contact Number!");
                    return;
                }

                custSuggestionDialog(custName, custAddress, conName, conNumber, custDialog);
            }
        });
        custDialog.show();
    }

    public void custSuggestionDialog(final String customer_name, final String customer_address,
                                     final String contact_name, final String contact_number, final AlertDialog custDialog){

        AlertDialog.Builder suggestionDialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater suggestionInflater = this.getLayoutInflater();
        View suggestionDialogView = suggestionInflater.inflate(R.layout.cust_info_dialog, null);
        suggestionDialogBuilder.setView(suggestionDialogView);

        final ListView suggestedCustList = suggestionDialogView.findViewById(R.id.suggestedCustList);
        Button btnAddCust = suggestionDialogView.findViewById(R.id.btnAddCust);
        final AlertDialog suggestionDialog = suggestionDialogBuilder.create();

        ApiManager apiManager = ApiManager.getInstance();
        Call<List<CustomerInfo>> suggestions = apiManager.getCustomerSuggestions(loginUser.getToken(), loginUser.getUsername(), loginUser.getLoginType(), loginUser.getUser().getEmpId(), MyApplication.getAppUniqueID(getApplicationContext()), customer_name,this);
        suggestions.enqueue(new Callback<List<CustomerInfo>>() {
            @Override
            public void onResponse(Call<List<CustomerInfo>> call, Response<List<CustomerInfo>> response) {
                List<CustomerInfo> responseData = response.body();
                if (responseData != null) {
                    if(responseData.toString().equals("[]")){
                        createCustomer(customer_name, customer_address, contact_name, contact_number, custDialog, suggestionDialog);
                    }
                    else{
                        reloadLoVs();
                        Log.d(TAG, "onResponse: " + responseData);

                        CustomerInfoAdapter customerInfoAdapter = new CustomerInfoAdapter(thisContext, responseData);
                        suggestedCustList.setAdapter(customerInfoAdapter);

                        suggestedCustList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                CustomerInfo customerInfo = (CustomerInfo) parent.getAdapter().getItem(position);
                                String name = customerInfo.getCustomerName();
                                setSelectedCustomer(name, custDialog, suggestionDialog);
                            }
                        });
//                        locationSaver.startLocationProcess(thisContext, "0","New Customer Created");
                    }
                }
            }

            @Override
            public void onFailure(Call<List<CustomerInfo>> call, Throwable t) {
                Log.d(TAG, "On Suggestion Failure: " + t.toString());
            }
        });

        btnAddCust.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createCustomer(customer_name, customer_address, contact_name, contact_number, custDialog, suggestionDialog);
            }
        });

        suggestionDialog.show();
    }

    public void createCustomer(final String customer_name, String customer_address, String contact_name, String contact_number,
                               final AlertDialog custDialog, final AlertDialog suggestionDialog){
        ApiManager apiManager = ApiManager.getInstance();
        Call<LoVItem> customerInfo = apiManager.createCustomer(loginUser.getToken(), loginUser.getUsername(), loginUser.getUser().getEmpId(), MyApplication.getAppUniqueID(getApplicationContext()),
                customer_name, customer_address, contact_name, contact_number);
        customerInfo.enqueue(new Callback<LoVItem>() {
            @Override
            public void onResponse(Call<LoVItem> call, Response<LoVItem> response) {
                if (response.body() != null) {
                    LoVItem lov_items = response.body();
                    Realm realm = Realm.getDefaultInstance();
                    realm.beginTransaction();
                    realm.insert(lov_items);
                    realm.commitTransaction();
                    setSelectedCustomer(customer_name, custDialog, suggestionDialog);
                }
            }

            @Override
            public void onFailure(Call<LoVItem> call, Throwable t) {
                Log.d(TAG, "On Create Failure: " + t.getMessage());
            }
        });
    }

    public void setSelectedCustomer(String name, AlertDialog custDialog, AlertDialog suggestionDialog){

        custDialog.dismiss();
        suggestionDialog.dismiss();
        alertDialog.dismiss();
        reloadLoVs();
        sCustomer = name;
//        openNewInstantVisitDialog();
        DialogController.openNewInstantVisitDialog(null, thisActivity);
    }

    public void reloadLoVs(){
        ApiManager apiManager = ApiManager.getInstance();

        Call<List<LoVItem>> lovItems = apiManager.getLoVItems(loginUser.getToken(), loginUser.getUsername(), loginUser.getUser().getEmpId(), MyApplication.getAppUniqueID(getApplicationContext()));
        lovItems.enqueue(new Callback<List<LoVItem>>() {
            @Override
            public void onResponse(Call<List<LoVItem>> call, Response<List<LoVItem>> response) {
                MyApplication.removeAllLovItems();
                if (response.body() != null) {
                    Log.d(TAG, "onResponse: " + response.body().toString());
                    List<LoVItem> lov_items = response.body();
                    Realm realm = Realm.getDefaultInstance();
                    realm.beginTransaction();
                    realm.insert(lov_items);
                    realm.commitTransaction();
                }
            }

            @Override
            public void onFailure(Call<List<LoVItem>> call, Throwable t) {
                Log.d("LoV failed: ", t.getMessage());
            }
        });
    }

    public void cancelInstantVisitDialog(View view) {
        alertDialog.dismiss();
    }
    public void createInstantVisit(View view) {
        DialogController.openNewInstantVisitDialog(view, thisActivity);
//        Spinner spinnerInstantVisitPurpose = (Spinner) dialogView.findViewById(R.id.spinnerInstantVisitPurpose);
//        Toast.makeText(thisActivity,"You clicked YES button with Reason: "+spinnerInstantVisitPurpose.getSelectedItem().toString(),Toast.LENGTH_LONG).show();
//        alertDialog.dismiss();
    }

    private void listTitle(){
        Resources res = getResources();
        if(!dashboardFilterTitle.equals("")){
            visitListTitle.setText(dashboardFilterTitle);
        }
        else{
            if (appliedFilter){
                visitListTitle.setText(res.getString(R.string.custom_filter_visit_title)+" "+fromDate+" to "+toDate);
            }
            else if (visitFilterPref.equals(res.getString(R.string.FILTER_TODAY_VISITS))){
                visitListTitle.setText(res.getString(R.string.todays_visit_title));
            }
            else if (visitFilterPref.equals(res.getString(R.string.FILTER_NEXT_DAY_VISITS))){
                visitListTitle.setText(res.getString(R.string.nextdays_visit_title));
            }
            else if (visitFilterPref.equals(res.getString(R.string.FILTER_WEEKS_VISITS))){
                visitListTitle.setText(res.getString(R.string.weeks_visit_title));
            }
            else if (visitFilterPref.equals(res.getString(R.string.FILTER_MONTHS_VISITS))){
                visitListTitle.setText(res.getString(R.string.months_visit_title));
            }
            else if (visitFilterPref.equals(res.getString(R.string.FILTER_ALL_VISITS))){
                visitListTitle.setText(res.getString(R.string.all_visit_title));
            }
            else if (visitFilterPref.equals(res.getString(R.string.FILTER_PAST_VISITS))){
                visitListTitle.setText(res.getString(R.string.past_visit_title));
            }
            else if (visitFilterPref.equals(res.getString(R.string.FILTER_RECENTLY_VISITED))){
                visitListTitle.setText(res.getString(R.string.recent_visit_title));
            }
            else if (visitFilterPref.equals(res.getString(R.string.FILTER_ADJOURNED_VISITS))){
                visitListTitle.setText(res.getString(R.string.adjourned_visit_title));
            }
            else{
                visitListTitle.setText(res.getString(R.string.all_visit_title));
            }
        }
        if(MyApplication.isRunningVisit != null){
            btnNewInstantVisit.setVisibility(View.GONE);
        }
        else{
            btnNewInstantVisit.setVisibility(View.VISIBLE);
        }
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onRefresh() {
        getVisits();
    }

    @Override
    public void onResponse(Call<List<Object>> call, Response<List<Object>> response) {
        if(response.body() != null) {
            Log.d(TAG, "Visit List Response: " + response.body().toString());
            MyApplication.isRunningVisit = (String) response.body().get(0);
            List<LinkedTreeMap> tempVisits = (List<LinkedTreeMap>) response.body().get(1);
            visits = new ArrayList<>();
            for (LinkedTreeMap ltm : tempVisits) {
                visits.add(
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

            visitItemAdapter = new VisitItemAdapter(this, visits);
            myVisitList.setAdapter(visitItemAdapter);
        }
        listTitle();
    }

    @Override
    public void onFailure(Call<List<Object>> call, Throwable t) {
        mSwipeRefreshLayout.setRefreshing(false);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = null;
        if (connectivityManager != null) {
            activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        }
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    //  Location Permission ->
    private boolean checkAndRequestPermissions() {
        int permissionCoarseLocation = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);
        int permissionFineLocation = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int permissionPhoneCall = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CALL_PHONE);
        List<String> listPermissionsNeeded = new ArrayList<>();
        if (permissionFineLocation != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (permissionCoarseLocation != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        if (permissionPhoneCall != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CALL_PHONE);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),REQUEST_LOCATION_CODE);
            return false;
        }
        return true;
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION_CODE: {
                Map<String, Integer> perms = new HashMap<>();
                perms.put(Manifest.permission.ACCESS_COARSE_LOCATION, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.ACCESS_FINE_LOCATION, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.CALL_PHONE, PackageManager.PERMISSION_GRANTED);
                if (grantResults.length > 0) {
                    for (int i = 0; i < permissions.length; i++)
                        perms.put(permissions[i], grantResults[i]);
                    if (perms.get(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                            && perms.get(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                            && perms.get(Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                        showToast("Permission granted");
                    }
                    else {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION) || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION) || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CALL_PHONE)) {
                            showDialogOK("Location and Call Permission required for this app", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    switch (which) {
                                        case DialogInterface.BUTTON_POSITIVE:
                                            checkAndRequestPermissions();
                                            break;
                                        case DialogInterface.BUTTON_NEGATIVE:
                                            break;
                                    }
                                }
                            });
                        }
                        else {
                            showToast("Permissions Denied");
                        }
                    }
                }
            }
        }

    }

    private void showDialogOK(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", okListener)
                .create()
                .show();
    }

//  -> Location Permission


    @Override
    public void onBackPressed() {
        startActivity(new Intent(thisContext, DashboardActivity.class));
        finish();
    }
}
