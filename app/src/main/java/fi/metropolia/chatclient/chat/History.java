package fi.metropolia.chatclient.chat;

import android.support.annotation.NonNull;
import java.util.ArrayList;
import java.util.List;

public class History {
    private HistoryChangeListener listener;
    private final List<Message> messages;

    public interface HistoryChangeListener {
        void onHistoryUpdated(List<Message> list);
    }

    public History() {
        this.messages = new ArrayList();
    }

    public HistoryChangeListener getListener() {
        return this.listener;
    }

    public void setListener(HistoryChangeListener listener) {
        this.listener = listener;
    }

    public List<Message> getMessages() {
        return this.messages;
    }

    public void addMessage(@NonNull Message message) {
        this.messages.add(message);
        notifyHistoryUpdated();
    }

    public void addMessages(@NonNull List<Message> messages) {
        this.messages.addAll(messages);
        notifyHistoryUpdated();
    }

    public void clear() {
        this.messages.clear();
    }

    private void notifyHistoryUpdated() {
        if (getListener() != null) {
            getListener().onHistoryUpdated(getMessages());
        }
    }
}
