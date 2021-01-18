# HeadsetHelper使用说明

## 概述
`HeadsetHelper`是一个用于连接蓝牙耳机的帮助类，使用此类可以快速帮你实现蓝牙耳机连接的功能。

## 使用说明
### 一.逻辑梳理
`Android`实现蓝牙功能的麻烦地方在于蓝牙的 搜索，配对，以及此过程中对于蓝牙各状态的监听。在寻找蓝牙的过程中，我们可以用两种方式来搜索和连接蓝牙，具体如下：  
- (方法A)自己搜索蓝牙，然后过滤实现一个蓝牙列表
- (方法B)跳转到系统蓝牙界面，将蓝牙的搜索及列表展示交给系统来处理

此两种方式的优劣如下：
`方法A`劣势在于实现起来比较麻烦，要监听的东西太多,在搜索展示蓝牙列表时，容易出现搜索不到或搜索重复的问题，需要自己对搜索结果做进一步处理。优势在于，搜索完蓝牙列表后，可方便用户去点击所需连接蓝牙项，
让整个蓝牙搜索，配对，连接过程环环相扣，很好的做到了整个业务流程的完整性，中途不会受到打断。  
`方法B`劣势在于当把蓝牙的搜索，配对和连接过程交由系统处理后，蓝牙配对完毕后不能直接跳到蓝牙已配对列表界面，对于蓝牙的连接需要重新进入蓝牙列表界面，然后再点击该蓝牙项才能连接蓝牙，打断了蓝牙搜索，配对，
连接的一贯完整性。但其优点是，蓝牙搜索的功能交给系统后，搜索结果要准确得多，不会出现搜索蓝牙的重复性，对于搜搜得到的蓝牙结果不需要我们做处理。

### 二. 蓝牙耳机蓝牙连接流程
上面我们已经讲过了蓝牙搜索，配对，连接实现的两种方式。下面来一 一 讲解。  
#### 2.1 自己实现蓝牙搜索流程
整个流程逻辑的话，在进入界面后，要展示蓝牙已配对列表，还要注册蓝牙监听广播，用来监听蓝牙各状态，然后好做处理，则界面初始化话时要做一下准备：
```
        //获取蓝牙相关
        mHeadsetHelper=new HeadsetHelper(BluetoothActivity.this);
        //注册蓝牙监听
        mHeadsetHelper.registerReceiver();
        //显示蓝牙列表
        List<BluetoothDevice> list=mHeadsetHelper.getPairedDevices();
```
接着在`setListener()`中要对蓝牙广播接收做相应处理：
```
    private void setListener(){
        //监听蓝牙耳机连接状态
        mHeadsetHelper.setOnReceiver(new OnBluetoothListener() {
            @Override
            public void deviceConnected(BluetoothDevice bluetoothDevice) {
                BluetoothLog.i("====deviceConnected====");
                //连接成功
                Toast.makeText(BluetoothActivity.this,"连接成功",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void deviceFound(BluetoothDevice bluetoothDevice) {
                BluetoothLog.i("====deviceFound====");
                //发现新设备，填充到列表中展示
                ToastUtil.shortShow("发现新设备");
                //以下处理省略
                //......
            }

            @Override
            public void deviceBonded(BluetoothDevice bluetoothDevice) {
                BluetoothLog.i("====deviceBonded====");
                //取消搜索
                ToastUtil.shortShow("取消搜索");
            }

            @Override
            public void deviceBondNone(BluetoothDevice bluetoothDevice) {
                BluetoothLog.i("====deviceBondNone====");
                //配对失败
                //以下处理省略
                //......
            }

            @Override
            public void blootoothStateOn() {
                BluetoothLog.i("====blootoothStateOn====");

                ToastUtil.shortShow("蓝牙已打开,开始搜索并连接service");
                //以下处理省略
                //......
            }
        });
    }
```
然后在点击`搜索`按钮时，用以下方法进行蓝牙搜索：
```
           //搜索
           int searchCode=mHeadsetHelper.searchBluetooth(mRequestBlueToothCode);
```
搜索过程中涉及到打开蓝牙请求的询问，对于此请求的返回需要在`Activity`的`onActivityResult`进行处理,类似如下：
```
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mHeadsetHelper.onActivityResult(mRequestCode, requestCode, resultCode, new OnBluetoothStartListener() {
            @Override
            public void startSuccess() {
                BluetoothLog.i("====open success====");
                //以下处理省略
                //......
            }

            @Override
            public void startFailed() {
                BluetoothLog.i("====open failed====");
                //以下处理省略
                //......
            }
        });
    }
```
最后，在需要断开蓝牙连接的地方(一般为`Activity`的`onDestroy()`方法)调用以下方法用以回收资源：
```
        //取消蓝牙耳机连接
        mHeadsetHelper.destory();
```
下面给出`自己实现蓝牙搜索流程`在`Activity`界面中使用的完整示例代码：
```
/**
 * Title:
 * description:
 * autor:pei
 * created on 2020/11/24
 */
public class BluetoothActivity extends AppCompatActivity {

    private Button mButton;//搜索蓝牙
    private Button mButtonCancel;//取消搜索蓝牙
    private RecyclerView mRecyclerView;

    private List<BluetoothDevice> mDeviceList;
    private MyAdapter myAdapter;

    private int mRequestBlueToothCode=100;//请求开蓝牙的code
    private HeadsetHelper mHeadsetHelper;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        //初始化控件
        initView();
        //初始化数据
        initData();
        //设置监听
        setListener();

    }

    /**初始化控件**/
    private void initView(){
        mButton=findViewById(R.id.btn1);
        mButtonCancel=findViewById(R.id.btn2);
        mRecyclerView=findViewById(R.id.rv);
    }

    /**初始化数据**/
    private void initData(){
        //设置蓝牙打印调试
        BluetoothLog.setDebug(true);
        //获取蓝牙相关
        mHeadsetHelper=new HeadsetHelper(BluetoothActivity.this);
        //注册蓝牙监听
        mHeadsetHelper.registerReceiver();
        //显示蓝牙列表
        List<BluetoothDevice> list=mHeadsetHelper.getPairedDevices();

        //列表填充数据
        mDeviceList=new ArrayList<>();
        if(!list.isEmpty()){
            mDeviceList.addAll(list);
        }
        myAdapter=new MyAdapter<>(this,mDeviceList);
        myAdapter.setRecyclerManager(mRecyclerView);
    }

    /**设置监听**/
    private void setListener(){
        //搜索
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //搜索
                int searchCode=mHeadsetHelper.searchBluetooth(mRequestBlueToothCode);
                if (searchCode == BluetoothManager.NO_SUPPORT) {
                    ToastUtil.shortShow("本设备不支持蓝牙");
                }
            }
        });

        //连接
        myAdapter.setOnRecyclerItemClickListener(new MyAdapter.OnRecyclerItemClickListener() {
            @Override
            public void onRecyclerClick(View view, int position) {
                BluetoothDevice device = mDeviceList.get(position);

                BluetoothLog.i("=====点击了=======");

                int connectCode=mHeadsetHelper.connectBluetooth(device,mRequestBlueToothCode);
                if(connectCode==BluetoothManager.NO_SUPPORT){
                    ToastUtil.shortShow("本设备不支持蓝牙");
                }
            }
        });

        //取消搜索
        mButtonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //停止搜索
                mHeadsetHelper.getHeadsetManager().cancelDiscovery();
            }
        });

        //监听蓝牙耳机连接状态
        mHeadsetHelper.setOnReceiver(new OnBluetoothListener() {
            @Override
            public void deviceConnected(BluetoothDevice bluetoothDevice) {
                BluetoothLog.i("====deviceConnected====");
                //连接成功
                Toast.makeText(BluetoothActivity.this,"连接成功",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void deviceFound(BluetoothDevice bluetoothDevice) {
                BluetoothLog.i("====deviceFound====");
                //发现新设备，填充到列表中展示
                ToastUtil.shortShow("发现新设备");

                BluetoothLog.i("===发现新设备==="+bluetoothDevice.getName());

                if (mHeadsetHelper.getHeadsetManager().getAddressIndex(mDeviceList, bluetoothDevice)==BluetoothManager.NO_BLUETOOTH_CODE) {
                    mDeviceList.add(bluetoothDevice);
                    myAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void deviceBonded(BluetoothDevice bluetoothDevice) {
                BluetoothLog.i("====deviceBonded====");
                //取消搜索
                ToastUtil.shortShow("取消搜索");
            }

            @Override
            public void deviceBondNone(BluetoothDevice bluetoothDevice) {
                BluetoothLog.i("====deviceBondNone====");
                //                //配对失败(尝试重新配对)
                //                mHeadsetManager.createBond(bluetoothDevice);

                ToastUtil.shortShow("配对失败");
            }

            @Override
            public void blootoothStateOn() {
                BluetoothLog.i("====blootoothStateOn====");

                ToastUtil.shortShow("蓝牙已打开,开始搜索并连接service");
                //蓝牙已打开，开始搜索并连接service
                mHeadsetHelper.getHeadsetManager().getBluetoothA2DP(BluetoothActivity.this);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mHeadsetHelper.onActivityResult(mRequestBlueToothCode, requestCode, resultCode, new OnBluetoothStartListener() {
            @Override
            public void startSuccess() {
                BluetoothLog.i("====open success====");

                //开始查询配对设备
                List<BluetoothDevice> devices = mHeadsetHelper.getPairedDevices();
                //添加到列表中显示
                mDeviceList.clear();
                mDeviceList.addAll(devices);
                myAdapter.notifyDataSetChanged();
            }

            @Override
            public void startFailed() {
                ToastUtil.shortShow("蓝牙打开失败");
                BluetoothLog.i("====open failed====");
            }
        });
    }

    @Override
    protected void onDestroy() {
        //取消蓝牙耳机连接
        mHeadsetHelper.destory();
        super.onDestroy();
    }

}
```
#### 2.2 跳转到系统蓝牙界面进行蓝牙搜索和配对
用此种方法的话，就不需要注册蓝牙广播相关监听了，也不需要处理`onActivityResult`,我们只需要在进入界面时：
```
        //获取蓝牙相关
        mHeadsetHelper=new HeadsetHelper(BluetoothActivity.this);
        //显示蓝牙列表
        List<BluetoothDevice> list=mHeadsetHelper.getPairedDevices();
```
然后搜索时：
```
        //搜索
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //跳转到系统蓝牙界面
                mHeadsetHelper.getHeadsetManager().goBluetoothSetting(BluetoothActivity.this);             
            }
        });
```
最后连接的话，就和`2.1`一样了：
```
        //连接
        myAdapter.setOnRecyclerItemClickListener(new MyAdapter.OnRecyclerItemClickListener() {
            @Override
            public void onRecyclerClick(View view, int position) {
                BluetoothDevice device = mDeviceList.get(position);

                BluetoothLog.i("=====点击了=======");

                int connectCode=mHeadsetHelper.connectBluetooth(device,mRequestBlueToothCode);
                if(connectCode==BluetoothManager.NO_SUPPORT){
                    ToastUtil.shortShow("本设备不支持蓝牙");
                }
            }
        });
```
最后，在需要断开蓝牙连接的地方(一般为`Activity`的`onDestroy()`方法)调用以下方法用以回收资源：
```
        //取消蓝牙耳机连接
        mHeadsetHelper.destory();
```
#### 2.3 蓝牙显示连接上，但仍然没连上的处理
一般出在已配对的蓝牙，然后在连接的时候，容易出现此现象。我们可以尝试用以下方法先清空已配对蓝牙列表：
```
                //得到配对的设备列表,清除已配对的设备
                mHeadsetHelper.getHeadsetManager().removePairDeviceList();
```
然后再重新搜索蓝牙设备。

