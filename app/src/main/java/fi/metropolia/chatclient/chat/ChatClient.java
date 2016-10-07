package fi.metropolia.chatclient.chat;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import fi.metropolia.chatclient.BuildConfig;
import fi.metropolia.chatclient.socket.SocketClient;
import fi.metropolia.chatclient.socket.SocketObserver;
import java.util.ArrayList;
import java.util.List;

public class ChatClient extends SocketClient {
    private static final String host = "10.0.2.2";
    private static int port;
    private static ChatClient staticClient;
    private final History history;
    private final UserRegistry users;

    private enum Command {
        List("list"),
        History("history"),
        User("user"),
        Message(BuildConfig.FLAVOR),
        Quit("quit");
        
        private static final String COMMAND_PREFIX = ":";
        public final String name;

        private Command(@NonNull String name) {
            this.name = name;
        }

        public String toString() {
            return TextUtils.isEmpty(this.name) ? BuildConfig.FLAVOR : COMMAND_PREFIX + this.name;
        }
    }

    public interface ChatClientObserver extends SocketObserver {
        void onError(Exception exception);

        void onUsernameRegistered(String str);
    }

    static {
        port = 4444;
        staticClient = null;
    }

    public static ChatClient getInstance() {
        if (staticClient == null) {
            staticClient = new ChatClient(host, port);
        }
        return staticClient;
    }

    private ChatClient(String host, int port) {
        super(host, port);
        this.history = new History();
        this.users = new UserRegistry();
    }

    public History getHistory() {
        return this.history;
    }

    public UserRegistry getUserRegistry() {
        return this.users;
    }

    public void registerUser(@NonNull String username) {
        sendCommand(Command.User, username);
    }

    public void loadHistory() {
        this.history.clear();
        sendCommand(Command.History, null);
    }

    public void loadUsers() {
        this.users.clear();
        sendCommand(Command.List, null);
    }

    private void sendCommand(@NonNull Command command, @Nullable String argument) {
        sendMessage(command.toString() + (TextUtils.isEmpty(argument) ? BuildConfig.FLAVOR : " " + argument));
    }

    public void disconnect() {
        super.disconnect();
        resetData();
    }

    protected void onMessageReceived(String message) {
        super.onMessageReceived(message);
        if (!TextUtils.isEmpty(message)) {
            if (message.startsWith("Username is already taken")) {
                notifyObserversWithError(new Exception(message));
            } else if (message.startsWith("Username is")) {
                notifyObserversUsernameRegistered(message.replace("Username is ", BuildConfig.FLAVOR));
            } else if (message.matches(".+:.+")) {
                onHistoryReceived(message);
            } else if (!message.contains(":")) {
                onUserReceived(message);
            }
        }
    }

    private void resetData() {
        this.history.clear();
        this.users.clear();
    }

    private void onHistoryReceived(String history) {
        String[] components = history.split("\n");
        List<Message> result = new ArrayList();
        for (String string : components) {
            String[] messageComp = string.split(":");
            if (messageComp.length >= 2) {
                result.add(MessageFactory.historyMessage(messageComp[0], messageComp[1]));
            }
        }
        this.history.addMessages(result);
    }

    private void onUserReceived(String user) {
        this.users.addUser(user);
    }

    private void notifyObserversUsernameRegistered(String username) {
        for (SocketObserver observer : getObservers()) {
            if (observer instanceof ChatClientObserver) {
                ((ChatClientObserver) observer).onUsernameRegistered(username);
            }
        }
    }

    private void notifyObserversWithError(Exception error) {
        for (SocketObserver observer : getObservers()) {
            if (observer instanceof ChatClientObserver) {
                ((ChatClientObserver) observer).onError(error);
            }
        }
    }
}
