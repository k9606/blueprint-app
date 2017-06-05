package com.a91zsc.www.myapplication.action;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.a91zsc.www.myapplication.service.BluetoothService;
import com.a91zsc.www.myapplication.R;

public class BluetoothAction implements OnClickListener {

    private Button searchDevices = null;
    private Activity activity = null;

    private ListView unbondDevices = null;
    private ListView bondDevices = null;
    private Context context = null;
    private BluetoothService bluetoothService = null;

    public BluetoothAction(Context context, ListView unbondDevices,
                           ListView bondDevices, Button searchDevices,
                           Activity activity) {
        super();
        this.context = context;
        this.unbondDevices = unbondDevices;
        this.bondDevices = bondDevices;
        this.searchDevices = searchDevices;
        this.activity = activity;
        this.bluetoothService = new BluetoothService(this.context,
                this.unbondDevices, this.bondDevices,
                this.searchDevices);
    }

    public void setSearchDevices(Button searchDevices) {
        this.searchDevices = searchDevices;
    }

    public void setUnbondDevices(ListView unbondDevices) {
        this.unbondDevices = unbondDevices;
    }

    /**
     * 初始化界面
     */
    public void initView() {

        if (this.bluetoothService.isOpen()) {
            Toast.makeText(context, "蓝牙已打开！", Toast.LENGTH_LONG).show();
        }
        if (!this.bluetoothService.isOpen()) {
            Toast.makeText(context, "蓝牙未打开！请打开蓝牙", Toast.LENGTH_LONG).show();
            this.searchDevices.setEnabled(false);
        }
    }

    private void searchDevices() {
        bluetoothService.searchDevices();
    }

    /**
     * 各种按钮的监听
     */
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.searchDevices) {
            this.searchDevices();
        }
    }

}
