package com.a91zsc.www.myapplication.service;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.app.ProgressDialog;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.a91zsc.www.myapplication.R;
import com.a91zsc.www.myapplication.util.toolsFileIO;
import com.a91zsc.www.myapplication.util.utilsTools;

public class BluetoothService extends Service {
    private String driverName;
    private Context context;
    private BluetoothAdapter bluetoothAdapter = BluetoothAdapter
            .getDefaultAdapter();                               //本地蓝牙适配器
    private ArrayList<BluetoothDevice> unbondDevices = null;    // 用于存放未配对蓝牙设备
    private ArrayList<BluetoothDevice> bondDevices = null;      // 用于存放已配对蓝牙设备
    private Button searchDevices = null;                        //点击搜索按钮
    private ListView unbondDevicesListView = null;              //加载未绑定buuletooth 视图
    private ListView bondDevicesListView = null;                //加载绑定buletooth视图
    private toolsFileIO fileIO = new toolsFileIO();
    private static boolean AA = true;
    ProgressDialog progressDialog = null;

    public void searchDevices() {
//        this.bondDevices.clear();
//        this.unbondDevices.clear();
        System.out.println("Sys");
        this.bluetoothAdapter.startDiscovery();
        System.out.println("Sys");
    }






    /**
     * 添加已绑定蓝牙设备到ListView
     */
    private void addBondDevicesToListView() {
        final ArrayList<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();
        int count = this.bondDevices.size();
        for (int i = 0; i < count; i++) {
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("deviceName", this.bondDevices.get(i).getName());
            data.add(map);
        }
        String[] from = {"deviceName"};
        int[] to = {R.id.device_name};
        SimpleAdapter simpleAdapter = new SimpleAdapter(this.context, data,
                R.layout.bonddevice_item, from, to);
        // 把适配器装载到listView中
        this.bondDevicesListView.setAdapter(simpleAdapter);
        //点击事件
        this.bondDevicesListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
//                if(utilsTools.isFastClick()) {
                BluetoothDevice device = bondDevices.get(arg2);
                Intent intent = new Intent();
                intent.setClassName(context,
                        "com.a91zsc.www.myapplication.view.PrintDataActivity");
                intent.putExtra("deviceAddress", device.getAddress());
                context.startActivity(intent);
                onDestroy();
//                }
            }
        });

    }


    /**
     * 添加未绑定蓝牙设备到ListView
     */
    private void addUnbondDevicesToListView() {
        ArrayList<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();
        int count = this.unbondDevices.size();
        for (int i = 0; i < count; i++) {
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("deviceName", this.unbondDevices.get(i).getName());
            data.add(map);// 把item项的数据加到data中
        }
        String[] from = {"deviceName"};
        int[] to = {R.id.undevice_name};
        SimpleAdapter simpleAdapter = new SimpleAdapter(this.context, data,
                R.layout.unbonddevice_item, from, to);
        // 把适配器装载到listView中
        this.unbondDevicesListView.setAdapter(simpleAdapter);

        // 为每个item绑定监听，用于设备间的配对
        this.unbondDevicesListView
                .setOnItemClickListener(new OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> arg0, View arg1,
                                            int arg2, long arg3) {
//                        if(utilsTools.isFastClick()){
                        try {
                            Method createBondMethod = BluetoothDevice.class
                                    .getMethod("createBond");
                            createBondMethod.invoke(unbondDevices.get(arg2));
                            bondDevices.add(unbondDevices.get(arg2));
                            unbondDevices.remove(arg2);
                            addBondDevicesToListView();
                            addUnbondDevicesToListView();
                        } catch (Exception e) {
                            Toast.makeText(context, "配对失败", Toast.LENGTH_SHORT)
                                    .show();
                        }
//                        }
                    }
                });
    }


    public BluetoothService(Context context, ListView unbondDevicesListView,
                            ListView bondDevicesListView, Button searchDevices) {
        this.context = context;
        this.unbondDevicesListView = unbondDevicesListView;
        this.bondDevicesListView = bondDevicesListView;
        // this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.unbondDevices = new ArrayList<BluetoothDevice>();
        this.bondDevices = new ArrayList<BluetoothDevice>();
        this.searchDevices = searchDevices;
        this.initIntentFilter();

    }

    private void initIntentFilter() {
        // 设置广播信息过滤
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);

        context.registerReceiver(receiver, intentFilter);

    }


    /**
     * 添加未绑定蓝牙设备到list集合
     *
     * @param device
     */
    public void addUnbondDevices(BluetoothDevice device) {
        if (!this.unbondDevices.contains(device)) {
            this.unbondDevices.add(device);
        }
    }

    /**
     * 添加已绑定蓝牙设备到list集合
     *
     * @param device
     */
    public void addBandDevices(BluetoothDevice device) {
        if (AA) {
            this.driverName = fileIO.getBlueTooth(context);
//            AA = false;
        }
        if (!this.bondDevices.contains(device)) {
            this.bondDevices.add(device);
        }
        if (driverName != "") {
            if (device.toString().equals(driverName)) {
                Intent intent = new Intent();
                intent.setClassName(context,
                        "com.a91zsc.www.myapplication.view.PrintDataActivity");
                intent.putExtra("deviceAddress", device.getAddress());
                context.startActivity(intent);
                onDestroy();
            }
        }
    }


    /**
     * 蓝牙广播接收器
     */
    public BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                System.out.println("123445567");
                //获取BuleTooth搜索到的设备
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                    addBandDevices(device);
                } else {
                    addUnbondDevices(device);
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                progressDialog = ProgressDialog.show(context, null,
                        "稍等", false);

            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED
                    .equals(action)) {
                progressDialog.dismiss();
                addUnbondDevicesToListView();
                addBondDevicesToListView();
                bluetoothAdapter.cancelDiscovery();
            }


        }

    };

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