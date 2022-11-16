package com.example.insecurecommunicationserverclient;
import android.annotation.SuppressLint;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

@SuppressLint("SetTextI18n")
public class MainActivity extends AppCompatActivity {
    Thread Thread1 = null;
    EditText etIP, etPort, userName;
    TextView tvMessages;
    EditText etMessage;
    Button btnSend;
    String SERVER_IP, user;
    int SERVER_PORT;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        etIP = findViewById(R.id.etIP);
        etPort = findViewById(R.id.etPort);
        userName = findViewById(R.id.userName);
        tvMessages = findViewById(R.id.tvMessages);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);

        etMessage.setVisibility(View.GONE);
        btnSend.setVisibility(View.GONE);
        tvMessages.setVisibility(View.GONE);

        Button btnConnect = findViewById(R.id.btnConnect);
        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvMessages.setText("");
                SERVER_IP = etIP.getText().toString().trim();
                SERVER_PORT = Integer.parseInt(etPort.getText().toString().trim());
                user = userName.getText().toString().trim();

                etIP.setVisibility(View.GONE);
                etPort.setVisibility(View.GONE);
                userName.setVisibility(View.GONE);
                btnConnect.setVisibility(View.GONE);
                tvMessages.setVisibility(View.VISIBLE);
                etMessage.setVisibility(View.VISIBLE);
                btnSend.setVisibility(View.VISIBLE);

                Thread1 = new Thread(new Thread1());
                Thread1.start();
            }
        });
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = etMessage.getText().toString().trim();
                if (!message.isEmpty()) {
                    new Thread(new Thread3(message)).start();
                }
            }
        });
    }
    private PrintWriter output;
    private BufferedReader input;
    class Thread1 implements Runnable {
        public void run() {
            Socket socket;
            try {
                socket = new Socket(SERVER_IP, SERVER_PORT);
                output = new PrintWriter(socket.getOutputStream());
                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                output.write(user + "\n");
                output.flush();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvMessages.setText("Connected, IP: " + SERVER_IP + ", Port: " + String.valueOf(SERVER_PORT) + " \nYour username is: " + String.valueOf(user) + "\n");
                    }
                });
                new Thread(new Thread2()).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    class Thread2 implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    final String message = input.readLine();
                    if (message != null) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tvMessages.append("\nServer: " + message + " ");
                            }
                        });
                    } else {
                        Thread1 = new Thread(new Thread1());
                        Thread1.start();
                        return;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    class Thread3 implements Runnable {
        private String message;
        Thread3(String message) {
            this.message = message;
        }
        @Override
        public void run() {
            output.write(message);
            output.write("\n");
            output.flush();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tvMessages.append("\n" + user + ": " + message + " ");
                            etMessage.setText("");
                }
            });
        }
    }
}