# flutter plugin
# android_usb_printer

flutter android 端插件，提供操作 usb 打印机方法能力

### 使用方法

```dart
//添加依赖
android_usb_printer: ^0.0.1
```
```dart
// 发送打印字节数组示例
AndroidUsbPrinterPlatform.instance.writeBytes(
      usbDeviceInfo,
      bytes,
    )
```
+ ### 提供API方法
```dart
  /// 获取本地打印机列表
  Future<List<UsbDeviceInfo>> queryLocalUsbDevice()

  /// 打印机发送字节数据
  /// @param singleLimit, 单次最大传输字节长度，内部会将 bytes 按照 singleLimit 阈值进行切割分组后依次 write
  Future<int> writeBytes(UsbDeviceInfo usbDeviceInfo, List<int> bytes, {int singleLimit = -1})

  /// 接收打印机字节数据
  /// @param timeOut, timeOut毫秒内无接收数据直接返回null
  Future<Uint8List?> readBytes(UsbDeviceInfo usbDeviceInfo, {int timeOut = 2000})

  /// 获取打印机连接状态
  Future<bool> checkDeviceConn(UsbDeviceInfo usbDeviceInfo)

  /// 打印机连接
  Future<bool> connect(UsbDeviceInfo usbDeviceInfo)

  /// 打印机断开连接
  Future<bool> disconnect(UsbDeviceInfo usbDeviceInfo)

  /// 获取打印机设备权限
  Future<bool> checkDevicePermission(UsbDeviceInfo usbDeviceInfo)

  /// 申请打印机设备权限
  Future<bool> requestDevicePermission(UsbDeviceInfo usbDeviceInfo)

  /// 删除原生层中 usb conn 缓存（删除设备时可调用）
  Future<dynamic> removeUsbConnCache(UsbDeviceInfo usbDeviceInfo)
```
+ ### 提供监听 USB 插拔，USB 设备授权能力
```dart
/// 监听 USB 插拔，USB 设备授权
AndroidUsbPrinterPlatform.instance.usbPlugInfoStream.listen((usbPlugInfo) {
      final usbDeviceInfo = usbPlugInfo.usbDevice; //插入的usb设备信息，（vId, pId, sId）
      final plug = usbPlugInfo.plug; //消息类型枚举（包含状态：拔出、插入、授权成功）
})
```
UsbPlugInfo 结构如下：
```dart
class UsbPlugInfo {
  final UsbPlugInfoEnum plug;
  final UsbDeviceInfo usbDevice;

  UsbPlugInfo(this.usbDevice, this.plug);
}

enum UsbPlugInfoEnum {
  attached, //插入
  detached, //拔出
  granted, //授权成功
}
```



