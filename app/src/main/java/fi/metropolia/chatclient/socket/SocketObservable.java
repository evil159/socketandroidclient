package fi.metropolia.chatclient.socket;

public interface SocketObservable {
    void deregisterObserver(SocketObserver socketObserver);

    void registerObserver(SocketObserver socketObserver);
}
