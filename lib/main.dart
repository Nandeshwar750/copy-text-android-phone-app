import 'package:flutter/material.dart';
import 'package:permission_handler/permission_handler.dart';
import 'package:flutter/services.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Text Reader',
      theme: ThemeData(
        primarySwatch: Colors.blue,
      ),
      home: MyHomePage(),
    );
  }
}

class MyHomePage extends StatefulWidget {
  @override
  _MyHomePageState createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  static const platform = MethodChannel('com.example.txt_reader_flut/text');

  @override
  void initState() {
    super.initState();
    requestPermissions();
  }

  void requestPermissions() async {
    var status = await Permission.storage.status;
    if (!status.isGranted) {
      await Permission.storage.request();
    }
  }

  Future<void> _startReading() async {
    try {
      await platform.invokeMethod('startReading');
    } on PlatformException catch (e) {
      print("Failed to start reading: '${e.message}'.");
    }
  }

  Future<void> _stopReading() async {
    try {
      await platform.invokeMethod('stopReading');
    } on PlatformException catch (e) {
      print("Failed to stop reading: '${e.message}'.");
    }
  }

  Future<void> _showNotification() async {
    try {
      await platform.invokeMethod('showNotification');
    } on PlatformException catch (e) {
      print("Failed to show notification: '${e.message}'.");
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('Text Reader'),
      ),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: <Widget>[
            ElevatedButton(
              onPressed: _startReading,
              child: Text('Start Reading'),
            ),
            ElevatedButton(
              onPressed: _stopReading,
              child: Text('Stop Reading'),
            ),
            ElevatedButton(
              onPressed: _showNotification,
              child: Text('Show Notification'),
            ),
          ],
        ),
      ),
    );
  }
}
