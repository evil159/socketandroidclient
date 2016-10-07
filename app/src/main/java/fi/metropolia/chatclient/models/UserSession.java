package fi.metropolia.chatclient.models;

import android.support.annotation.NonNull;

public class UserSession {
    private static final String USERNAME_KEY = "username";
    private static UserSession staticSession;
    private String currentUserName;

    static {
        staticSession = null;
    }

    public static UserSession getInstance() {
        if (staticSession == null) {
            staticSession = new UserSession();
        }
        return staticSession;
    }

    private UserSession() {
        this.currentUserName = null;
    }

    public boolean isOpen() {
        return this.currentUserName != null;
    }

    public String getCurrentUser() {
        return this.currentUserName;
    }

    public void close() {
        this.currentUserName = null;
        Settings.remove(USERNAME_KEY);
    }

    public void open(@NonNull String username) {
        this.currentUserName = username;
        Settings.put(USERNAME_KEY, username);
    }

    public boolean restore() {
        this.currentUserName = Settings.get(USERNAME_KEY);
        if (isOpen()) {
            return true;
        }
        return false;
    }
}
