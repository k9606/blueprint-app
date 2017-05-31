package com.a91zsc.www.myapplication.view;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketException;
import de.tavendo.autobahn.WebSocketHandler;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.a91zsc.www.myapplication.R;
import com.a91zsc.www.myapplication.action.PrintDataAction;
import com.a91zsc.www.myapplication.service.PrintDataService;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import java.util.Objects;

public class PrintDataActivity extends AppCompatActivity  {
    public static final int takeaway = 0;
    public static final int shopMeal = 1;
    public static final int booked = 2;
    public static int a = 1;
    public static int b = 1;
    public static int c = 1;

    public Context context = null;
    PrintDataAction printDataAction;


    /**
     * 复位打印机
     */
    public static final byte[] RESET = {0x1b, 0x40};

    /**
     * 左对齐
     */
    public static final byte[] ALIGN_LEFT = {0x1b, 0x61, 0x00};

    /**
     * 中间对齐
     */
    public static final byte[] ALIGN_CENTER = {0x1b, 0x61, 0x01};

    /**
     * 右对齐
     */
    public static final byte[] ALIGN_RIGHT = {0x1b, 0x61, 0x02};

    /**
     * 选择加粗模式
     */
    public static final byte[] BOLD = {0x1b, 0x45, 0x01};

    /**
     * 取消加粗模式
     */
    public static final byte[] BOLD_CANCEL = {0x1b, 0x45, 0x00};

    /**
     * 宽高加倍
     */
    public static final byte[] DOUBLE_HEIGHT_WIDTH = {0x1d, 0x21, 0x11};

    /**
     * 宽加倍
     */
    public static final byte[] DOUBLE_WIDTH = {0x1d, 0x21, 0x10};

    /**
     * 高加倍
     */
    public static final byte[] DOUBLE_HEIGHT = {0x1d, 0x21, 0x01};

    /**
     * 字体不放大
     */
    public static final byte[] NORMAL = {0x1d, 0x21, 0x00};

    /**
     * 设置默认行间距
     */
    public static final byte[] LINE_SPACING_DEFAULT = {0x1b, 0x32};
    /**
     * 打印纸一行最大的字节
     */
    private static final int LINE_BYTE_SIZE = 32;

    /**
     * 打印三列时，中间一列的中心线距离打印纸左侧的距离
     */
    private static final int LEFT_LENGTH = 16;

    /**
     * 打印三列时，中间一列的中心线距离打印纸右侧的距离
     */
    private static final int RIGHT_LENGTH = 16;

    /**
     * 打印三列时，第一列汉字最多显示几个文字
     */
    private static final int LEFT_TEXT_MAX_LENGTH = 99;

//    private final static String TAG = "My Runnable ===> ";

    //    List<byte[]> Information = demo.getbytedate();
    //    List<Integer> spacing = demo.getintList();


    private  TextView deviceName;
    private  TextView connectState;



    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setTitle("哎呀科技");
        this.setContentView(R.layout.printdata_layout);
        this.context = this;
        deviceName = (TextView) this.findViewById(R.id.device_name);
        connectState = (TextView) this
                .findViewById(R.id.connect_state);

        printDataAction= new PrintDataAction(this.context,
                this.getDeviceAddress(), deviceName, connectState);

        EditText printData = (EditText) this.findViewById(R.id.print_data);
        Button send = (Button) this.findViewById(R.id.send);
        Button command = (Button) this.findViewById(R.id.command);
        printDataAction.setPrintData(printData);

        send.setOnClickListener(printDataAction);
        command.setOnClickListener(printDataAction);


        //this.initListener();


        Button wsStart = (Button) findViewById(R.id.wsStart);
        wsStart.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                wsC.sendTextMessage("服务器正常");
                new Thread(new Runnable() {
                    public void run() {

                    }
                }).start();
            }
        });



        start();
    }


    /**
     * 获得从上一个Activity传来的蓝牙地址
     * @return String
     */
    public String getDeviceAddress() {
        // 直接通过Context类的getIntent()即可获取Intent
        Intent intent = this.getIntent();
        // 判断
        if (intent != null) {
            return intent.getStringExtra("deviceAddress");
        } else {
            return null;
        }
    }

    public void initListener() {


    }


    @Override
    protected void onDestroy() {
        PrintDataService.disconnect();
        super.onDestroy();
        if (wsC.isConnected()) {
            wsC.disconnect();
        }
    }

//########################################格式相关########################################//

    private static int getBytesLength(String msg) {
        return msg.getBytes(Charset.forName("GB2312")).length;
    }
    public static String printTwoData(String leftText, String rightText) {
        StringBuilder sb = new StringBuilder();
        int leftTextLength = getBytesLength(leftText);
        int rightTextLength = getBytesLength(rightText);
        sb.append(leftText);

        // 计算两侧文字中间的空格
        int marginBetweenMiddleAndRight = LINE_BYTE_SIZE - leftTextLength - rightTextLength;

        for (int i = 0; i < marginBetweenMiddleAndRight; i++) {
            sb.append(" ");
        }
        sb.append(rightText);
        return sb.toString();
    }
    public static String printThreeData(String leftText, String middleText, String rightText) {
        StringBuilder sb = new StringBuilder();
        // 左边最多显示 LEFT_TEXT_MAX_LENGTH 个汉字 + 两个点
        if (leftText.length() > LEFT_TEXT_MAX_LENGTH) {
            leftText = leftText.substring(0, LEFT_TEXT_MAX_LENGTH) + "..";
        }
        int leftTextLength = getBytesLength(leftText);
        int middleTextLength = getBytesLength(middleText);
        int rightTextLength = getBytesLength(rightText);

        sb.append(leftText);
        // 计算左侧文字和中间文字的空格长度
        //int marginBetweenLeftAndMiddle = LEFT_LENGTH - leftTextLength - middleTextLength / 2;
        if(leftTextLength>20){
            sb.append("\n");
        }else{
            for (int i = 0; i < 20-leftTextLength; i++) {
                sb.append(" ");
            }
        }
        sb.append(middleText);

        // 计算右侧文字和中间文字的空格长度
        //int marginBetweenMiddleAndRight = RIGHT_LENGTH - middleTextLength / 2 - rightTextLength;

        for (int i = 0; i < 10-rightTextLength; i++) {
            sb.append(" ");
        }

        // 打印的时候发现，最右边的文字总是偏右一个字符，所以需要删除一个空格
        sb.delete(sb.length() - 1, sb.length()).append(rightText);
        return sb.toString();
    }

//########################################socket相关########################################//

    private final String TAG = "PrintDataActivity";
    public static String wsUrl = "ws://third.91zsc.com:2345"; // TODO: 运行时替换ip port
    public WebSocketConnection wsC = new WebSocketConnection();

    /*TextView deviceName = (TextView) this.findViewById(R.id.device_name);
    TextView connectState = (TextView) this
            .findViewById(R.id.connect_state);

    PrintDataAction printDataAction1 = new PrintDataAction(context,getDeviceAddress(),deviceName,connectState);*/



    Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            JSONObject jsonObject = JSONObject.fromObject(msg.obj+"");
            JSONObject order = jsonObject.getJSONObject("order");
           switch (msg.what){
               case takeaway:
                   waimai(jsonObject,order);
                   break;
               case shopMeal:
                   diannei(jsonObject,order);
                   break;
               case booked:
                   yuding(jsonObject,order);
                   break;
           }
        }
    };


    private void toastLog(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }
    //public static String i=null;
    //public  static int it =0;
    public void start(){
        new Thread(){
            public void run(){
                    try {
                        wsC.connect(wsUrl, new WebSocketHandler() {
                            @Override
                            public void onOpen() {
                                toastLog("客户端准备");
                            }
                            //根据数据进行判断数据发送格式
                            @Override
                            public void onTextMessage(String payload) {

                               //String i = i+"";
                                //toastLog(payload);
                                //printDataAction.printDataService.sendInfo(payload+"\n\n\n\n\n\n");

                                //printDataAction.printDataService.send("json",RESET);


                                JSONObject jsonObject = JSONObject.fromObject(payload);
                                JSONObject order = jsonObject.getJSONObject("order");
                                String order_type = order.getString("order_type");
                                switch (order_type){
                                    case "外卖":
                                        Message msg = new Message();
                                        msg.what = takeaway;
                                        msg.obj = payload;
                                        handler.sendMessage(msg);
                                        break;
                                    case "店内点餐":
                                        Message msg1 = new Message();
                                        msg1.what = shopMeal;
                                        msg1.obj = payload;
                                        handler.sendMessage(msg1);
                                        break;
                                    case "预订到店":
                                        Message msg2 = new Message();
                                        msg2.what = booked;
                                        msg2.obj = payload;
                                        handler.sendMessage(msg2);
                                        break;
                                    default:
                                        System.out.println("default");
                                        break;
                                }
                            }


                            @Override
                            public void onClose(int code, String reason) {
                                toastLog("断开服务器");
                            }
                        });
                    } catch (WebSocketException e) {
                        e.printStackTrace();
                    }
                }
        }.start();
    }


    //解析order数据进行 发送

    public void diannei(JSONObject jsonObject,JSONObject order){

        JSONObject shop = jsonObject.getJSONObject("shop");
        String shop_name = shop.getString("name");
        String shoporder_sales = order.getString("day_no");
        String table_number = order.getString("table_number");
        String orderno = order.getString("orderno");
        String ordertime = order.getString("ordertime");
        JSONArray order_detail = jsonObject.getJSONArray("order_detail");
        String should = order.getString("should");
        String total = order.getString("total");
        String promotions = order.getString("promotions");
        String pay_type = order.getString("pay_type");
        pay_type = (pay_type=="现金支付")?"(未付款)":"(已付款)";
        String memo = order.getString("memo");

//        TextView deviceName = (TextView) PrintDataActivity.this.findViewById(R.id.device_name);
//        TextView connectState = (TextView) PrintDataActivity.this.findViewById(R.id.connect_state);
//        PrintDataAction printDataAction = new PrintDataAction(PrintDataActivity.this.context,PrintDataActivity.this.getDeviceAddress(),deviceName,connectState);
        printDataAction.printDataService.send(c+"单号",RESET);
        printDataAction.printDataService.send(shop_name+"\n",ALIGN_CENTER);
        printDataAction.printDataService.send("",RESET);
        printDataAction.printDataService.send(printTwoData("店内："+shoporder_sales,"桌号："+table_number)+"\n",DOUBLE_HEIGHT);
        printDataAction.printDataService.send("",RESET);
        printDataAction.printDataService.send("--------------------------------"+"\n",RESET);
        printDataAction.printDataService.send("",RESET);
        printDataAction.printDataService.send("订单号："+orderno+"\n",ALIGN_LEFT);
        printDataAction.printDataService.send("",RESET);
        printDataAction.printDataService.send("点餐时间："+ordertime+"\n",ALIGN_LEFT);
        printDataAction.printDataService.send("",RESET);
        if(memo!=""){
            printDataAction.printDataService.send("",RESET);
            printDataAction.printDataService.send("",ALIGN_LEFT);
            printDataAction.printDataService.send("其他要求："+memo+"\n",DOUBLE_HEIGHT_WIDTH);
        }
        printDataAction.printDataService.send("--------------------------------"+"\n",RESET);
        printDataAction.printDataService.send("",RESET);
        for (int i = 0; i < order_detail.size(); i++) {
            JSONObject info = order_detail.getJSONObject(i);
            printDataAction.printDataService.send(printThreeData(info.getString("goods_name"),"X "+info.getString("qty")+" ",info.getString("price")+"\n"),DOUBLE_HEIGHT);
            //System.out.print("菜名：" + info.getString("goods_name") + " "+"数量："+info.getString("qty")+""+"价格:"+info.getString("price")+"\n");
        }
        printDataAction.printDataService.send("",RESET);
        printDataAction.printDataService.send("--------------------------------"+"\n",RESET);
        printDataAction.printDataService.send("",RESET);
        printDataAction.printDataService.send("合计："+should+"\n",ALIGN_RIGHT);
        printDataAction.printDataService.send("",RESET);
        if(promotions!=""){
            printDataAction.printDataService.send("",RESET);
            printDataAction.printDataService.send("优惠："+promotions+"\n",ALIGN_RIGHT);
        }
        printDataAction.printDataService.send("",RESET);
        printDataAction.printDataService.send("",ALIGN_RIGHT);
        printDataAction.printDataService.send("实付："+total+pay_type+"\n",DOUBLE_HEIGHT);
        printDataAction.printDataService.send("",RESET);
        printDataAction.printDataService.send("--------------------------------"+"\n",RESET);
        printDataAction.printDataService.send("",RESET);
        printDataAction.printDataService.send("\n",RESET);
        printDataAction.printDataService.send("\n",RESET);
        printDataAction.printDataService.send("\n",RESET);
        c+=1;

    }

    public void waimai(JSONObject jsonObject,JSONObject order){
        String order_type = order.getString("order_type");
        //String a = order.getString("a");
        String orderno = order.getString("orderno");
        String ordertime = order.getString("ordertime");
        String people = order.getString("people");
        String should = order.getString("should");
        String total = order.getString("total");
        String pay_type = order.getString("pay_type");
        pay_type = (pay_type=="现金支付")?"(未付款)":"(已付款)";
        String promotions = order.getString("promotions");
        String memo = order.getString("memo");
        String contact = order.getString("contact");
        String tel = order.getString("tel");
        String address = order.getString("address");
        JSONArray order_detail = jsonObject.getJSONArray("order_detail");
        JSONObject shop = jsonObject.getJSONObject("shop");
        String shop_name = shop.getString("name");
        String takeout_sales = order.getString("day_no");
//        TextView deviceName = (TextView) PrintDataActivity.this.findViewById(R.id.device_name);
//        TextView connectState = (TextView) PrintDataActivity.this.findViewById(R.id.connect_state);
//        PrintDataAction printDataAction = new PrintDataAction(PrintDataActivity.this.context,PrintDataActivity.this.getDeviceAddress(),deviceName,connectState);
        printDataAction.printDataService.send(b+" 号单   ",RESET);
        printDataAction.printDataService.send(shop_name+"\n",ALIGN_CENTER);
        printDataAction.printDataService.send("",RESET);
        printDataAction.printDataService.send("",ALIGN_CENTER);
        printDataAction.printDataService.send(order_type+":"+takeout_sales+"\n",DOUBLE_HEIGHT);
        printDataAction.printDataService.send("",RESET);
        printDataAction.printDataService.send("--------------------------------"+"\n",RESET);
        printDataAction.printDataService.send("用餐人数："+people+"\n",ALIGN_LEFT);
        printDataAction.printDataService.send("",RESET);
        printDataAction.printDataService.send("订单号："+orderno+"\n",ALIGN_LEFT);
        printDataAction.printDataService.send("",RESET);
        printDataAction.printDataService.send("点餐时间："+ordertime+"\n",ALIGN_LEFT);
        printDataAction.printDataService.send("",RESET);
        if(memo!=""){
            printDataAction.printDataService.send("",RESET);
            printDataAction.printDataService.send("",ALIGN_LEFT);
            printDataAction.printDataService.send("其他要求："+memo+"\n",DOUBLE_HEIGHT_WIDTH);
        }
        printDataAction.printDataService.send("--------------------------------"+"\n",RESET);
        printDataAction.printDataService.send("",RESET);
        for (int i = 0; i < order_detail.size(); i++) {
            JSONObject info = order_detail.getJSONObject(i);
            printDataAction.printDataService.send(printThreeData(info.getString("goods_name"),"X "+info.getString("qty")+" ",info.getString("price")+"\n"),DOUBLE_HEIGHT);
            //System.out.print("菜名：" + info.getString("goods_name") + " "+"数量："+info.getString("qty")+""+"价格:"+info.getString("price")+"\n");
        }
        printDataAction.printDataService.send("",RESET);
        printDataAction.printDataService.send("--------------------------------"+"\n",RESET);
        printDataAction.printDataService.send("合计："+should+"\n",ALIGN_RIGHT);
        printDataAction.printDataService.send("",RESET);
        if(promotions!=""){
            printDataAction.printDataService.send("优惠："+promotions+"\n",ALIGN_RIGHT);
            printDataAction.printDataService.send("",RESET);
            printDataAction.printDataService.send("",ALIGN_RIGHT);
        }
        printDataAction.printDataService.send("",RESET);
        printDataAction.printDataService.send("",ALIGN_RIGHT);
        printDataAction.printDataService.send("实付："+total+pay_type+"\n",DOUBLE_HEIGHT);
        printDataAction.printDataService.send("",RESET);
        printDataAction.printDataService.send("--------------------------------"+"\n",RESET);
        printDataAction.printDataService.send(contact+"\n",DOUBLE_HEIGHT_WIDTH);
        printDataAction.printDataService.send("",RESET);
        printDataAction.printDataService.send(tel+"\n",DOUBLE_HEIGHT_WIDTH);
        printDataAction.printDataService.send("",RESET);
        printDataAction.printDataService.send(address+"\n",DOUBLE_HEIGHT_WIDTH);
        printDataAction.printDataService.send("\n",RESET);
        printDataAction.printDataService.send("\n",RESET);
        printDataAction.printDataService.send("\n",RESET);
        b += 1;
    }

    public void yuding(JSONObject jsonObject,JSONObject order){
        JSONObject shop = jsonObject.getJSONObject("shop");
        String shop_name = shop.getString("name");
        String booking_sales =order.getString("day_no");
        String people = order.getString("people");
        String orderno = order.getString("orderno");
        String ordertime = order.getString("ordertime");
        String entertime = order.getString("entertime");



        JSONArray order_detail = jsonObject.getJSONArray("order_detail");
        String should = order.getString("should");
        String total = order.getString("total");
        String contact = order.getString("contact");
        String tel = order.getString("tel");
        String promotions = order.getString("promotions");
        String memo = order.getString("memo");
        String pay_type = order.getString("pay_type");
        pay_type = (pay_type=="现金支付")?"(未付款)":"(已付款)";
//        TextView deviceName = (TextView) PrintDataActivity.this.findViewById(R.id.device_name);
//        TextView connectState = (TextView) PrintDataActivity.this.findViewById(R.id.connect_state);
//        PrintDataAction printDataAction = new PrintDataAction(PrintDataActivity.this.context,PrintDataActivity.this.getDeviceAddress(),deviceName,connectState);
        printDataAction.printDataService.send(a+"单号",RESET);
        printDataAction.printDataService.send(shop_name+"\n",ALIGN_CENTER);
        printDataAction.printDataService.send("",RESET);
        printDataAction.printDataService.send("",ALIGN_CENTER);
        printDataAction.printDataService.send("预订："+booking_sales+"\n",DOUBLE_HEIGHT);
        printDataAction.printDataService.send("",RESET);
        printDataAction.printDataService.send("--------------------------------"+"\n",RESET);
        printDataAction.printDataService.send("用餐人数："+people+"\n",ALIGN_LEFT);
        printDataAction.printDataService.send("",RESET);
        printDataAction.printDataService.send("订单号："+orderno+"\n",ALIGN_LEFT);
        printDataAction.printDataService.send("",RESET);
        printDataAction.printDataService.send("点餐时间："+ordertime+"\n",ALIGN_LEFT);
        printDataAction.printDataService.send("",RESET);
        printDataAction.printDataService.send("到店时间："+entertime+"\n",ALIGN_LEFT);
        if(memo!=""){
            printDataAction.printDataService.send("",RESET);
            printDataAction.printDataService.send("",ALIGN_LEFT);
            printDataAction.printDataService.send("其他要求："+memo+"\n",DOUBLE_HEIGHT_WIDTH);
        }
        printDataAction.printDataService.send("",RESET);
        printDataAction.printDataService.send("--------------------------------"+"\n",RESET);
        for (int i = 0; i < order_detail.size(); i++) {
            JSONObject info = order_detail.getJSONObject(i);
            printDataAction.printDataService.send(printThreeData(info.getString("goods_name"),"X "+info.getString("qty")+" ",info.getString("price")+"\n"),DOUBLE_HEIGHT);
            //System.out.print("菜名：" + info.getString("goods_name") + " "+"数量："+info.getString("qty")+""+"价格:"+info.getString("price")+"\n");
        }
        printDataAction.printDataService.send("",RESET);
        printDataAction.printDataService.send("--------------------------------"+"\n",RESET);
        printDataAction.printDataService.send("合计："+should+"\n",ALIGN_RIGHT);
        printDataAction.printDataService.send("",RESET);
        if(promotions!=""){
            printDataAction.printDataService.send("优惠："+promotions+"\n",ALIGN_RIGHT);
            printDataAction.printDataService.send("",RESET);
            printDataAction.printDataService.send("",ALIGN_RIGHT);
        }
        printDataAction.printDataService.send("",RESET);
        printDataAction.printDataService.send("",ALIGN_RIGHT);
        printDataAction.printDataService.send("实付："+total+pay_type+"\n",DOUBLE_HEIGHT);
        printDataAction.printDataService.send("",RESET);
        printDataAction.printDataService.send("--------------------------------"+"\n",RESET);
        printDataAction.printDataService.send(contact+"\n",DOUBLE_HEIGHT_WIDTH);
        printDataAction.printDataService.send("",RESET);
        printDataAction.printDataService.send(tel+"\n",DOUBLE_HEIGHT_WIDTH);
        printDataAction.printDataService.send("\n",RESET);
        printDataAction.printDataService.send("\n",RESET);
        printDataAction.printDataService.send("\n",RESET);
        a+=1;
    }

}

