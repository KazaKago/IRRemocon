package com.kazakago.irremocon

import com.google.android.things.pio.Gpio
import com.google.android.things.pio.GpioCallback
import com.google.android.things.pio.PeripheralManagerService
import java.io.IOException
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * 赤外線リモコンセンサー受信クラス
 *
 * @param pinName GPIOピン名
 * @param scanActiveTrigger スキャン開始のトリガーとなる値
 * @param breakUpTime 受信が完了したとみなす間隔(Mill)
 *
 * Created by tamura_k on 2017/02/24.
 */
class IRReceiver(pinName: String,
                 var scanActiveTrigger: Int = Gpio.ACTIVE_LOW,
                 var breakUpTime: Long = TimeUnit.SECONDS.toMillis(1)) {

    /**
     * スキャン結果リストのリスト
     */
    val irInfoListList = ArrayList<ArrayList<IRInfo>>()

    private var receiverGpio: Gpio
    private var isScanning: Boolean = false
    private val irInfoList = ArrayList<IRInfo>()
    private var receiverCallback: IRReceiverCallback? = null
    private val breakUpScheduler = Executors.newSingleThreadScheduledExecutor()
    private val breakUpTask = {
        //スキャンを停止
        stopScan()
    }

    private val gpioCallback = object : GpioCallback() {
        override fun onGpioEdge(gpio: Gpio): Boolean {
            val gpioActive = if (gpio.value) Gpio.ACTIVE_HIGH else Gpio.ACTIVE_LOW
            if (scanActiveTrigger == gpioActive) {
                onReceiveScanStartIR()
            }
            onReceiveIR(gpioActive = gpioActive)
            return true
        }

        override fun onGpioError(gpio: Gpio?, error: Int) {
            receiverCallback?.onGpioError(gpio = gpio, error = error)
        }
    }

    init {
        val service = PeripheralManagerService()
        receiverGpio = service.openGpio(pinName)
        // ピンをインプットモードで動作させます
        receiverGpio.setDirection(Gpio.DIRECTION_IN)
        // センサーがHighを返した時にtrueを返すようにします
        receiverGpio.setActiveType(Gpio.ACTIVE_HIGH)
        // センサーがHigh,Lowどちらかに変更になった場合にコールバック関数を呼び出す
        receiverGpio.setEdgeTriggerType(Gpio.EDGE_BOTH)
        breakUpScheduler.schedule({

        }, breakUpTime, TimeUnit.MILLISECONDS)
    }

    /**
     * GPIOを購読する
     */
    @Throws(IOException::class)
    fun registerCallback(callback: IRReceiverCallback) {
        receiverCallback = callback
        receiverGpio.registerGpioCallback(gpioCallback)
    }

    /**
     * GPIOの購読を解除する
     */
    fun unregisterCallback() {
        receiverCallback = null
        receiverGpio.unregisterGpioCallback(gpioCallback)
    }

    /**
     * GPIOを閉じる
     */
    fun close() {
        receiverGpio.close()
    }

    /**
     * スキャン開始のトリガーとなる赤外線情報を受信した時
     */
    private fun onReceiveScanStartIR() {
        if (!isScanning) {
            //スキャン中でなければスキャンを開始する
            startScan()
        }
    }

    /**
     * 赤外線情報受信した時
     */
    private fun onReceiveIR(gpioActive: Int) {
        if (isScanning) {
            //スキャン中なら前回との差を取得して値を書き込む
            val timeDifference = getTimeDifference()
            addIRInfo(gpioActive = gpioActive, timeDifference = timeDifference)
            //過去にタイマーが動いていれば停止して新しく作成
            breakUpScheduler.shutdownNow()
            breakUpScheduler.schedule(breakUpTask, breakUpTime, TimeUnit.MILLISECONDS)
        }
    }

    /**
     * スキャンを開始する
     */
    private fun startScan() {
        if (!isScanning) {
            irInfoList.clear()
            isScanning = true
        }
    }

    /**
     * スキャンを停止する
     */
    private fun stopScan() {
        if (isScanning) {
            isScanning = false
            irInfoListList.add(irInfoList)
            receiverCallback?.onReceiveIR(irInfoList = irInfoList)
            irInfoList.clear()
        }
    }

    /**
     * 赤外線情報をリストに追加する
     */
    private fun addIRInfo(gpioActive: Int, timeDifference: Long) {
        irInfoList.add(IRInfo(nanoTime = timeDifference, gpioActive = gpioActive))
    }

    /**
     * 赤外線情報リストの最後の時間と現在の時間との差を取得する
     */
    private fun getTimeDifference(): Long {
        var timeDifference: Long = 0
        irInfoList.lastOrNull()?.nanoTime?.let {
            //リストの最後の時間との差を取得する
            timeDifference = System.nanoTime() - it
        }
        return timeDifference
    }

}