package com.ideaxen.hr.ideasms.services;

import android.app.ActivityManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.ideaxen.hr.ideasms.MyApplication;
import com.ideaxen.hr.ideasms.api.ApiManager;
import com.ideaxen.hr.ideasms.model.Visit;
import com.ideaxen.hr.ideasms.model.Login;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import io.realm.Realm;
import io.realm.RealmList;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OfflineDataService extends Service {

    private static final String TAG = "OfflineDataService";
    private Handler mHandler = new Handler();
    long notify_interval = 60000 * 2;
    boolean isActivity = false;
    Login loginUser;

    public OfflineDataService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        loginUser = MyApplication.isLoggedin();

        isActivity = isForeground("com.ideaxen.client.visitapp");

        if(!isActivity && isNetworkAvailable()){
            Timer mTimer = new Timer();
            mTimer.schedule(new TimerTaskToGetLocation(), 0, notify_interval);
        }
        else{
            Log.d(TAG, "Application in Use or No Internet Connection");
        }
    }

    public boolean isForeground(String myPackage) {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        boolean foreground = true;
        List<ActivityManager.RunningTaskInfo> runningTaskInfo;
        if (manager != null) {
            runningTaskInfo = manager.getRunningTasks(1);
            ComponentName componentInfo;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                componentInfo = runningTaskInfo.get(0).topActivity;
                foreground =  componentInfo.getPackageName().equals(myPackage);
            }
        }
        return foreground;
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

    private class TimerTaskToGetLocation extends TimerTask {
        @Override
        public void run() {

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    ApiManager apiManager = ApiManager.getInstance();
                    Call<List<Visit>> visitItems = apiManager.getOfflineData(loginUser.getToken(), loginUser.getUsername(), loginUser.getLoginType(), loginUser.getUser().getEmpId(), MyApplication.getAppUniqueID(getApplicationContext()));
                    visitItems.enqueue(new Callback<List<Visit>>() {
                        @Override
                        public void onResponse(Call<List<Visit>> call, Response<List<Visit>> response) {
                            if (response.body() != null) {

                                RealmList<Visit> realmVisitList = new RealmList<>();
                                Realm realm = Realm.getDefaultInstance();
                                realm.beginTransaction();

                                for (Visit visitItem: response.body()){
                                    Log.d(TAG, "Each Item: " + visitItem.toString());
                                    Visit visitObject = realm.where(Visit.class)
                                            .equalTo("id", visitItem.getId())
                                            .findFirst();
                                    if(visitObject == null){
                                        realmVisitList.add(visitItem);
                                    }
                                    else{
                                        Log.d(TAG, "Data already exists");
                                    }
                                }
                                realm.insert(realmVisitList);
                                realm.commitTransaction();
                            }
                        }

                        @Override
                        public void onFailure(Call<List<Visit>> call, Throwable t) {
                            Log.d(TAG, "onFailure: " + t.getMessage());
                        }
                    });
                }
            });
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
