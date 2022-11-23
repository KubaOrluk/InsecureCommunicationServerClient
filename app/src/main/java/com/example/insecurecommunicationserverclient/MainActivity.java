package com.example.insecurecommunicationserverclient;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;

import javax.net.ssl.SSLSocket;

@SuppressLint("SetTextI18n")
public class MainActivity extends AppCompatActivity {
    Thread Thread1 = null;
    Thread thr2 = null;
    EditText etIP, etPort, userName, encryptionPass;
    TextView tvMessages;
    EditText etMessage;
    Button btnSend;
    Button btnDisconn;
    Button btnConnect;
    Button btnEditPin;
    String SERVER_IP, user;
    int SERVER_PORT;
    SSLSocket sslSocket;

    TextView editPin1, editPin2, editPin3, editPin4;

    private static final String TLS_VERSION = "TLSv1.2";
    private static final int SERVER_COUNT = 1;
    private static final String TRUST_STORE_NAME = "servercert.p12";
    //private static final char[] TRUST_STORE_PWD = new char[] {'a', 'b', 'c', '1', '2', '3'};
    private static final char[] TRUST_STORE_PWD = "abc123".toCharArray();
    private static final String KEY_STORE_NAME = "clientcert.p12";
    //private static final char[] KEY_STORE_PWD = new char[] {'a', 'b', 'c', '1', '2', '3'};
    private static final char[] KEY_STORE_PWD = "abc123".toCharArray();


    EncryptData encryptData;

    private static Context context;

    public static Context getAppContext() {
        return MainActivity.context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainActivity.context = getApplication().getApplicationContext();
        encryptData = new EncryptData();

        setContentView(R.layout.pin);
        editPin1 = findViewById(R.id.editPin1);
        editPin2 = findViewById(R.id.editPin2);
        editPin3 = findViewById(R.id.editPin3);
        editPin4 = findViewById(R.id.editPin4);

        editPin1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (editPin1.length() == 1) {
                    editPin1.clearFocus();
                    editPin2.requestFocus();
                    editPin2.setCursorVisible(true);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        editPin2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (editPin2.length() == 1) {
                    editPin2.clearFocus();
                    editPin3.requestFocus();
                    editPin3.setCursorVisible(true);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        editPin3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (editPin3.length() == 1) {
                    editPin3.clearFocus();
                    editPin4.requestFocus();
                    editPin4.setCursorVisible(true);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        editPin4.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (editPin4.length() == 1) {
                    editPin4.clearFocus();
                    if(verifyPin())
                        initMainView();
                    else
                        clearPin();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

    }

    protected Boolean verifyPin() {
        StringBuilder pinSb = new StringBuilder();
        pinSb.append(editPin1.getText());
        pinSb.append(editPin2.getText());
        pinSb.append(editPin3.getText());
        pinSb.append(editPin4.getText());
        String pinSbStr = pinSb.toString();
        if(pinSbStr.equals("1234")) { //TODO: check Pin
            return true;
        }
        return false;
    }

    protected void clearPin() {
        editPin1.setText("");
        editPin2.setText("");
        editPin3.setText("");
        editPin4.setText("");

        editPin4.clearFocus();
        editPin1.requestFocus();
        editPin1.setCursorVisible(true);
    }

    protected void initMainView() {
        setContentView(R.layout.activity_main);
        etIP = findViewById(R.id.etIP);
        etPort = findViewById(R.id.etPort);
        userName = findViewById(R.id.userName);
        encryptionPass = findViewById(R.id.encryptionPass);
        tvMessages = findViewById(R.id.tvMessages);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        btnDisconn = findViewById(R.id.btnDisconnect);
        btnEditPin = findViewById(R.id.btnEditPin);

        btnDisconn.setVisibility(View.GONE);
        etMessage.setVisibility(View.GONE);
        btnSend.setVisibility(View.GONE);
        tvMessages.setVisibility(View.GONE);

        btnConnect = findViewById(R.id.btnConnect);
        //if (etIP==null) Log.i("MainActivity", "null");

        //detect if any text field is empty
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().equals("")) {
                    btnConnect.setVisibility(View.GONE);
                } else {
                    btnConnect.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void afterTextChanged(Editable s) {}
        };

        etIP.addTextChangedListener(textWatcher);
        etPort.addTextChangedListener(textWatcher);
        userName.addTextChangedListener(textWatcher);
        encryptionPass.addTextChangedListener(textWatcher);

        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(MainActivity.context, EncryptData.getSha256Hash(encryptionPass.getText().toString().trim()), Toast.LENGTH_SHORT).show();
                changeToConectedState();
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
        btnDisconn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeToDisconectedState();
            }
        });
    }

    private void changeToConectedState() {
        tvMessages.setText("");
        SERVER_IP = etIP.getText().toString().trim();
        SERVER_PORT = Integer.parseInt(etPort.getText().toString().trim());
        user = userName.getText().toString().trim();

        if(SERVER_IP != null){
            etIP.setVisibility(View.GONE);
            etPort.setVisibility(View.GONE);
            userName.setVisibility(View.GONE);
            encryptionPass.setVisibility(View.GONE);
            btnConnect.setVisibility(View.GONE);
            btnEditPin.setVisibility(View.GONE);
            btnDisconn.setVisibility(View.VISIBLE);
            tvMessages.setVisibility(View.VISIBLE);
            etMessage.setVisibility(View.VISIBLE);
            btnSend.setVisibility(View.VISIBLE);
        }

        Thread1 = new Thread(new Thread1());
        Thread1.start();
    }

    private void changeToDisconectedState() {
        thr2.interrupt();
        new Thread(new Thread4Disconnect()).start();

        tvMessages.setText("");

        etIP.setVisibility(View.VISIBLE);
        etPort.setVisibility(View.VISIBLE);
        userName.setVisibility(View.VISIBLE);
        encryptionPass.setVisibility(View.VISIBLE);
        btnConnect.setVisibility(View.VISIBLE);

        btnDisconn.setVisibility(View.GONE);
        tvMessages.setVisibility(View.GONE);
        etMessage.setVisibility(View.GONE);
        btnSend.setVisibility(View.GONE);


    }



    private PrintWriter output;
    private BufferedReader input;
    class Thread1 implements Runnable {
        public void run() {
            TLSClient client = new TLSClient();
            sslSocket = null;
            encryptData.setSecretKeyFromString(encryptionPass.getText().toString().trim());
            try {
                sslSocket = client.request(
                        InetAddress.getByName(SERVER_IP), SERVER_PORT, TLS_VERSION,
                        TRUST_STORE_NAME, TRUST_STORE_PWD, KEY_STORE_NAME, KEY_STORE_PWD);

                input = new BufferedReader(new InputStreamReader(sslSocket.getInputStream()));
                output = new PrintWriter(sslSocket.getOutputStream(), true);
                output.write(encryptData.encryptToSend(user )+ "\n");
                //output.write(user + "\n");
                output.flush();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvMessages.setText("Connected, IP: " + SERVER_IP + ", Port: " + String.valueOf(SERVER_PORT) + " \nYour username is: " + String.valueOf(user) + "\n");
                    }
                });
                thr2 = new Thread(new Thread2());
                thr2.start();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    class Thread2 implements Runnable {
        @Override
        public void run() {
            while (true) {
                if (Thread.interrupted()) {
                    return;
                }
                try {
                    String inp = input.readLine();
                    final String message = encryptData.decryptFromRec(inp);
                    if (message != null) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tvMessages.append("\nServer: " + message + " ");
                            }
                        });
                    } else {
                        if(Thread.interrupted())
                            return;
                        else {
                            //Propably disconnected from server, do a disconnect
                            //TODO: verify if working
                            changeToDisconectedState();
                            return;
                        }
                        //Thread1 = new Thread(new Thread1());
                        //Thread1.start();
                    }
                } catch (SocketException e) {
                    return;
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
            output.write(encryptData.encryptToSend(message)+ "\n");
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
    class Thread4Disconnect implements Runnable {
        Thread4Disconnect() { }

        @Override
        public void run() {
            try {
                sslSocket.close();
            } catch (Exception e) {
                return;
            }
        }
    }
}