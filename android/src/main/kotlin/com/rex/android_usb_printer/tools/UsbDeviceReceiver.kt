package com.rex.android_usb_printer.tools

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager

/**
 * @Description:    监听USB插拔、USB设备授权
 * @Author:         liyufeng
 * @CreateDate:     2022/2/18 10:42 上午
 */

class UsbDeviceReceiver : BroadcastReceiver() {

    object Config {
        const val ACTION_USB_PERMISSION = "com.usb.printer.USB_PERMISSION"
    }

    private var usbListener: OnUsbListener? = null

    override fun onReceive(context: Context, intent: Intent) {
        when {
            Config.ACTION_USB_PERMISSION == intent.action -> {
                synchronized(this) {
                    val usbDevice = intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)
                    usbDevice?.let { device ->
                        usbListener?.onDeviceGranted(
                            device,
                            intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)
                        )
                    }
                }
            }
            UsbManager.ACTION_USB_DEVICE_DETACHED == intent.action -> {
                val usbDevice = intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)
                usbListener?.onDeviceDetached(usbDevice)
            }
            UsbManager.ACTION_USB_DEVICE_ATTACHED == intent.action -> {
                val usbDevice = intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)
                usbListener?.onDeviceAttached(usbDevice)
            }
        }
    }

    fun setUsbListener(listener: OnUsbListener) {
        this.usbListener = listener
    }

    /**
     * 注册广播
     */
    fun registerUsbReceiver(context: Context) {
        val filter = IntentFilter(Config.ACTION_USB_PERMISSION)
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        context.registerReceiver(this, filter)
    }

    /**
     * 取消注册
     */
    fun unRegisterUsbReceiver(context: Context) {
        context.unregisterReceiver(this)
    }
}

interface OnUsbListener {
    fun onDeviceAttached(usbDevice: UsbDevice?) //usb插入
    fun onDeviceDetached(usbDevice: UsbDevice?) //usb拔出
    fun onDeviceGranted(usbDevice: UsbDevice, success: Boolean) //usb设备授权
}