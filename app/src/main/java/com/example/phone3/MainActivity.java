package com.example.phone3;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {

    private ServerSocket serverSocket;
    private TextView textViewStatus, textViewReceived;
    private Handler handler;
    private Thread serverThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textViewStatus = findViewById(R.id.textViewStatus);
        textViewReceived = findViewById(R.id.textViewReceived);
        handler = new Handler(Looper.getMainLooper());

        // 启动服务器线程
        serverThread = new Thread(new ServerThread());
        serverThread.start();
    }

    private class ServerThread implements Runnable {
        @Override
        public void run() {
            try {
                // 在8080端口监听
                serverSocket = new ServerSocket(8080);

                // 更新UI显示服务器已启动
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        textViewStatus.setText("服务器已启动，等待连接...\nIP: " + getLocalIpAddress() + "\n端口: 8080");
                    }
                });

                while (!Thread.currentThread().isInterrupted()) {
                    // 等待客户端连接
                    Socket client = serverSocket.accept();

                    // 更新UI显示客户端已连接
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            textViewStatus.setText("客户端已连接: " + client.getInetAddress().getHostAddress());
                        }
                    });

                    // 处理客户端消息
                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(client.getInputStream()));

                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        final String message = inputLine;

                        // 在主线程显示Toast和更新UI
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this,
                                        "收到数字: " + message,
                                        Toast.LENGTH_SHORT).show();
                                textViewReceived.setText("最新收到: " + message);
                            }
                        });
                    }

                    client.close();

                    // 更新UI显示客户端已断开
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            textViewStatus.setText("客户端已断开，等待新连接...");
                        }
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // 获取设备IP地址
    private String getLocalIpAddress() {
        try {
            java.util.Enumeration<java.net.NetworkInterface> en = java.net.NetworkInterface.getNetworkInterfaces();
            while (en.hasMoreElements()) {
                java.net.NetworkInterface intf = en.nextElement();
                java.util.Enumeration<java.net.InetAddress> enumIpAddr = intf.getInetAddresses();
                while (enumIpAddr.hasMoreElements()) {
                    java.net.InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof java.net.Inet4Address) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "无法获取IP";
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}