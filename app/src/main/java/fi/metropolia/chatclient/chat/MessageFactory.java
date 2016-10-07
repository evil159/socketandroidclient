package fi.metropolia.chatclient.chat;

import android.support.annotation.NonNull;
import fi.metropolia.chatclient.models.UserSession;

public class MessageFactory {
    @NonNull
    public static Message historyMessage(@NonNull String from, @NonNull String text) {
        return new Message(from, text, UserSession.getInstance().getCurrentUser().equals(from));
    }

    @NonNull
    public static Message notDeliveredMessage(@NonNull String from, @NonNull String text) {
        Message message = historyMessage(from, text);
        message.setDelivered(false);
        return message;
    }
}
