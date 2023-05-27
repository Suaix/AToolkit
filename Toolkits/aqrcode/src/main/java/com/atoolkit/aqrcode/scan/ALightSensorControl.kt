package com.atoolkit.aqrcode.scan

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.atoolkit.aqrcode.BRIGHT_LIGHT_LUX
import com.atoolkit.aqrcode.DARK_LIGHT_LUX
import com.atoolkit.aqrcode.DEFAULT_LIGHT_SENSOR_INTERVAL_TIME
import com.atoolkit.aqrcode.TAG
import com.atoolkit.aqrcode.aLog
import com.atoolkit.aqrcode.application


/**
 * Author:summer
 * Time: 2023/5/27 15:07
 * Description: ALightSensorControl是环境感光传感器控制器，用来感光环境光线的亮度
 */
class ALightSensorControl(
    private val darkLightLux: Float = DARK_LIGHT_LUX,
    private val brightLightLux: Float = BRIGHT_LIGHT_LUX,
    private val intervalTime: Long = DEFAULT_LIGHT_SENSOR_INTERVAL_TIME
) : SensorEventListener {

    private var mSensorManager: SensorManager? = null
    private var mLightSensor: Sensor? = null
    private var mLightChangeListener: ILightChangedListener? = null
    private var mLastTime: Long = 0

    init {
        val service = application.getSystemService(Context.SENSOR_SERVICE)
        if (service is SensorManager) {
            mSensorManager = service
            mLightSensor = mSensorManager?.getDefaultSensor(Sensor.TYPE_LIGHT)
        }
    }

    /**
     * Description: 注册手机感光传感器监听
     * Author: summer
     *
     * @param onLightChangedListener 感光传感器变化监听
     */
    fun register(onLightChangedListener: ILightChangedListener) {
        mLightChangeListener = onLightChangedListener
        mLightSensor?.let {
            mSensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    /**
     * Description: 反注册感光传感器监听
     * Author: summer
     */
    fun unregister() {
        mSensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        val currentTimeMillis = System.currentTimeMillis()
        if (currentTimeMillis - mLastTime < intervalTime) {
            return
        }
        mLastTime = currentTimeMillis
        val lightLux = event?.values?.get(0)
        lightLux?.let {
            var state = LightState.NORMAL
            if (it <= darkLightLux) {
                state = LightState.DARK
            } else if (it >= brightLightLux) {
                state = LightState.BRIGHT
            }
            mLightChangeListener?.onLightChanged(state, it)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }
}