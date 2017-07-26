package com.a91zsc.www.myapplication.service;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.a91zsc.www.myapplication.R;
import com.a91zsc.www.myapplication.action.PrintDataAction;
import com.a91zsc.www.myapplication.util.blueToothsearch;
import com.a91zsc.www.myapplication.util.showTime;
import com.a91zsc.www.myapplication.util.toolsFileIO;
import com.a91zsc.www.myapplication.view.PrintDataActivity;

import static android.content.ContentValues.TAG;


public class PrintDataService extends Service {
    //    implements Runnable
    private Context context;
    private String deviceAddress = null;
    private BluetoothAdapter bluetoothAdapter = BluetoothAdapter
            .getDefaultAdapter();
    private toolsFileIO fileIO = new toolsFileIO();
    private BluetoothDevice device = null;
    private static BluetoothSocket bluetoothSocket = null;
    private static OutputStream outputStream = null;
    private static final UUID uuid = UUID
            .fromString("00001101-0000-1000-8000-00805F9B34FB");
    public boolean isConnection = false;
    Intent intent;
    private Button button1;
    private Button button2;
    public TextView textView;
    private ConnectivityManager connManager = null;
    private NetworkInfo networkInfo = null;
    private showTime  showTime = new showTime();
    private boolean connectionCode = true;
    private boolean netWorkIs = false;
    private static final int CURRENT_NETWORK_STATES_NO = -1;//  没有网络
    private static final int CURRENT_NETWORK_STATES_WIFI = 1;// WIFI网络
    private static final int CURRENT_NETWORK_STATES_WAP = 2;// wap网络
    private static final int CURRENT_NETWORK_STATES_NET = 3;// net网络
    public PrintDataService(){

    }
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * 当服务被kill 的时候会启动该服务
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        context.startService(intent);
    }
    public void ondestroy(){
        super.onDestroy();
    }

    public PrintDataService(Context context, String deviceAddress,TextView textView,Button send ,Button serean) {
        super();
        this.button1 =send;
        this.button2 = serean;
        this.context = context;
        this.deviceAddress = deviceAddress;
        this.textView = textView;
        this.device = this.bluetoothAdapter.getRemoteDevice(this.deviceAddress);
    }

    /**
     * 获取设备名称
     *
     * @return String
     */
    public String getDeviceName() {
        return this.device.getName();
    }

    /**
     * 蓝牙连接
     *
     * @return
     */
    public boolean connect() {
        if (!this.isConnection) {
            try {
                bluetoothSocket = this.device.createRfcommSocketToServiceRecord(uuid);
                this.bluetoothSocket.connect();
                this.outputStream = bluetoothSocket.getOutputStream();
                fileIO.putBlueToothDrive(context, device.toString());
                return this.isConnection =true;
            } catch (Exception e) {
                Toast.makeText(this.context, "设备连接异常！", Toast.LENGTH_SHORT).show();
                return  false;
            }
        }
        return isConnection;
    }

    /**
     * 获取当前网络状况
     */
        public boolean getAPNType(Context context,Boolean isNetWork) {
            this.connManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            this.networkInfo = connManager.getActiveNetworkInfo();
            int isnetwork = networkInfo.getType();
            if(isnetwork!=CURRENT_NETWORK_STATES_NO&&isNetWork){
               this.netWorkIs = true;
            }
            return netWorkIs;
//            if (networkInfo == null) {
//                return netType;
//            }
//            if (nType == ConnectivityManager.TYPE_MOBILE) {
//                netType = networkInfo.getExtraInfo().toLowerCase().equals("cmnet") ? CURRENT_NETWORK_STATES_NET : CURRENT_NETWORK_STATES_WAP;
//            } else if (nType == ConnectivityManager.TYPE_WIFI) {
//                netType = CURRENT_NETWORK_STATES_WIFI;
//            }
//            return netType;
        }

    /**
     * 断开蓝牙设备连接
     */
    public static void disconnect() {
        try {
            bluetoothSocket.close();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 向蓝牙推送数据
     *
     * @param sendData
     * @param command
     */
    public void send(String sendData, byte[] command) {
        if (isConnection) {
            try {
                byte[] data = sendData.getBytes("gbk");
                outputStream.write(command);
                outputStream.write(data, 0, data.length);
            } catch (IOException e) {
                showConnect();
            }
        } else {
            Toast.makeText(this.context, "设备未连接，请重新连接！", Toast.LENGTH_SHORT)
                    .show();
        }
    }

    /**
     * outputStream接收数据
     */
    public void sendInfo(String sendData) {
        this.showConnect();
                if (isConnection) {
                    try {
                        byte[] data = sendData.getBytes("gbk");
                        outputStream.write(data, 0, data.length);
                        outputStream.flush();
                    } catch (IOException e) {
                        showConnect();
                    }
                }else{
                    Toast.makeText(this.context, "设备未连接，请重新连接！", Toast.LENGTH_SHORT)
                            .show();
                }
            }
        /**
         * 触发蓝牙判断机制
         */

    public void showConnect(){
        this.isConnection = false;
        this.disconnect();
        showTime.Delayed(500);
        try{
            while (true) {
                button1.setEnabled(false);
                button2.setEnabled(false);
                if (this.connect()) {
                    if(connectionCode){
                        this.textView.setText(this.getDeviceName()+" 连接成功");
                        this.connectionCode = false;
                    }
                    button1.setEnabled(true);
                    button2.setEnabled(true);
                    break;
                }
            }
        }catch (Exception e){
            textView.setText("蓝牙连接异常！请检测蓝牙设备！");


        }

        Log.e("Connetct","连接成功");
    }
    /**
     * 进程拉活
     * @param ietent
     * @param flags
     * @param starId
     * @return
     */
    public int onStartCommand(Intent ietent,int flags,int starId){
        return Service.START_STICKY;
    }

}