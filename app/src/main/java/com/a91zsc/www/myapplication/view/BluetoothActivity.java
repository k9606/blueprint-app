package com.a91zsc.www.myapplication.view;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.ListView;

import com.a91zsc.www.myapplication.R;
import com.a91zsc.www.myapplication.action.BluetoothAction;
import com.a91zsc.www.myapplication.service.BluetoothService;
import com.a91zsc.www.myapplication.util.toolsFileIO;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class BluetoothActivity extends Activity {

    public Context context;
    BluetoothAdapter mBluetoothAdapter;
    private toolsFileIO fileIO = new toolsFileIO();
    private BluetoothAdapter bluetoothAdapter = BluetoothAdapter
            .getDefaultAdapter();
    private BluetoothService bluetoothService;
    private Button searchDevices;
    private BluetoothDevice bluetoothDevice;
    private static final boolean A = true;
    private Thread newThread;


    public void onCreate(Bundle savedInstanceState) {
        SharedPreferences pref = getSharedPreferences("data", MODE_PRIVATE);
        //获取login Name
        String account = pref.getString("acc", "");
        //获取设备的蓝牙适配器实例
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        //强制打开手机蓝牙
        openBlue();
        //蓝牙搜索
        if (account == null || account.length() <= 0) {
            Intent intent = new Intent(BluetoothActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
        super.onCreate(savedInstanceState);
        this.context = this;

        setContentView(R.layout.bluetooth_layout);

        this.initListener();

    }
    //自动执行的方法每次到这个页面都会自动执行

    /**
     * 自动在第二个面执行搜索所有蓝牙的方法
     * 1：获取本地已经存储的已绑定的蓝牙设备获取地址进行自动绑定
     * 2：进入页面自动执行搜索蓝牙的方法
     * 3：将搜索到的结果加载到Item
     * 如果判断系统已经绑定的BlueTooth进行绑定直接跳转到第三个页面去
     */
    public void onStart() {
        super.onStart();
        if (A) {
            new Thread() {
                @Override
                public void run() {
                    bluetoothAdapter.startDiscovery();
                }
            }.start();
        }
        System.out.println("调试2");
    }

    //打开蓝牙设备
    public void openBlue() {
        if (mBluetoothAdapter.isEnabled()) {
        } else {
            mBluetoothAdapter.enable();
        }

    }

    //初始化点击事件click
    private void initListener() {
        System.out.println("初始化 1");
        searchDevices = (Button) this.findViewById(R.id.searchDevices);
        ListView unbondDevices = (ListView) this.findViewById(R.id.unbondDevices);
        ListView bondDevices = (ListView) this.findViewById(R.id.bondDevices);
        BluetoothAction bluetoothAction = new BluetoothAction(this.context,
                unbondDevices, bondDevices, searchDevices,
                BluetoothActivity.this);
        bluetoothAction.setSearchDevices(searchDevices);
        searchDevices.setOnClickListener(bluetoothAction);
    }

    //屏蔽返回键的代码:
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }


}
