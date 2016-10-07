package fi.metropolia.chatclient.socket;

public interface SocketObserver {
    void onConnectedToSocket();

    void onConnectionError(Exception exception);
}
