## AStorage存储相关能力

### 初始化

```kotlin
initAStorage(context, config)
```

调用 `initAStorage` 进行初始化，其中 `config` 为可选字段，可以配置使用的log（可为null，表示不输出log）、
k-v存储实现类（默认为null，内部会使用DataStore进行k-v存储）和额外参数（供k-v存储初始化使用）。如不使用DataStore进
行k-v存储，可以实现 `IKVStorage` 接口，配置自己实现的k-v存储实现类。k-v存储使用示例如下：

```kotlin
kvStorage().putValue(key, value)
val result = kvStorage().getValue(key, defaultValue)
```

### 其他储存相关能力

