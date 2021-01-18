package com.bluetoothpro;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.bluetoothlibrary.headset.HeadsetHelper;
import com.bluetoothlibrary.interfacer.OnBluetoothListener;
import com.bluetoothlibrary.interfacer.OnBluetoothStartListener;
import com.bluetoothlibrary.util.BluetoothLog;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Button mButton;
    private Button mButton1;
    private Button mButton2;

    private int mRequestCode=100;
    private HeadsetHelper mHeadsetHelper;
    private BluetoothDevice mDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BluetoothLog.setDebug(true);

        BluetoothLog.i("=======我是水啊=====");

        initView();
        initData();
        setListener();
    }

    private void initView(){
        mButton=findViewById(R.id.button);
        mButton1=findViewById(R.id.button1);
        mButton2=findViewById(R.id.button2);
    }

    private void initData(){
        mHeadsetHelper=new HeadsetHelper(MainActivity.this);
        //注册广播
        mHeadsetHelper.registerReceiver();

        //显示蓝牙列表
        List<BluetoothDevice> list=mHeadsetHelper.getPairedDevices();
        for(BluetoothDevice device:list){
            BluetoothLog.i("=========device.getName="+device.getName());
        }
    }

    private void setListener(){
        mHeadsetHelper.setOnReceiver(new OnBluetoothListener() {
            @Override
            public void deviceConnected(BluetoothDevice bluetoothDevice) {
                BluetoothLog.i("====deviceConnected====");
                //连接成功
                Toast.makeText(MainActivity.this,"连接成功",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void deviceFound(BluetoothDevice bluetoothDevice) {
                BluetoothLog.i("====deviceFound====");
                //发现新设备，填充到列表中展示
                Toast.makeText(MainActivity.this,"发现新设备",Toast.LENGTH_SHORT).show();

                BluetoothLog.i("===发现新设备==="+bluetoothDevice.getName());

                String name="AirPods";
                if(bluetoothDevice.getName().contains(name)){
                    mDevice=bluetoothDevice;

                    //停止搜索
                    mHeadsetHelper.getHeadsetManager().cancelDiscovery();
                }
            }

            @Override
            public void deviceBonded(BluetoothDevice bluetoothDevice) {
                BluetoothLog.i("====deviceBonded====");
                //取消搜索
                Toast.makeText(MainActivity.this,"取消搜索",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void deviceBondNone(BluetoothDevice bluetoothDevice) {
                BluetoothLog.i("====deviceBondNone====");
//                //配对失败(尝试重新配对)
//                mHeadsetManager.createBond(bluetoothDevice);

                Toast.makeText(MainActivity.this,"配对失败",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void blootoothStateOn() {
                BluetoothLog.i("====blootoothStateOn====");

                Toast.makeText(MainActivity.this,"蓝牙已打开，开始搜索并连接service",Toast.LENGTH_SHORT).show();
                //蓝牙已打开，开始搜索并连接service
                mHeadsetHelper.getHeadsetManager().getBluetoothA2DP(MainActivity.this);
            }
        });

        //搜索
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //搜索
                mHeadsetHelper.searchBluetooth(mRequestCode);
            }
        });

        //连接
        mButton1.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                 if(mDevice!=null){
                     BluetoothLog.i("=====开始连接蓝牙===="+mDevice.getName());
                     Toast.makeText(MainActivity.this,"开始连接",Toast.LENGTH_SHORT).show();

                     mHeadsetHelper.connectBluetooth(mDevice,mRequestCode);
                 }else{
                     Toast.makeText(MainActivity.this,"设备为空还",Toast.LENGTH_SHORT).show();
                 }
            }
        });

        //停止搜索
        mButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //停止搜索
                mHeadsetHelper.getHeadsetManager().cancelDiscovery();
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mHeadsetHelper.onActivityResult(mRequestCode, requestCode, resultCode, new OnBluetoothStartListener() {
            @Override
            public void startSuccess() {
                BluetoothLog.i("====open success====");

                //开始查询配对设备
                List<BluetoothDevice>devices=mHeadsetHelper.getPairedDevices();
                for(BluetoothDevice device:devices){
                    BluetoothLog.i("====search=====device.getName="+device.getName());
                }
            }

            @Override
            public void startFailed() {
                BluetoothLog.i("====open failed====");
            }
        });
    }

    @Override
    protected void onDestroy() {
        mHeadsetHelper.destory();
        BluetoothLog.i("======destory=====");
        super.onDestroy();
    }
}
