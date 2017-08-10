package com.a91zsc.www.myapplication.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.a91zsc.www.myapplication.R;
import com.a91zsc.www.myapplication.util.printUtils;
import com.a91zsc.www.myapplication.util.showTime;
import com.a91zsc.www.myapplication.util.toolsFileIO;
import com.a91zsc.www.myapplication.view.PrintDataActivity;

import net.sf.json.JSONObject;

import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketException;
import de.tavendo.autobahn.WebSocketHandler;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import static com.a91zsc.www.myapplication.string.staticBluetoothData.BluetoothServiceSend;
import static com.a91zsc.www.myapplication.string.staticBluetoothData.BluetoothSocketSend;
import static com.a91zsc.www.myapplication.string.staticBluetoothData.resevre;
import static com.a91zsc.www.myapplication.string.staticBluetoothData.restaurant;
import static com.a91zsc.www.myapplication.string.staticBluetoothData.restaurantWaiter;
import static com.a91zsc.www.myapplication.string.staticBluetoothData.serviceBindURL;
import static com.a91zsc.www.myapplication.string.staticBluetoothData.socketClose;
import static com.a91zsc.www.myapplication.string.staticBluetoothData.socketContentURL;
import static com.a91zsc.www.myapplication.string.staticBluetoothData.takoutFood;


public class PrintDataService extends Service {
    private Context context;
    private static Context contextprint;
    private String deviceAddress = null;
    private BluetoothAdapter bluetoothAdapter = BluetoothAdapter
            .getDefaultAdapter();
    private toolsFileIO fileIO = new toolsFileIO();
    showTime util = new showTime();
    private BluetoothDevice device = null;
    private static BluetoothSocket bluetoothSocket = null;
    private static OutputStream outputStream = null;
    private static final UUID uuid = UUID
            .fromString("00001101-0000-1000-8000-00805F9B34FB");
    public boolean isConnection = false;
    private JSONObject order;
    private JSONObject jsonObject;
    private JSONObject jsonObjectThread = null;
    private JSONObject orderThread;
    private String order_type;
    public TextView textView;
    private String msg_service = null;
    private static final String DYNAMICACTION = "com.www.printService";
    public static final WebSocketConnection wsC = new WebSocketConnection();
    public printUtils printutils = new printUtils();
    private String string = null;
    showTime time = new showTime();


    public PrintDataService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.e("Socket", "onBind");
        return null;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        contextprint = this;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        dataProcessing();
        contextprint = this;
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        printutils.closeSocket(context);
//        disconnect();
    }

public void oncolose(){
    Intent intentBroadcast = new Intent();
    intentBroadcast.setAction(DYNAMICACTION);
    sendBroadcast(intentBroadcast);
    Log.e("自定义动态广播","广播");
}

    public PrintDataService(Context context, String deviceAddress, TextView textView) {
        super();
        this.context = context;
        this.deviceAddress = deviceAddress;
        this.textView = textView;
        this.device = this.bluetoothAdapter.getRemoteDevice(this.deviceAddress);
    }

    public String getDeviceName() {
        return this.device.getName();
    }

    public boolean connect() {
        if (!isConnection) {
            try {
                bluetoothSocket = device.createRfcommSocketToServiceRecord(uuid);
                bluetoothSocket.connect();
                outputStream = bluetoothSocket.getOutputStream();
                fileIO.putBlueToothDrive(context, device.toString());
                isConnection = true;
            } catch (Exception e) {
                Toast.makeText(context, "设备连接异常！", Toast.LENGTH_SHORT).show();
                isConnection = false;
            }
        }
        return isConnection;
    }


//    public static void disconnect() {
//        try {
//            bluetoothSocket.close();
//            outputStream.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    public void sendInfo() {
        printutils.sendInfo("打印机测试" + "\n\n\n\n");
    }

    //    public void showConnect(){
//        this.isConnection = false;
//        this.disconnect();
//        showTime.Delayed(500);
//        try{
//            while (true) {
//                button1.setEnabled(false);
//                button2.setEnabled(false);
//                if (this.connect()) {
//                    if(connectionCode){
//                        this.textView.setText(this.getDeviceName()+" 连接成功");
//                        this.connectionCode = false;
//                    }
//                    button1.setEnabled(true);
//                    button2.setEnabled(true);
//                    break;
//                }
//            }
//        }catch (Exception e){
//            textView.setText("蓝牙连接异常！请检测蓝牙设备！");
//        }
//        Log.e("Connetct","连接成功");
//    }


    public void dataProcessing() {
        new Thread(new Runnable() {
            @Override
            public void run() {


                try {
                    wsC.connect(socketContentURL, new WebSocketHandler() {
                        @Override
                        public void onOpen() {
                            Log.e("Socket", "onOpen");
                            if(PrintDataActivity.BluetoothIntent!=null){
                                printutils.IntentOutPut(wsC,outputStream);
                                printutils.setButton();
                            }
                        }
                        @Override
                        public void onTextMessage(String payload) {
                            if(payload.equals("")||payload.equals(null)){
                            }else {
                                DataOrder(payload);
                            }
                        }
                        @Override
                        public void onClose(int code, String reason) {
                            Log.e("websocket","onClose");
                            Log.e("断开的时候",wsC.toString());
                            MediaPlayer mediaPlayer;
                            mediaPlayer = MediaPlayer.create(contextprint, R.raw.audio_end);
                            mediaPlayer.start();
                            if (PrintDataActivity.BluetoothIntent!=null&&time.getAPNType(PrintDataActivity.context)){
                                dataProcessing();
                            }else {
                                oncolose();
                                Log.e("发送广播","onClose");
                            }
                        }
                    });
                    util.Delayed(3000);
                } catch (WebSocketException e) {

                    e.printStackTrace();
                }
            }
        }).start();

    }

    public void DataOrder(String payload) {
        jsonObject = JSONObject.fromObject(payload);
        Log.e("Socket", jsonObject + "");
        order = jsonObject.getJSONObject("order");
        order_type = order.getString("order_type");
        if (order.has("msg")) {
            msg_service = order.getString("msg");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    switch (order_type) {
                        case "bind":
                            Log.e("banding", "绑定线程");
                            printutils.bind(msg_service,contextprint);
                            break;
                        case "test":
                            for (int i = 0; i < PrintDataActivity.shulian; i++) {
                                printutils.sendInfo(BluetoothSocketSend + "\n\n\n\n");
                                util.Delayed(2000);
                            }
                            break;
                        case "heartbeat":
                            printutils.heartbeat();
                            break;
                        default:
                            System.out.println("default");
                            break;
                    }
                }
            }).start();
        } else {
            switch (order_type) {
                case takoutFood:
                    jsonObjectThread = jsonObject;
                    orderThread = order;
                    for (int i = 0; i < PrintDataActivity.shulian; i++) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                printutils.waimai(jsonObjectThread, orderThread);
                            }
                        }).start();
                        util.Delayed(4000);
                    }
                    break;
                case restaurant:
                    jsonObjectThread = jsonObject;
                    orderThread = order;
                    for (int i = 0; i < PrintDataActivity.shulian; i++) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                printutils.diannei(jsonObjectThread, orderThread);
                            }
                        }).start();
                        util.Delayed(4000);
                    }
                    break;
                case restaurantWaiter:
                    jsonObjectThread = jsonObject;
                    orderThread = order;
                    for (int i = 0; i < PrintDataActivity.shulian; i++) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                printutils.waiterdiannei(jsonObjectThread, orderThread);
                            }
                        }).start();
                        util.Delayed(4000);
                    }
                    break;
                case resevre:
                    jsonObjectThread = jsonObject;
                    orderThread = order;
                    for (int i = 0; i < PrintDataActivity.shulian; i++) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                printutils.yuding(jsonObjectThread, orderThread);
                            }
                        }).start();
                        util.Delayed(5000);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    public void sendTextMassage() {
        printutils.test();
    }

//    public void stop(){
//        Log.e("service","暂停服务");
//        Intent intent = new Intent(context,PrintDataActivity.class);
//        stopService(intent);
//        onDestroy();
//    }
}

