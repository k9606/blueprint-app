package com.a91zsc.www.myapplication.view;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.graphics.Color;
import android.net.NetworkInfo.State;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.text.Layout;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketException;
import de.tavendo.autobahn.WebSocketHandler;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Toast;

import com.a91zsc.www.myapplication.R;
import com.a91zsc.www.myapplication.action.PrintDataAction;
import com.a91zsc.www.myapplication.service.PrintDataService;
import com.a91zsc.www.myapplication.util.printUtils;
import com.a91zsc.www.myapplication.util.showTime;
import com.a91zsc.www.myapplication.util.toolsFileIO;
import com.a91zsc.www.myapplication.util.utilsTools;

import android.support.v7.app.AppCompatActivity;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.Charset;
import java.sql.Date;
import java.text.SimpleDateFormat;

import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import static android.R.attr.action;
import static android.R.attr.filter;

public class PrintDataActivity extends AppCompatActivity implements View.OnClickListener {


    public boolean isNetWork = false;
    public Context context;
    public TextView connectState = null;
    PrintDataAction printDataAction;
    PrintDataService printDataService;
    private toolsFileIO fileIO = new toolsFileIO();
    private PopupWindow mPopWindow;
    public static int shulian = 1;
    private Button saveSet;
    private Button send;
    private Button command;

//    @Bind(R.id.start) Button start;
//    @Bind(R.id.wsStart) Button stop;
//    @Bind(R.id.SetData) Button bind;
//    @Bind(R.id.unbind) Button unbind;
//    private NetworkChangeReceiver networkChangeReceiver =new NetworkChangeReceiver();;


    /**
     * 打印三列时，第一列汉字最多显示几个文字
     */


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.printdata_layout);
        getSupportActionBar().hide();
        this.context = this;
        startService(new Intent(context, PrintDataService.class));
        this.connectState = (TextView) this.findViewById(R.id.connect_state);
        send = (Button) this.findViewById(R.id.send);
        command = (Button) this.findViewById(R.id.wsStart);
        saveSet = (Button) findViewById(R.id.SetData);
        printDataAction = new PrintDataAction(this.context, this.getDeviceAddress(), connectState, send, command);
//        IntentFilter intentFilter = new IntentFilter();
//        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
//        registerReceiver(networkChangeReceiver, intentFilter);
        send.setOnClickListener(this);
        command.setOnClickListener(this);
        saveSet.setOnClickListener(this);
        printDataService = printDataAction.getService();
        getData(context);
        ButterKnife.bind(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.wsStart:
                printDataService.sendTextMassage();
                break;
            case R.id.SetData:
                showPopupWindow();
                break;
            case R.id.send:
                printDataService.sendInfo();
                break;
            default:
                break;
        }

    }

    public void getData(Context context) {
        if (fileIO.getSetData(context) > 1) {
            this.shulian = fileIO.getSetData(context);
            saveSet.setText("小票设置  " + shulian);
        }
    }


    private void showPopupWindow() {
        final View contentView = LayoutInflater.from(PrintDataActivity.this).inflate(R.layout.popupwindow_item, null);
        this.mPopWindow = new PopupWindow(contentView);
        mPopWindow.setFocusable(true);
        mPopWindow.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        mPopWindow.setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
        final EditText editText = (EditText) contentView.findViewById(R.id.inttext);
        editText.setCursorVisible(false);
        editText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editText.setCursorVisible(true);
            }
        });
        Button cancel = (Button) contentView.findViewById(R.id.cancel);
        cancel.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                mPopWindow.dismiss();
            }
        });
        Button save = (Button) contentView.findViewById(R.id.saveSet);
        save.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    int data = Integer.parseInt(editText.getText().toString());
                    if (data > 10 || data == 0) {
                        Toast.makeText(context, "保存失败！数字大小在1-10范围内。", Toast.LENGTH_SHORT).show();
                    } else {
                        shulian = data;
                        fileIO.putSetData(context, data);
                        if (!(data == 1)) {
                            saveSet.setText("小票设置  " + shulian);
                        } else {
                            saveSet.setText("小票设置");
                        }
                        mPopWindow.dismiss();
                        Toast.makeText(context, "保存成功", Toast.LENGTH_SHORT).show();
                    }
                } catch (NumberFormatException e) {
                    mPopWindow.dismiss();
                }
            }
        });
        mPopWindow.showAsDropDown(connectState);
    }


    /**
     * 获得从上一个Activity传来的蓝牙地址
     *
     * @return String
     */
    public String getDeviceAddress() {
        Intent intent = this.getIntent();
        if (intent != null) {
            String Bluetooth = intent.getStringExtra("deviceAddress");
            return Bluetooth;
        } else {
            return null;
        }
    }

    /**
     * 关闭服务
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();

    }


    /**
     * 服务器断开重连
     */
    class NetworkChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager connectionManager = (ConnectivityManager)
                    getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectionManager.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isAvailable()) {
                if (isNetWork) {
                    isNetWork = false;
                    PrintDataActivity.this.recreate();
                }
            }
        }
    }


    private void toastLog(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }


    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            AlertDialog isExit = new AlertDialog.Builder(this).create();
            isExit.setMessage("确认退出?退出将导致连接中断！");
            isExit.setButton("确定", listener);
            isExit.setButton2("取消", listener);
            isExit.show();

        }

        return false;

    }

    /**
     * 监听对话框里面的button点击事件
     * 断开服务器 连接 清除sharedPreferences数据
     */
    DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case AlertDialog.BUTTON_POSITIVE:// "确认"按钮退出程序
                    Intent intent = new Intent(PrintDataActivity.this, BluetoothActivity.class);
                    fileIO.putBlueToothDrive(context, "");
                    startActivity(intent);
                    finish();
                    printDataAction.getService().onDestroy();
                    break;
                case AlertDialog.BUTTON_NEGATIVE:// "取消"第二个按钮取消对话框
                    break;
                default:
                    break;
            }
        }
    };


}

