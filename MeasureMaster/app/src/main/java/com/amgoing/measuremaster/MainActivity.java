package com.amgoing.measuremaster;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.ghgande.j2mod.modbus.ModbusException;
import com.ghgande.j2mod.modbus.facade.ModbusTCPMaster;
import com.ghgande.j2mod.modbus.io.ModbusTCPTransaction;
import com.ghgande.j2mod.modbus.io.ModbusTCPTransport;
import com.ghgande.j2mod.modbus.msg.ReadWriteMultipleRequest;
import com.ghgande.j2mod.modbus.msg.WriteSingleRegisterRequest;
import com.ghgande.j2mod.modbus.net.TCPMasterConnection;
import com.ghgande.j2mod.modbus.procimg.SimpleInputRegister;
import com.ghgande.j2mod.modbus.procimg.SimpleRegister;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private String TAG = "MainActivityMaster";
    private Button init,calculate;
    private TextView tvShow;
    private ModbusTCPMaster master = null;
    private String address = "127.0.0.1";
    private int port = 1502;
    private ModbusTCPTransport transport = null;
    private ModbusTCPTransaction transaction = null;
    private String resStr = "";
    //这是发送初始化指令用的寄存器，总共10个，其实只写了第一个
    private SimpleRegister initRegisters[] = {
            new SimpleRegister(1), new SimpleRegister(0), new SimpleRegister(0), new SimpleRegister(0), new SimpleRegister(0),
            new SimpleRegister(0), new SimpleRegister(0), new SimpleRegister(0), new SimpleRegister(0), new SimpleRegister(0),};
    //这是发送计算指令用的寄存器，总共10个，其实只写了第一个
    private SimpleRegister calculateRegisters[] = {
            new SimpleRegister(2), new SimpleRegister(0), new SimpleRegister(0), new SimpleRegister(0), new SimpleRegister(0),
            new SimpleRegister(0), new SimpleRegister(0), new SimpleRegister(0), new SimpleRegister(0), new SimpleRegister(0),};
    //这个handler主要用于操作UI线程，WeakHandler已经处理了以前handler的内存泄漏问题
    private WeakHandler handler = new WeakHandler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    switch (msg.what){
                        case 0:
                            tvShow.setText(resStr);
                            break;
                        default:
                            break;
                    }
                }
            });
            return false;
        }
    });
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init = findViewById(R.id.btn_init);
        calculate = findViewById(R.id.btn_calculate);
        tvShow = findViewById(R.id.tv_show);
        //初始化modbus master
        modbusInit();
        init.setOnClickListener(this);
        calculate.setOnClickListener(this);
    }

    /**
     * 这个函数用于初始化Modbus Master
     */
    private void modbusInit(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                master = new ModbusTCPMaster(address, port);
                try {
                    master.connect();
                    transport = (ModbusTCPTransport) master.getTransport();
                    transaction = (ModbusTCPTransaction) transport.createTransaction();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_init:
                startInit();
                break;
            case R.id.btn_calculate:
                startCalculate();
                break;
        }
    }

    /**
     * 这个是发送初始化指令的函数
     */
    private void startInit(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                //构造请求，ReadWriteMultipleRequest请求码为23
                ReadWriteMultipleRequest request = new ReadWriteMultipleRequest();
                //设置寄存器
                request.setRegisters(initRegisters);
                //设置write偏移量
                request.setWriteReference(0);
                //模块ID，统一为1
                request.setUnitID(1);
                //数据长度
                request.setDataLength(32);
                //pid设置为15
                request.setProtocolID(15);
                //设置read的偏移量
                request.setReadReference(0);
                //设置读寄存器的数量
                request.setReadWordCount(10);
                //将请求设置到通信中
                transaction.setRequest(request);
                try {
                    //执行请求
                    transaction.execute();
                    //获取回复,此处是一个16进制字符串，可用getMessage获取为byte[]，对应处理
                    resStr = transaction.getResponse().getHexMessage();
                    //发送handler消息，更新UI
                    handler.sendEmptyMessage(0);
                    Log.e(TAG, resStr);
                } catch (ModbusException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    /**
     * 这个是发送测脚指令的函数（具体注释同上一个函数）
     */
    private void startCalculate(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                ReadWriteMultipleRequest request = new ReadWriteMultipleRequest();
                request.setRegisters(calculateRegisters);
                request.setWriteReference(0);
                request.setUnitID(1);
                request.setDataLength(32);
                request.setProtocolID(15);
                request.setReadReference(0);
                request.setReadWordCount(10);
                transaction.setRequest(request);
                try {
                    transaction.execute();
                    resStr = transaction.getResponse().getHexMessage();
                    handler.sendEmptyMessage(0);
                    Log.e(TAG, resStr);
                } catch (ModbusException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    @Override
    protected void onDestroy() {
        master.disconnect();
        master = null;
        transport = null;
        transaction = null;
        super.onDestroy();
    }
}
