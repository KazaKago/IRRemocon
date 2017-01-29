package com.kazakago.humansensor

import android.app.Activity
import android.os.Bundle
import android.util.Log
import com.google.android.things.pio.Gpio
import com.google.android.things.pio.GpioCallback
import com.google.android.things.pio.PeripheralManagerService

class MainActivity : Activity() {

    companion object {
        private val TAG = MainActivity::class.java.simpleName
        private val PIN_NAME = "BCM18"
    }

    private lateinit var gpio: Gpio
    private val gpioCallback = object : GpioCallback() {
        override fun onGpioEdge(gpio: Gpio): Boolean {
            if (gpio.value) {
                // 人の動きを検知
                Log.i(TAG, "GPIO High")
            } else {
                // 人が動かずに一定時間経過
                Log.i(TAG, "GPIO Low")
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
        gpio = service.openGpio(PIN_NAME)
        // ピンをインプットモードで動作させます
        gpio.setDirection(Gpio.DIRECTION_IN)
        // センサーがHighを返した時にtrueを返すようにします
        gpio.setActiveType(Gpio.ACTIVE_HIGH)
        // センサーがHigh,Lowどちらかに変更になった場合にコールバック関数を呼び出す
        gpio.setEdgeTriggerType(Gpio.EDGE_BOTH)
    }

    override fun onStart() {
        super.onStart()
        // コールバックを登録
        gpio.registerGpioCallback(gpioCallback)
    }

    override fun onStop() {
        super.onStop()
        // コールバックを解除
        gpio.unregisterGpioCallback(gpioCallback)
    }

}
