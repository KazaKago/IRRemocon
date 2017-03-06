package com.kazakago.irremocon

import com.google.android.things.pio.Gpio
import com.google.android.things.pio.PeripheralManagerService
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

/**
 * 赤外線リモコンセンサー送信クラス
 *
 * @param pinName ピン名
 * @param direction GPIOモード。アウトプットモードを指定して下さい。
 *
 * Created by tamura_k on 2017/02/24.
 */
class IRSender(pinName: String, direction: Int = Gpio.DIRECTION_OUT_INITIALLY_LOW) {

    private val senderGpio: Gpio

    init {
        val service = PeripheralManagerService()
        senderGpio = service.openGpio(pinName)
        // ピンをアウトプットモードで動作させます
        senderGpio.setDirection(direction)
    }

    /**
     * 赤外線情報を送信する
     */
    fun sendIRInfoList(irInfoList: List<IRInfo>) {
        irInfoList.forEach {
            TimeUnit.NANOSECONDS.sleep(it.nanoTime)
            senderGpio.value = (it.gpioActive == Gpio.ACTIVE_HIGH)
        }
    }

    /**
     * 非同期に赤外線情報を送信する
     */
    fun sendIRInfoListAsync(irInfoList: List<IRInfo>, callback: (() -> Unit)?) {
        thread {
            sendIRInfoList(irInfoList)
            callback?.invoke()
        }
    }

    /**
     * GPIOを閉じる
     */
    fun close() {
        senderGpio.close()
    }

}