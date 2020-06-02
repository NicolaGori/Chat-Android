package com.example.server;
//@Author = Nicola Gori
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.icu.text.SimpleDateFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Enumeration;

public class MainActivity extends AppCompatActivity {

    private LinearLayout msgList;
    private Handler handler;
    private EditText edMessage;
    private int serverTextColor;
    private ServerThread serverThread;
    private Thread thread;
    private ServerSocket serverSocket;
    public static final int SERVER_PORT = 3003;
    private Socket socket;
    private Socket tempClientSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setTitle("Server");
        serverTextColor = ContextCompat.getColor(this, R.color.black);
        handler = new Handler();
        msgList = findViewById(R.id.msgList);
        edMessage = findViewById(R.id.edMessage);
    }
//========================================================================================================
//========================================================================================================
    @SuppressLint("NewApi")
        public TextView textView(String messaggio, int color){
            if(null == messaggio || messaggio.trim().isEmpty()){
                messaggio = "<Messaggio Vuoto>";
            }
            TextView tv = new TextView(this);
            tv.setTextColor(color);
            tv.setText(messaggio + " [" + getTime() + "}");
            tv.setTextSize(20);
            tv.setPadding(0,5,0,0);
            return tv;
        }
//========================================================================================================
//==============================================SHOWMESSAGE===============================================
//========================================================================================================
    public void showMessage(final String messaggio, final int color) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                msgList.addView(textView(messaggio, color));
            }
        });
    }
//========================================================================================================
//==============================================SENDMESSAGE===============================================
//========================================================================================================
    public  void sendMessage(final String messaggio){
        try {
            if (null != tempClientSocket) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        PrintWriter out = null;
                        try {
                            out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(tempClientSocket.getOutputStream())), true);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        out.println(messaggio);
                    }
                }).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
//========================================================================================================
//================================================ONCLICK=================================================
//========================================================================================================
    public void onClick(View view){
        if (view.getId() == R.id.start_server) {
            msgList.removeAllViews();
            serverThread = new ServerThread();
            thread = new Thread(new ServerThread());//istanzio un server con il server thread
            thread.start();//AVVIO DEL THREAD
            showMessage("Server Started", serverTextColor);
            return;
        }
        if (view.getId() == R.id.send_data) {
            String msg = edMessage.getText().toString().trim();
            showMessage("Server: " + msg, Color.BLUE);
            sendMessage(msg);
        }
    }
//========================================================================================================
//==============================================SERVER THREAD=============================================
//========================================================================================================
class ServerThread implements Runnable{

    @Override
    public void run() {
        Socket socket;
        try{
            serverSocket = new ServerSocket(SERVER_PORT);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    findViewById(R.id.start_server).setVisibility(View.GONE);//permette di togliere il bottone
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    void sendMessage(final String messaggio) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (null != socket) {
                        PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                        out.println(messaggio);
                    }
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
//========================================================================================================
//=============================================getIpAddress===============================================
//========================================================================================================
    private String getIpAddress(){
        String ip = "";
        try{
            Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (enumNetworkInterfaces.hasMoreElements()){
                NetworkInterface networkInterface = enumNetworkInterfaces.nextElement();
                Enumeration<InetAddress> enumInetAddress = networkInterface.getInetAddresses();
                while (enumInetAddress.hasMoreElements()){
                    InetAddress inetAddress = enumInetAddress.nextElement();
                    if (inetAddress.isSiteLocalAddress()){
                        ip += "SiteLocalAddress: " + inetAddress.getHostAddress() + "\n";
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
            ip += "Something Wrong!" + e.toString() + "\n";
        }
        return ip;
    }
//========================================================================================================
//================================================GETTIME=================================================
//========================================================================================================
    @RequiresApi(api = Build.VERSION_CODES.N)
    String getTime(){
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        return sdf.format(new Date());
    }
//========================================================================================================
//========================================================================================================
}
