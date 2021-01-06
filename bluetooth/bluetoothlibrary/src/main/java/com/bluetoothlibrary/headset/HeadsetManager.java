package com.bluetoothlibrary.headset;

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.bluetoothlibrary.base.BluetoothManager;
import com.bluetoothlibrary.util.BluetoothLog;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Title: 蓝牙耳机管理类
 * description:
 * autor:pei
 * created on 2021/1/5
 */
public class HeadsetManager extends BluetoothManager {

    private BluetoothA2dp mBluetoothA2dp;

    //connect和disconnect都是hide方法，普通应用只能通过反射机制来调用该方法
    /**建立蓝牙连接**/
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void connect(BluetoothDevice bluetoothDevice){
        BluetoothLog.i("connect");
        if(mBluetoothA2dp == null){
            BluetoothLog.e("=========mBluetoothA2dp为null=====");
            return;
        }
        if(bluetoothDevice == null){
            BluetoothLog.e("=========bluetoothDevice为null=====");
            return;
        }
        try {
            Method connect = mBluetoothA2dp.getClass().getDeclaredMethod("connect", BluetoothDevice.class);
            connect.setAccessible(true);
            connect.invoke(mBluetoothA2dp,bluetoothDevice);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            BluetoothLog.e("connect exception:"+e);
            e.printStackTrace();
        }
    }

    /**连接蓝牙耳机**/
    public void getBluetoothA2DP(Context context){
        BluetoothLog.i("getBluetoothA2DP");
        if(mBluetoothAdapter == null){
            return;
        }
        if(mBluetoothA2dp != null){
            return;
        }
        mBluetoothAdapter.getProfileProxy(context, new BluetoothProfile.ServiceListener() {
            @Override
            public void onServiceConnected(int profile, BluetoothProfile proxy) {
                if(profile == BluetoothProfile.A2DP){
                    //Service连接成功，获得BluetoothA2DP
                    mBluetoothA2dp = (BluetoothA2dp)proxy;
                    BluetoothLog.i("====mBluetoothAdapter===连接成功=======");
                }
            }

            @Override
            public void onServiceDisconnected(int profile) {
                BluetoothLog.i("=====mBluetoothAdapter==连接失败=======");
            }
        }, BluetoothProfile.A2DP);
    }


    /**
     * 断开蓝牙耳机连接
     *
     * 注意，在程序退出之前(OnDestroy)，需要断开蓝牙相关的Service
     * 否则，程序会报异常：service leaks
     */
    public void disableAdapter(){
        BluetoothLog.i("disableAdapter");
        if(mBluetoothAdapter == null){
            return;
        }
        if(mBluetoothAdapter.isDiscovering()){
            mBluetoothAdapter.cancelDiscovery();
        }
        //关闭ProfileProxy，也就是断开service连接
        if(mBluetoothA2dp!=null){
            mBluetoothAdapter.closeProfileProxy(BluetoothProfile.A2DP,mBluetoothA2dp);
        }
    }


}
