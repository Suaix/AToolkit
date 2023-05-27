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
internal const val DARK_LIGHT_LUX = 90f
internal const val BRIGHT_LIGHT_LUX = 150f
internal const val DEFAULT_LIGHT_SENSOR_INTERVAL_TIME = 500L
const val QR_RESULT_CONTENT = "qr_result_content"
