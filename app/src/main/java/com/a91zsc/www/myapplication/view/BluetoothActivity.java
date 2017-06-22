package com.a91zsc.www.myapplication.view;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.a91zsc.www.myapplication.R;
import com.a91zsc.www.myapplication.action.BluetoothAction;
import com.a91zsc.www.myapplication.util.XMLTool;

import net.sf.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class BluetoothActivity extends Activity {

    LinearLayout linearLayout;
    public Context context;
    BluetoothAdapter mBluetoothAdapter;
    private Button searchDevices;
    private String url = "";
    Uri uri = null;
    public static Button deition;

    public void onCreate(Bundle savedInstanceState) {
        this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        super.onCreate(savedInstanceState);
        this.context = this;
        openBlue();
        setContentView(R.layout.bluetooth_layout);
        this.initListener();
        testCode();
        this.linearLayout = (LinearLayout) findViewById(R.id.deitionlinear);
        deition = (Button) findViewById(R.id.deition);
        deition.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sendXml();
            }
        });
    }


    public void sendXml() {
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    public static String getVersionName(Context context) {
        deition.setText("版本" + getPackageInfo(context).versionName);

        return getPackageInfo(context).versionName;
    }

    public void openBlue() {
        if (mBluetoothAdapter.isEnabled()) {
        } else {
            mBluetoothAdapter.enable();
        }
    }

    public void onStart() {
        super.onStart();
        mBluetoothAdapter.startDiscovery();
    }


    /**
     * 初始化点击事件
     */
    private void initListener() {
        this.searchDevices = (Button) this.findViewById(R.id.searchDevices);
        ListView unbondDevices = (ListView) this.findViewById(R.id.unbondDevices);
        ListView bondDevices = (ListView) this.findViewById(R.id.bondDevices);
        BluetoothAction bluetoothAction = new BluetoothAction(this.context,
                unbondDevices, bondDevices, searchDevices,
                BluetoothActivity.this);
        bluetoothAction.setSearchDevices(searchDevices);
        searchDevices.setOnClickListener(bluetoothAction);
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
    public String testCode() {
        new Thread() {
            public void run() {
                String strUrl = "https://third.91zsc.com/PrintApp/version.json";
                try {
                    URL url = new URL(strUrl);
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setConnectTimeout(5000);
                    //设置对象链接超时
                    urlConnection.setReadTimeout(5000);
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
            deition.setTextColor(Color.parseColor("#494949"));
        } else {
            this.url = jsonObject.getString("downloadUrl");
            uri = Uri.parse(url);

            deition.setText("  版本更新");
            deition.setEnabled(true);
//            deition.setBackgroundResource(R.drawable.button_edition_red);
            linearLayout.setBackgroundColor(Color.parseColor("#00FF00"));
            deition.setTextColor(Color.parseColor("#FFFFFF"));
//            deition.setTextColor(@c);
        }
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
}
