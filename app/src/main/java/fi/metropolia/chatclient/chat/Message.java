package fi.metropolia.chatclient.chat;

import android.support.annotation.NonNull;

public class Message {
    private boolean delivered;
    private final String from;
    private final boolean ownMessage;
    private final String text;

    public Message(@NonNull String from, @NonNull String text, boolean ownMessage) {
        this.delivered = true;
        this.from = from;
        this.text = text;
        this.ownMessage = ownMessage;
    }

    public String getFrom() {
        return this.from;
    }

    public String getText() {
        return this.text;
    }

    public boolean isOwnMessage() {
        return this.ownMessage;
    }

    public boolean isDelivered() {
        return this.delivered;
    }

    public void setDelivered(boolean delivered) {
        this.delivered = delivered;
    }

    public String toString() {
        return String.format("%s:%s", new Object[]{this.from, this.text});
    }
}
