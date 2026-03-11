import 'dart:async';
import 'package:flutter/services.dart';

class SoftPosBridge {
  static const MethodChannel _method = MethodChannel('softpos/methods');
  static const EventChannel _events = EventChannel('softpos/events');

  Stream<Map<String, dynamic>> events() {
    return _events.receiveBroadcastStream().map((event) {
      return Map<String, dynamic>.from(event as Map);
    });
  }

  Future<void> setupSdk() async {
    await _method.invokeMethod('setupSdk');
  }

  Future<String> initializeSdk() async {
    final result = await _method.invokeMethod('initializeSdk');
    return result.toString();
  }

  Future<void> registerSdk() async {
    await _method.invokeMethod('registerSdk');
  }

  Future<void> checkPosService() async {
    await _method.invokeMethod('checkPosService');
  }

  Future<void> startSale({
    required int amountMinor,
    int timeoutMs = 20000,
  }) async {
    await _method.invokeMethod('startSale', {
      'amount': amountMinor,
      'timeoutMs': timeoutMs,
    });
  }

  Future<bool> cancelTransaction() async {
    final result = await _method.invokeMethod('cancelTransaction');
    return result == true;
  }
}