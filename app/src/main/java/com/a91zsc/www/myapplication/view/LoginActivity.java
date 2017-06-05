package com.a91zsc.www.myapplication.view;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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

public class LoginActivity extends AppCompatActivity {

    private EditText accountEdit;

    private EditText passwordEdit;

    private Button login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences pref1 = getSharedPreferences("data",MODE_PRIVATE);
        String account = pref1.getString("account","");

        if(!(account == null || account.length() <= 0)){
            Intent intent = new Intent(LoginActivity.this,BluetoothActivity.class);
            startActivity(intent);
            finish();
        }
        super.onCreate(savedInstanceState);
        //this.setTitle("哎呀科技");
        setContentView(R.layout.activity_login);

        getSupportActionBar().hide();

        accountEdit = (EditText) findViewById(R.id.account);
        passwordEdit = (EditText) findViewById(R.id.password);
        login = (Button) findViewById(R.id.login);

//        String account = pref.getString("account", "");
//        String password = pref.getString("password", "");
//        accountEdit.setText(account);
//        passwordEdit.setText(password);
//        rememberPass.setChecked(true);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //SharedPreferences.Editor editor1 = getSharedPreferences("data",MODE_PRIVATE).edit();
                final String account = accountEdit.getText().toString();
                final String password = passwordEdit.getText().toString();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            OkHttpClient client = new OkHttpClient();
                            RequestBody requestBody = new FormBody.Builder()
                                    .add("account",account)
                                    .add("password",password)
                                    .build();
                            Request request = new Request.Builder()
                                    //.url("http://192.168.3.110/print-test/index.php/Home/index/login")
                                    .url("https://www.91zsc.com/Home/Print/login")
                                    .post(requestBody)
                                    .build();
                            Response response = client.newCall(request).execute();
                            String responseData = response.body().string();
                            //Log.e("login",responseData);
                            if(responseData.equals("oo")){
                                Log.e("loginoo",responseData);
                                SharedPreferences.Editor editor1 = getSharedPreferences("data",MODE_PRIVATE).edit();
                                editor1.putString("acc", account);
                                editor1.putString("password", password);

                                editor1.apply();

                                Intent intent = new Intent(LoginActivity.this, BluetoothActivity.class);
                                startActivity(intent);
                                finish();
                            }else {
                                Log.e("loginxx",responseData);
                            }

                        } catch (Exception e) {
                            e.printStackTrace();

                        }
                    }
                }).start();

//                // 如果账号是admin且密码是123456，就认为登录成功
//                if (account.equals("123") && password.equals("123")) {
//                    //editor1 = pref.edit();
//
//                    editor1.putString("acc", account);
//                    editor1.putString("password", password);
//
//                    editor1.apply();
//
//                    Intent intent = new Intent(LoginActivity.this, BluetoothActivity.class);
//                    startActivity(intent);
//                    finish();
//                } else {
//                    Toast.makeText(LoginActivity.this, "帐号或密码无效",
//                            Toast.LENGTH_SHORT).show();
//                }
            }
        });
    }
}