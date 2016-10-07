package fi.metropolia.chatclient;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;
import com.joanzapata.iconify.fonts.FontAwesomeModule;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import fi.metropolia.chatclient.chat.ChatClient;
import fi.metropolia.chatclient.chat.ChatClient.ChatClientObserver;
import fi.metropolia.chatclient.chat.History.HistoryChangeListener;
import fi.metropolia.chatclient.chat.Message;
import fi.metropolia.chatclient.models.Settings;
import fi.metropolia.chatclient.models.UserSession;

public class MainActivity extends AppCompatActivity implements ChatClientObserver, HistoryChangeListener, TextWatcher, OnEditorActionListener {
    private static final int LOGIN_ACTIVITY_CODE = 88;
    @BindView(R.id.messageInput)
    EditText messageInput;
    @BindView(R.id.listView)
    ListView messagesListView;
    @BindView(R.id.sendButton)
    Button sendButton;
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Iconify.with(new FontAwesomeModule());
        Settings.initialize(getApplicationContext());
        ButterKnife.bind(this);

        messageInput.addTextChangedListener(this);
        messageInput.setOnEditorActionListener(this);

        setSupportActionBar(toolbar);

        if (!UserSession.getInstance().isOpen()) {
            showLoginScreen();
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        menu.findItem(R.id.users).setIcon(
                new IconDrawable(this, FontAwesomeIcons.fa_users)
                        .colorRes(android.R.color.white)
                        .actionBarSize()
        );
        menu.findItem(R.id.logout).setIcon(
                new IconDrawable(this, FontAwesomeIcons.fa_sign_out)
                        .colorRes(android.R.color.white)
                        .actionBarSize()
        );

        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.users:
                usersButtonPressed();
                return true;
            case R.id.logout:
                logoutButtonPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showLoginScreen() {
        startActivityForResult(new Intent(this, LoginActivity.class), LOGIN_ACTIVITY_CODE);
    }

    private void resetHistory() {
        onHistoryUpdated(new ArrayList<Message>());
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == LOGIN_ACTIVITY_CODE && resultCode == -1) {
            onSuccessfulLogin();
        }
    }

    protected void onPause() {
        super.onPause();

        deregisterNotifications();
    }

    protected void onResume() {
        super.onResume();

        if (UserSession.getInstance().isOpen()) {
            ChatClient.getInstance().registerObserver(this);
            ChatClient.getInstance().getHistory().setListener(this);
            ChatClient.getInstance().loadHistory();
            ChatClient.getInstance().loadUsers();
        }
    }

    private void deregisterNotifications() {
        ChatClient.getInstance().deregisterObserver(this);
        ChatClient.getInstance().getHistory().setListener(null);
    }

    private void onSuccessfulLogin() {
    }

    private void logoutButtonPressed() {

        new AlertDialog.Builder(this)
                .setTitle(R.string.log_out_warning)
                .setPositiveButton(R.string.action_log_out, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        deregisterNotifications();

                        ChatClient.getInstance().disconnect();
                        UserSession.getInstance().close();

                        showLoginScreen();
                        resetHistory();

                    }
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    private void usersButtonPressed() {
        startActivity(new Intent(this, UsersActivity.class));
    }

    @OnClick({R.id.sendButton})
    public void sendButtonPressed() {
        String message = messageInput.getText().toString();

        messageInput.setText("");

        ChatClient.getInstance().sendMessage(message);
    }

    public void onError(Exception error) {
        onConnectionError(error);
    }

    public void onUsernameRegistered(String username) {
    }

    public void onHistoryUpdated(List<Message> history) {
        messagesListView.setAdapter(new ChatListAdapter(this, history));
    }

    public void onConnectedToSocket() {
    }

    public void onConnectionError(Exception error) {
        Snackbar.make(findViewById(android.R.id.content), error.getMessage(), Snackbar.LENGTH_INDEFINITE).show();
    }

    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    public void afterTextChanged(Editable s) {
        this.sendButton.setEnabled(!TextUtils.isEmpty(s));
    }

    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId != 4 || TextUtils.isEmpty(v.getText())) {
            return false;
        }
        sendButtonPressed();
        return true;
    }
}
