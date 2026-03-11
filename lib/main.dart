import 'dart:async';
import 'package:flutter/material.dart';
import 'softpos_bridge.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'SoftPOS Demo',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(useMaterial3: true),
      home: const SoftPosDemoPage(),
    );
  }
}

class SoftPosDemoPage extends StatefulWidget {
  const SoftPosDemoPage({super.key});

  @override
  State<SoftPosDemoPage> createState() => _SoftPosDemoPageState();
}

class _SoftPosDemoPageState extends State<SoftPosDemoPage> {
  final SoftPosBridge _softPos = SoftPosBridge();
  final List<String> _logs = [];
  StreamSubscription<Map<String, dynamic>>? _subscription;

  @override
  void initState() {
    super.initState();

    _subscription = _softPos.events().listen((event) {
      _addLog('EVENT => ${event['event']} | ${event['data']}');
    }, onError: (e) {
      _addLog('EVENT ERROR => $e');
    });
  }

  @override
  void dispose() {
    _subscription?.cancel();
    super.dispose();
  }

  void _addLog(String text) {
    setState(() {
      _logs.insert(0, '${DateTime.now().toIso8601String()}  $text');
    });
  }

  Future<void> _run(String label, Future<void> Function() action) async {
    try {
      _addLog('START => $label');
      await action();
      _addLog('DONE => $label');
    } catch (e) {
      _addLog('ERROR => $label => $e');
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('SoftPOS Flutter Demo'),
      ),
      body: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          children: [
            Wrap(
              spacing: 12,
              runSpacing: 12,
              children: [
                ElevatedButton(
                  onPressed: () => _run('setupSdk', () async {
                    await _softPos.setupSdk();
                  }),
                  child: const Text('1. Setup SDK'),
                ),
                ElevatedButton(
                  onPressed: () => _run('initializeSdk', () async {
                    final status = await _softPos.initializeSdk();
                    _addLog('initializeSdk => $status');
                  }),
                  child: const Text('2. Initialize'),
                ),
                ElevatedButton(
                  onPressed: () => _run('registerSdk', () async {
                    await _softPos.registerSdk();
                  }),
                  child: const Text('3. Register'),
                ),
                ElevatedButton(
                  onPressed: () => _run('checkPosService', () async {
                    await _softPos.checkPosService();
                  }),
                  child: const Text('4. Check POS'),
                ),
                ElevatedButton(
                  onPressed: () => _run('startSale', () async {
                    await _softPos.startSale(amountMinor: 100);
                  }),
                  child: const Text('5. Start Sale'),
                ),
                ElevatedButton(
                  onPressed: () => _run('cancelTransaction', () async {
                    final ok = await _softPos.cancelTransaction();
                    _addLog('cancelTransaction => $ok');
                  }),
                  child: const Text('Cancel'),
                ),
              ],
            ),
            const SizedBox(height: 16),
            const Align(
              alignment: Alignment.centerLeft,
              child: Text(
                'Logs',
                style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
              ),
            ),
            const SizedBox(height: 8),
            Expanded(
              child: Container(
                width: double.infinity,
                padding: const EdgeInsets.all(12),
                decoration: BoxDecoration(
                  border: Border.all(color: Colors.grey.shade400),
                  borderRadius: BorderRadius.circular(12),
                ),
                child: ListView.builder(
                  itemCount: _logs.length,
                  itemBuilder: (context, index) {
                    return Padding(
                      padding: const EdgeInsets.only(bottom: 8),
                      child: Text(_logs[index]),
                    );
                  },
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}