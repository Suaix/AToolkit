package com.atoolkit.astorage

import com.atoolkit.common.ILog

data class AStorageConfig(
    val log: ILog?,
    val kvStorage: IKVStorage? = null,
    val extra: Any? = null
)