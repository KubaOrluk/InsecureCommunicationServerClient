package com.example.insecurecommunicationserverclient;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.Objects;

import javax.net.SocketFactory;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManagerFactory;

public class TLSClient {
    public SSLSocket request(InetAddress serverHost, int serverPort,
                          String tlsVersion, String trustStoreName, char[] trustStorePassword,
                          String keyStoreName, char[] keyStorePassword) throws Exception {

        Objects.requireNonNull(tlsVersion, "TLS version is mandatory");

        Objects.requireNonNull(serverHost, "Server host cannot be null");

        if (serverPort <= 0) {
            throw new IllegalArgumentException(
                    "Server port cannot be lesss than or equal to 0");
        }

        KeyStore trustStore = KeyStore.getInstance("PKCS12");
        InputStream ts = MainActivity.getAppContext().getAssets().open(trustStoreName);

        trustStore.load(ts, trustStorePassword);
        ts.close();
        TrustManagerFactory tmf = TrustManagerFactory
                .getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(trustStore);

        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        InputStream ks = MainActivity.getAppContext().getAssets().open(keyStoreName);

        keyStore.load(ks, keyStorePassword);
        KeyManagerFactory kmf = KeyManagerFactory
                .getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, keyStorePassword);
        SSLContext ctx = SSLContext.getInstance("TLSv1.2");
        ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(),
                SecureRandom.getInstanceStrong());

        SocketFactory factory = ctx.getSocketFactory();
        Socket connection = null;
        try {
            connection = factory.createSocket(serverHost, serverPort);
            ((SSLSocket) connection).setEnabledProtocols(new String[] {tlsVersion});
            SSLParameters sslParams = new SSLParameters();
            sslParams.setEndpointIdentificationAlgorithm("HTTPS");
            ((SSLSocket) connection).setSSLParameters(sslParams);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (SSLSocket)connection;
    }
}