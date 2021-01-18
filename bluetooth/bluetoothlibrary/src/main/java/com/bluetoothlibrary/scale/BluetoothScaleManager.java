package com.bluetoothlibrary.scale;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import com.bluetoothlibrary.base.BluetoothManager;
import com.bluetoothlibrary.interfacer.OnBluethScaleListener;
import com.bluetoothlibrary.util.BluetoothLog;
import com.bluetoothlibrary.util.StringUtil;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.UUID;

/**
 * Title:蓝牙电子秤连接管理类
 *
 * description:
 * autor:pei
 * created on 2021/1/18
 */
public class BluetoothScaleManager extends BluetoothManager {

    //SPP服务UUID号,固定为 00001101-0000-1000-8000-00805F9B34FB
    private static final String SPP_UUID="00001101-0000-1000-8000-00805F9B34FB";
    private static final String EN_UNIT_KG="k";
    private static final String EN_UNIT_G="g";

    public static final String UN_KG_G_UNIT="UN_KG_G_UNIT";//非 kg/g 的单位

    private BluetoothSocket mBluetoothSocket;
    private OnBluethScaleListener mOnBluethScaleListener;
    private boolean mCanRead=false;
    private String mCharset= BluetoothManager.UTF_8;//解析字符集默认为utf-8

    private BluetoothScaleManager(){}

    private static class Holder {
        private static BluetoothScaleManager instance = new BluetoothScaleManager();
    }

    public static BluetoothScaleManager getInstance() {
        return Holder.instance;
    }

    /**获取蓝牙电子秤数值的监听**/
    public void setOnBluethScaleListener(OnBluethScaleListener listener){
        this.mOnBluethScaleListener=listener;
    }

    /***
     * 设置字符集
     *
     * @param charset 默认为 utf-8
     * @return
     */
    public BluetoothScaleManager setCharset(String charset){
        if(StringUtil.isNotEmpty(charset)){
            this.mCharset=charset;
        }
        return BluetoothScaleManager.this;
    }

    /**电子秤连接状态**/
    public boolean isConnect(){
        if(mBluetoothSocket!=null){
            return mBluetoothSocket.isConnected();
        }
        return false;
    }

    /***
     * 连接蓝牙电子秤
     *
     * @param device
     * @return BluetoothManager.NO_SUPPORT：设备不支持蓝牙
     *         BluetoothManager.SCALE_CONNECT_FAILED：电子秤蓝牙连接失败
     *         BluetoothManager.SCALE_CONNECT_SUCCESS: 电子秤蓝牙连接成功
     */
    public int connect(BluetoothDevice device){
        if(!readyForBluetooth()){
            //设备不支持蓝牙
            BluetoothLog.i("=====设备不支持蓝牙==");
            return BluetoothManager.NO_SUPPORT;
        }
        mCanRead=true;
        //SPP服务UUID号,固定为 00001101-0000-1000-8000-00805F9B34FB
        UUID uuid= UUID.fromString(SPP_UUID);
        try {
            BluetoothLog.i("=====开始获取mBluetoothSocket对象=======");
            mBluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(uuid);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (mBluetoothSocket == null) {
            //电子秤蓝牙连接失败
            BluetoothLog.i("=====电子秤蓝牙连接失败==");
            return BluetoothManager.SCALE_CONNECT_FAILED;
        }
        //mBluetoothSocket连接
        if (!mBluetoothSocket.isConnected()) {
            BluetoothLog.i("======蓝牙mBluetoothSocket未连接,现在开始连接=======");
            try {
                mBluetoothSocket.connect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            BluetoothLog.i("======蓝牙mBluetoothSocket已连接=======");
        }
        BluetoothLog.i("======蓝牙mBluetoothSocket连接成功=======");

        //新建线程读取数据
        new Thread(new Runnable() {
            @Override
            public void run() {
                //读取电子秤数据
                readScaleData();
                //线程结束
                BluetoothLog.i("========线程结束=========");
            }
        }).start();

        //电子秤蓝牙连接成功
        return BluetoothManager.SCALE_CONNECT_SUCCESS;
    }

    /**读取电子秤数据**/
    private void readScaleData(){
        InputStream inputStream =null;
        try {
            inputStream = mBluetoothSocket.getInputStream();
            while(mCanRead){
                BluetoothLog.i("======线程已经准备读电子秤========");
                byte buffer[]= new byte[32];
                if (inputStream != null && inputStream.read(buffer) > 0 ) {
                    String message= new String(buffer, Charset.forName(mCharset));
                    BluetoothLog.i("=======电子秤称量原始值==="+message);
                    if(StringUtil.isNotEmpty(message)) {
                        String tempValueArray[] = formatValueAndUnit(message);
                        if (tempValueArray != null && StringUtil.isNotEmpty(tempValueArray[0]) && mOnBluethScaleListener != null) {
                            BluetoothLog.i("=======电子秤称读取====数值:" + tempValueArray[0] + ",  单位：" + tempValueArray[1]);
                            mOnBluethScaleListener.readScaleValue(message,tempValueArray[0], tempValueArray[1]);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**获取电子秤读取的值及单位(只识别单位为kg或为g)**/
    private String[] formatValueAndUnit(String message){
        //读取的值必须包含数字和单位
        String tempStr=message.trim();
        if(StringUtil.isDoubleFormat(tempStr)){
            //若获取的数值为数字则返回为null,需要重新读取
            return null;
        }else{
            //含数字和单位的需要截取
            int indx=BluetoothManager.NO_BLUETOOTH_CODE;
            for (int i = 0; i <tempStr.length(); i++) {
                String temp=String.valueOf(tempStr.charAt(i));
                if(StringUtil.isAllLetter(temp)){
                    //只识别 g 和 kg
                    indx=i;
                    break;
                }
            }

            if(indx!=BluetoothManager.NO_BLUETOOTH_CODE){
                String tempMessage=tempStr.substring(0,indx+1).trim();
                //确认tempMessage由数字和字母组成,则拆分开
                if(StringUtil.isNotEmpty(tempMessage)){
                    String value=tempMessage.substring(0,tempMessage.length()-1).trim();
                    String unit=tempMessage.substring(tempMessage.length()-1).trim();
                    if(StringUtil.isDoubleFormat(value)){
                        String valueArray[]=new String[2];

                        String unitStr=null;
                        if (BluetoothScaleManager.EN_UNIT_KG.equalsIgnoreCase(unit)) {//忽略大小写比较
                            unitStr="kg";
                        } else if (BluetoothScaleManager.EN_UNIT_G.equalsIgnoreCase(unit)) {//忽略大小写比较
                            unitStr="g";
                        } else if (StringUtil.isNotEmpty(unit)) {
                            //为非 g 或 kg 的单位,提示电子秤要切换到  g 或 kg 的单位
                            unitStr=BluetoothScaleManager.UN_KG_G_UNIT;
                        }else{
                            unitStr=null;
                        }
                        valueArray[0]=value;
                        valueArray[1]=unitStr;
                        return valueArray;
                    }
                }
            }
        }
        return null;
    }

    /**为蓝牙连接做前置处理**/
    public boolean readyForBluetooth(){
        if (isSupportBluetooth()) {
            if (!isBluetoothOpen()) {
                //强制开蓝牙
                openBluetooth();
            }
            return true;
        }
        //当前设备不支持蓝牙
        return false;
    }

    /**关闭连接**/
    public void disConnect(){
        mCanRead=false;
        //关闭socket
        try {
            if (mBluetoothSocket != null) {
                mBluetoothSocket.close();
                mBluetoothSocket = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        //取消搜索
        cancelDiscovery();
        BluetoothLog.i("=======BluetoothScaleManager断开=======");
    }

}
