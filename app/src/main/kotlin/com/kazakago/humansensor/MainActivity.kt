package com.kazakago.humansensor

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import com.google.android.things.contrib.driver.button.Button
import com.google.android.things.contrib.driver.button.ButtonInputDriver
import com.google.android.things.pio.Gpio
import com.google.android.things.pio.GpioCallback
import com.google.android.things.pio.PeripheralManagerService

class MainActivity : Activity() {

    companion object {
        private val TAG = MainActivity::class.java.simpleName
        private val PIN_NAME__HUMAN_SENSOR = "BCM18"
        private val PIN_NAME__LED = "BCM6"
        private val PIN_NAME__BUTTON = "BCM21"
    }

    private lateinit var humanSensorGpio: Gpio
    private lateinit var ledGpio: Gpio
    private lateinit var buttonInputDriver: ButtonInputDriver
    private var isLedEnabled = true

    private val gpioCallback = object : GpioCallback() {
        override fun onGpioEdge(gpio: Gpio): Boolean {
            if (gpio.value) {
                // 人の動きを検知
                Log.i(TAG, "GPIO High")
            } else {
                // 人が動かずに一定時間経過
                Log.i(TAG, "GPIO Low")
            }
            if (isLedEnabled) {
                ledGpio.value = gpio.value
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
        humanSensorGpio = service.openGpio(PIN_NAME__HUMAN_SENSOR)
        // ピンをインプットモードで動作させます
        humanSensorGpio.setDirection(Gpio.DIRECTION_IN)
        // センサーがHighを返した時にtrueを返すようにします
        humanSensorGpio.setActiveType(Gpio.ACTIVE_HIGH)
        // センサーがHigh,Lowどちらかに変更になった場合にコールバック関数を呼び出す
        humanSensorGpio.setEdgeTriggerType(Gpio.EDGE_BOTH)

        ledGpio = service.openGpio(PIN_NAME__LED)
        // ピンをアウトプットモードで動作させます
        ledGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW)

        buttonInputDriver = ButtonInputDriver(PIN_NAME__BUTTON, Button.LogicState.PRESSED_WHEN_LOW, KeyEvent.KEYCODE_SPACE)
    }

    override fun onStart() {
        super.onStart()
        // コールバックを登録
        humanSensorGpio.registerGpioCallback(gpioCallback)
        // ボタンを登録
        buttonInputDriver.register()
    }

    override fun onStop() {
        super.onStop()
        // コールバックを解除
        humanSensorGpio.unregisterGpioCallback(gpioCallback)
        // LEDを消灯する
        ledGpio.value = false
        // ボタンを解除
        buttonInputDriver.unregister()
    }

    override fun onDestroy() {
        super.onDestroy()
        humanSensorGpio.close()
        ledGpio.close()
        buttonInputDriver.close()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_SPACE) {
            isLedEnabled = !isLedEnabled
            if (isLedEnabled) {
                Log.i(TAG, "LED Enabled")
                if (humanSensorGpio.value) {
                    // LEDを点灯する
                    ledGpio.value = true
                }
            } else {
                Log.i(TAG, "LED Disabled")
                // LEDを消灯する
                ledGpio.value = false
            }
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

}
