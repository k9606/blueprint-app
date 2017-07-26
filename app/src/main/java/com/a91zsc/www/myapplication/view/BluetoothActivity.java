package com.a91zsc.www.myapplication.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Layout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.a91zsc.www.myapplication.R;
import com.a91zsc.www.myapplication.service.BluetoothService;
import com.a91zsc.www.myapplication.service.PrintDataService;
import com.a91zsc.www.myapplication.util.showTime;
import com.a91zsc.www.myapplication.util.toolsFileIO;

import net.sf.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import okhttp3.OkHttpClient;

public class BluetoothActivity extends Activity {
    public Context context;
    private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private String url = "";
    Uri uri = null;
    toolsFileIO TFO = new toolsFileIO();
    public static Button serblue;
    public static Button deition;
    private static final String STRUL = "https://www.91zsc.com/PrintApp/version.json";
    private BluetoothService bluetoothService ;
    private LinearLayout barlayout;
    private Toast toast = null;
    private showTime showTime = new showTime();

    /**
     * 初始化
     * @param savedInstanceState
     */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = this;
        openBlue();
        isuserpassworld();
        startService(new Intent(context, BluetoothService.class));
        setContentView(R.layout.bluetooth_layout);
        this.initListener();

        deitionCode();
        deition = (Button) findViewById(R.id.deition);
        deition.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                VersioUpdate();
            }
        });
        serblue = (Button) findViewById(R.id.searchDevices);
        serblue.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                bluetoothService.searchDevices();
            }
        });
//        mBluetoothAdapter.startDiscovery();
        openBluetootjScovery();

    }

    /**
     * 打开蓝牙搜索
     */
    public void openBluetootjScovery() {
        mBluetoothAdapter.startDiscovery();
        toast  = Toast.makeText(context, "正在搜索...", Toast.LENGTH_LONG);
        showTime.showToast(toast,13000);
        Toast.makeText(context, "搜索完成", Toast.LENGTH_SHORT).show();
    }
    /**
     * 用户名密码检测
     */
    public void isuserpassworld(){
        TFO.ishttp(context);
    }
    /**
     * 版本更新
     */
    public void VersioUpdate() {
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    /**
     * 获取当前版本信息
     * @param context
     * @return
     */
    public static String getVersionName(Context context) {
        deition.setText("版本 " + getPackageInfo(context).versionName);
        return getPackageInfo(context).versionName;
    }
    /**
     * 打开蓝牙设备
     */
    public void openBlue() {
            mBluetoothAdapter.enable();
    }
    /**
     * 初始化点击事件
     */
    private void initListener() {
        Button button = (Button) findViewById(R.id.searchDevices);
        ListView unbondDevices = (ListView) this.findViewById(R.id.unbondDevices);
        ListView bondDevices = (ListView) this.findViewById(R.id.bondDevices);
        this.bluetoothService = new BluetoothService(this.context,unbondDevices,bondDevices);
    }
    /**
     * 屏蔽返回键
     * @param keyCode
     * @param event
     * @return
     */
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    /**
     * 获取当前版本号
     *
     * @param context
     * @return
     */
    private static PackageInfo getPackageInfo(Context context) {
        PackageInfo pi = null;
        try {
            PackageManager pm = context.getPackageManager();
            pi = pm.getPackageInfo(context.getPackageName(),
                    PackageManager.GET_CONFIGURATIONS);

            return pi;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return pi;
    }
    /**
     * 版本更新
     *
     * @return
     */
    public String deitionCode() {
        new Thread() {
            public void run() {
                try {
                    URL url = new URL(STRUL);
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    //设置对象链接超时
                    //获取对象获取超时
                    urlConnection.setRequestMethod("GET");
                    //设置本次请求方式
                    urlConnection.connect();
                    //链接
                    int code = urlConnection.getResponseCode();
                    if (code == 200) {
                        //获取本次网络请求的状态码
                        InputStream inStream = urlConnection.getInputStream();
                        //获取本次访问的输出流

                        BufferedReader reader = new BufferedReader(new InputStreamReader(inStream));
                        //创建一个BufferedReder，去读数据
                        String readLine;

                        StringBuffer buffer = new StringBuffer();
                        while ((readLine = reader.readLine()) != null) {
                            buffer.append(readLine);

                        }
                        String result = buffer.toString();
                        Message message = new Message();
                        message.what = 0;
                        message.obj = result;
                        handler.sendMessage(message);
                        inStream.close();

                    } else {
                        deition.setEnabled(false);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
        return null;
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            String getResult1 = (String) msg.obj;
            JSONObject json_test = JSONObject.fromObject(getResult1);
            deitionconfig(json_test);
        }
    };
    public void deitionconfig(JSONObject jsonObject) {
        if (jsonObject.getString("versionName").equals(getVersionName(context))) {
            deition.setEnabled(false);
            deition.setTextColor(Color.parseColor("#BBBBBB"));
        } else {
            this.url = jsonObject.getString("downloadUrl");
            uri = Uri.parse(url);

            new AlertDialog.Builder(this)
                    .setTitle("提示")
                    .setMessage(jsonObject.getString("msg"))
                    .setPositiveButton("下载更新",new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            VersioUpdate();
                        }
                    })
                    .show();
            deition.setText("版本更新");
            deition.setTextColor(Color.parseColor("#FF0000"));
            deition.setEnabled(true);
        }
    }
}
