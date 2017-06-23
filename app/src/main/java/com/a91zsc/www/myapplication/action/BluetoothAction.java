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

import static android.R.attr.onClick;

public class BluetoothAction implements OnClickListener{


    private ListView unbondDevices = null;
    private ListView bondDevices = null;
    private Context context = null;
    private Button button = null;
    private BluetoothService bluetoothService ;



    public BluetoothAction(Context context, ListView unbondDevices,
                           ListView bondDevices,Button button) {

        super();
        this.context = context;
        this.unbondDevices = unbondDevices;
        this.bondDevices = bondDevices;
        this.button = button;
        this.bluetoothService = new BluetoothService(this.context,
                this.unbondDevices, this.bondDevices);
    }
    public void onClick(View v) {
        if (v.getId() == R.id.searchDevices) {
            System.out.println("Hello world!");
            this.searchDevices();
        }
    }


    private void searchDevices() {
        bluetoothService.searchDevices();
    }
}
