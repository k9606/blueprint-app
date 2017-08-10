package com.a91zsc.www.myapplication.service;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.a91zsc.www.myapplication.R;
import com.a91zsc.www.myapplication.util.printUtils;
import com.a91zsc.www.myapplication.util.showTime;
import com.a91zsc.www.myapplication.util.toolsFileIO;
import com.a91zsc.www.myapplication.util.utilsTools;
import com.a91zsc.www.myapplication.view.BluetoothActivity;
import com.a91zsc.www.myapplication.view.LoginActivity;

import static com.a91zsc.www.myapplication.string.staticBluetoothData.bluetoothContent;
import static com.a91zsc.www.myapplication.string.staticBluetoothData.netWorkContent;

public class BluetoothService extends Service {
    private String driverName;
    private Context context;
    private BluetoothAdapter bluetoothAdapter = BluetoothAdapter
            .getDefaultAdapter();                               //本地蓝牙适配器
    private ArrayList<BluetoothDevice> unbondDevices = null;    // 用于存放未配对蓝牙设备
    private ArrayList<BluetoothDevice> bondDevices = null;      // 用于存放已配对蓝牙设备
    private ListView unbondDevicesListView = null;              //加载未绑定buuletooth 视图
    private ListView bondDevicesListView = null;                //加载绑定buletooth视图
    private toolsFileIO fileIO = new toolsFileIO();
    private static boolean AA = true;
    Intent intent;
    private boolean BB = false;
    private Boolean aboolean = true;
    showTime showTime = new showTime();
    private Toast toast = null;
    private Intent intentservice = null;
    private String action = null;
    private Context contextservice;
    private static final int ONBIND = 0;
    private static final int BIND = 1;
    private static final int SHOW = 2;

    @Override
    public void onCreate(){
        super.onCreate();
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    public void searchDevices() {
        bluetoothAdapter.cancelDiscovery();
        this.aboolean = true;
        this.bluetoothAdapter.startDiscovery();
    }

    public BluetoothService() {

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
                if (utilsTools.isFastClick()) {
                    bluetoothAdapter.cancelDiscovery();
                    BluetoothDevice device = bondDevices.get(arg2);
                    Intent intent = new Intent();
                    intent.setClassName(context,
                            "com.a91zsc.www.myapplication.view.PrintDataActivity");
                    intent.putExtra("deviceAddress", device.getAddress());
                    intent.putExtra("activity","BluetoothActivity");
                    toast.cancel();
                    if(showTime.getAPNType(BluetoothActivity.context)){
                        showText(bluetoothContent);
                        context.startActivity(intent);
                        onDestroy();
                    }else {
                        showText(netWorkContent);

                    }
                }
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
                        if (utilsTools.isFastClick()) {
                            try {
                                Method createBondMethod = BluetoothDevice.class
                                        .getMethod("createBond");
                                bluetoothAdapter.cancelDiscovery();
                                createBondMethod.invoke(unbondDevices.get(arg2));
                                bondDevices.add(unbondDevices.get(arg2));
                                unbondDevices.remove(arg2);
                                addBondDevicesToListView();
                                addUnbondDevicesToListView();
                            } catch (Exception e) {
                                Toast.makeText(context, "配对失败", Toast.LENGTH_SHORT)
                                        .show();
                            }
                        }
                    }
                });
    }


    public BluetoothService(Context context, ListView unbondDevicesListView,
                            ListView bondDevicesListView) {
        this.context = context;
        Log.e("1", "加载一次");
        this.unbondDevicesListView = unbondDevicesListView;
        this.bondDevicesListView = bondDevicesListView;
        this.unbondDevices = new ArrayList<BluetoothDevice>();
        this.bondDevices = new ArrayList<BluetoothDevice>();
        this.searchDevices();
        this.initIntentFilter();
    }

    private void initIntentFilter() {
        // 设置广播信息过滤
        IntentFilter intentFilter = new IntentFilter();
        //说明：蓝牙扫描时，扫描到任一远程蓝牙设备时，会发送此广播。
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        //蓝牙扫描过程开始
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        //蓝牙扫描过程结束
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        //蓝牙状态值发生改变
//        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
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
            AA = false;
        }
        if (driverName != ""&&device.toString().equals(driverName)) {
                bluetoothAdapter.cancelDiscovery();
                intent = new Intent();
                intent.setClassName(context,
                        "com.a91zsc.www.myapplication.view.PrintDataActivity");
                intent.putExtra("deviceAddress", device.getAddress());
                intent.putExtra("activity","BluetoothActivity");
                BB = true;
                toast.cancel();
                if(showTime.getAPNType(BluetoothActivity.context)){
                    showText(bluetoothContent);
                    context.startActivity(intent);
                    onDestroy();
                }else {
                    showText(netWorkContent);
                }

        }else {
            if (!this.bondDevices.contains(device)) {
                this.bondDevices.add(device);
            }
        }
    }

    public void showText(String text){
        Toast.makeText(BluetoothActivity.context,text,Toast.LENGTH_SHORT).show();
    }

    /**
     * 蓝牙广播接收器
     */
    public BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            action = intent.getAction();
            new Thread(new Runnable() {
                @Override
                public void run() {


                    if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                        if (aboolean) {
                            Message msg = new Message();
                            msg.what = SHOW;
                            msg.obj = "";
                            handler.sendMessage(msg);
                        }
                    } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        int deviceType = device.getBluetoothClass().getMajorDeviceClass();
                        if (Integer.toString(deviceType).equals("1536")) {
                            if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                                Message msg = new Message();
                                msg.what = BIND;
                                msg.obj = device;
                                handler.sendMessage(msg);
                            } else {
                                Message msg = new Message();
                                msg.what = ONBIND;
                                msg.obj = device;
                                handler.sendMessage(msg);

                            }
                        }
                    } else {
                        if (BB) {
                            BB = false;
                            bluetoothAdapter.cancelDiscovery();
                            aboolean = true;
                        }
                    }
                }
            }).start();
        }
    };

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case BIND:
                    BluetoothDevice dervicebind = bluetoothAdapter.getRemoteDevice(msg.obj + "");
                    showBluetooth(dervicebind);
                    break;
                case ONBIND:
                    BluetoothDevice derviceonbind = bluetoothAdapter.getRemoteDevice(msg.obj + "");
                    showBluetoothon(derviceonbind);
                    break;
                case SHOW:
                    toast = Toast.makeText(context, "正在搜索", Toast.LENGTH_LONG);
                    showTime.showToast(toast, 5000);
                    aboolean = false;
                    break;
                default:
                    break;

            }
        }
    };

    public void showBluetooth(BluetoothDevice device) {
        addBandDevices(device);
        addBondDevicesToListView();
    }

    public void showBluetoothon(BluetoothDevice device) {
        addUnbondDevices(device);
        addUnbondDevicesToListView();
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        context.unregisterReceiver(receiver);
    }
}