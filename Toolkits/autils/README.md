## AUtils工具类能力

### 初始化

```kotlin
initAUtils(context, log, isUserLocalCache)
```

调用 `AUtils` 里的 `initAUtils(context, log, isUseLocalCache)` 初始化工具，其中第三个参数 `isUseLocalCache`
指代是否使用本地缓存，对于一些隐私合规项信息（如设备信息imei、androidId、手机型号等）为了避免重复获取，再获取到一次后
是否 缓存到本地，默认为使用缓存（会使用 `DataStore` 进行 `key-value` 存储）。除此之外也会进行内存缓存，获取一次之后
下次使用会 优先使用内存缓存数据，没有内存缓存时会尝试获取本地缓存，最后才会调用对应api获取。从而避免设备等敏感信息频繁获取。

### 相关工具能力列表

**DeviceInfoUtil**：提供获取设备相关信息的工具方法
| 方法 | 说明 | 备注 |
| :-- | :-- | :-- |
| `getImei()` | 获取设备imei号，Android10及以上系统获取为空；| |
| `getAndroidId()` | 获取设备AndroidId | |
| `getMac()` | 获取设备mac地址 | |
| `getBSSID()` |获取设备连接的wifi的bssid标识 | |
| `getSSID()` | 获取设备wifi的ssid标识 | |
| `getPhoneModel()` | 获取手机型号 | 即`Build.MODEL` |
| `getPhoneBrand()` | 获取手机品牌 | 即`Build.BRAND` |
| `getPhoneManufacturer()` | 获取手机制造商 | 即`Build.MANUFACTURER` |
| `getPhoneDevice()` | 获取手机设计外观名称 | 即`Build.DEVICE` |
| `getScreenWidth()` | 获取手机屏幕宽度 | 单位px |
| `getScreenHeight()` | 获取手机屏幕高度 | 单位px |
| `getStatusBarHeight()` | 获取手机状态栏高度 | |
| `getNavigationBarHeight()` | 获取手机底部导航栏高度 | |
| `dp2Px(dpValue: Float)` | dp转px | 返回Float结果 |
| `sp2Px(spValue: Float)` | sp转px | 返回Float结果 |
| `px2Dp(pxValue: Float)` | px转dp | 返回Int结果 |

**PhoneStatusUtils**：提供获取手机状态的相关工具方法
| 方法 | 说明 | 备注 |
| :-- | :-- | :-- |
| `isNavigationBarShow()` | 底部状态栏是否展示 | |
| `isNotificationEnabled()` | 判断APP通知是否打开 | |
| `goNotificationSetting()` | 跳转到通知设置页面，API26及以上系统会直接跳转到通知设置页，</br>API26以下跳转到应用详情设置页 | |
| `isLocationEnabled()` | 判断手机定位是否开启 | |
| `getLocationSetting()` | 跳转到定位开关设置页 | |
| `isWifiEnabled()` | 判断手机wifi是否打开 | |
| `goWifiSetting()` | 跳转到wifi设置页面 | |
| `isBluetoothEnabled()` | 判断手机蓝牙是否打开 | |
| `goBluetoothSetting()` | 跳转蓝牙设置页面 | |
| `isNFCEnabled()` | 判断NFC是否可用 | |
| `goNFCSetting()` | 跳转到NFC设置页（如果有NFC功能的话） | |


**AFileUtils**：提供文件操作相关工具方法
| 方法 | 说明 | 备注 |
| :-- | :-- | :-- |
| `getInternalFileDir()` | 获取app内部文件目录 | 无需申请权限，模拟器示例：`/data/user/0/[package]/files` |
| `getInternalCacheDir()` | 获取app内部缓存文件目录 | 无需申请权限，模拟器示例：`/data/user/0/[package]/cache` |
| `isExternalStorageReadable()` | 外部存储是否可读 | |
| `isExternalStorageWritable()` | 外部存储是否可写 | |
| `getExternalFilesDir()` | 获取app指定类型外部文件存储目录 | Api>=19时无需声明权限， 模拟器示例：</br>`/storage/emulated/0/Android/data/[package]/files` |
| `getExternalCacheDir()` | 获取app外部缓存目录 | Api>=19时无需声明权限，模拟器示例：</br>`/storage/emulated/0/Android/data/[package]/cache` |
| `createFile()` | 创建文件 | 会关联创建父目录（如不存在） |
| `deleteFileByPath()` | 根据文件路径删除文件 | |
| `renameFile()` | 重命名文件 | |
| `zipFile()` | 对文件进行压缩，生成.zip文件 | 耗时操作，kotlin需要在协程里调用，java在线程里调用 |
| `unzipFile()` | 解压缩.zip文件 | 耗时操作，kotlin需要在协程中调用，java在线程中调用 |
