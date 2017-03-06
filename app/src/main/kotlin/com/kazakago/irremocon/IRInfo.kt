package com.kazakago.irremocon

/**
 * IRオブジェクト
 *
 * @param nanoTime 前の信号との間隔(nano)
 * @param gpioActive GPIOのActive値
 * Created by tamura_k on 2017/02/24.
 */
data class IRInfo(val nanoTime: Long, val gpioActive: Int)