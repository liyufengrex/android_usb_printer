import 'package:android_usb_printer/android_usb_printer.dart';

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

extension ExUsbPlugEnum on UsbPlugInfoEnum {
  int get value {
    switch (this) {
      case UsbPlugInfoEnum.attached:
        return 1;
      case UsbPlugInfoEnum.detached:
        return 0;
      case UsbPlugInfoEnum.granted:
        return 2;
    }
  }
}
