package com.bluetoothlibrary.interfacer;

/**
 * Title:蓝牙电子秤称量数据监听
 * description:
 * autor:pei
 * created on 2021/1/18
 */
public interface OnBluethScaleListener {

    /***
     * @param original 原始值,若想对称量获取的值自己做处理的话，可以基于此值做处理
     * @param value 称量值
     * @param unit 返回的单位
     */
    void readScaleValue(String original,String value, String unit);
}
