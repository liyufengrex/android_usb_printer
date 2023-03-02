import 'package:android_usb_printer/android_usb_printer.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'android_usb_printer_method_channel.dart';
import 'model/usb_device_info.dart';

abstract class AndroidUsbPrinterPlatform extends PlatformInterface {
  /// Constructs a AndroidUsbPrinterPlatform.
  AndroidUsbPrinterPlatform() : super(token: _token);

  static final Object _token = Object();

  static AndroidUsbPrinterPlatform _instance = MethodChannelAndroidUsbPrinter();

  /// The default instance of [AndroidUsbPrinterPlatform] to use.
  ///
  /// Defaults to [MethodChannelAndroidUsbPrinter].
  static AndroidUsbPrinterPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [AndroidUsbPrinterPlatform] when
  /// they register themselves.
  static set instance(AndroidUsbPrinterPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Stream<UsbPlugInfo> get usbPlugInfoStream;

  /// 获取本地打印机列表
  Future<List<UsbDeviceInfo>> queryLocalUsbDevice() {
    throw UnimplementedError('queryLocalUsbDevice() has not been implemented.');
  }

  /// 打印机发送字节数据
  Future<int> writeBytes(UsbDeviceInfo usbDeviceInfo, List<int> bytes) {
    throw UnimplementedError('writeBytes() has not been implemented.');
  }

  /// 获取打印机连接状态
  Future<bool> checkDeviceConn(UsbDeviceInfo usbDeviceInfo) {
    throw UnimplementedError('checkDeviceConn() has not been implemented.');
  }

  /// 打印机连接
  Future<bool> connect(UsbDeviceInfo usbDeviceInfo) {
    throw UnimplementedError('connect() has not been implemented.');
  }

  /// 打印机断开连接
  Future<bool> disconnect(UsbDeviceInfo usbDeviceInfo) {
    throw UnimplementedError('disconnect() has not been implemented.');
  }

  /// 获取打印机设备权限
  Future<bool> checkDevicePermission(UsbDeviceInfo usbDeviceInfo) {
    throw UnimplementedError(
        'checkUsbDevicePermission() has not been implemented.');
  }

  /// 申请打印机设备权限
  Future<bool> requestDevicePermission(UsbDeviceInfo usbDeviceInfo) {
    throw UnimplementedError(
        'requestUsbPermission() has not been implemented.');
  }

  /// 删除原生层中 usb conn 缓存（删除设备时可调用）
  Future<dynamic> removeUsbConnCache(UsbDeviceInfo usbDeviceInfo) {
    throw UnimplementedError('removeUsbConnCache() has not been implemented.');
  }
}
