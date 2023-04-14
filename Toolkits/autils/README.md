## AUtils工具类能力
### 初始化
调用`AUtils`里的`initAUtils(context, log, isUseLocalCache)`初始化工具，其中第三个参数`isUseLocalCache`指代
是否使用本地缓存，对于一些隐私合规项信息（如设备信息imei、androidId、手机型号等）为了避免重复获取，再获取到一次后是否
缓存到本地，默认为使用缓存（会使用`DataStore`进行`key-value`存储）。除此之外也会进行内存缓存，获取一次之后下次使用会
优先使用内存缓存数据，没有内存缓存时会尝试获取本地缓存，最后才会调用对应api获取。从而避免设备等敏感信息频繁获取。

`DeviceInfoUtil`
    | `getImei()` 获取设备imei号，Android10及以上系统获取为空；
    | `getAndroidId()` 获取设备AndroidId
    | `getMac()` 获取设备mac地址
    | `getBSSID()` 获取设备连接的wifi的bssid标识
    | `getSSID()` 获取设备wifi的ssid标识
    | `getPhoneModel()` 获取手机型号，即`Build.MODEL`
    | `getPhoneBrand()` 获取手机品牌，即`Build.BRAND`
    | `getPhoneManufacturer()` 获取手机制造商，即`Build.MANUFACTURER`
    | `getPhoneDevice()` 获取手机设计外观名称，即`Build.DEVICE`
    | `getScreenWidth()` 获取手机屏幕宽度
    | `getScreenHeight()` 获取手机屏幕高度
    | `getStatusBarHeight()` 获取手机状态栏高度
    | `getNavigationBarHeight()` 获取手机底部导航栏高度
    | `dp2Px(dpValue: Float)` dp转px，返回Float
    | `sp2Px(spValue: Float)` sp转px，返回Float
    | `px2Dp(pxValue: Float)` px转dp，返回Int

`PhoneStatusUtils`
    | `isNavigationBarShow()` 底部状态栏是否展示
    | `isNotificationEnabled()` 判断APP通知是否打开
    | `goNotificationSetting()` 跳转到通知设置页面，API26及以上系统会直接跳转到通知设置页，API26以下跳转到应用详情设置页
    | `isLocationEnabled()` 判断手机定位是否开启
    | `getLocationSetting()` 跳转到定位开关设置页
    | `isWifiEnabled()` 判断手机wifi是否打开
    | `goWifiSetting()` 跳转到wifi设置页面
    | `isBluetoothEnabled()` 判断手机蓝牙是否打开
    | `goBluetoothSetting()` 跳转蓝牙设置页面
    | `isNFCEnabled()` 判断NFC是否可用
    | `goNFCSetting()` 跳转到NFC设置页（如果有NFC功能的话）
