package com.a91zsc.www.myapplication.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.ListView;

import com.a91zsc.www.myapplication.R;
import com.a91zsc.www.myapplication.action.BluetoothAction;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

public class BluetoothActivity extends Activity {

	private Context context = null;
	//    private Button searchDevices ;
	private Button searchDevices ;
	public void onCreate(Bundle savedInstanceState) {
		SharedPreferences pref = getSharedPreferences("data", MODE_PRIVATE);
		String account = pref.getString("acc", "");



                    /*Toast.makeText(LoginActivity.this, "cun",
                            Toast.LENGTH_SHORT).show();*/

//		SharedPreferences pref4 = getSharedPreferences("data",MODE_PRIVATE);
//		String acc = pref2.getString("acc","");
//		Log.d("llllllllllll",acc);
//		Log.d("llllllllllll","acc");
//		Toast.makeText(LoginActivity.this, "帐号"+acc,
//				Toast.LENGTH_SHORT).show();


		if (account == null || account.length() <= 0) {
			Intent intent = new Intent(BluetoothActivity.this, LoginActivity.class);
			startActivity(intent);
			finish();
		}
		super.onCreate(savedInstanceState);
		this.context = this;

		setContentView(R.layout.bluetooth_layout);

		this.initListener();
	}

	private void initListener() {
		searchDevices = (Button) this.findViewById(R.id.searchDevices);
		ListView unbondDevices = (ListView) this.findViewById(R.id.unbondDevices);
		ListView bondDevices = (ListView) this.findViewById(R.id.bondDevices);


		BluetoothAction bluetoothAction = new BluetoothAction(this.context,
				unbondDevices, bondDevices, searchDevices,
				BluetoothActivity.this);

		bluetoothAction.setSearchDevices(searchDevices);
		bluetoothAction.initView();


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

}
