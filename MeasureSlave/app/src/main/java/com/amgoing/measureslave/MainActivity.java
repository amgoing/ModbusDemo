package com.amgoing.measureslave;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.ghgande.j2mod.modbus.procimg.SimpleProcessImage;
import com.ghgande.j2mod.modbus.procimg.SimpleRegister;
import com.ghgande.j2mod.modbus.slave.ModbusSlave;
import com.ghgande.j2mod.modbus.slave.ModbusSlaveFactory;

public class MainActivity extends AppCompatActivity {
    private TextView tvShow;
    private ModbusSlave slave = null;
    private int port = 1502;
    //一个进程镜像对象
    SimpleProcessImage spi;
    private String TAG = "MainActivitySlave";
    private String resStr = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvShow = findViewById(R.id.tv_show);
        modbusInit();
        resStr = NetWorkUtil.getHostIp();
        tvShow.setText(resStr);
    }
    /**
     * 这个方法用于初始化ModBus
     */
    public void modbusInit() {

        int unit = 1;//模块id，不需改动

        try {
            // Create the process image for this test.创建进程镜像对象，必须要有这个对象才能进行读写寄存器的操作
            spi = new SimpleProcessImage(15);
            //添加10个保持寄存器
            for(int i = 0; i < 10; ++i)
            {
                spi.addRegister(new SimpleRegister(0));
            }

//            // 2. Setup and start slave。设置和启动TCP slave端，端口号1502，线程池Size：5
            slave = ModbusSlaveFactory.createTCPSlave(port, 5);
//             2. Set up serial parameters 设置串口通信参数
//            SerialParameters params = new SerialParameters();
//            params.setPortName("tty1502");
//            params.setBaudRate(19200);
//            params.setDatabits(8);
//            params.setParity("None");
//            params.setStopbits(1);
//            params.setEncoding("rtu");
//            params.setEcho(false);
//
//            // 3. Setup and start slave. 创建串口通信Slave端
//            slave = ModbusSlaveFactory.createSerialSlave(params);
            //为slave设置进程镜像对象及id
            slave.addProcessImage(unit, spi);
            spi.setCallBack(new SimpleProcessImage.HandleBack() {
                @Override
                public void onSuccess() {
                    switch (spi.getRegister(0).getValue()){
                        case 1:
                            int res[] = {1, 4};
                            sendModbusMessage(res);
                            break;
                        case 2:
                            int res1[] = {1, 0, 230, 105, 230, 106};
                            sendModbusMessage(res1);
                            break;
                    }
                }
            });
            //启动slave端
            slave.open();

        }
        catch (Exception ex) {//初始化失败时的异常处理
            ex.printStackTrace();
        }
    }
    public void sendModbusMessage(int measure_result[]){
        tvShow.setText(measure_result.length > 4? resStr+":初始化完成":resStr+":计算完成");
        if(slave == null) return;
        for(int i = 0; i < measure_result.length; ++i)
        {
            //Log.e(TAG, "" + i + " " + measure_result[i]);
            slave.getProcessImage(1).getRegister(i + 1).setValue((short)(measure_result[i]));
        }
    }

    @Override
    protected void onDestroy() {
        slave.close();
        super.onDestroy();
    }
}
