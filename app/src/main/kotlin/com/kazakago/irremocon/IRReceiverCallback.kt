package com.kazakago.irremocon

import com.google.android.things.pio.Gpio

/**
 * IRReceiverコールバック
 *
 * Created by tamura_k on 2017/02/24.
 */
interface IRReceiverCallback {

    /**
     * 赤外線受信時
     */
    fun onReceiveIR(irInfoList: List<IRInfo>)

    /**
     * GPIOエラー受信時
     */
    fun onGpioError(gpio: Gpio?, error: Int)
}