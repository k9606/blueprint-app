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
import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
    private Context contextprint;
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
    private Button button1;
    private Button button2;
    NotificationCompat.Builder builder;
    NotificationManager notifyManager;
    private Boolean isConnect = true;
    private static final int SHOW = 0;
    public TextView textView;
    public final static int NOTIFICATION = 1;
    public final static int NOTIFICATION2 = 2;
    private String msg_service = null;
    public static final WebSocketConnection wsC = new WebSocketConnection();
    public printUtils printutils = new printUtils();
    private String string = null;
    private WindowManager.LayoutParams wmParams;
    private WindowManager mWindowManager;
    private LinearLayout mFloatLayout;
    private Button mFloatView;


    public PrintDataService() {
    }

    //    @Override
//    public void onCreate(){
//        super.onCreate();
//        sendNotification();
//        dataProcessing();
//    }
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sendNotification();
        dataProcessing();
        contextprint = this;
//        showWindows();

    }
//    public void showWindows() {
//        Message msg = new Message();
//        msg.what = SHOW;
//        msg.obj = "1";
//        handler.sendMessage(msg);
//    }

//    class NetworkChangeReceiver extends BroadcastReceiver {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            ConnectivityManager connectionManager = (ConnectivityManager)
//                    getSystemService(Context.CONNECTIVITY_SERVICE);
//            NetworkInfo networkInfo = connectionManager.getActiveNetworkInfo();
//            if (networkInfo != null && networkInfo.isAvailable()) {
//                Log.e("contiont", "有网络重连");
//                dataProcessing();
//            }else if(networkInfo ==null){
//                Log.e("contiont", "暂无网络");
//            }
//        }
//    }

//    public boolean getAPNType(Context context,Boolean isNetWork) {
//            this.connManager = (ConnectivityManager) context
//                    .getSystemService(Context.CONNECTIVITY_SERVICE);
//            this.networkInfo = connManager.getActiveNetworkInfo();
//            int isnetwork = networkInfo.getType();
//            if(isnetwork!=CURRENT_NETWORK_STATES_NO&&isNetWork){
//               this.netWorkIs = true;
//            }
//            return netWorkIs;
//
//        }

    @Override
    public void onDestroy() {
        super.onDestroy();
        printutils.postData(context);
        
        disconnect();

    }

    private void sendNotification() {
        //获取NotificationManager实例
        notifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        //实例化NotificationCompat.Builde并设置相关属性
        builder = new NotificationCompat.Builder(this)
                //设置小图标
                .setSmallIcon(R.mipmap.ic_launcher)
                //设置通知标题
                .setContentTitle("蓝牙打印")
                //设置通知内容
                .setContentText("打印服务正在后台运行中");
        notifyManager.notify(NOTIFICATION, builder.build());
        startForeground(NOTIFICATION, builder.getNotification());


    }


    public PrintDataService(Context context, String deviceAddress, TextView textView, Button send, Button serean) {
        super();
        this.button1 = send;
        this.button2 = serean;
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


    public static void disconnect() {
        try {
            bluetoothSocket.close();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendInfo() {
        printutils.sendInfo("打印机测试" + "\n\n\n\n");
        //BluetoothServiceSend
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
    @Override
    public int onStartCommand(Intent ietent, int flags, int starId) {
        return Service.START_STICKY;
    }

    public void dataProcessing() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    wsC.connect(socketContentURL, new WebSocketHandler() {
                        @Override
                        public void onOpen() {
                            Log.e("Socket", "执行一次");
                        }

                        @Override
                        public void onTextMessage(String payload) {
                            jsonObject = JSONObject.fromObject(payload);
                            Log.e("Socket", jsonObject + "");
                            order = jsonObject.getJSONObject("order");
                            order_type = order.getString("order_type");
                            DataOrder(jsonObject, order_type, order);
                        }
                        @Override
                        public void onClose(int code, String reason) {
                            Log.e("onClose", "socket 断开");
                            dataProcessing();
                        }
                    });
                } catch (WebSocketException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void DataOrder(JSONObject jsonObject, String stringdata, JSONObject order) {
        printutils.IntentOutPut(wsC,outputStream);
        if (order.has("msg")) {
            msg_service = order.getString("msg");
            string = stringdata;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    switch (string) {
                        case "bind":
                            Log.e("banding", "绑定线程");
//                            new Thread(new Runnable() {
//                                @Override
//                                public void run() {
                            printutils.bind(msg_service, contextprint);
//                                }
//                            }).start();
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
            switch (stringdata) {
                case takoutFood:
                    Log.e("Socket", "外卖");
                    jsonObjectThread = jsonObject;
                    orderThread = order;
                    for (int i = 0; i < PrintDataActivity.shulian; i++) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                printutils.waimai(jsonObjectThread, orderThread);
                            }
                        }).start();

                        util.Delayed(3000);
                    }
                    break;
                case restaurant:
                    Log.e("Socket", "店内");
                    jsonObjectThread = jsonObject;
                    orderThread = order;
                    for (int i = 0; i < PrintDataActivity.shulian; i++) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                printutils.diannei(jsonObjectThread, orderThread);
                            }
                        }).start();
                        util.Delayed(3000);
                    }
                    break;
                case restaurantWaiter:
                    Log.e("Socket", "店内服务员");
                    jsonObjectThread = jsonObject;
                    orderThread = order;
                    for (int i = 0; i < PrintDataActivity.shulian; i++) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                printutils.waiterdiannei(jsonObjectThread, orderThread);
                            }
                        }).start();
                        util.Delayed(3000);
                    }
                    break;
                case resevre:
                    Log.e("Socket", "预定到店");
                    jsonObjectThread = jsonObject;
                    orderThread = order;
                    for (int i = 0; i < PrintDataActivity.shulian; i++) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                printutils.yuding(jsonObjectThread, orderThread);
                            }
                        }).start();
                        util.Delayed(3000);
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

//    Handler handler = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//            switch (msg.what) {
//                case SHOW:
////                    createFloatView();
//                    break;
//                default:
//                    break;
//
//            }
//        }





    }

