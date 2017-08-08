package com.a91zsc.www.myapplication.action;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.a91zsc.www.myapplication.service.PrintDataService;
import com.a91zsc.www.myapplication.R;
import com.a91zsc.www.myapplication.util.showTime;


public class PrintDataAction  {
    private Context context = null;
    private TextView connectState = null;
    private String deviceAddress = null;
    public PrintDataService printDataService = null;
    private BluetoothAdapter bluetoothAdapter = BluetoothAdapter
            .getDefaultAdapter();

    //初始化参数
    public PrintDataAction(Context context, String deviceAddress, TextView connectState, Button a, Button b) {
        super();
        this.context = context;
        this.deviceAddress = deviceAddress;
        this.connectState = connectState;
        this.printDataService = new PrintDataService(this.context,this.deviceAddress,connectState,a,b);
        this.initView();
    }

    /**
     * 判断是否连接成功
     *  connect（）
     */
    private void initView() {
        if (printDataService.connect()){
            this.connectState.setText(this.printDataService.getDeviceName()+" 连接成功");
        } else {
            this.connectState.setText(this.printDataService.getDeviceName()+" 连接异常,请重连!");
            connectState.setTextColor(Color.parseColor("#FF4500"));
        }
    }
    public PrintDataService getService(){
        return printDataService;
    }
}