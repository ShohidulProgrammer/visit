package com.ideaxen.hr.ideasms;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import com.ideaxen.hr.ideasms.api.ApiManager;
import com.ideaxen.hr.ideasms.model.ContactPerson;
import com.ideaxen.hr.ideasms.model.Login;
import com.ideaxen.hr.ideasms.util.ContactPersonAdapter;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ContactPersonActivity extends AppCompatActivity {

    private static final String TAG = "ContactPersonActivity";
    public Context thisContext;
    public Activity thisActivity;

    ContactPersonAdapter contactPersonAdapter;
    Login loginUser;

    ListView cust_contact_list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_person);

        thisContext = getApplicationContext();
        thisActivity = ContactPersonActivity.this;

        loginUser = MyApplication.isLoggedin();

        cust_contact_list = findViewById(R.id.cust_contact_list);

        ApiManager apiManager = ApiManager.getInstance();
        Call<List<ContactPerson>> contactsCall = apiManager.getContactPersons(loginUser.getToken(), loginUser.getUsername(), loginUser.getLoginType(), loginUser.getUser().getEmpId(), MyApplication.getAppUniqueID(getApplicationContext()));
        contactsCall.enqueue(new Callback<List<ContactPerson>>() {
            @Override
            public void onResponse(Call<List<ContactPerson>> call, Response<List<ContactPerson>> response) {
                if (response.body() != null) {
                    List<ContactPerson> contactPersonList = response.body();
                    Log.d(TAG, "onResponse: " + contactPersonList.toString());

                    contactPersonAdapter = new ContactPersonAdapter(thisContext, contactPersonList);
                    cust_contact_list.setAdapter(contactPersonAdapter);
                }
            }

            @Override
            public void onFailure(Call<List<ContactPerson>> call, Throwable t) {
                Log.d(TAG, "onFailure: " + t.getMessage());
            }
        });

    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(thisContext, DashboardActivity.class));
        finish();
    }

}
