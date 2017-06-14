package com.a91zsc.www.myapplication.view;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.a91zsc.www.myapplication.R;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.os.SystemClock.sleep;

public class LoginActivity extends AppCompatActivity {

    private EditText accountEdit;

    private EditText passwordEdit;

    private Button login;

    private OkHttpClient client;

    private Response response = null;
    private String responseData = null;
    private ProgressDialog progressDialog;
    private Context context;
    Handler mHandler = new Handler();
    Intent intent = new Intent();
    private int intDate = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences pref1 = getSharedPreferences("data", MODE_PRIVATE);
        String account = pref1.getString("account", "");

        if (!(account == null || account.length() <= 0)) {
            intent = new Intent(LoginActivity.this, BluetoothActivity.class);
            startActivity(intent);
            finish();
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getSupportActionBar().hide();
        accountEdit = (EditText) findViewById(R.id.account);
        passwordEdit = (EditText) findViewById(R.id.password);
        login = (Button) findViewById(R.id.login);

        login.setOnClickListener(new View.OnClickListener() {
                                     @Override
                                     public void onClick(View v) {
                                         System.out.println("间隔俩秒");
                                         final String account = accountEdit.getText().toString();
                                         final String password = passwordEdit.getText().toString();
                                         new Thread(new Runnable() {
                                             @Override
                                             public void run() {
                                                 try {
                                                     client = new OkHttpClient();
                                                     RequestBody requestBody = new FormBody.Builder()
                                                             .add("account", account)
                                                             .add("password", password)
                                                             .build();
                                                     Request request = new Request.Builder()
                                                             .url("https://www.91zsc.com/Home/Print/login")
                                                             .post(requestBody)
                                                             .build();
                                                     response = client.newCall(request).execute();
                                                     responseData = response.body().string();
                                                     if (responseData.equals("oo")) {
                                                         Log.e("loginoo", responseData);
                                                         SharedPreferences.Editor editor1 = getSharedPreferences("data", MODE_PRIVATE).edit();
                                                         editor1.putString("acc", account);
                                                         editor1.putString("password", password);
                                                         editor1.apply();
                                                         Intent intent = new Intent(LoginActivity.this, BluetoothActivity.class);
                                                         startActivity(intent);
                                                         finish();
                                                     } else {
                                                         Log.e("loginxx", responseData);
                                                     }

                                                 } catch (Exception e) {
                                                     e.printStackTrace();

                                                 }
                                             }
                                         }).start();

                                     }
                                 }

        );
    }
}