package com.a91zsc.www.myapplication.application;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.TextView;


import com.a91zsc.www.myapplication.service.PrintDataService;

import net.sf.json.JSONArray;


public class CustomApplication extends Application {
    private Activity activity;
    private Context context;
    private int ishu = 10;
    private String ble = "";
    private CustomApplication instance;
    private View viewcontent;
    public Boolean isconnection = false;
    int numbertype = 0;
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public CustomApplication getInstance() {
        return instance;
    }

    public void setInstance(Activity instance) {
        activity = instance;
    }

    public Activity getinstance() {
        return activity;
    }

    public void setData(int i) {
        ishu = i;
    }

    public int getData() {
        return ishu;
    }

    public void setBluetoothIntent(String Ibluentent) {
        ble = Ibluentent;
    }

    public String getBluetoothIntent() {
        return ble;
    }

    public void setConnection(Boolean b) {
        isconnection = b;

    }
    public void setJsonTypeNumber(int number){
        numbertype = number;
    }
    public int getJsonTypeNumber(JSONArray json){
        return numbertype;
    }

    public Boolean reVonnection() {
        return isconnection;
    }
}