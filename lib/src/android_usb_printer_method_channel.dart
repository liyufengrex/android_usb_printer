import 'dart:async';

import 'package:android_usb_printer/android_usb_printer.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'android_usb_printer_platform_interface.dart';
import 'model/usb_device_info.dart';

/// An implementation of [AndroidUsbPrinterPlatform] that uses method channels.
class MethodChannelAndroidUsbPrinter extends AndroidUsbPrinterPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel =
  const MethodChannel('android_usb_printer_method_channel');
  @visibleForTesting
  final eventChannel = const EventChannel('android_usb_printer_event_channel');

  @override
  Stream<UsbPlugInfo> get usbPlugInfoStream {
    final stream = eventChannel.receiveBroadcastStream();
    final transFormer =
    StreamTransformer<dynamic, UsbPlugInfo>.fromHandlers(
      handleData: (value, sink) {
        if (value['eventType'] == 'eventTypeUsb') {
          final data = value['eventData'];
          if (data != null) {
            final plug = data['plug'];
            final device = data['device'];
            final productName = device['productName'];
            final vId = device['vId'];
            final pId = device['pId'];
            final sId = device['sId'];
            final usbDevice = UsbDeviceInfo(
              productName: productName,
              vId: vId,
              pId: pId,
              sId: sId ?? '',
            );
            final usbPlugInfo = UsbPlugInfo(
              usbDevice,
              plug == UsbPlugInfoEnum.attached.value
                  ? UsbPlugInfoEnum.attached
                  : (plug == UsbPlugInfoEnum.detached.value
                  ? UsbPlugInfoEnum.detached
                  : UsbPlugInfoEnum.granted),
            );
            sink.add(usbPlugInfo);
          }
        }
      },
    );
    return stream.transform<UsbPlugInfo>(transFormer);
  }

  @override
  Future<List<UsbDeviceInfo>> queryLocalUsbDevice() {
    return methodChannel
        .invokeMethod<List<dynamic>>('queryLocalUsbDevice')
        .then((value) {
      if (value == null) {
        return [];
      } else {
        return value.map((e) {
          final productName = e['productName'];
          final vId = e['vId'];
          final pId = e['pId'];
          final sId = e['sId'];
          final position = e['position'];
          return UsbDeviceInfo(
            productName: productName,
            vId: vId,
            pId: pId,
            sId: sId,
            position: position,
          );
        }).toList();
      }
    });
  }

  @override
  Future<int> writeBytes(UsbDeviceInfo usbDeviceInfo, List<int> bytes, {int singleLimit = -1}) {
    final params = generateDeviceMap(usbDeviceInfo);
    params['bytes'] = Uint8List.fromList(bytes);
    params['singleLimit'] = singleLimit;
    return methodChannel
        .invokeMethod(
      'writeBytes',
      params,
    )
        .then((value) => value as int);
  }

  @override
  Future<bool> checkDeviceConn(UsbDeviceInfo usbDeviceInfo) {
    final params = generateDeviceMap(usbDeviceInfo);
    return methodChannel
        .invokeMethod(
      'checkDeviceConn',
      params,
    )
        .then((value) => value as bool);
  }

  @override
  Future<bool> connect(UsbDeviceInfo usbDeviceInfo) {
    final params = generateDeviceMap(usbDeviceInfo);
    return methodChannel
        .invokeMethod(
      'connect',
      params,
    )
        .then((value) => value as bool);
  }

  @override
  Future<bool> disconnect(UsbDeviceInfo usbDeviceInfo) {
    final params = generateDeviceMap(usbDeviceInfo);
    return methodChannel
        .invokeMethod(
      'disconnect',
      params,
    )
        .then((value) => value as bool);
  }

  @override
  Future<bool> checkDevicePermission(UsbDeviceInfo usbDeviceInfo) {
    final params = generateDeviceMap(usbDeviceInfo);
    return methodChannel
        .invokeMethod(
      'checkDevicePermission',
      params,
    )
        .then((value) => value as bool);
  }

  @override
  Future<bool> requestDevicePermission(UsbDeviceInfo usbDeviceInfo) {
    final params = generateDeviceMap(usbDeviceInfo);
    return methodChannel
        .invokeMethod(
      'requestDevicePermission',
      params,
    )
        .then((value) => value as bool);
  }


  @override
  Future<dynamic> removeUsbConnCache(UsbDeviceInfo usbDeviceInfo) async {
    final params = generateDeviceMap(usbDeviceInfo);
    return methodChannel
        .invokeMethod(
      'requestDevicePermission',
      params,
    );
  }

  Map<String, Object> generateDeviceMap(UsbDeviceInfo usbDevice,) {
    var params = <String, Object>{};
    params['vId'] = usbDevice.vId;
    params['pId'] = usbDevice.pId;
    params['sId'] = usbDevice.sId;
    params['position'] = usbDevice.position;
    return params;
  }
}
