package com.a91zsc.www.myapplication.service;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.Toast;

import com.a91zsc.www.myapplication.util.toolsFileIO;

public class PrintDataService {
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
//	final String[] items = { "复位打印机", "标准ASCII字体", "压缩ASCII字体", "字体不放大",
//			"宽高加倍", "取消加粗模式", "选择加粗模式", "取消倒置打印", "选择倒置打印", "取消黑白反显", "选择黑白反显",
//			"取消顺时针旋转90°", "选择顺时针旋转90°" };
//	final byte[][] byteCommands = { { 0x1b, 0x40 },// 复位打印机
//			{ 0x1b, 0x4d, 0x00 },// 标准ASCII字体
//			{ 0x1b, 0x4d, 0x01 },// 压缩ASCII字体
//			{ 0x1d, 0x21, 0x00 },// 字体不放大
//			{ 0x1d, 0x21, 0x11 },// 宽高加倍
//			{ 0x1b, 0x45, 0x00 },// 取消加粗模式
//			{ 0x1b, 0x45, 0x01 },// 选择加粗模式
//			{ 0x1b, 0x7b, 0x00 },// 取消倒置打印
//			{ 0x1b, 0x7b, 0x01 },// 选择倒置打印
//			{ 0x1d, 0x42, 0x00 },// 取消黑白反显
//			{ 0x1d, 0x42, 0x01 },// 选择黑白反显
//			{ 0x1b, 0x56, 0x00 },// 取消顺时针旋转90°
//			{ 0x1b, 0x56, 0x01 },// 选择顺时针旋转90°
//	};

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
                Toast.makeText(this.context, "发送失败！", Toast.LENGTH_SHORT)
                        .show();
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