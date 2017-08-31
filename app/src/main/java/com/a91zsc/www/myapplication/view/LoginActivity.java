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
import com.a91zsc.www.myapplication.util.toolsFileIO;
import com.a91zsc.www.myapplication.util.utilsTools;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.os.SystemClock.sleep;
import static com.a91zsc.www.myapplication.string.staticBluetoothData.userLoginURLVerification;
import static com.a91zsc.www.myapplication.string.staticBluetoothData.usernameOrPassworlderror;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText accountEdit, passwordEdit;
    private Button login;
    private OkHttpClient client;
    public Context context;
    private Response response = null;
    private String responseData = null;
    private static final String CODELOGIN = "oo";
    private static final String USER = "user";
    Intent intent;
    private utilsTools utilsTools = new utilsTools();
    private toolsFileIO tlfo = new toolsFileIO();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            finish();
            return;
        }
        context  = getApplicationContext();
        versionUser();
        getSupportActionBar().hide();
        accountEdit = (EditText) findViewById(R.id.account);
        passwordEdit = (EditText) findViewById(R.id.password);
        login = (Button) findViewById(R.id.login);
        login.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login:
                sendVersionUser();
                break;
            default:
                System.out.println("default");
                break;

        }

    }

    public void sendVersionUser() {
        if (utilsTools.isFastClick()) {
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
                                .url(userLoginURLVerification)
                                .post(requestBody)
                                .build();
                        response = client.newCall(request).execute();
                        responseData = response.body().string();
                        if (responseData.equals(CODELOGIN)) {
                            tlfo.toolsFileIO(context, account, password);
                            Intent intent = new Intent(LoginActivity.this, BluetoothActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            startFunction();
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    public void versionUser() {
        String account = tlfo.getUserName(context);
        if (!(account == null || account.length() <= 0)) {
            intent = new Intent(LoginActivity.this, BluetoothActivity.class);
            startActivity(intent);
            this.finish();
        }
    }

    public void startFunction() {
        runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(LoginActivity.this, usernameOrPassworlderror, Toast.LENGTH_LONG).show();

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}