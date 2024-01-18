package com.rex.android_usb_printer.tools

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.hardware.usb.*
import io.flutter.Log

/**
 * @Description:    usb设备工具
 * @Author:         liyufeng
 * @CreateDate:     2022/3/18 10:36 上午
 */

class UsbDeviceHelper private constructor() {

    private lateinit var mContext: Context
    private val usbDeviceReceiver: UsbDeviceReceiver = UsbDeviceReceiver()
    private lateinit var mPermissionIntent: PendingIntent
    private lateinit var usbManager: UsbManager

    companion object {
        val instance by lazy(LazyThreadSafetyMode.NONE) {
            UsbDeviceHelper()
        }
    }

    fun init(context: Context) {
        this.mContext = context
        mPermissionIntent = PendingIntent.getBroadcast(
            context,
            0,
            Intent(UsbDeviceReceiver.Config.ACTION_USB_PERMISSION),
            0
        )
        usbManager = context.applicationContext.getSystemService(Context.USB_SERVICE) as UsbManager
    }

    fun setUsbListener(listener: OnUsbListener) {
        usbDeviceReceiver.setUsbListener(listener)
    }

    fun queryLocalPrinterMap(): List<HashMap<String, Any?>> {
        val deviceIdCache = arrayListOf<String>()
        fun matchDeviceIdCache(deviceId: String): Int {
            var matchCount = 0
            for (cacheId in deviceIdCache) {
                if (deviceId == cacheId) {
                    matchCount++
                }
            }
            return matchCount
        }

        val resultData = arrayListOf<HashMap<String, Any?>>()
        val deviceList = queryPrinterDevices()
        for (index in deviceList.indices) {
            val item = deviceList[index]
            checkPermission(item)?.let { hasPermission ->
                if (hasPermission) {
                    val sId = item.serialNumber ?: ""
                    val deviceId = "${item.productId}-${item.vendorId}-${sId}"
                    val itemMap = HashMap<String, Any?>()
                    itemMap["productName"] = item.productName
                    itemMap["pId"] = item.productId
                    itemMap["vId"] = item.vendorId
                    itemMap["sId"] = sId
                    itemMap["position"] = matchDeviceIdCache(deviceId)
                    resultData.add(itemMap)
                    deviceIdCache.add(deviceId)
                }
            }
        }
        return resultData
    }

    /**
     * 获取打印机设备
     */
    private fun queryPrinterDevices(): ArrayList<UsbDevice> {
        val devices = arrayListOf<UsbDevice>()
        val deviceList = usbManager.deviceList
        val deviceIterator: Iterator<UsbDevice> = deviceList.values.iterator()
        while (deviceIterator.hasNext()) {
            val device = deviceIterator.next()
            if (filterPrintUsbDevice(device)) {
                devices.add(device)
            }
        }
        return devices
    }

    //过滤打印机类型的Usb设备
    fun filterPrintUsbDevice(usbDevice: UsbDevice): Boolean {
        var isFit = false
        val count: Int = usbDevice.interfaceCount
        for (index in 0 until count) {
            val usbInterface: UsbInterface = usbDevice.getInterface(index)
            if (usbInterface.interfaceClass == UsbConstants.USB_CLASS_PRINTER) {
                isFit = true
                break
            }
        }
        return isFit
    }

    //根据 vId、pId、sId 匹配 usbDevice
    fun matchUsbDevice(vId: Int, pId: Int, sId: String, position: Int): UsbDevice? {
        var usbDevice: UsbDevice? = null
        val deviceList = queryPrinterDevices()
        val hitDevices = arrayListOf<UsbDevice>()
        var realSID = sId
        var realSIDIndex = position
        val usbList = StringBuilder()
        deviceList.forEach { e ->
            checkPermission(e)?.let { hasPermission ->
                if (hasPermission) {
                    if (e.vendorId == vId && e.productId == pId && e.serialNumber == realSID) {
                        hitDevices.add(e)
                    }
                }
                usbList.append("{vId: ${e.vendorId}}, pId: ${e.productId}, sId: ${if (hasPermission) e.serialNumber else "null"}")
            }
        }
        val params = "vId: $vId - pId: $pId - sId: $sId"
        val log = "params: ($params) - ustList: [$usbList]"
//        Log.i("rex", log)
        if (hitDevices.isNotEmpty()) {
            usbDevice = if (realSIDIndex < hitDevices.size) {
                hitDevices[realSIDIndex]
            } else {
                hitDevices.first()
            }
        }
        return usbDevice
    }

    fun openDevice(usbDevice: UsbDevice): UsbDeviceConnection {
        return usbManager.openDevice(usbDevice)
    }

    fun requestPermission(usbDevice: UsbDevice) {
        usbManager.requestPermission(usbDevice, mPermissionIntent)
    }

    fun hasPermission(usbDevice: UsbDevice): Boolean {
        return usbManager.hasPermission(usbDevice)
    }

    //校验申请usb设备权限
    fun checkPermission(usbDevice: UsbDevice): Boolean? {
        return if (!hasPermission(usbDevice)) {
            requestPermission(usbDevice)
            null
        } else {
            true
        }
    }

    fun registerUsbReceiver(context: Context) {
        usbDeviceReceiver.registerUsbReceiver(context)
    }

    fun unRegisterUsbReceiver(context: Context) {
        usbDeviceReceiver.unRegisterUsbReceiver(context)
    }

}
