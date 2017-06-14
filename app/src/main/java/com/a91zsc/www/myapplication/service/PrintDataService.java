package com.a91zsc.www.myapplication.service;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

import android.app.AlertDialog;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

import com.a91zsc.www.myapplication.util.toolsFileIO;


public class PrintDataService extends Service {
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
    private boolean isConnection = false;
    Intent intent;
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }


    /**
     * 当服务被kill 的时候会启动该服务
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        context.startService(intent);

    }
    public PrintDataService(Context context, String deviceAddress) {
        super();
        this.context = context;
        this.deviceAddress = deviceAddress;
        //返回相应的被制定的蓝牙连接的远端设备
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
                //连接蓝牙
                bluetoothSocket = this.device.createRfcommSocketToServiceRecord(uuid);
                bluetoothSocket.connect();
                fileIO.putBlueToothDrive(context, device.toString());
                outputStream = bluetoothSocket.getOutputStream();
                this.isConnection = true;
            } catch (Exception e) {
                Toast.makeText(this.context, "连接失败！", Toast.LENGTH_SHORT).show();
                return false;
            }
            return true;
        } else {
            return true;
        }
    }

    public void contentChon(BluetoothDevice bluetoothDevice) {

        try {
            bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(uuid);
            bluetoothSocket.connect();
            fileIO.putBlueToothDrive(context, device.toString());
            outputStream = bluetoothSocket.getOutputStream();

        } catch (Exception e) {
            Toast.makeText(this.context, "连接失败！", Toast.LENGTH_SHORT).show();
        }

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


    //向打印机BuleToothDivcer发送数据
    public void send(String sendData, byte[] command) {
        if (this.isConnection) {
            try {
                byte[] data = sendData.getBytes("gbk");
                outputStream.write(command);
                outputStream.write(data, 0, data.length);
                outputStream.flush();
            } catch (IOException e) {
            }
        } else {
            Toast.makeText(this.context, "设备未连接，请重新连接！", Toast.LENGTH_SHORT)
                    .show();
        }
    }

    //outputStream接收数据
    public void sendInfo(String sendData) {
        if (this.isConnection) {
            try {
                byte[] data = sendData.getBytes("gbk");
                outputStream.write(data, 0, data.length);
                outputStream.flush();
            } catch (IOException e) {
                Toast.makeText(this.context, "发送失败！", Toast.LENGTH_SHORT)
                        .show();
            }
        } else {
            Toast.makeText(this.context, "设备未连接，请重新连接！", Toast.LENGTH_SHORT)
                    .show();

        }
    }

}