import 'package:flutter/material.dart';
import 'package:wifinetworkspecifierconnect/wifinetworkspecifierconnect.dart';

void main() => runApp(MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String ssid='', password='', state = 'This will show state';

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Column(
          children: <Widget>[
            TextFormField(
              onChanged: (s) {
                if (s.isNotEmpty) ssid = s;
              },
              decoration: InputDecoration(hintText: "SSID"),
            ),
            TextFormField(
              onChanged: (s) {
                if (s.isNotEmpty) password = s;
              },
              decoration: InputDecoration(hintText: "PASSWORD"),
            ),
            RaisedButton(
              onPressed: () async {
                if (ssid.isNotEmpty && password.isNotEmpty) {
                  String res = await Wifinetworkspecifierconnect.connect(ssid,password);
                  print(res);
                  state = res;
                } else {
                  state = 'Please Enter Both Feilds';
                }
                if (this.mounted) setState(() {});
              },
              child: Text("Connect"),
            ),
            Text(state)
          ],
        ),
      ),
    );
  }
}
