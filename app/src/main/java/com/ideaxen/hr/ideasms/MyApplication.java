package com.ideaxen.hr.ideasms;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.ideaxen.hr.ideasms.model.LoVItem;
import com.ideaxen.hr.ideasms.model.Login;
import com.ideaxen.hr.ideasms.util.LovSpinnerAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

public class                                                                                    MyApplication extends Application {
    public static String isRunningVisit=null;
    @Override
    public void onCreate() {
        super.onCreate();
        MyApplication.getAppUniqueID(getApplicationContext());
        Realm.init(this);
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder().deleteRealmIfMigrationNeeded().build();
        Realm.setDefaultConfiguration(realmConfiguration);
    }

    public static String getAppUniqueID(Context context) {
        String PREF_UNIQUE_ID = "XB_APP_UNIQUE_ID";
        SharedPreferences sharedPrefs = context.getSharedPreferences(
                PREF_UNIQUE_ID, Context.MODE_PRIVATE);
        String uniqueID = sharedPrefs.getString(PREF_UNIQUE_ID, null);
        if (uniqueID == null) {
            uniqueID = UUID.randomUUID().toString();
            SharedPreferences.Editor editor = sharedPrefs.edit();
            editor.putString(PREF_UNIQUE_ID, uniqueID).apply();
        }
//        Log.d("App_ID", uniqueID);
        return uniqueID;
    }
    public static String setVisitPreference(Context context, String visit_pref) {
        String VISIT_PREF_ID = "IX_VISIT_LIST_PREF";
        SharedPreferences sharedPrefs = context.getSharedPreferences(
                VISIT_PREF_ID, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString(VISIT_PREF_ID, visit_pref);
        editor.commit();
//        Log.d("App_ID", uniqueID);
        return visit_pref;
    }
    public static String getVisitPreference(Context context) {
        String VISIT_PREF_ID = "IX_VISIT_LIST_PREF";
        SharedPreferences sharedPrefs = context.getSharedPreferences(
                VISIT_PREF_ID, Context.MODE_PRIVATE);
        String visit_pref=null;
        if(sharedPrefs.contains(VISIT_PREF_ID)) {
            visit_pref = sharedPrefs.getString(VISIT_PREF_ID, null);
        }
        if (visit_pref == null) {
            Resources res = context.getResources();
            visit_pref = "2";//default week's visits/"1"=>Today's visits
            visit_pref = MyApplication.setVisitPreference(context, visit_pref);
        }
        return visit_pref;
    }

    public static Login isLoggedin(){
        Realm realm = Realm.getDefaultInstance();
        Login loginUser = realm.where(Login.class).findFirst();
        return loginUser;
    }
    public static List<LoVItem> getLovItems(String lovName){
        Realm realm = Realm.getDefaultInstance();
        List<LoVItem> loVItems = realm.where(LoVItem.class).equalTo("lovName", lovName).findAll();
        return realm.copyFromRealm(loVItems);

    }
    public static void bindLovToSpinner2(Context context, Spinner spinner, String lovName, String firstItem){
        int id = 32;
        LoVItem loVItem = new LoVItem(lovName, firstItem);
        List<LoVItem> lovItems = MyApplication.getLovItems(lovName);
        lovItems.add(0, loVItem);
        LovSpinnerAdapter dataAdapter = new LovSpinnerAdapter(context, lovItems);

        // attaching data adapter to spinner
        spinner.setAdapter(dataAdapter);
    }
    public static void bindLovToSpinner2(Context context, Spinner spinner,
                                         String lovName, String firstItem, String customer){
        LoVItem loVItem = new LoVItem(lovName, firstItem);
        List<LoVItem> lovItems = MyApplication.getLovItems(lovName);
        lovItems.add(0, loVItem);
        LovSpinnerAdapter dataAdapter = new LovSpinnerAdapter(context, lovItems);

        // attaching data adapter to spinner
        spinner.setAdapter(dataAdapter);
        int pos = -1;
        for(int i = 0; i < spinner.getCount(); i++){
            LoVItem loVItemValue = (LoVItem) spinner.getItemAtPosition(i);
            Log.d("TAG", loVItemValue.getValue());
            if(loVItemValue.getValue().equals(customer)){
                pos = i;
                break;
            }
        }
        spinner.setSelection(pos);
    }
    public static List<String> getLovItemsAsStringList(String lovName, String firstItem){
        List<LoVItem> lovItems = MyApplication.getLovItems(lovName);
        List<String> lovItemList = new ArrayList<>();
        lovItemList.add(firstItem);
        for(LoVItem loVItem:lovItems){
            lovItemList.add(loVItem.getValue());
        }
        return lovItemList;
    }
    public static void bindLovToSpinner(Context context, Spinner spinner, String lovName, String firstItem){
        List<String> lovItems = MyApplication.getLovItemsAsStringList(lovName, firstItem);
        MyApplication.bindListToSpinner(context, spinner, lovItems);
    }
    public static void bindListToSpinner(Context context, Spinner spinner, List<String> listItems){
        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, listItems);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        spinner.setAdapter(dataAdapter);
    }
    public static void removeAllLovItems(){
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.where(LoVItem.class).findAll().deleteAllFromRealm();
        realm.commitTransaction();
    }
    public static boolean logout(){
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        RealmResults<Login> loginEntries = realm.where(Login.class).findAll();
        loginEntries.deleteAllFromRealm();
        realm.commitTransaction();

        return true;
    }
}
