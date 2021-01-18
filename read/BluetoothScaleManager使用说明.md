## BluetoothScaleManager使用说明

### 简介
`BluetoothScaleManager`主要用于连接蓝牙电子秤，并获取电子秤称量数据传值的问题。开发者可以通过本类快速实现读取蓝牙电子秤称量数据的功能。

### 使用说明
#### 前置说明
蓝牙搜索与展示有两种方法，具体可参看[HeadsetHelper使用说明](https://github.com/ShaoqiangPei/BluetoothPro/blob/master/read/HeadsetHelper%E4%BD%BF%E7%94%A8%E8%AF%B4%E6%98%8E.md)
中的讲述。本类主要讲`BluetoothScaleManager`读取蓝牙电子秤的相关信息，故此处关于蓝牙搜索与展示就从简，采用系统展示并连接蓝牙的方式。  

#### 一.接入前的准备
##### 1.1 蓝牙及读写权限
蓝牙数据传输是通过`socket`读写的，蓝牙在`Androidmanifast.xml`中的权限已经在本库中添加，所以开发者需要在你项目的`Androidmanifast.xml`中添加读写相关权限：
```    
    <!-- 读写权限 -->
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
```
然后还要在你项目中添加 `FileProvider`及`Android 6.0+`文件手动权限申请，这里就不详细介绍了。

##### 1.2 设置电子秤数据传输模式
电子秤在称量结束后，将数据传输到Android设备上时，会有多种模式传值(电子秤说明书上称为打印方式)：
- contin：连续送出
- stable：稳定送,大于等于 20d 才可稳定输出
- key：按键送，有动作就输出
- ckok：检重ok后打印  

以上是我开发过程中，我的电子秤的打印模式，具体的大家在开发时可参考自己电子秤的说明书，都大同小异。然后选择打印模式为key,这样我们在每次按电子秤上的类似列印/HI的按钮，
就会把称量值传输到Android设备上。

#### 二.BluetoothScaleManager 基本方法介绍
```
    /***
     * 设置字符集
     *
     * @param charset 默认为 utf-8
     * @return
     */
    public BluetoothScaleManager setCharset(String charset)
```
蓝牙电子秤在读取并发送称量数据的时候，会涉及到字符集的问题，`setCharset(String charset)`主要用于设置读取数据时的字符集，默认不设置的时候，字符集采用`utf-8`,
此方法主要用于处理读取数据乱码的问题。一般可以不用设置，当收到数据乱码的时候，可考虑修改字符集。

```
    /**获取蓝牙电子秤数值的监听**/
    public void setOnBluethScaleListener(OnBluethScaleListener listener)
```
用于监听获取电子秤读取的数据信息。方法内部会返回三个参数：`original`，`value`和`unit`，其中`value`和`unit`是一组信息，主要用于获取电子秤单位为`g`或`kg`情况下的数据信息。
当你项目中称重单位只有两种：`g`和`kg`，则可以直接用`value`和`unit`两个值来作业务处理。`value`表示称重数值，`unit`表示单位`g`或`kg`，在此情况下，
若电子秤的单位不为`g`或`kg`的时候，则传值过去的时候，`unit`会返回`UN_KG_G_UNIT`，表示电子秤当前单位不为`g`或`kg`。当`unit`返回`null`时则表示未读取到电子秤数据中的单位信息。
若实际业务中，要求电子秤的单位为其他情况，如斤等，则此时你可以基于源数据`original`做自己的业务解析。

```
    /**电子秤连接状态**/
    public boolean isConnect()
```
此方法用于获取蓝牙电子秤连接状态。

```
    /***
     * 连接蓝牙电子秤
     *
     * @param device
     * @return BluetoothManager.NO_SUPPORT：设备不支持蓝牙
     *         BluetoothManager.SCALE_CONNECT_FAILED：电子秤蓝牙连接失败
     *         BluetoothManager.SCALE_CONNECT_SUCCESS: 电子秤蓝牙连接成功
     */
    public int connect(BluetoothDevice device)
```
`connect(BluetoothDevice device)`用于连接蓝牙电子秤，会返回3种可能的参数，分别代表`设备是否支持蓝牙`,`电子秤蓝牙连接失败`和`电子秤蓝牙连接成功`。

```
    /**为蓝牙连接做前置处理**/
    public boolean readyForBluetooth()
```
用于判断设备是否支持蓝牙功能，一般在调用`connect(BluetoothDevice device)`方法前调用该方法。

```
    /**关闭连接**/
    public void disConnect()
```
断开蓝牙电子秤的连接，一般在程序关闭退出时调用该方法。

#### 三.BluetoothScaleManager 在 Activity 中的使用
下面贴出`BluetoothScaleManager`在`Activity`中使用代码：
```
public class TempActivity extends AppCompatActivity{

    private TextView mTvTest;
    private Button mBtnTest;
    private RecyclerView mRecyclerView;

    private MyAdapter<BluetoothDevice>myAdapter;
    private List<BluetoothDevice>mDeviceList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temp);

        //初始化控件
        initView();
        //初始化数据
        initData();
        //控件监听
        setListener();
    }

    /**初始化控件**/
    private void initView(){
        mTvTest=findViewById(R.id.mTvTest);
        mBtnTest=findViewById(R.id.mBtnTest);
        mRecyclerView=findViewById(R.id.recyclerView);
    }

    private void initData(){
        mDeviceList=new ArrayList<>();

        //获取蓝牙已配备列表
        if(BluetoothScaleManager.getInstance().isSupportBluetooth()&&BluetoothScaleManager.getInstance().isBluetoothOpen()){
            List<BluetoothDevice>deviceList = BluetoothScaleManager.getInstance().checkDevices();
            if(!deviceList.isEmpty()){
                mDeviceList.addAll(deviceList);
            }
        }

        myAdapter=new MyAdapter<>(this,mDeviceList);
        myAdapter.setRecyclerManager(mRecyclerView);

    }


    /**控件监听**/
    private void setListener() {
        //跳转系统蓝牙界面
        mBtnTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogUtil.i("======我被点击了=======");
                BluetoothScaleManager.getInstance().goBluetoothSetting(TempActivity.this);
                //关闭当前界面
                finish();
            }
        });

        //连接蓝牙电子秤
        myAdapter.setOnRecyclerItemClickListener(new MyAdapter.OnRecyclerItemClickListener() {
            @Override
            public void onRecyclerClick(View view, int position) {
                BluetoothDevice device= mDeviceList.get(position);
                LogUtil.i("========点击连接======device.getName=${device.name}  address=${device.address}");
                //连接电子秤
                int status=BluetoothScaleManager.getInstance().connect(device);
                if(status== BluetoothManager.NO_SUPPORT){
                    ToastUtil.shortShow("本设备不支持蓝牙ok");
                }else if(status==BluetoothManager.SCALE_CONNECT_FAILED){
                    ToastUtil.shortShow("电子秤蓝牙连接失败ok");
                }else if(status==BluetoothManager.SCALE_CONNECT_SUCCESS){
                    ToastUtil.shortShow("电子秤蓝牙连接成功ok");
                }
            }
        });

        //电子秤监听
        BluetoothScaleManager.getInstance().setOnBluethScaleListener(new OnBluethScaleListener() {
            @Override
            public void readScaleValue(String original, String value, String unit) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        StringBuffer buffer=new StringBuffer();
                        buffer.append("原始值:"+original+"\n");
                        buffer.append("电子秤数值:"+value+"\n");
                        buffer.append("单位:"+unit);
                        ToastUtil.shortShow(buffer.toString());
                        LogUtil.i(buffer.toString());
                    }
                });
            }
        });

    }

    @Override
    protected void onDestroy() {
//        //断开电子秤连接,一般在程序退出时调用
//        BluetoothScaleManager.getInstance().disConnect();
        super.onDestroy();
    }
}
```



