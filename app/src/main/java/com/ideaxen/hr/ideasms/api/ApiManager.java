package com.ideaxen.hr.ideasms.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ideaxen.hr.ideasms.model.ApprovalDetailItem;
import com.ideaxen.hr.ideasms.model.ContactPerson;
import com.ideaxen.hr.ideasms.model.CustomerInfo;
import com.ideaxen.hr.ideasms.model.DashboardData;
import com.ideaxen.hr.ideasms.model.DataParam;
import com.ideaxen.hr.ideasms.model.InstantVisit;
import com.ideaxen.hr.ideasms.model.LatLon;
import com.ideaxen.hr.ideasms.model.LoVItem;
import com.ideaxen.hr.ideasms.model.Login;
import com.ideaxen.hr.ideasms.model.Visit;
import com.ideaxen.hr.ideasms.model.VisitDetailItem;
import com.ideaxen.hr.ideasms.model.VisitReport;
import com.ideaxen.hr.ideasms.model.VisitUpdateData;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiManager {
    private static final String BASE_URL = "http://192.168.0.20/ideaxen/ierp-apps/VisitApp/Web/index.php/";
    private static ApiManager apiManager = null;
    private final MapApi mapApiService;

    private ApiManager() {
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(client)
                .build();

        mapApiService = retrofit.create(MapApi.class);

    }

    public static ApiManager getInstance() {
        if (apiManager == null) {
            apiManager = new ApiManager();
        }
        return apiManager;
    }

    public void login(String username, String password, String loginType, String source, Callback<Login> callback) {
        Call<Login> loginInfo = null;
        try {
            loginInfo = mapApiService.login(URLEncoder.encode(username, "UTF-8"), password, loginType, source);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        loginInfo.enqueue(callback);
    }

    public void getVisits(String appKey, String userId, String loginType, String empId, String instanceId, String durationType, String startDate, String endDate, Callback<List<Object>> callback) {
        Call<List<Object>> visits = mapApiService.getVisits(appKey, userId, loginType, empId, instanceId, durationType, startDate, endDate);
        visits.enqueue(callback);
    }

    public Call<List<Visit>> getOfflineData(String appKey, String userId, String loginType, String empId, String instanceId) {
        Call<List<Visit>> visitItems = mapApiService.getOfflineData(appKey, userId, loginType, empId, instanceId);
        return visitItems;
    }

    public void getApprovals(String appKey, String userId, String loginType, String empId, String instanceId, String durationType, String startDate, String endDate, Callback<List<Object>> callback) {
        Call<List<Object>> visits = mapApiService.getApprovals(appKey, userId, loginType, empId, instanceId, durationType, startDate, endDate);
        visits.enqueue(callback);
    }
    public void getVisitDetail(String appKey, String userId, String empId, String instanceId, String visitId, Callback<List<VisitDetailItem>> callback) {
        Call<List<VisitDetailItem>> visits = mapApiService.getVisitDetail(appKey, userId, empId, instanceId, visitId);
        visits.enqueue(callback);
    }
    public void getApprovalDetail(String appKey, String userId, String empId, String instanceId, String visitId, Callback<List<ApprovalDetailItem>> callback) {
        Call<List<ApprovalDetailItem>> visits = mapApiService.getApprovalDetail(appKey, userId, empId, instanceId, visitId);
        visits.enqueue(callback);
    }
    public Call<VisitUpdateData> getDeclineUpdate(String appKey, String userId, String empId, String instanceId, String visitId) {
        Call<VisitUpdateData> visits = mapApiService.getDeclineUpdate(appKey, userId, empId, instanceId, visitId);
        return visits;
    }
    public Call<VisitUpdateData> getApprovalUpdate(String appKey, String userId, String empId, String instanceId, String visitId, String visitPriority) {
        Call<VisitUpdateData> visits = mapApiService.getApprovalUpdate(appKey, userId, empId, instanceId, visitId, visitPriority);
        return visits;
    }

    public Call<List<String>> updateVisitReport(String appKey, String userId, String empId, String instanceId,
                                                String visitId, String visitStatus, String msg1, String msg2, String msg3, String msg4) {
        DataParam dataParam = new DataParam(visitId, visitStatus, msg1, msg2, msg3, msg4);
        Call<List<String>> visits = mapApiService.updateVisitReport(appKey, userId, empId, instanceId, dataParam);
        return visits;
    }
    public Call<VisitReport> getVisitReport(String appKey, String userId, String empId, String instanceId, String visitId) {
        Call<VisitReport> visits = mapApiService.getVisitReport(appKey, userId, empId, instanceId, visitId);
        return visits;
    }
    public Call<List<String>> createInstantVisit(String appKey, String userId, String empId, String instanceId,
                                                 String customerId, String customerName, String reportingId, String reportingName,
                                                 String reason, String from, String instantVisitAfterAnother) {
        InstantVisit instantVisit = new InstantVisit(customerId, customerName, reportingId, reportingName,reason, from, instantVisitAfterAnother);
        Call<List<String>>  visitDetails = mapApiService.createInstantVisit(appKey, userId, empId, instanceId, instantVisit);

        return visitDetails;
    }
    public Call<List<LoVItem>> getLoVItems(String appKey, String username, String empId, String source) {
        Call<List<LoVItem>> lovItems = mapApiService.getLoVItems(appKey, username, empId, source);
        return lovItems;
    }

    public Call<List<CustomerInfo>> getCustomerSuggestions(String appKey, String userId, String loginType, String empId, String instanceId, String customer_name, Callback<List<Object>> callback) {
        Call<List<CustomerInfo>> suggestion = mapApiService.getCustomerSuggestions(appKey, userId, loginType, empId, instanceId, customer_name);
        return suggestion;
    }

    public Call<List<ContactPerson>> getContactPersons(String appKey, String userId, String loginType, String empId, String instanceId) {
        Call<List<ContactPerson>> suggestion = mapApiService.getContactPersons(appKey, userId, loginType, empId, instanceId);
        return suggestion;
    }

    public Call<LoVItem> createCustomer(String appKey, String userId, String empId, String instanceId,
                                                 String custName, String custAddress, String conName, String conNumber) {
        Call<LoVItem>  customerInfo = mapApiService.createCustomer(appKey, userId, empId, instanceId,
                custName, custAddress, conName,conNumber);
        return customerInfo;
    }

    public Call<LatLon> eventLocation(String appKey, String userId, String empId, String instanceId,
                                            String visId, String event, String lat, String lon) {
        LatLon latLon = new LatLon(event, lat, lon, visId, empId);
        Call<LatLon> eventInfo = mapApiService.eventLocation(appKey, userId, empId, instanceId, latLon);
        return eventInfo;
    }

    public void getDashboardData(String appKey, String username, String loginType, String empId, String instanceId, Callback<List<DashboardData>> callback) {
        Call<List<DashboardData>> dashboardData = mapApiService.getDashboardData(appKey, username, loginType, empId, instanceId);
        dashboardData.enqueue(callback);
    }

    public Call<List<CustomerInfo>> getCustomerAddresses(String appKey, String userId, String loginType, String empId, String instanceId) {
        Call<List<CustomerInfo>> addresses = mapApiService.getCustomerAddresses(appKey, userId, loginType, empId, instanceId);
        return addresses;
    }

    public Call<String> updateCustomerAddress(String appKey, String userId, String loginType, String empId, String instanceId,
                                      String id, String name, String address) {
        CustomerInfo customerInfo = new CustomerInfo(id, name, address);
        Call<String> eventInfo = mapApiService.updateCustomerAddress(appKey, userId, loginType, empId, instanceId, customerInfo);
        return eventInfo;
    }

}