package com.a91zsc.www.myapplication.view;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.a91zsc.www.myapplication.R;
import com.a91zsc.www.myapplication.application.CustomApplication;
import com.a91zsc.www.myapplication.service.BluetoothService;
import com.a91zsc.www.myapplication.util.showTime;
import com.a91zsc.www.myapplication.util.toolsFileIO;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.a91zsc.www.myapplication.string.staticBluetoothData.URLJSON;
import static com.a91zsc.www.myapplication.string.staticBluetoothData.versionUpdateURL;

public class BluetoothActivity extends Activity implements View.OnClickListener {
    public Context context;
    private String urlString = null;
    private Uri UriConnection = null;
    private Intent intent;
    private PopupWindow showItem;
    private Button serblue, deition;
    private BluetoothService bluetoothService;
    public JSONObject order_type_user, order_type_order;
    private ImageButton pinuser;
    private String[] hobbies;

    private String id = null, type = null;
    ListView unbondDevices, bondDevices;
    private JSONArray array = null;
    private JSONObject object = new JSONObject();
    private JSONArray arrayItem = null;
    private JSONObject objectItem = new JSONObject();
    toolsFileIO TFO = new toolsFileIO();
    boolean isShow = true;
    boolean bool = true;
    showTime util = new showTime();
    String UserName;
    private boolean[] jsonarraydata;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bluetooth_layout);
        context = getApplicationContext();
        ((CustomApplication) getApplication()).setInstance(this);
        postJsonService();
        deition = (Button) findViewById(R.id.deition);
        serblue = (Button) findViewById(R.id.searchDevices);
        unbondDevices = (ListView) findViewById(R.id.unbondDevices);
        bondDevices = (ListView) findViewById(R.id.bondDevices);
        pinuser = (ImageButton) findViewById(R.id.pinUser);
        deition.setOnClickListener(this);
        serblue.setOnClickListener(this);
        pinuser.setOnClickListener(this);
        bluetoothService = new BluetoothService(context, unbondDevices, bondDevices);
        getVersionName();
        deitionCode();
        startService(new Intent(context, BluetoothService.class));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.deition:
                VersioUpdate();
                break;
            case R.id.searchDevices:
                bluetoothService.searchDevices();
                break;
            case R.id.pinUser:
                if (isShow) {
                    showItemUser();
                }
                break;
            case R.id.linearlayoutid:
                showItem.dismiss();
                isShow = true;
                break;
            default:
                break;
        }

    }

    public void showItemUser() {
        try {
            isShow = false;
            final View contentView = LayoutInflater.from(BluetoothActivity.this).inflate(R.layout.user_item, null);
            this.showItem = new PopupWindow(contentView);
            showItem.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
            showItem.setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
            showItem.showAsDropDown(pinuser, 100, 0);
            showItem.setOutsideTouchable(true);
            Button order = (Button) contentView.findViewById(R.id.Order);
            order.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if (util.getAPNType(context)) {
                        showItem();
                    } else
                        Toast.makeText(context, "无网络访问", Toast.LENGTH_LONG).show();
                }
            });
            Button exit = (Button) contentView.findViewById(R.id.exit);
            exit.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    android.os.Process.killProcess(android.os.Process.myPid());    //获取PID
                    System.exit(0);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void postJsonService() {
        if (array == null) {
            array = new JSONArray();
            UserName = TFO.getSharedPreferencesuser(context);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        OkHttpClient client = new OkHttpClient();
                        RequestBody requestBody = new FormBody.Builder()
                                .add("account", UserName)
                                .build();
                        Request request = new Request.Builder()
                                .url(URLJSON)
                                .post(requestBody)
                                .build();
                        Response response = client.newCall(request).execute();
                        String responseData = response.body().string();
                        JSONObject jsonObject = JSONObject.fromObject(responseData);
                        JSONObject orderData = jsonObject.getJSONObject("data");
                        JSONArray goodsType = orderData.getJSONArray("goodsType");
                        JSONArray partner = orderData.getJSONArray("partner");
                        for (int i = 0; i < goodsType.size(); i++) {
                            order_type_order = goodsType.getJSONObject(i);
                            wihiteList(order_type_order);
                        }
                        for (int i = 0; i < partner.size(); i++) {
                            order_type_user = partner.getJSONObject(i);
                            wihiteList(order_type_user);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    /**
     * 把用户分类添加到Json中
     *
     * @param jsonObject
     */
    public void wihiteList(JSONObject jsonObject) {
        this.id = jsonObject.getString("id");
        this.type = jsonObject.getString("name");
        object.put("id", id);
        object.put("type", type);
        array.add(object);
    }

    /**
     * 把Json中的数组放至到数组中
     *
     * @param jsonarray
     * @return
     */
    public final String[] getStringJson(JSONArray jsonarray) {
        hobbies = new String[jsonarray.size()];
        for (int i = 0; i < jsonarray.size(); i++) {
            JSONObject ajson = jsonarray.getJSONObject(i);
            hobbies[i] = ajson.getString("type");
        }
        return hobbies;
    }

    public void getBooleanJson(String[] hobbiesjson) {
        final boolean[] jsonarray = new boolean[hobbiesjson.length];
        JSONArray data = TFO.getIsText(context);
        if (data != null) {
            for (int i = 0; i < data.size(); i++) {
                JSONObject json = data.getJSONObject(i);
                String is = json.getString("choice");
                if(is.equals("0")){
                    jsonarray[i] = false;
                }else {
                    jsonarray[i] = true;
                }
            }
        } else {
            for (int i = 0; i < hobbiesjson.length; i++) {
                jsonarray[i] = true;
            }
        }
        this.jsonarraydata = jsonarray;
    }


    public void showItem() {
        try {
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(BluetoothActivity.this);
            builder.setTitle("分类打印");
            final String[] hobbiesjson = getStringJson(array);
            if (bool) {
                getBooleanJson(hobbiesjson);
            }
            bool = false;
            if (arrayItem != null) {
                arrayItem.clear();
            }
            /**
             * 第一个参数指定我们要显示的一组下拉多选框的数据集合
             * 第二个参数代表哪几个选项被选择，如果是null，则表示一个都不选择，如果希望指定哪一个多选选项框被选择，
             * 需要传递一个boolean[]数组进去，其长度要和第一个参数的长度相同，例如 {true, false, false, true};
             * 第三个参数给每一个多选项绑定一个监听器
             */
            builder.setMultiChoiceItems(hobbiesjson, jsonarraydata, new DialogInterface.OnMultiChoiceClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                    if (isChecked) {
                        jsonarraydata[which] = true;
                    } else {
                        jsonarraydata[which] = false;
                    }

                }
            });
            builder.setPositiveButton("保存", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    arrayItem = new JSONArray();
                    for (int i = 0; i < jsonarraydata.length; i++) {
                        if (jsonarraydata[i]) {
                            objectItem.put("type", hobbiesjson[i]);
                            arrayItem.add(objectItem);
                        }
                    }
                    bluetoothService.whilteIsText(jsonarraydata);
                    bluetoothService.getTypeData(arrayItem, array,jsonarraydata.length);
                }
            });
            builder.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void VersioUpdate() {
        try {
            intent = new Intent(Intent.ACTION_VIEW, UriConnection);
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void getVersionName() {
        deition.setText("版本:" + getPackageInfo(context).versionName);
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

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

    public void deitionconfig(JSONObject jsonObject) {
        if (jsonObject.getString("versionName").equals(getPackageInfo(context).versionName)) {
            deition.setEnabled(false);
            deition.setTextColor(Color.parseColor("#BBBBBB"));
        } else {
            urlString = jsonObject.getString("downloadUrl");
            UriConnection = Uri.parse(urlString);
            deition.setText("版本更新");
            deition.setEnabled(true);
            deition.setTextColor(Color.parseColor("#FF0000"));
            new AlertDialog.Builder(this)
                    .setTitle("提示")
                    .setMessage(jsonObject.getString("msg"))
                    .setPositiveButton("下载更新", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            VersioUpdate();
                        }
                    })
                    .show();

        }
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
}
