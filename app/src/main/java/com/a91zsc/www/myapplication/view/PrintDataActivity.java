package com.a91zsc.www.myapplication.view;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.graphics.Color;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;

import android.view.View;
import android.widget.Toast;

import com.a91zsc.www.myapplication.R;
import com.a91zsc.www.myapplication.application.CustomApplication;
import com.a91zsc.www.myapplication.application.IntentConnection;
import com.a91zsc.www.myapplication.service.PrintDataService;
import com.a91zsc.www.myapplication.util.printUtils;
import com.a91zsc.www.myapplication.util.toolsFileIO;

import android.support.v7.app.AppCompatActivity;

import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import net.sf.json.JSONArray;

import java.util.List;


public class PrintDataActivity extends AppCompatActivity implements View.OnClickListener {
    private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    public static TextView connectState;
    public Context context;
    public Button saveSet, send, command;
    public static boolean isconnection = false;

    PrintDataService printDataService;
    BluetoothDevice dervicebind = null;
    private toolsFileIO fileIO = new toolsFileIO();
    private PopupWindow mPopWindow;
    printUtils printutils = new printUtils();
    private NetworkChangeReceiver networkChangeReceiver;
    public static String BluetoothIntent = null;
    public static String name;
    public static int shuliang = 1;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.printdata_layout);
        getSupportActionBar().hide();
        context = getApplicationContext();
        connectState = (TextView) findViewById(R.id.connect_state);
        send = (Button) findViewById(R.id.send);
        command = (Button) findViewById(R.id.wsStart);
        saveSet = (Button) findViewById(R.id.SetData);
        getDeviceAddress();
        viewnetworkstatus();
        printDataService = new PrintDataService(context, BluetoothIntent);
        send.setOnClickListener(this);
        command.setOnClickListener(this);
        saveSet.setOnClickListener(this);
        startService(new Intent(context, PrintDataService.class));
        smallticketsetup(context);
        Settings.System.getInt(getContentResolver(), Settings.System.WIFI_SLEEP_POLICY,
                Settings.System.WIFI_SLEEP_POLICY_DEFAULT);
        Settings.System.putInt(getContentResolver(), Settings.System.WIFI_SLEEP_POLICY,
                Settings.System.WIFI_SLEEP_POLICY_NEVER);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.wsStart:
                printDataService.serviceTest();
                break;
            case R.id.send:
                printDataService.printTest();
                break;
            case R.id.SetData:
                showPopupWindow();
                break;
            case R.id.largeLabe:
                mPopWindow.dismiss();
                break;
            default:
                break;
        }
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        switch (level) {
            case TRIM_MEMORY_MODERATE:
                printDataService.cleatr();
                Runtime.getRuntime().gc();
                break;
            case TRIM_MEMORY_RUNNING_LOW:
                printDataService.cleatr();
                Runtime.getRuntime().gc();
                break;
            default:
                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void viewnetworkstatus() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        networkChangeReceiver = new NetworkChangeReceiver();
        registerReceiver(networkChangeReceiver, intentFilter);
    }

    private void setShowText(int i, String string) {
        if (i == 1) {
            connectState.setText(string);
            connectState.setTextColor(Color.parseColor("#FF4500"));
        } else {
            this.connectState.setText(dervicebind.getName() + string);
            connectState.setTextColor(Color.parseColor("#FFFFFF"));
        }
    }

    private void smallticketsetup(Context context) {
        shuliang = fileIO.getSetData(context);
        if (shuliang > 1) {
            saveSet.setText("小票设置" + shuliang);
        }
    }

    private class NetworkChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager connectionManager = (ConnectivityManager)
                    getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectionManager.getActiveNetworkInfo();
            Log.e("",IntentConnection.network+"");
            if (IntentConnection.network!=0) {
                Log.e("服务器断开广播",""+IntentConnection.network);
                if (networkInfo == null || printUtils.webSocketConnection == null) {
                    setShowText(1, "服务器异常,请查看网络!");
                    setShowButton(false);
                }
                if (networkInfo != null && networkInfo.isAvailable()) {
                    if (printUtils.webSocketConnection==null && BluetoothIntent != null) {
                        Log.e("服务器重连广播",""+IntentConnection.network);
                        setShowText(2, " 连接成功!");
                        printDataService.dataProcessing();
                        setShowButton(true);
                        IntentConnection.network = 0;
                    }
                }

            }
        }
    }

    private void setShowButton(Boolean isButton) {
        send.setEnabled(isButton);
        command.setEnabled(isButton);

    }

    private void showPopupWindow() {
        final View contentView = LayoutInflater.from(PrintDataActivity.this).inflate(R.layout.popupwindow_item, null);
        this.mPopWindow = new PopupWindow(contentView);
        mPopWindow.setFocusable(true);
        mPopWindow.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        mPopWindow.setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
        final EditText editText = (EditText) contentView.findViewById(R.id.inttext);
        editText.setCursorVisible(false);
        editText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editText.setCursorVisible(true);
            }
        });
        Button cancel = (Button) contentView.findViewById(R.id.cancel);
        cancel.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                mPopWindow.dismiss();
            }
        });
        Button save = (Button) contentView.findViewById(R.id.saveSet);
        save.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    int data = Integer.parseInt(editText.getText().toString());
                    if (data > 10 || data == 0) {
                        Toast.makeText(context, "保存失败！数字大小在1-10范围内。", Toast.LENGTH_SHORT).show();
                    } else {
                        shuliang = data;
                        fileIO.putSetData(context, data);
                        if (!(data == 1)) {
                            saveSet.setText("小票设置  " + data);
                        } else {
                            saveSet.setText("小票设置");
                        }
                        mPopWindow.dismiss();
                        Toast.makeText(context, "保存成功", Toast.LENGTH_SHORT).show();
                    }
                } catch (NumberFormatException e) {
                    mPopWindow.dismiss();
                }
            }
        });
        mPopWindow.showAsDropDown(connectState);
    }

    private void getDeviceAddress() {
        Intent intent = this.getIntent();
        String Bluetooth = intent.getStringExtra("deviceAddress");
        this.dervicebind = bluetoothAdapter.getRemoteDevice(Bluetooth);
        this.BluetoothIntent = intent.getStringExtra("activity");
        name = dervicebind.getName();
        connectState.setText(name + "连接成功");
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            AlertDialog isExit = new AlertDialog.Builder(this).create();
            isExit.setMessage("确认退出将断开服务器!");
            isExit.setButton("确定", listener);
            isExit.setButton2("取消", listener);
            isExit.show();
        }
        return false;
    }

    DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case AlertDialog.BUTTON_POSITIVE:
                    IntentConnection.network = 0;
                    BluetoothIntent = null;
                    fileIO.putBlueToothDrive(context, "");
                    printutils.closeSocket(context);
                    unregisterReceiver(networkChangeReceiver);
                    stopService(new Intent(context, PrintDataService.class));
                    Intent intent = new Intent(context, BluetoothActivity.class);
                    startActivity(intent);
                    finish();
                    break;
                case AlertDialog.BUTTON_NEGATIVE:
                    break;
                default:
                    break;
            }
        }
    };
//    private boolean isServiceRunning() {
//        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
//        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
//            if ("com.a91zsc.www.myapplication.service.PrintDataService".equals(service.service.getClassName())) {
//                return true;
//            }
//        }
//        return false;
//    }

}

