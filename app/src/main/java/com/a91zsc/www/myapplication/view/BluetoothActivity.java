package com.a91zsc.www.myapplication.view;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.a91zsc.www.myapplication.R;
import com.a91zsc.www.myapplication.service.BluetoothService;

import net.sf.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.a91zsc.www.myapplication.string.staticBluetoothData.versionUpdateURL;

public class BluetoothActivity extends Activity {
    LinearLayout linearLayout;
    public Context context;
    private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private String url = "";
    Uri uri = null;
    public static Button serblue;
    public static Button deition;
    //third
//    private static final String STRURL = "https://wwww.91zsc.com/PrintApp/version.json";
    private BluetoothService bluetoothService;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = this;
        openBlue();
        startService(new Intent(context, BluetoothService.class));
        setContentView(R.layout.bluetooth_layout);
        this.initListener();
        deitionCode();
        this.linearLayout = (LinearLayout) findViewById(R.id.deitionlinear);
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
    }

    public void VersioUpdate() {
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    public static String getVersionName(Context context) {
        deition.setText("版本:" + getPackageInfo(context).versionName);
        return getPackageInfo(context).versionName;
    }

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
        bluetoothService = new BluetoothService(this.context, unbondDevices, bondDevices);
    }

    //屏蔽返回键的代码:
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
                    URL url = new URL(versionUpdateURL);
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setConnectTimeout(5000);
                    urlConnection.setReadTimeout(5000);
                    urlConnection.setRequestMethod("GET");
                    urlConnection.connect();
                    int code = urlConnection.getResponseCode();
                    if (code == 200) {
                        InputStream inStream = urlConnection.getInputStream();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(inStream));
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
//            deition.setTextColor(Color.parseColor("#494949"));
            deition.setTextColor(Color.parseColor("#BBBBBB"));
        } else {
            this.url = jsonObject.getString("downloadUrl");
            uri = Uri.parse(url);
            deition.setText("版本更新");
            deition.setEnabled(true);
//            linearLayout.setBackgroundColor(Color.parseColor("#00FF00"));
            deition.setTextColor(Color.parseColor("#FF0000"));
//            new AlertDialog.Builder(this)
//                    .setTitle("提示")
////                    .setMessage(jsonObject.getString("msg"))
//                    .setMessage("当前系统有最新版本的BLUETOOTH 推送，更新了XX特征，修复了，xxxxBUG")
//                    .setPositiveButton("下载更新",new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            VersioUpdate();
//                        }
//                    })
//                    .show();

        }
    }
}
