package com.bluetoothlibrary.receiver;

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.bluetoothlibrary.interfacer.OnBluetoothListener;
import com.bluetoothlibrary.util.BluetoothLog;
import com.bluetoothlibrary.util.StringUtil;


/**
 * Title:蓝牙广播
 * Description:
 * <p>
 * Created by pei
 * Date: 2018/4/27
 */
public class BluetoothReceiver extends BroadcastReceiver {

    private OnBluetoothListener mOnBluetoothListener;

    public void setOnBluetoothListener(OnBluetoothListener onBluetoothListener){
        this.mOnBluetoothListener=onBluetoothListener;
    }

    @Override
    public void onReceive(Context context, Intent intent){
        BluetoothLog.i("=========蓝牙接收处理广播========"+intent.getAction());
        BluetoothDevice device;
        switch (intent.getAction()) {
            case BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED:
                switch (intent.getIntExtra(BluetoothA2dp.EXTRA_STATE, -1)) {
                    case BluetoothA2dp.STATE_CONNECTING:
                        device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        BluetoothLog.i("device: " + device.getName() +" connecting");
                        break;
                    case BluetoothA2dp.STATE_CONNECTED:
                        device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        BluetoothLog.i("device: " + device.getName() +" connected");
                        mOnBluetoothListener.deviceConnected(device);
                        break;
                    case BluetoothA2dp.STATE_DISCONNECTING:
                        device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        BluetoothLog.i("device: " + device.getName() +" disconnecting");
                        break;
                    case BluetoothA2dp.STATE_DISCONNECTED:
                        device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        BluetoothLog.i("device: " + device.getName() +" disconnected");
                        break;
                    default:
                        break;
                }
                break;
            case BluetoothA2dp.ACTION_PLAYING_STATE_CHANGED:
                int state = intent.getIntExtra(BluetoothA2dp.EXTRA_STATE, -1);
                switch (state) {
                    case BluetoothA2dp.STATE_PLAYING:
                        BluetoothLog.i("state: playing.");
                        break;
                    case BluetoothA2dp.STATE_NOT_PLAYING:
                        BluetoothLog.i("state: not playing");
                        break;
                    default:
                        BluetoothLog.i("state: unkown");
                        break;
                }
                break;
            case BluetoothDevice.ACTION_FOUND:
                device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                int deviceClassType = device.getBluetoothClass().getDeviceClass();
                //找到指定的蓝牙设备
                if (deviceClassType == BluetoothClass.Device.AUDIO_VIDEO_WEARABLE_HEADSET
                        || deviceClassType == BluetoothClass.Device.AUDIO_VIDEO_HEADPHONES) {
                    BluetoothLog.i("Found device:" + device.getName()+"   address:"+device.getAddress());
                    if(StringUtil.isNotEmpty(device.getName())){
                        //添加到设备列表
                        mOnBluetoothListener.deviceFound(device);
                    }
                }else{//找到可用蓝牙
                    if(StringUtil.isNotEmpty(device.getName())){
                        BluetoothLog.i("=====Found device====11===" + device.getName()+"   address:"+device.getAddress());
                        //添加到设备列表
                        mOnBluetoothListener.deviceFound(device);
                    }
                }
                break;
            case BluetoothDevice.ACTION_BOND_STATE_CHANGED:
                int bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.BOND_NONE);
                device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                switch (bondState){
                    case BluetoothDevice.BOND_BONDED:  //配对成功
                        BluetoothLog.i("Device:"+device.getName()+" bonded.");
                        //取消搜索，连接蓝牙设备
                        mOnBluetoothListener.deviceBonded(device);
                        break;
                    case BluetoothDevice.BOND_BONDING:
                        BluetoothLog.i("Device:"+device.getName()+" bonding.");
                        break;
                    case BluetoothDevice.BOND_NONE:
                        BluetoothLog.i("Device:"+device.getName()+" not bonded.");
                        //不知道是蓝牙耳机的关系还是什么原因，经常配对不成功
                        //配对不成功的话，重新尝试配对
                        mOnBluetoothListener.deviceBondNone(device);
                        break;
                    default:
                        break;

                }
                break;
            case BluetoothAdapter.ACTION_STATE_CHANGED:
                state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
                switch (state) {
                    case BluetoothAdapter.STATE_TURNING_ON:
                        BluetoothLog.i("BluetoothAdapter is turning on.");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        BluetoothLog.i("BluetoothAdapter is on.");
//                        //蓝牙已打开，开始搜索并连接service
//                        findBluetoothDevice();
//                        getBluetoothA2DP();

                        mOnBluetoothListener.blootoothStateOn();
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        BluetoothLog.i("BluetoothAdapter is turning off.");
                        break;
                    case BluetoothAdapter.STATE_OFF:
                        BluetoothLog.i("BluetoothAdapter is off.");
                        break;
                }
                break;
            default:
                break;
        }
    }

}
