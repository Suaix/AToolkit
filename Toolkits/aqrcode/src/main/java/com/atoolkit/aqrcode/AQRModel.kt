package com.atoolkit.aqrcode

import com.atoolkit.common.ILog

data class AQRConfig(
    val log: ILog?,
    val extra: Any? = null
)

internal const val IMAGE_QUALITY_1080P = 1080
internal const val IMAGE_QUALITY_720P = 720
internal const val ASPECT_RATIO_4_3 = 4f / 3f
internal const val ASPECT_RATIO_16_9 = 16f / 9f
