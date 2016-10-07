package fi.metropolia.chatclient;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import fi.metropolia.chatclient.chat.ChatClient;
import fi.metropolia.chatclient.chat.UserRegistry.UserRegistryChangeListener;

public class UsersActivity extends AppCompatActivity implements UserRegistryChangeListener {
    @BindView(R.id.listView)
    ListView listView;
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        reloadData(ChatClient.getInstance().getUserRegistry().getUsers());

        ChatClient.getInstance().loadUsers();
        ChatClient.getInstance().getUserRegistry().setListener(this);
    }

    protected void onPause() {
        super.onPause()
        ;
        ChatClient.getInstance().getUserRegistry().setListener(null);
    }

    private void reloadData(@NonNull List<String> users) {
        this.listView.setAdapter(new ArrayAdapter(this, android.R.layout.simple_list_item_1, users));
    }

    public void onUserRegistryChanged(@NonNull List<String> users) {
        reloadData(users);
    }
}
