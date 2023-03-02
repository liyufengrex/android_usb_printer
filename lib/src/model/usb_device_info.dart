/// usb 设备信息
class UsbDeviceInfo {
  final String productName;
  final int vId;
  final int pId;
  final String sId;
  final int position;

  UsbDeviceInfo({
    required this.productName,
    required this.vId,
    required this.pId,
    required this.sId,
    this.position = 0,
  });

  factory UsbDeviceInfo.fromMap(Map<String, dynamic> map) {
    return UsbDeviceInfo(
      productName: map["productName"],
      vId: map["vId"],
      pId: map["pId"],
      sId: map["sId"],
      position: map.containsKey('position') ? map['position'] : 0,
    );
  }

  Map<String, dynamic> toMap() {
    return <String, dynamic>{
      "productName": productName,
      "vId": vId,
      "pId": pId,
      "sId": sId,
      "position": position,
    };
  }

  String get id => "$vId-$pId-$sId";
}
