package com.ideaxen.hr.ideasms;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.ideaxen.hr.ideasms.api.ApiManager;
import com.ideaxen.hr.ideasms.model.LoVItem;
import com.ideaxen.hr.ideasms.model.Login;
import com.ideaxen.hr.ideasms.services.LocationSaver;

import java.io.IOException;
import java.util.List;

import io.realm.Realm;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity implements Callback<Login> {

    private static final String TAG = "LoginActivity";

    EditText edtTxtUsername;
    EditText edtTxtPassword;

    private String loginType;
    LocationSaver locationSaver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        locationSaver = new LocationSaver();

        edtTxtUsername = findViewById(R.id.edtTxtUsername);
        edtTxtPassword = findViewById(R.id.edtTxtPassword);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Login loginUser = MyApplication.isLoggedin();

        if(loginUser != null && loginUser.getUser() != null){
            if(loginUser.getLoginType().equals("Visitor")){
                moveToDashboardActivity();
            }
            else if(loginUser.getLoginType().equals("Approver")){
                moveToApprovalActivity();
            }
        }
    }

    public void showToast(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public void login(View view) {
        RadioGroup radioGroup = findViewById(R.id.user_type);
        int loginTypeID = radioGroup.getCheckedRadioButtonId();
        RadioButton radioButton = findViewById(loginTypeID);
        loginType = radioButton.getText().toString();
        if(edtTxtUsername.getText().toString().isEmpty() || edtTxtPassword.getText().toString().isEmpty()){
            showToast("You must enter user and password");
        }
        else{
            ApiManager apiManager = ApiManager.getInstance();
            apiManager.login(edtTxtUsername.getText().toString(), edtTxtPassword.getText().toString(), loginType, MyApplication.getAppUniqueID(getApplicationContext()), this);
        }
    }
    private void moveToDashboardActivity(){
        Intent intent = new Intent(this, DashboardActivity.class);
        startActivity(intent);
        finish();
    }
    private void moveToApprovalActivity(){
        Intent intent = new Intent(this, MyApprovalActivity.class);
        startActivity(intent);
        finish();
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 1;
    }

    @Override
    public void onResponse(Call<Login> call, Response<Login> response) {

        if(response.body() != null){
            Login login = response.body();
            if (response.isSuccessful()) {
                if(login.getUser() == null){
                    showToast(login.getMessage());
                    return;
                }
                Realm realm = Realm.getDefaultInstance();
                realm.beginTransaction();
                realm.insert(login);
                realm.commitTransaction();

                if(!login.getUser().getEmpId().equals("0")){
                    ApiManager apiManager = ApiManager.getInstance();
                    Call<List<LoVItem>> lovItems = apiManager.getLoVItems(login.getToken(), login.getUsername(), login.getUser().getEmpId(), MyApplication.getAppUniqueID(getApplicationContext()));
                    lovItems.enqueue(new Callback<List<LoVItem>>() {
                        @Override
                        public void onResponse(Call<List<LoVItem>> call, Response<List<LoVItem>> response) {
                            List<LoVItem> lov_items = response.body();

                            Realm realm = Realm.getDefaultInstance();
                            realm.beginTransaction();
                            if (lov_items != null) {
                                realm.insert(lov_items);
                            }
                            realm.commitTransaction();
                        }
                        @Override
                        public void onFailure(Call<List<LoVItem>> call, Throwable t) {
                            Log.d("LoV failed: ", t.getMessage());
                        }
                    });
                }
                if(loginType.equals("Visitor")){
                    moveToDashboardActivity();
                }
                else if(loginType.equals("Approver")){
                    moveToApprovalActivity();
                }
            }
            else {
                switch (response.code()) {
                    case 403:
                        try {
                            if (response.errorBody() != null) {
                                showToast(response.errorBody().string());
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    case 400:
                    case 404:
                        if (response.errorBody() != null) {
                            showToast(response.errorBody().toString());
                        }
                        break;
                }
            }
        }
    }

    @Override
    public void onFailure(Call<Login> call, Throwable t) {
        Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_LONG).show();

    }
}
