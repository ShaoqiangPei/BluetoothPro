package com.bluetoothlibrary.base;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import com.bluetoothlibrary.util.BluetoothLog;

import java.lang.reflect.Method;
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

    public static final int NO_BLUETOOTH_CODE=-1; //无效值
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

    /**跳转蓝牙设置界面**/
    public void goBluetoothSetting(Context context){
        Intent intent=new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
        context.startActivity(intent);
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

    /***
     * 判断新蓝牙是否存在于蓝牙列表中
     *
     * @param deviceList
     * @param bluetoothDevice
     * @return BluetoothManager.NO_BLUETOOTH_CODE:表示bluetoothDevice不存在于deviceList中
     *         若存在则返回具体蓝牙设备下标
     */
    public int getAddressIndex(List<BluetoothDevice>deviceList,BluetoothDevice bluetoothDevice){
        if(deviceList!=null&&!deviceList.isEmpty()&&bluetoothDevice!=null){
            for(int i=0;i<deviceList.size();i++){
                if(deviceList.get(i).getAddress().equals(bluetoothDevice.getAddress())){
                    return i;
                }
            }
        }
        return BluetoothManager.NO_BLUETOOTH_CODE;
    }


    /**得到配对的设备列表,清除已配对的设备**/
    public void removePairDevice(){
        if(mBluetoothAdapter!=null){
            Set<BluetoothDevice> bondedDevices = mBluetoothAdapter.getBondedDevices();
            for(BluetoothDevice device : bondedDevices ){
                unpairDevice(device);
            }
        }
    }

    //反射来调用BluetoothDevice.removeBond取消设备的配对
    private void unpairDevice(BluetoothDevice device) {
        try {
            Method m = device.getClass()
                    .getMethod("removeBond", (Class[]) null);
            m.invoke(device, (Object[]) null);
        } catch (Exception e) {
            BluetoothLog.e("====unpairDevice==="+e.getMessage());
        }
    }

}
