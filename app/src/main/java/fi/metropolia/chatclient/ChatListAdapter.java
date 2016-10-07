package fi.metropolia.chatclient;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import fi.metropolia.chatclient.chat.Message;


/**
 * iConnect iCR
 * <p/>
 * Created by Roman Laitarenko on 10/7/16.
 * Copyright (c) 2016 iConnect POS. All rights reserved.
 */

public class ChatListAdapter extends BaseAdapter {

    private final Context context;
    private final List<Message> history;

    public ChatListAdapter(Context context, List<Message> history) {
        this.context = context;
        this.history = history;
    }

    @Override
    public int getCount() {
        return history.size();
    }

    @Override
    public Message getItem(int position) {
        return history.get(position);
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position).isOwnMessage() ? 0 : 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        Message message = getItem(position);

        if (convertView == null) {
            int resource = message.isOwnMessage() ? R.layout.item_outgoing_message : R.layout.item_incoming_message;
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            holder = new ViewHolder();
            convertView = inflater.inflate(resource, parent, false);
            holder.messageTextView = (TextView) convertView.findViewById(R.id.message_text_view);
            holder.senderTextView = (TextView) convertView.findViewById(R.id.sender_text_view);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }


        holder.messageTextView.setText(message.getText());
        holder.senderTextView.setText(message.getFrom());

        return convertView;
    }

    private static class ViewHolder {
        private TextView messageTextView;
        private TextView senderTextView;
    }
}
