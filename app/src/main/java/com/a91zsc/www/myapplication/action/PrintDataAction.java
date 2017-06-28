package com.a91zsc.www.myapplication.action;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.a91zsc.www.myapplication.service.PrintDataService;
import com.a91zsc.www.myapplication.R;

import static com.a91zsc.www.myapplication.view.PrintDataActivity.RESET;

public class PrintDataAction implements OnClickListener {
    private Context context = null;
    private TextView connectState = null;
    private String deviceAddress = null;
    public PrintDataService printDataService = null;

    public PrintDataAction(Context context, String deviceAddress, TextView connectState) {
        super();
        this.context = context;
        this.deviceAddress = deviceAddress;
        this.connectState = connectState;
        this.printDataService = new PrintDataService(this.context,this.deviceAddress);
        this.initView();

    }

    private void initView() {
        // 给Item绑定名称,从printDataService中得到的值，通过GetDeviceName方法，返回的是一个String类型的值
        // 链接蓝牙设备
        boolean flag = this.printDataService.connect();
        if (flag == false) {
            // 连接失败
            this.connectState.setText(this.printDataService.getDeviceName()+" 连接失败!");
        } else {
            // 连接成功
            this.connectState.setText(this.printDataService.getDeviceName()+" 连接成功!");

        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.send) {
            if(printDataService.connect()){
                this.printDataService.sendInfo("打印机服务正常" + "\n\n\n\n");
            }
        }
    }
}