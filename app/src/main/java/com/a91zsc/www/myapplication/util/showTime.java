package com.a91zsc.www.myapplication.util;import android.bluetooth.BluetoothDevice;import android.content.Context;import android.net.ConnectivityManager;import android.net.NetworkInfo;import android.net.Uri;import android.os.Handler;import android.util.Log;import android.webkit.WebView;import java.io.BufferedReader;import java.io.ByteArrayOutputStream;import java.io.InputStream;import java.io.InputStreamReader;import java.net.HttpURLConnection;import java.net.URL;import java.util.Timer;import java.util.TimerTask;import android.os.Message;import android.widget.Button;import android.widget.TextView;import android.widget.Toast;import net.sf.json.JSONObject;import static android.R.attr.button;import static android.R.attr.factor;import static android.R.attr.path;import static java.net.Proxy.Type.HTTP;/** * Created by yangx on 17.6.20. */public class showTime {    /**     * 提示显示时间     * @param toast     * @param cnt     */    public void showToast(final Toast toast, final int cnt) {        final Timer timer = new Timer();        timer.schedule(new TimerTask() {            @Override            public void run() {                toast.show();            }        }, 0,3000);        new Timer().schedule(new TimerTask() {            @Override            public void run() {                toast.cancel();                timer.cancel();            }        }, cnt );    }    private NetworkInfo networkInfo = null;    private  ConnectivityManager connManager;    public boolean getAPNType(Context context) {        this.connManager = (ConnectivityManager) context                .getSystemService(Context.CONNECTIVITY_SERVICE);        networkInfo = connManager.getActiveNetworkInfo();        if( networkInfo != null && networkInfo.isAvailable()){            return true;        }else{            return false;        }    }////    public void cancel(Boolean isboolean){//        this.Boolean = isboolean;//        Log.e("Hello","TESTOTWO");//        new Thread(new Runnable() {//            @Override//                public void run() {//                new Handler().post(new Runnable() {//                    @Override//                    public void run() {//                        toast.cancel();//                    }//                });//            }//        }).run();//    }    public void Delayed(int ms){        try {            Thread.currentThread();            Thread.sleep(ms);        } catch (InterruptedException e) {            e.printStackTrace();        }    }//    public void setTiew(TextView textView, BluetoothDevice device){//        textView.setText(device.getName()+DATA);//    }//    public void settextView(TextView textView, BluetoothDevice device){//        textView.setText(device.getName()+DATAON);//    }//    public void showText(Context context,String string){//        Toast.makeText(context, string, Toast.LENGTH_SHORT)//                .show();//    }}