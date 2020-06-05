package com.ideaxen.hr.ideasms.api;

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

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface MapApi {
//    String BASE_URL = "http://192.168.0.10/cirestapi/index.php/";
//    String BASE_URL = "http://192.168.0.102/cirestapi/index.php/";

    @Headers({
            "Client-Service: frontend-client",
            "Auth-Key: simplerestapi"
    })
    @FormUrlEncoded
    @POST("apiauth/login")
    Call <Login> login(@Field("username") String username,
                       @Field("password") String password,
                       @Field("loginType") String loginType,
                       @Field("source") String source);

    @Headers({
            "Client-Service: frontend-client",
            "Auth-Key: simplerestapi",
            "Content-Type: application/x-www-form-urlencoded"
    })
    @GET("visit/index/{durationType}/{startDate}/{endDate}")
    Call <List<Object>> getVisits(@Header("Authorization") String key,
                                  @Header("User-ID") String id,
                                  @Header("Login-Type") String loginType,
                                  @Header("Emp-ID") String empId,
                                  @Header("Dev-Source") String instanceId,
                                  @Path("durationType") String durationType,
                                  @Path("startDate") String startDate,
                                  @Path("endDate") String endDate);
    @Headers({
            "Client-Service: frontend-client",
            "Auth-Key: simplerestapi",
            "Content-Type: application/x-www-form-urlencoded"
    })
    @GET("util/get_offline_data")
    Call <List<Visit>> getOfflineData(@Header("Authorization") String key,
                                      @Header("User-ID") String id,
                                      @Header("Login-Type") String loginType,
                                      @Header("Emp-ID") String empId,
                                      @Header("Dev-Source") String instanceId);

    @Headers({
            "Client-Service: frontend-client",
            "Auth-Key: simplerestapi",
            "Content-Type: application/x-www-form-urlencoded"
    })
    @GET("visit/detail/{visitId}")
    Call <List<VisitDetailItem>> getVisitDetail(@Header("Authorization") String key,
                                                @Header("User-ID") String id,
                                                @Header("Emp-ID") String empId,
                                                @Header("Dev-Source") String instanceId,
                                                @Path("visitId") String visitId);

    @Headers({
            "Client-Service: frontend-client",
            "Auth-Key: simplerestapi",
            "Content-Type: application/x-www-form-urlencoded"
    })
    @GET("approval/detail/{visitId}")
    Call <List<ApprovalDetailItem>> getApprovalDetail(@Header("Authorization") String key,
                                                      @Header("User-ID") String id,
                                                      @Header("Emp-ID") String empId,
                                                      @Header("Dev-Source") String instanceId,
                                                      @Path("visitId") String visitId);
    @Headers({
            "Client-Service: frontend-client",
            "Auth-Key: simplerestapi",
            "Content-Type: application/x-www-form-urlencoded"
    })

    @PUT("visit/update_report")
    Call <List<String>> updateVisitReport(@Header("Authorization") String key,
                                          @Header("User-ID") String id,
                                          @Header("Emp-ID") String empId,
                                          @Header("Dev-Source") String instanceId,
                                          @Body DataParam body
    );
    @Headers({
            "Client-Service: frontend-client",
            "Auth-Key: simplerestapi",
            "Content-Type: application/x-www-form-urlencoded"
    })
    @GET("visit/get_report/{visitId}")
    Call <VisitReport> getVisitReport(@Header("Authorization") String key,
                                      @Header("User-ID") String id,
                                      @Header("Emp-ID") String empId,
                                      @Header("Dev-Source") String instanceId,
                                      @Path("visitId") String visitId);
    @Headers({
            "Client-Service: frontend-client",
            "Auth-Key: simplerestapi",
            "Content-Type: application/x-www-form-urlencoded"
    })
//    @FormUrlEncoded
    @PUT("visit/new_visit")
    Call<List<String>> createInstantVisit(@Header("Authorization") String key,
                                          @Header("User-ID") String id,
                                          @Header("Emp-ID") String empId,
                                          @Header("Dev-Source") String instanceId,
                                          @Body InstantVisit body);

    @Headers({
            "Client-Service: frontend-client",
            "Auth-Key: simplerestapi",
            "Content-Type: application/json"
    })
    @GET("util/get_lovs")
    Call <List<LoVItem>> getLoVItems(@Header("Authorization") String key,
                                     @Header("Username") String username,
                                     @Header("Emp-ID") String empId,
                                     @Header("Dev-Source") String source);

    //username=admin&password=Admin123$
    @Headers({
            "Client-Service: frontend-client",
            "Auth-Key: simplerestapi",
            "Content-Type: application/x-www-form-urlencoded"
    })
    @GET("approval/index/{durationType}/{startDate}/{endDate}")
    Call <List<Object>> getApprovals(@Header("Authorization") String key,
                                     @Header("User-ID") String id,
                                     @Header("Login-Type") String loginType,
                                     @Header("Emp-ID") String empId,
                                     @Header("Dev-Source") String instanceId,
                                     @Path("durationType") String durationType,
                                     @Path("startDate") String startDate,
                                     @Path("endDate") String endDate);
    @Headers({
            "Client-Service: frontend-client",
            "Auth-Key: simplerestapi",
            "Content-Type: application/x-www-form-urlencoded"
    })
    @PUT("approval/decline_visit_update/{visitId}")
    Call <VisitUpdateData> getDeclineUpdate(@Header("Authorization") String key,
                                         @Header("User-ID") String id,
                                         @Header("Emp-ID") String empId,
                                         @Header("Dev-Source") String instanceId,
                                         @Path("visitId") String visitId);
    @Headers({
            "Client-Service: frontend-client",
            "Auth-Key: simplerestapi",
            "Content-Type: application/x-www-form-urlencoded"
    })
    @PUT("approval/approve_visit_update/{visitId}/{visitPriority}")
    Call <VisitUpdateData> getApprovalUpdate(@Header("Authorization") String key,
                                             @Header("User-ID") String id,
                                             @Header("Emp-ID") String empId,
                                             @Header("Dev-Source") String instanceId,
                                             @Path("visitId") String visitId,
                                             @Path("visitPriority") String visitPriority);

    @Headers({
            "Client-Service: frontend-client",
            "Auth-Key: simplerestapi",
            "Content-Type: application/x-www-form-urlencoded"
    })
    @GET("visit/customer_suggestions/{customer_name}")
    Call <List<CustomerInfo>> getCustomerSuggestions(@Header("Authorization") String key,
                                                     @Header("User-ID") String id,
                                                     @Header("Login-Type") String loginType,
                                                     @Header("Emp-ID") String empId,
                                                     @Header("Dev-Source") String instanceId,
                                                     @Path("customer_name") String customer_name);
    @Headers({
            "Client-Service: frontend-client",
            "Auth-Key: simplerestapi",
            "Content-Type: application/x-www-form-urlencoded"
    })
    @GET("visit/get_contact_persons")
    Call <List<ContactPerson>> getContactPersons(@Header("Authorization") String key,
                                                 @Header("User-ID") String id,
                                                 @Header("Login-Type") String loginType,
                                                 @Header("Emp-ID") String empId,
                                                 @Header("Dev-Source") String instanceId);
    @Headers({
            "Client-Service: frontend-client",
            "Auth-Key: simplerestapi",
            "Content-Type: application/x-www-form-urlencoded"
    })
//    @FormUrlEncoded
    @PUT("visit/create_customer/{custName}/{custAddress}/{conName}/{conNumber}")
    Call<LoVItem> createCustomer(@Header("Authorization") String key,
                                 @Header("User-ID") String id,
                                 @Header("Emp-ID") String empId,
                                 @Header("Dev-Source") String instanceId,
                                 @Path("custName") String custName,
                                 @Path("custAddress") String custAddress,
                                 @Path("conName") String conName,
                                 @Path("conNumber") String conNumber);
    @Headers({
            "Client-Service: frontend-client",
            "Auth-Key: simplerestapi",
            "Content-Type: application/x-www-form-urlencoded"
    })

    @PUT("util/save_event_location")
    Call<LatLon> eventLocation(@Header("Authorization") String key,
                               @Header("User-ID") String id,
                               @Header("Emp-ID") String empId,
                               @Header("Dev-Source") String instanceId,
                               @Body LatLon latLon);
    @Headers({
            "Client-Service: frontend-client",
            "Auth-Key: simplerestapi",
            "Content-Type: application/x-www-form-urlencoded"
    })
    @GET("visit/get_dashboard_data")
    Call <List<DashboardData>> getDashboardData(@Header("Authorization") String key,
                                                @Header("Username") String username,
                                                @Header("Login-Type") String loginType,
                                                @Header("Emp-ID") String empId,
                                                @Header("Dev-Source") String instanceId);

    @Headers({
            "Client-Service: frontend-client",
            "Auth-Key: simplerestapi",
            "Content-Type: application/x-www-form-urlencoded"
    })
    @GET("visit/get_customer_addresses")
    Call <List<CustomerInfo>> getCustomerAddresses(@Header("Authorization") String key,
                                                   @Header("User-ID") String id,
                                                   @Header("Login-Type") String loginType,
                                                   @Header("Emp-ID") String empId,
                                                   @Header("Dev-Source") String instanceId);
    @Headers({
            "Client-Service: frontend-client",
            "Auth-Key: simplerestapi",
            "Content-Type: application/x-www-form-urlencoded"
    })

    @PUT("visit/update_customer_address")
    Call<String> updateCustomerAddress(@Header("Authorization") String key,
                                       @Header("User-ID") String id,
                                       @Header("Login-Type") String loginType,
                                       @Header("Emp-ID") String empId,
                                       @Header("Dev-Source") String instanceId,
                                       @Body CustomerInfo customerInfo);

}
