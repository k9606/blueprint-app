package com.a91zsc.www.myapplication.view;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.ListView;

import com.a91zsc.www.myapplication.R;
import com.a91zsc.www.myapplication.action.BluetoothAction;

public class BluetoothActivity extends Activity {

    public Context context;
    BluetoothAdapter mBluetoothAdapter;
    private BluetoothAdapter bluetoothAdapter = BluetoothAdapter
            .getDefaultAdapter();
    private Button searchDevices;


    public void onCreate(Bundle savedInstanceState) {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothAdapter.enable();
        super.onCreate(savedInstanceState);
        if (mBluetoothAdapter.isEnabled()) {
        } else {
            mBluetoothAdapter.enable();
        }
        this.context = this;
        setContentView(R.layout.bluetooth_layout);
        this.initListener();
        bluetoothAdapter.startDiscovery();

    }


    //初始化点击事件click
    private void initListener() {
        searchDevices = (Button) this.findViewById(R.id.searchDevices);
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


}
