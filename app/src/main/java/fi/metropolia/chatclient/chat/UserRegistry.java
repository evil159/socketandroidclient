package fi.metropolia.chatclient.chat;

import android.support.annotation.NonNull;
import java.util.ArrayList;
import java.util.List;

public class UserRegistry {
    private UserRegistryChangeListener listener;
    private final List<String> users;

    public interface UserRegistryChangeListener {
        void onUserRegistryChanged(@NonNull List<String> list);
    }

    public UserRegistry() {
        this.users = new ArrayList();
    }

    public UserRegistryChangeListener getListener() {
        return this.listener;
    }

    public void setListener(UserRegistryChangeListener listener) {
        this.listener = listener;
    }

    public List<String> getUsers() {
        return this.users;
    }

    public void addUser(String user) {
        if (!this.users.contains(user)) {
            this.users.add(user);
            notifyListenerUserRegistryChanged();
        }
    }

    public void clear() {
        this.users.clear();
    }

    private void notifyListenerUserRegistryChanged() {
        if (getListener() != null) {
            getListener().onUserRegistryChanged(getUsers());
        }
    }
}
