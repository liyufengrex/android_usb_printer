package com.rex.android_usb_printer.tools

import android.hardware.usb.UsbDevice
import io.flutter.plugin.common.MethodCall


/// Author       : liyufeng
/// Date         : 14:27
/// Description  : 
object MethodCallParser {

    fun parseDevice(call: MethodCall): ExUsbDevice? {
        val vID = call.argument<Int>("vId")
        val pID = call.argument<Int>("pId")
        val sID = call.argument<String>("sId")
        val position = call.argument<Int>("position")
        var usbDevice: ExUsbDevice? = null
        if (vID != null && pID != null && sID != null && position != null) {
            val matchedDevice = UsbDeviceHelper.instance.matchUsbDevice(
                vId = vID,
                pId = pID,
                sId = sID,
                position = position
            )
            matchedDevice?.let {
                usbDevice = ExUsbDevice(
                    deviceId = "$vID - $pID - $sID - $position",
                    usbDevice = it,
                )
            }
        }
        return usbDevice
    }

}

class ExUsbDevice(
    var deviceId: String,
    var usbDevice: UsbDevice
)