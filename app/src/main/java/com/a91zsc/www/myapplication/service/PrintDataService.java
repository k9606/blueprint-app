package com.a91zsc.www.myapplication.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.a91zsc.www.myapplication.R;
import com.a91zsc.www.myapplication.application.CustomApplication;
import com.a91zsc.www.myapplication.util.printUtils;
import com.a91zsc.www.myapplication.util.showTime;
import com.a91zsc.www.myapplication.view.PrintDataActivity;

import net.sf.json.JSONObject;

import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketException;
import de.tavendo.autobahn.WebSocketHandler;

import static com.a91zsc.www.myapplication.string.staticBluetoothData.BluetoothSocketSend;
import static com.a91zsc.www.myapplication.string.staticBluetoothData.BluetoothprintSend;
import static com.a91zsc.www.myapplication.string.staticBluetoothData.GRAY_SERVICE_ID;
import static com.a91zsc.www.myapplication.string.staticBluetoothData.heartbeat;
import static com.a91zsc.www.myapplication.string.staticBluetoothData.msgService;
import static com.a91zsc.www.myapplication.string.staticBluetoothData.resevre;
import static com.a91zsc.www.myapplication.string.staticBluetoothData.restaurant;
import static com.a91zsc.www.myapplication.string.staticBluetoothData.restaurantWaiter;
import static com.a91zsc.www.myapplication.string.staticBluetoothData.serviceBind;
import static com.a91zsc.www.myapplication.string.staticBluetoothData.serviceTest;
import static com.a91zsc.www.myapplication.string.staticBluetoothData.socketContentURL;
import static com.a91zsc.www.myapplication.string.staticBluetoothData.takoutFood;


public class PrintDataService extends Service {
    public static Context context;
    public showTime util = new showTime();
    public printUtils printutils = new printUtils();
    public MediaPlayer mediaPlayer;
    public WebSocketConnection wsC = new WebSocketConnection();
    public JSONObject order;
    public static JSONObject jsonObject = null;
    private String BluetoothIntent, order_type = null;
    public int in = 1;

    public IBinder onBind(Intent intent) {
        return null;
    }

    public static final Intent INTENTSERVIC = new Intent();
    ;

    public PrintDataService() {
    }

    public void cleatr() {
        if (jsonObject != null) {
            order.clear();
            jsonObject.clear();
            order_type = null;
        }

    }

    @Override
    public void onCreate() {
        super.onCreate();
//        startForeground(this);
    }


    public static void startForeground(Service context) {
        NotificationManager nm = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setContentTitle("蓝牙服务")
                .setContentText("蓝牙打印机正在运行中")
                .setWhen(System.currentTimeMillis())
                .setPriority(Notification.PRIORITY_MIN)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(true);
        Notification notification = builder.build();
        context.startForeground(GRAY_SERVICE_ID, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        dataProcessing();
        Log.e("服务器链接", "执行一次");
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopSelf();
    }


    public PrintDataService(Context contextactivity, String Buletooth) {
        this.context = contextactivity;
        BluetoothIntent = Buletooth;
    }

    public void printTest() {
        printutils.printTextTest(BluetoothprintSend + "\n\n\n\n");
    }

    public void dataProcessing() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    wsC.connect(socketContentURL, new WebSocketHandler() {
                        @Override
                        public void onOpen() {
                            printutils.IntentOutPut(wsC);
                        }
                        @Override
                        public void onTextMessage(String payload) {
                            if (payload.equals("") || payload.equals(null)) {
                            } else {
                                DataOrder(payload);
                            }
                        }

                        @Override
                        public void onClose(int code, String reason) {
                            Log.e("服务器断开", "oncleason" + code + reason);
                            if (PrintDataActivity.BluetoothIntent != null && util.getAPNType(context)) {
                                dataProcessing();
                            } else {
                                if(PrintDataActivity.BluetoothIntent != null){
                                    send();
                                }
                                printUtils.webSocketConnection = null;
                            }
                        }
                    });
                } catch (WebSocketException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void send() {
        Intent intent = new Intent();
        intent.setAction("android.netWork.onclose");
//        context.sendBroadcast(intent);
        context.sendOrderedBroadcast(intent, null);
    }


    public void DataOrder(String payload) {
        this.jsonObject = JSONObject.fromObject(payload);
        order = jsonObject.getJSONObject("order");
        order_type = order.getString("order_type");
        if (order.has(msgService)) {
            String msg_service = order.getString("msg");
            switch (order_type) {
                case serviceBind:
                    printutils.bind(msg_service, context);
                    break;
                case serviceTest:

                    if (PrintDataActivity.shuliang > 1) {
                        for (int i = 0; i < PrintDataActivity.shuliang; i++) {
                            printutils.printTextTest(BluetoothSocketSend + "\n\n\n\n");
                            util.Delayed(2000);
                        }
                    } else {
                        printutils.printTextTest(BluetoothSocketSend + "\n\n\n\n");
                    }
                    break;
                case heartbeat:
                    printutils.heartbeat();
                    break;
                default:
                    break;
            }
        } else {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    in = PrintDataActivity.shuliang;
                    switch (order_type) {
                        case takoutFood:
                            printutils.waimai(jsonObject, order, in);
                            break;
                        case restaurant:
                            printutils.diannei(jsonObject, order, in);
                            break;
                        case restaurantWaiter:
                            printutils.waiterdiannei(jsonObject, order, in);
                            break;
                        case resevre:
                            printutils.yuding(jsonObject, order, in);
                            break;
                        default:
                            break;
                    }
                }
            }).start();
        }
    }

    public void serviceTest() {
        printutils.serviceSendtTest();
    }
}

