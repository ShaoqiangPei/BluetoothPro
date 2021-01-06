package com.bluetoothlibrary.base;

import android.bluetooth.BluetoothDevice;

/**
 * Title:蓝牙链接状态返回处理接口
 * Description:
 * <p>
 * Created by pei
 * Date: 2018/4/27
 */
public interface OnBluetoothListener {

    void deviceConnected(BluetoothDevice bluetoothDevice);

    void deviceFound(BluetoothDevice bluetoothDevice);

    void deviceBonded(BluetoothDevice bluetoothDevice);

    void deviceBondNone(BluetoothDevice bluetoothDevice);

    void blootoothStateOn();

}
