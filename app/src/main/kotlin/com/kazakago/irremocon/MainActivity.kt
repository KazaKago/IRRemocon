package com.kazakago.irremocon

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import com.google.android.things.contrib.driver.button.Button
import com.google.android.things.contrib.driver.button.ButtonInputDriver
import com.google.android.things.pio.Gpio
import java.util.*

class MainActivity : Activity() {

    companion object {
        private val TAG = MainActivity::class.java.simpleName
        private val PIN_NAME__IR_RECEIVER = "BCM18"
        private val PIN_NAME__IR_SENDER = "BCM16"
        private val PIN_NAME__IR_SEND_BUTTON = "BCM21"
    }

    private lateinit var irReceiver: IRReceiver
    private val irReceiverCallback = object : IRReceiverCallback {

        override fun onReceiveIR(irInfoList: List<IRInfo>) {
            lastReceivedIrInfoList = irInfoList
            Log.d(TAG, "---- Start IR Receive Log. ---")
            irInfoList.forEachIndexed { i, irInfo ->
                Log.d(TAG, i.toString() + " GPIO " + (if (irInfo.gpioActive == Gpio.ACTIVE_HIGH) "HIGH" else "LOW ") + " " + irInfo.nanoTime.toString())
            }
            Log.d(TAG, "----  End IR Receive Log.  ---")
        }

        override fun onGpioError(gpio: Gpio?, error: Int) {
            Log.d(TAG, "Error : " + error.toString())
        }
    }
    private lateinit var irSender: IRSender
    private lateinit var irSendButtonInputDriver: ButtonInputDriver
    private var lastReceivedIrInfoList: List<IRInfo> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        irReceiver = IRReceiver(pinName = PIN_NAME__IR_RECEIVER)
        irSender = IRSender(pinName = PIN_NAME__IR_SENDER)
        irSendButtonInputDriver = ButtonInputDriver(PIN_NAME__IR_SEND_BUTTON, Button.LogicState.PRESSED_WHEN_LOW, KeyEvent.KEYCODE_SPACE)
    }

    override fun onStart() {
        super.onStart()
        irReceiver.registerCallback(irReceiverCallback)
    }

    override fun onStop() {
        super.onStop()
        irReceiver.unregisterCallback()
    }

    override fun onDestroy() {
        super.onDestroy()
        irReceiver.close()
        irSender.close()
        irSendButtonInputDriver.close()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_SPACE) {
            onKeyDownSpace()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun onKeyDownSpace() {
        sendIRInfo(irInfoList = lastReceivedIrInfoList)
    }

    private fun sendIRInfo(irInfoList: List<IRInfo>) {
        Log.d(TAG, "---- Start Send IR Info.  ----")
        irSender.sendIRInfoListAsync(irInfoList = irInfoList) {
            Log.d(TAG, "---- Finish Send IR Info. ----")
        }
    }

}
