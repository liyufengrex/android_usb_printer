package com.rex.android_usb_printer.tools

import android.content.Context
import android.hardware.usb.UsbDevice
import io.flutter.plugin.common.EventChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * @Description:    将消息发送给 flutter 端
 * @Author:         liyufeng
 * @CreateDate:     2022/3/22 2:35 下午
 */

abstract class MessageSender {

    companion object {
        lateinit var applicationContext: Context
        var eventSink: EventChannel.EventSink? = null

        private fun doOnMain(pFunc: () -> Unit) {
            GlobalScope.launch {
                withContext(Dispatchers.Main) {
                    pFunc.invoke()
                }
            }
        }

        private fun send(eventType: String, eventData: HashMap<String, Any?>) {
            doOnMain {
                eventSink?.let {
                    val resultData = HashMap<String, Any>()
                    resultData["eventType"] = eventType
                    resultData["eventData"] = eventData
                    it.success(resultData)
                }
            }
        }

        /**
         * 发送Usb设备插拔状态
         * plug: 0 - 拔出， 1 - 插入， 2 - 授权成功
         */
        fun sendUsbPlugStatus(usbDevice: UsbDevice?, plug: Int) {
            val device = HashMap<String, Any?>()
            device["productName"] = usbDevice?.productName
            device["pId"] = usbDevice?.productId
            device["vId"] = usbDevice?.vendorId
            try {
                device["sId"] = usbDevice?.serialNumber
            } catch (e: Exception) {
                device["sId"] = ""
            }
            val result = HashMap<String, Any?>()
            result["plug"] = plug
            result["device"] = device
            send("eventTypeUsb", result)
        }

    }

}