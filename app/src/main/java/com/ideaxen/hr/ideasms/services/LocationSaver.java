package com.ideaxen.hr.ideasms.services;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.ideaxen.hr.ideasms.MyApplication;
import com.ideaxen.hr.ideasms.api.ApiManager;
import com.ideaxen.hr.ideasms.model.LatLon;
import com.ideaxen.hr.ideasms.model.Login;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LocationSaver {

    private static final String TAG = "LocationSaver";

    private Login loginUser;
    private Context mContext;

    public void startLocationProcess(Context context, String visId, String event){

        loginUser = MyApplication.isLoggedin();
        mContext = context;

        LocationTrack locationTrack = new LocationTrack(mContext);
        LatLng latLon = locationTrack.getLatLon();

        if(latLon == null){
            Toast.makeText(mContext,"Please enable GPS.",Toast.LENGTH_SHORT).show();
            return;
        }
        else {
            saveLocation(visId, event, String.valueOf(latLon.latitude), String.valueOf(latLon.longitude));
        }
        locationTrack.stopListener();
    }

    public void autoLocationProcess(Context context){

        loginUser = MyApplication.isLoggedin();
        mContext = context;

        LocationTrack locationTrack = new LocationTrack(mContext);
        LatLng latLon = locationTrack.getLatLon();

        if(latLon == null){
            Toast.makeText(mContext,"Please enable GPS.",Toast.LENGTH_SHORT).show();
            return;
        }
        else {
            saveLocation("0", "Auto Location", String.valueOf(latLon.latitude), String.valueOf(latLon.longitude));
        }
        locationTrack.stopListener();
    }

    private void saveLocation(String visId, String event, String lat, String lng){

        ApiManager apiManager = ApiManager.getInstance();
        Call<LatLon> eventInfo = apiManager.eventLocation(loginUser.getToken(), loginUser.getUsername(), loginUser.getUser().getEmpId(), MyApplication.getAppUniqueID(mContext),
                visId, event, lat, lng);
        eventInfo.enqueue(new Callback<LatLon>() {
            @Override
            public void onResponse(Call<LatLon> call, Response<LatLon> response) {
                if (response.body() != null) {
                    LatLon latLon = response.body();
                    Log.d(TAG, "Id: " + latLon.getId());
                    Log.d(TAG, "Event: " + latLon.getEvent());
                    Log.d(TAG, "Lat: " + latLon.getLat());
                    Log.d(TAG, "Lng: " + latLon.getLng());
                    Log.d(TAG, "Visit: " + latLon.getVisId());
                    Log.d(TAG, "Emp: " + latLon.getEmpId());
                    Log.d(TAG, "Created: " + latLon.getCreatedAt());
                }
            }

            @Override
            public void onFailure(Call<LatLon> call, Throwable t) {
                Log.d(TAG, "Event Failed: " + t.getMessage());
            }
        });
    }
}
