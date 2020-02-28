package com.labmatic.lablocation.framework.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import com.labmatic.lablocation.core.data.source.SensorManager
import com.labmatic.lablocation.core.data.source.SensorManagerCallbacks

class LABSensorManager: SensorManager, SensorEventListener {
    override var delegate: SensorManagerCallbacks? = null

    private lateinit var sensorManager: android.hardware.SensorManager
    private var pressure: Sensor? = null

    private var lastPressureUpdateTime: Long = 0

    override fun startPressureMonitoring(context: Context) {
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as android.hardware.SensorManager
        pressure = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE)

        if(hasBarometer()) {
            sensorManager.registerListener(this, pressure, android.hardware.SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun stopPressureMonitoring() {
        if(hasBarometer()) {
            if(sensorManager != null) {
                sensorManager.unregisterListener(this)
            }
        }
    }

    override fun hasBarometer() = pressure != null

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    override fun onSensorChanged(sensorEvent: SensorEvent?) {
        sensorEvent ?: return

        val currentTime = System.currentTimeMillis()

        if(currentTime - lastPressureUpdateTime >= 1000 * 2) {
            val pressure = sensorEvent?.values[0]
            lastPressureUpdateTime = currentTime
            delegate?.onPressureUpdated(pressure)
        }
    }
}