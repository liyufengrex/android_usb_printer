package com.rex.android_usb_printer

import android.hardware.usb.UsbDevice
import com.rex.android_usb_printer.tools.MessageSender
import com.rex.android_usb_printer.tools.MethodCallParser
import com.rex.android_usb_printer.tools.OnUsbListener
import com.rex.android_usb_printer.tools.UsbDeviceHelper
import io.UsbConn
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** AndroidUsbPrinterPlugin */
class AndroidUsbPrinterPlugin : FlutterPlugin, MethodCallHandler, EventChannel.StreamHandler {
    private lateinit var binaryMessenger: BinaryMessenger
    private lateinit var channel: MethodChannel
    private lateinit var eventChannel: EventChannel

    private lateinit var usbConnCache: HashMap<String, UsbConn>

    private val usbBroadListener = object : OnUsbListener {
        override fun onDeviceAttached(usbDevice: UsbDevice?) {
            //Usb设备插入
            usbDevice?.let {
                UsbDeviceHelper.instance.filterPrintUsbDevice(usbDevice).let { isPrinter ->
                    if (isPrinter) {
                        UsbDeviceHelper.instance.checkPermission(usbDevice)?.let { hasPermission ->
                            if (hasPermission) {
                                MessageSender.sendUsbPlugStatus(usbDevice, 1)
                            }
                        }
                    }
                }
            }
        }

        override fun onDeviceDetached(usbDevice: UsbDevice?) {
            //Usb设备拔出
            usbDevice?.let {
                UsbDeviceHelper.instance.filterPrintUsbDevice(usbDevice).let { isPrinter ->
                    if (isPrinter) {
                        val deviceId = "${it.vendorId} - ${it.productId} - "
                        removeConnCacheWithKey(deviceId)
                        MessageSender.sendUsbPlugStatus(usbDevice, 0)
                    }
                }
            }
        }

        override fun onDeviceGranted(usbDevice: UsbDevice, success: Boolean) {
            //Usb设备授权
            if (success) {
                UsbDeviceHelper.instance.filterPrintUsbDevice(usbDevice).let { isPrinter ->
                    if (isPrinter) {
                        MessageSender.sendUsbPlugStatus(usbDevice, 2)
                    }
                }
            }
        }
    }

    private fun onUsbBroadListen() {
        UsbDeviceHelper.instance.setUsbListener(usbBroadListener)
        UsbDeviceHelper.instance.registerUsbReceiver(MessageSender.applicationContext)
    }

    override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        MessageSender.applicationContext = flutterPluginBinding.applicationContext
        this.binaryMessenger = flutterPluginBinding.binaryMessenger
        channel = MethodChannel(
            binaryMessenger,
            "android_usb_printer_method_channel"
        )
        eventChannel =
            EventChannel(binaryMessenger, "android_usb_printer_event_channel")
        channel.setMethodCallHandler(this)
        eventChannel.setStreamHandler(this)

        usbConnCache = HashMap()
        UsbDeviceHelper.instance.init(flutterPluginBinding.applicationContext)
        onUsbBroadListen()
    }

    override fun onMethodCall(call: MethodCall, result: Result) {
        when (call.method) {
            "queryLocalUsbDevice" -> {
                result.success(UsbDeviceHelper.instance.queryLocalPrinterMap())
            }

            "writeBytes" -> {
                val usbConn = fetchUsbConn(call);
                if (usbConn != null) {
                    val data = call.argument<ByteArray>("bytes")
                    var singleLimit = call.argument<Int>("singleLimit")
                    if (singleLimit == null) {
                        singleLimit = -1
                    }
                    if (data != null) {
                        Thread {
                            try {
                                val count = usbConn.writeDataImmediately(data, singleLimit)
                                GlobalScope.launch {
                                    withContext(Dispatchers.Main) {
                                        result.success(count)
                                    }
                                }
                            } catch (e: Exception) {
                                val error = e.message ?: ""
                                GlobalScope.launch {
                                    withContext(Dispatchers.Main) {
                                        result.error("-1", error, error)
                                    }
                                }
                            }
                        }.start()
                    } else {
                        result.success(-1)
                    }
                } else {
                    val error = "usb 设备无法匹配"
                    result.error("-1", error, error)
                }
            }

            "readBytes" -> {
                val usbConn = fetchUsbConn(call);
                if (usbConn != null) {
                    val timeOut = call.argument<Int>("timeOut")
                    Thread {
                        try {
                            val data = usbConn.readBytes(timeOut!!)
                            GlobalScope.launch {
                                withContext(Dispatchers.Main) {
                                    result.success(data)
                                }
                            }
                        } catch (e: Exception) {
                            val error = e.message ?: ""
                            GlobalScope.launch {
                                withContext(Dispatchers.Main) {
                                    result.error("-1", error, error)
                                }
                            }
                        }
                    }.start()
                } else {
                    val error = "usb 设备无法匹配"
                    result.error("-1", error, error)
                }
            }

            "checkDeviceConn" -> {
                val device = MethodCallParser.parseDevice(call)
                if (device != null) {
                    val usbDevice = device.usbDevice
                    val deviceId = device.deviceId
                    if (!usbConnCache.contains(deviceId)) {
                        usbConnCache[deviceId] = UsbConn(usbDevice)
                    }
                    result.success(usbConnCache[deviceId]!!.isConn)
                } else {
                    val error = "usb 设备无法匹配"
                    result.error("-1", error, error)
                }
            }

            "connect" -> {
                val device = MethodCallParser.parseDevice(call)
                if (device != null) {
                    val usbDevice = device.usbDevice
                    val deviceId = device.deviceId
                    if (!usbConnCache.contains(deviceId)) {
                        usbConnCache[deviceId] = UsbConn(usbDevice)
                    }
                    try {
                        var connected = usbConnCache[deviceId]!!.connect()
                        result.success(connected)
                    } catch (e: Exception) {
                        val error = e.message ?: ""
                        result.error("-1", error, error)
                    }
                } else {
                    val error = "usb 设备无法匹配"
                    result.error("-1", error, error)
                }
            }

            "disconnect" -> {
                val deviceId = MethodCallParser.parseDeviceId(call)
                if (usbConnCache.contains(deviceId)) {
                    usbConnCache[deviceId]!!.disconnect()
                    usbConnCache.remove(deviceId)
                    result.success(true)
                } else {
                    val error = "usb 设备无法匹配"
                    result.error("-1", error, error)
                }
            }

            "checkDevicePermission" -> {
                val device = MethodCallParser.parseDevice(call)
                if (device != null) {
                    result.success(UsbDeviceHelper.instance.hasPermission(device.usbDevice))
                } else {
                    val error = "usb 设备无法匹配"
                    result.error("-1", error, error)
                }
            }

            "requestDevicePermission" -> {
                val device = MethodCallParser.parseDevice(call)
                if (device != null) {
                    UsbDeviceHelper.instance.requestPermission(device.usbDevice)
                    result.success(true)
                } else {
                    val error = "usb 设备无法匹配"
                    result.error("-1", error, error)
                }
            }

            "removeUsbConnCache" -> {
                val deviceId = MethodCallParser.parseDeviceId(call)
                removeConnCacheWithKey(deviceId)
                result.success(true)
            }
        }
    }

    private fun fetchUsbConn(call: MethodCall): UsbConn? {
        val deviceId = MethodCallParser.parseDeviceId(call)
        if (!usbConnCache.contains(deviceId)) {
            val device = MethodCallParser.parseDevice(call)
            if (device != null) {
                usbConnCache[deviceId] = UsbConn(device.usbDevice)
            }
        }
        return usbConnCache[deviceId]
    }

    private fun removeConnCacheWithKey(key: String) {
        val removeCaches = arrayListOf<String>()
        usbConnCache.keys.forEach {
            if (it.contains(key)) {
                removeCaches.add(it)
            }
        }
        if (removeCaches.isNotEmpty()) {
            removeCaches.forEach {
                usbConnCache.remove(it)
            }
        }
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
        eventChannel.setStreamHandler(null)
        UsbDeviceHelper.instance.unRegisterUsbReceiver(binding.applicationContext)
    }

    override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
        MessageSender.eventSink = events
    }

    override fun onCancel(arguments: Any?) {
        //暂无处理
    }
}
