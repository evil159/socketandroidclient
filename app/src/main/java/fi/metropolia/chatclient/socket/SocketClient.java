package fi.metropolia.chatclient.socket;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class SocketClient implements Serializable, SocketObservable {
    private final String host;
    private BufferedReader in;
    private final List<String> messagesQueue = new ArrayList<String>();
    private final List<SocketObserver> observers = new ArrayList<SocketObserver>();
    private PrintWriter out;
    private final int port;
    private boolean running;
    private Socket socket;

    private class InitializationRoutine implements Runnable {

        private InitializationRoutine() {
        }

        public void run() {
            try {
                socket = new Socket(host, port);
                out = new PrintWriter(socket.getOutputStream());
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        onConnected();
                    }
                });
            } catch (final IOException e) {
                runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        onConnectionError(e);
                    }
                });
            }
        }
    }

    private class ServerListener implements Runnable {

        private ServerListener() {
        }

        public void run() {
            while (isRunning()) {
                try {
                    final String message = in.readLine();
                    if (message == null) {
                        throw new IOException("Disconnected from server");
                    }
                    runOnMainThread(new Runnable() {
                        @Override
                        public void run() {
                            onMessageReceived(message);
                        }
                    });
                } catch (final IOException e) {
                    runOnMainThread(new Runnable() {
                        @Override
                        public void run() {
                            notifyListenersOnConnectionFailure(e);
                        }
                    });
                    running = false;
                }
            }
            disposeOfResources();
        }
    }

    public SocketClient(String host, int port) {
        this.running = false;
        this.host = host;
        this.port = port;
    }

    protected List<SocketObserver> getObservers() {
        return this.observers;
    }

    public boolean isRunning() {
        return running;
    }

    public void connect() {
        new Thread(new InitializationRoutine()).start();
    }

    public void disconnect() {
        running = false;
        disposeOfResources();
    }

    private void disposeOfResources() {
        try {
            this.socket.close();
            this.in.close();
            this.out.close();
        } catch (Exception e) {
        }
    }

    public void sendMessage(@NonNull String message) {
        if (this.out == null || this.out.checkError()) {
            this.messagesQueue.add(message);
            return;
        }
        this.out.println(message);
        this.out.flush();
    }

    protected void onMessageReceived(String message) {
        Log.d(toString(), message);
    }

    private void onConnected() {
        this.running = true;
        new Thread(new ServerListener()).start();
        for (String message : this.messagesQueue) {
            sendMessage(message);
        }
        notifyListenersOnConnected();
        Log.d(toString(), "connected to " + this.host + " " + this.port);
    }

    private void onConnectionError(Exception error) {
        Log.d(toString(), "connection error " + error.getMessage());
        notifyListenersOnConnectionFailure(error);
    }

    private void runOnMainThread(@NonNull Runnable runnable) {
        new Handler(Looper.getMainLooper()).post(runnable);
    }

    private void notifyListenersOnConnected() {
        for (SocketObserver observer : this.observers) {
            observer.onConnectedToSocket();
        }
    }

    private void notifyListenersOnConnectionFailure(Exception error) {
        for (SocketObserver observer : this.observers) {
            observer.onConnectionError(error);
        }
    }

    public void registerObserver(SocketObserver observer) {
        this.observers.add(observer);
    }

    public void deregisterObserver(SocketObserver observer) {
        this.observers.remove(observer);
    }
}
