api < 6.0
> 默认授予权限，不需要动态申请；

6.0 <= api < 11
> 需要动态申请
> 用户可以选择：拒绝（之后还可以再申请）、拒绝后不再提醒（不可再申请）、授予权限；
> 拒绝：`shouldShowRequestPermissionRationale(permission)`返回`true`；
> 拒绝后不再提醒：`shouldShowRequestPermissionRationale(permission)`返回`false`；

api >= 11
> 需要动态申请
> 用户可以选择：拒绝或授予权限
> 拒绝：拒绝2次以上即为"拒绝后不再提醒"

特殊权限处理
> 申请精确位置权限`android.Manifest.permission.ACCESS_FINE_LOCATION`
> 必须带上大致范围权限`android.Manifest.permission.ACCESS_COARSE_LOCATION`。
> 申请大致范围权限可以不带精确位置权限。
> Android 13以上新增通知栏权限：android.permission.POST_NOTIFICATIONS。
> 写入外部存储权限在Android 10以上不可再申请：
