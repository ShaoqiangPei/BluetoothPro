package com.bluetoothlibrary.base;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import com.bluetoothlibrary.util.BluetoothLog;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Title:蓝牙连接管理类
 * description:
 * autor:pei
 * created on 2021/1/5
 */
public class BluetoothManager {

    public static final int NO_BLUETOOTH_CODE=-1; //设备不支持蓝牙
    public static final int NO_SUPPORT=0x1; //设备不支持蓝牙

    protected BluetoothAdapter mBluetoothAdapter;

    /**设备是否支持蓝牙**/
    public boolean isSupportBluetooth() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter != null) {
            return true;
        }
        return false;
    }


    /**蓝牙是否已经启动**/
    public boolean isBluetoothOpen() {
        if (mBluetoothAdapter != null) {
            return mBluetoothAdapter.isEnabled();
        }
        return false;
    }

    /**请求启动蓝牙**/
    public void requestStartBluetooth(int requestCode, Context context) {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        ((AppCompatActivity)context).startActivityForResult(enableBtIntent, requestCode);
    }

    /**强制打开蓝牙**/
    public void openBluetooth(){
        if(isSupportBluetooth()){
            mBluetoothAdapter.enable();
        }
    }

    /**关闭蓝牙**/
    public void closeBluetooth(){
        if(isSupportBluetooth()&&mBluetoothAdapter.isEnabled()){
            boolean ret = mBluetoothAdapter.disable();
            BluetoothLog.i("===disable adapter==="+ret);
        }
    }

    /**查询配对设备**/
    public List<BluetoothDevice> checkDevices() {
        List<BluetoothDevice> devices=new ArrayList<>();
        if(mBluetoothAdapter!=null){
            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
            if (pairedDevices != null && pairedDevices.size() > 0) {
                for (BluetoothDevice device : pairedDevices) {
                    devices.add(device);
                }
            }
        }
        return devices;
    }

    /**发现新设备**/
    public void findBluetoothDevice() {
        //其实是启动了一个异步线程，该方法将立即返回一个布尔值，指示发现是否已成功启动。
        // 发现过程通常涉及大约12秒的查询扫描，随后是每个找到的设备的页面扫描以检索其蓝牙名称
        if(mBluetoothAdapter!=null && mBluetoothAdapter.isEnabled() && !mBluetoothAdapter.isDiscovering()){
            if (mBluetoothAdapter.startDiscovery()) {
                BluetoothLog.i("=======已成功启动寻找新设备的异步线程=======");
            } else {
                BluetoothLog.i("=======启动寻找新设备的异步线程失败=======");
            }
        }
    }

    /**取消搜索**/
    public void cancelDiscovery(){
        if(isSupportBluetooth()){
            mBluetoothAdapter.cancelDiscovery();
        }
    }

    /**开始配对**/
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void createBond(BluetoothDevice bluetoothDevice) {
        if(bluetoothDevice!=null){
            bluetoothDevice.createBond();
        }else {
            BluetoothLog.e("====配对bluetoothDevice为null===");
        }
    }

}
