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
    private final List<String> messagesQueue;
    private final List<SocketObserver> observers;
    private PrintWriter out;
    private final int port;
    private boolean running;
    private Socket socket;

    private class InitializationRoutine implements Runnable {

        /* renamed from: fi.metropolia.chatclient.socket.SocketClient.InitializationRoutine.1 */
        class C02221 implements Runnable {
            C02221() {
            }

            public void run() {
                SocketClient.this.onConnected();
            }
        }

        /* renamed from: fi.metropolia.chatclient.socket.SocketClient.InitializationRoutine.2 */
        class C02232 implements Runnable {
            final /* synthetic */ IOException val$e;

            C02232(IOException iOException) {
                this.val$e = iOException;
            }

            public void run() {
                SocketClient.this.onConnectionError(this.val$e);
            }
        }

        private InitializationRoutine() {
        }

        public void run() {
            try {
                SocketClient.this.socket = new Socket(SocketClient.this.host, SocketClient.this.port);
                SocketClient.this.out = new PrintWriter(SocketClient.this.socket.getOutputStream());
                SocketClient.this.in = new BufferedReader(new InputStreamReader(SocketClient.this.socket.getInputStream()));
                SocketClient.this.runOnMainThread(new C02221());
            } catch (IOException e) {
                SocketClient.this.runOnMainThread(new C02232(e));
            }
        }
    }

    private class ServerListener implements Runnable {

        /* renamed from: fi.metropolia.chatclient.socket.SocketClient.ServerListener.1 */
        class C02241 implements Runnable {
            final /* synthetic */ String val$message;

            C02241(String str) {
                this.val$message = str;
            }

            public void run() {
                SocketClient.this.onMessageReceived(this.val$message);
            }
        }

        /* renamed from: fi.metropolia.chatclient.socket.SocketClient.ServerListener.2 */
        class C02252 implements Runnable {
            final /* synthetic */ IOException val$e;

            C02252(IOException iOException) {
                this.val$e = iOException;
            }

            public void run() {
                SocketClient.this.notifyListenersOnConnectionFailure(this.val$e);
            }
        }

        private ServerListener() {
        }

        public void run() {
            while (SocketClient.this.running) {
                try {
                    String message = SocketClient.this.in.readLine();
                    if (message == null) {
                        throw new IOException("Disconnected from server");
                    }
                    SocketClient.this.runOnMainThread(new C02241(message));
                } catch (IOException e) {
                    SocketClient.this.runOnMainThread(new C02252(e));
                }
            }
            SocketClient.this.disposeOfResources();
        }
    }

    public SocketClient(String host, int port) {
        this.observers = new ArrayList();
        this.messagesQueue = new ArrayList();
        this.running = false;
        this.host = host;
        this.port = port;
    }

    protected List<SocketObserver> getObservers() {
        return this.observers;
    }

    public boolean isRunning() {
        return this.running;
    }

    public void connect() {
        new Thread(new InitializationRoutine()).start();
    }

    public void disconnect() {
        this.running = false;
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
