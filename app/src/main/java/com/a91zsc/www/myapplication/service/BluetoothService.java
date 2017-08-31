package com.a91zsc.www.myapplication.service;

import java.lang.reflect.Method;
import java.util.ArrayList;

import android.app.Activity;
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
import android.support.v4.util.ArrayMap;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.a91zsc.www.myapplication.R;
import com.a91zsc.www.myapplication.application.CustomApplication;
import com.a91zsc.www.myapplication.util.bluetoothUtil;
import com.a91zsc.www.myapplication.util.showTime;
import com.a91zsc.www.myapplication.util.toolsFileIO;
import com.a91zsc.www.myapplication.util.utilsTools;
import com.a91zsc.www.myapplication.view.BluetoothActivity;
import com.a91zsc.www.myapplication.view.PrintDataActivity;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import static com.a91zsc.www.myapplication.string.staticBluetoothData.netWorkContent;

public class BluetoothService extends Service {
    private static Context context;
    private Intent intent = null;
    private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private ArrayList<BluetoothDevice> unbondDevices = null, bondDevices = null;
    private ListView unbondDevicesListView = null, bondDevicesListView = null;
    private static final int ONBIND = 0, BIND = 1, SHOW = 2, CREATEBIND = 3;
    private ArrayList<ArrayMap<String, Object>> array = null;
    private ArrayMap<String, Object> arrayMap;
    private toolsFileIO fileIO = new toolsFileIO();
    private showTime showTime = new showTime();
    private bluetoothUtil blueconnt = new bluetoothUtil();
    private boolean bluetoothIs = true;
    private JSONObject jsonArrayType = new JSONObject();
    private JSONArray jsonArrayId = null;
    private Toast toast = null;
    private BluetoothDevice dervicebind, device;
    private String action = null;
    public int deviceType = 0;
    public static String driverName = "";
    Activity activity = null;
    boolean[] jsonarray;

    public BluetoothService() {

    }

    @Override
    public void onCreate() {
        super.onCreate();
        CustomApplication app = (CustomApplication) getApplication();
        this.activity = app.getinstance();
        openBlue();

    }

    public BluetoothService(Context cet, ListView on, ListView bin) {
        this.context = cet;
        unbondDevicesListView = on;
        bondDevicesListView = bin;
        unbondDevices = new ArrayList<BluetoothDevice>();
        bondDevices = new ArrayList<BluetoothDevice>();
        initIntentFilter();
        searchDevices();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            this.driverName = fileIO.getBlueTooth(context);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            activity.finish();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void clear() {
        intent = null;
        fileIO = null;
        showTime = null;
        blueconnt = null;
        dervicebind = null;
        device = null;
        if (array != null) {
            array.clear();
            unbondDevices.clear();
            bondDevices.clear();
        }
        bluetoothAdapter = null;
    }

    private void openBlue() {
        bluetoothAdapter.enable();
    }

    public void searchDevices() {
        bluetoothAdapter.cancelDiscovery();
        bluetoothIs = true;
        bluetoothAdapter.startDiscovery();
    }

    private void addBondDevicesToListView() {
        array = new ArrayList<>();
        int count = this.bondDevices.size();
        for (int i = 0; i < count; i++) {
            arrayMap = new ArrayMap<>();
            arrayMap.put("deviceName", bondDevices.get(i).getName());
            array.add(arrayMap);
        }
        String[] from = {"deviceName"};
        int[] to = {R.id.device_name};
        SimpleAdapter simpleAdapter = new SimpleAdapter(context, array,
                R.layout.bonddevice_item, from, to);
        bondDevicesListView.setAdapter(simpleAdapter);
        bondDevicesListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                if (utilsTools.isFastClick()) {
                    bluetoothAdapter.cancelDiscovery();
                    BluetoothDevice device = bondDevices.get(arg2);
                    intent = new Intent();
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                    intent.setClassName(context,
                            "com.a91zsc.www.myapplication.view.PrintDataActivity");
                    intent.putExtra("deviceAddress", device.getAddress());
                    intent.putExtra("activity", "BluetoothActivity");
                    toast.cancel();
                    if (showTime.getAPNType(context)) {
                        if (blueconnt.connect(context, device)) {
                            context.startActivity(intent);
                            context.stopService(new Intent(context, BluetoothService.class));
                            showText("连接成功");
                        }else {
                            Toast.makeText(context,"连接失败请重试!",Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        showText(netWorkContent);

                    }
                }
            }
        });

    }

    private void addUnbondDevicesToListView() {
        array = new ArrayList<>();
        int count = this.unbondDevices.size();
        for (int i = 0; i < count; i++) {
            arrayMap = new ArrayMap<>();
            arrayMap.put("deviceName", unbondDevices.get(i).getName());
            array.add(arrayMap);
        }
        String[] from = {"deviceName"};
        int[] to = {R.id.undevice_name};
        SimpleAdapter simpleAdapter = new SimpleAdapter(this.context, array,
                R.layout.unbonddevice_item, from, to);
        // 把适配器装载到listView中想
        this.unbondDevicesListView.setAdapter(simpleAdapter);

        // 为每个item绑定监听，用于设备间的配对
        this.unbondDevicesListView
                .setOnItemClickListener(new OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> arg0, View arg1,
                                            int arg2, long arg3) {
                        if (utilsTools.isFastClick()) {
                            try {
                                bluetoothAdapter.cancelDiscovery();
                                dervicebind = unbondDevices.get(arg2);
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            Method createBondMethod = BluetoothDevice.class.getMethod("createBond");
                                            createBondMethod.invoke(dervicebind);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }).start();
                            } catch (Exception e) {
                                showText("配对失败");
                            }
                        }
                    }
                });
    }

    private void showItem(BluetoothDevice dervicebind) {
        bondDevices.add(dervicebind);
        unbondDevices.remove(dervicebind);
        addBondDevicesToListView();
        addUnbondDevicesToListView();
    }

    private void initIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        intentFilter.setPriority(Integer.MAX_VALUE);
        context.registerReceiver(receiver, intentFilter);
    }

    private void addUnbondDevices(BluetoothDevice device) {
        if (!unbondDevices.contains(device)) {
            unbondDevices.add(device);
        }
    }

    private void addBandDevices(BluetoothDevice device) {
        if (device.toString().equals(driverName)) {
            bluetoothAdapter.cancelDiscovery();
            intent = new Intent();
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            intent.setClassName(context, "com.a91zsc.www.myapplication.view.PrintDataActivity");
            intent.putExtra("deviceAddress", device.getAddress());
            intent.putExtra("activity", "BluetoothActivity");
            toast.cancel();
            if (showTime.getAPNType(context)) {
                showText("正在连接");
                if (blueconnt.connect(context, device)) {
                    context.startActivity(intent);
                    context.stopService(new Intent(context, BluetoothService.class));
                }else {
                    Toast.makeText(context,"连接失败请重试!",Toast.LENGTH_SHORT).show();
                }
            }

        } else {
            if (!bondDevices.contains(device)) {
                bondDevices.add(device);
            }


        }
    }

    public void storageID(String id) {
        jsonArrayType.put("id", id);
        jsonArrayId.add(jsonArrayType);

    }

    public void getTypeData(JSONArray choice, JSONArray local,int lenght) {
        if (jsonArrayId != null) {
            jsonArrayType.clear();
        }
        jsonArrayId = new JSONArray();
        for (int i = 0; i < choice.size(); i++) {
            JSONObject choiceTyep = choice.getJSONObject(i);
            String type = choiceTyep.getString("type");
            for (int o = 0; o < local.size(); o++) {
                JSONObject localType = local.getJSONObject(o);
                if (type.equals(localType.getString("type"))) {
                    storageID(localType.getString("id"));
                    break;
                }
            }
        }
        if(!jsonArrayId.toString().equals("[]") || lenght==jsonArrayId.size()){
            fileIO.whilteShopType(context, jsonArrayId.toString());
        }
    }


    public void whilteIsText(boolean[] jsonarraydata) {
        jsonarray = jsonarraydata;
        new Thread(new Runnable() {
            @Override
            public void run() {
                JSONArray json = new JSONArray();
                JSONObject jsonobject = new JSONObject();
                for (int i = 0; i < jsonarray.length; i++) {
                    if (jsonarray[i]) {
                        jsonobject.put("choice", "1");
                        json.add(jsonobject);
                    } else {
                        jsonobject.put("choice", "0");
                        json.add(jsonobject);
                    }
                }
                fileIO.whilteIsText(context, json.toString());
            }
        }).start();

    }

    private void showText(String text) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }

    private void showBluetooth(BluetoothDevice device) {
        addBandDevices(device);
        addBondDevicesToListView();
    }

    private void showBluetoothon(BluetoothDevice device) {
        addUnbondDevices(device);
        addUnbondDevicesToListView();
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            action = intent.getAction();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    switch (action) {
                        case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                            if (bluetoothIs) {
                                Message msg = new Message();
                                msg.what = SHOW;
                                msg.obj = "";
                                handler.sendMessage(msg);
                            }
                            break;
                        case BluetoothDevice.ACTION_FOUND:
                            try {
                                device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                                deviceType = device.getBluetoothClass().getMajorDeviceClass();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            if (deviceType != 512 && deviceType != 256) {
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
                            break;
                        case BluetoothDevice.ACTION_BOND_STATE_CHANGED:
                            if (dervicebind.getBondState() == BluetoothDevice.BOND_BONDED) {
                                Message msg = new Message();
                                msg.what = CREATEBIND;
                                msg.obj = dervicebind;
                                handler.sendMessage(msg);
                            }
                            break;
                        case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                            bluetoothAdapter.cancelDiscovery();
                            bluetoothIs = true;
                            break;

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
                    dervicebind = bluetoothAdapter.getRemoteDevice(msg.obj + "");
                    showBluetooth(dervicebind);
                    break;
                case ONBIND:
                    dervicebind = bluetoothAdapter.getRemoteDevice(msg.obj + "");
                    showBluetoothon(dervicebind);
                    break;
                case SHOW:
                    toast = Toast.makeText(context, "正在搜索", Toast.LENGTH_LONG);
                    showTime.showToast(toast, 5000);
                    bluetoothIs = false;
                    break;
                case CREATEBIND:
                    dervicebind = bluetoothAdapter.getRemoteDevice(msg.obj + "");
                    showItem(dervicebind);
                    break;
                default:
                    break;
            }
        }
    };
}
