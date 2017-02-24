package com.kazakago.irremocon

import android.app.Activity
import android.os.Bundle
import android.util.Log
import com.google.android.things.pio.Gpio
import com.google.android.things.pio.GpioCallback
import com.google.android.things.pio.PeripheralManagerService
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.concurrent.schedule

class MainActivity : Activity() {

    companion object {
        private val TAG = MainActivity::class.java.simpleName
        private val PIN_NAME__IR_SENSER = "BCM18"
    }

    private lateinit var irSensorGpio: Gpio
    private var isIrScanning: Boolean = false
    private var timeList = ArrayList<Long>()

    private val gpioCallback = object : GpioCallback() {
        override fun onGpioEdge(gpio: Gpio): Boolean {
            if (gpio.value) {
                Log.i(TAG, "GPIO High " + System.nanoTime())
            } else {
                if (isIrScanning) startScan()
                Log.i(TAG, "GPIO Low " + System.nanoTime())
            }
            return true
        }

        override fun onGpioError(gpio: Gpio?, error: Int) {
            Log.w(TAG, gpio.toString() + ": Error event " + error)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, "Starting MainActivity")

        val service = PeripheralManagerService()
        irSensorGpio = service.openGpio(PIN_NAME__IR_SENSER)
        // ピンをインプットモードで動作させます
        irSensorGpio.setDirection(Gpio.DIRECTION_IN)
        // センサーがHighを返した時にtrueを返すようにします
        irSensorGpio.setActiveType(Gpio.ACTIVE_HIGH)
        // センサーがHigh,Lowどちらかに変更になった場合にコールバック関数を呼び出す
        irSensorGpio.setEdgeTriggerType(Gpio.EDGE_BOTH)
    }

    override fun onStart() {
        super.onStart()
        // コールバックを登録
        irSensorGpio.registerGpioCallback(gpioCallback)
    }

    override fun onStop() {
        super.onStop()
        // コールバックを解除
        irSensorGpio.unregisterGpioCallback(gpioCallback)
    }

    override fun onDestroy() {
        super.onDestroy()
        irSensorGpio.close()
    }

    fun startScan() {
        isIrScanning = true
        timeList.clear()
        Timer().schedule(TimeUnit.SECONDS.toMillis(1)) {
            isIrScanning = false
        }
    }

}
