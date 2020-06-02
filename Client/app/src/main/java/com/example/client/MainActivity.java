package com.example.client;
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
import android.view.ContextThemeWrapper;
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
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final int SERVER_PORT = 3003;

    public static final String SERVER_IP = "192.168.232.2";
    private ClientThread clientThread;
    private Thread thread;
    private LinearLayout msgList;
    private Handler handler;
    private int clientTextColor;
    private EditText edMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setTitle("Client");
        clientTextColor = ContextCompat.getColor(this, R.color.green);
        handler = new Handler();
        msgList = findViewById(R.id.msgList);
        edMessage = findViewById(R.id.edMessage);

    }


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

    public void showMessage(final String messaggio, final int color) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                msgList.addView(textView(messaggio, color));
            }
        });
     }
//handler - è un gestore di tutti i thread in eseguzione messi in background e rimetterli in foreground
//mette il messaggio in coda dei messaggi che vogliono usare UIThread
    @Override
    public void onClick(View view) {

        if(view.getId() == R.id.connect_server){
            msgList.removeAllViews();
            showMessage("Connessione al Server...", clientTextColor);
            clientThread = new ClientThread();//oggetto tipo Runnable
            thread = new Thread(clientThread);//lo faccio diventare un thread
            thread.start();//avvio del thread
            showMessage("Connessione al Server...", clientTextColor);
            return;
        }

        if(view.getId() == R.id.send_data){
            String messaggioClient = edMessage.getText().toString().trim();
            showMessage(messaggioClient, Color.BLUE);
            if(null != clientThread){
                clientThread.sendMessage(messaggioClient);
            }
        }
    }

    class ClientThread implements Runnable{

        private Socket socket;
        private BufferedReader input;

        @Override
        public void run() {
            try{
                InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
                socket = new Socket(serverAddr, SERVER_PORT); //istanzio un socket, all'ip del server e alla porta

                while (!Thread.currentThread().isInterrupted()){
                    //======================LETTURA DEI MESSAGGI IN ARRIVO======================//
                    this.input = new BufferedReader(new InputStreamReader(socket.getInputStream()));//InputStream è uno stream di byte che lo trasformo in uno stream di caratteri con I.S.Reader() e poi lo bufferizzo
                    String messaggio = input.readLine();//LETTURA DELLE STRINGHE ARRIVATE
                    if(null == messaggio || "Disconnect".contentEquals(messaggio)){//Il messaggio è diverso da null
                        Thread.interrupted();
                        messaggio = "Server Disconnesso.";
                        showMessage(messaggio, Color.RED);//vedo il messaggio, sull'interfaccia grafica, rosso
                        break;
                    }
                 showMessage("Server: " + messaggio, clientTextColor);
                }
            } catch (UnknownHostException el) {
                el.printStackTrace();
            } catch (IOException el) {
                el.printStackTrace();
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

    @RequiresApi(api = Build.VERSION_CODES.N)
    String getTime(){
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        return sdf.format(new Date());
    }

    protected void onDestroy() {
        super.onDestroy();
        if (null != clientThread) {
            clientThread.sendMessage("Disconnesso");
            clientThread = null;
        }
    }
}



