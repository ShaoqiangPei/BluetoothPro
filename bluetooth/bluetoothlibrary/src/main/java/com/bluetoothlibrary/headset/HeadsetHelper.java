package com.bluetoothlibrary.headset;

import android.app.Activity;
import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.IntentFilter;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.bluetoothlibrary.R;
import com.bluetoothlibrary.base.BluetoothManager;
import com.bluetoothlibrary.base.BluetoothReceiver;
import com.bluetoothlibrary.base.OnBluetoothListener;
import com.bluetoothlibrary.base.OnBluetoothStartListener;
import com.bluetoothlibrary.util.BluetoothLog;
import com.bluetoothlibrary.util.StringUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Title: 蓝牙耳机连接帮助类
 * description:
 * autor:pei
 * created on 2021/1/5
 */
public class HeadsetHelper {

    private HeadsetManager mHeadsetManager;
    private BluetoothReceiver mBluetoothReceiver;
    private Context mContext;

    public HeadsetHelper(Context context){
        this.mContext=context;
        mHeadsetManager=new HeadsetManager();
    }

    /**获取蓝牙耳机管理类对象**/
    public HeadsetManager getHeadsetManager(){
        return mHeadsetManager;
    }

    /**注册广播**/
    public void registerReceiver(){
        mBluetoothReceiver=new BluetoothReceiver();
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED);
        filter.addAction(BluetoothA2dp.ACTION_PLAYING_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        mContext.registerReceiver(mBluetoothReceiver, filter);
    }

    /**获取已配对蓝牙列表**/
    public List<BluetoothDevice> getPairedDevices(){
        List<BluetoothDevice> list=null;

        if(mHeadsetManager.isSupportBluetooth()&&mHeadsetManager.isBluetoothOpen()){
            //强制打开蓝牙
            mHeadsetManager.openBluetooth();
            list=mHeadsetManager.checkDevices();
            BluetoothLog.i("====list=====list=="+list.size());
            //过滤列表
            Iterator<BluetoothDevice> it = list.iterator();
            while (it.hasNext()) {
                BluetoothDevice device = it.next();
                if (device != null&& StringUtil.isEmpty(device.getName())) {
                    it.remove();
                }
            }
        }
        if(list==null){
            list=new ArrayList<BluetoothDevice>();
        }
        return list;
    }

    /***
     * 搜索蓝牙
     *
     * @return  BluetoothManager.NO_SUPPORT：设备不支持蓝牙
     */
    public int searchBluetooth(int requestBluetooth){
        if(!mHeadsetManager.isSupportBluetooth()){
            //本设备不支持蓝牙
            return BluetoothManager.NO_SUPPORT;
        }
        if(mHeadsetManager.isBluetoothOpen()){
            //发现新设备
            mHeadsetManager.findBluetoothDevice();
        }else{
            //去启动蓝牙
            mHeadsetManager.requestStartBluetooth(requestBluetooth,mContext);
        }
        return BluetoothManager.NO_BLUETOOTH_CODE;
    }

    /***
     * 启动蓝牙后在Activity的onActivityResult方法中调用
     *
     * @param requestBluetooth  mHeadsetManager.requestStartBluetooth(requestBluetooth,mContext)启动蓝牙时自定义的code
     * @param requestCode  Activity的onActivityResult方法中的requestCode
     * @param resultCode   Activity的onActivityResult方法中的resultCode
     */
    public void onActivityResult(int requestBluetooth, int requestCode, int resultCode, OnBluetoothStartListener listener){
        if(requestBluetooth==requestCode){
            switch (resultCode) {
                case Activity.RESULT_OK://蓝牙启动成功
                    if(listener!=null){
                        listener.startSuccess();
                    }
                    break;
                case Activity.RESULT_CANCELED://蓝牙启动失败
                    if(listener!=null){
                        listener.startFailed();
                    }
                    break;
                default:
                    break;
            }
        }
    }

    /***
     * 连接蓝牙
     *
     * @param device
     * @return BluetoothManager.NO_SUPPORT：设备不支持蓝牙
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public int connectBluetooth(BluetoothDevice device,int requestBluetooth){
        if(!mHeadsetManager.isSupportBluetooth()){
            //本设备不支持蓝牙
            return BluetoothManager.NO_SUPPORT;
        }
        if(mHeadsetManager.isBluetoothOpen()){
            BluetoothLog.i("====开始配对=======");
            //绑定BluetoothA2DP，获得service
            mHeadsetManager.getBluetoothA2DP(mContext);
            //开始配对
            mHeadsetManager.createBond(device);
        }else{
            //去启动蓝牙
            mHeadsetManager.requestStartBluetooth(requestBluetooth,mContext);
        }
        return BluetoothManager.NO_BLUETOOTH_CODE;
    }

    /**监听蓝牙连接过程**/
    public void setOnReceiver(OnBluetoothListener listener){
        if(mBluetoothReceiver==null){
            return;
        }
        mBluetoothReceiver.setOnBluetoothListener(listener);

//        mBluetoothReceiver.setOnBluetoothListener(new OnBluetoothListener() {
//            @Override
//            public void deviceConnected(BluetoothDevice bluetoothDevice) {
////                //连接成功
////                ToastUtil.showShortToast(mContext,"连接成功");
//            }
//
//            @Override
//            public void deviceFound(BluetoothDevice bluetoothDevice) {
//                //发现新设备，填充到列表中展示
//
////                LogUtil.i("======发现新设备===");
////                if (!isExist(mDevices, bluetoothDevice)) {
////                    mDevices.add(bluetoothDevice);
////                    myAdapter.notifyDataSetChanged();
////                }
//            }
//
//            @Override
//            public void deviceBonded(BluetoothDevice bluetoothDevice) {
//                //取消搜索
//            }
//
//            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
//            @Override
//            public void deviceBondNone(BluetoothDevice bluetoothDevice) {
////                //配对不成功的话，重新尝试配对
////                mHeadsetManager.createBond(bluetoothDevice);
//            }
//
//            @Override
//            public void blootoothStateOn() {
////                //蓝牙已打开，开始搜索并连接service
////                mHeadsetManager.findBluetoothDevice();
////                mHeadsetManager.getBluetoothA2DP(mContext);
//            }
//        });
    }

    /**断开连接**/
    public void destory(){
        //注销广播
        if(mBluetoothReceiver!=null){
            mContext.unregisterReceiver(mBluetoothReceiver);
        }
        //注销蓝牙链接
        mHeadsetManager.disableAdapter();
        //断开蓝牙
        mHeadsetManager.closeBluetooth();
    }

}
