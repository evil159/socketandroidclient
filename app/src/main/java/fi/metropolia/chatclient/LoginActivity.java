package fi.metropolia.chatclient;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.text.TextUtils;
import android.widget.EditText;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import fi.metropolia.chatclient.chat.ChatClient;
import fi.metropolia.chatclient.chat.ChatClient.ChatClientObserver;
import fi.metropolia.chatclient.models.UserSession;

public class LoginActivity extends Activity implements ChatClientObserver {
    private Snackbar connectionErrorSnackbar;
    private ProgressDialog progressDialog;
    @BindView(R.id.usernameEditText)
    EditText usernameEditText;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);
        ChatClient.getInstance().registerObserver(this);

        ButterKnife.bind(this);
    }

    public void onBackPressed() {
        super.onBackPressed();

        moveTaskToBack(true);
    }

    @OnClick(R.id.loginButton)
    void onLoginClicked() {

        if (TextUtils.isEmpty(usernameEditText.getText())) {
            usernameEditText.setError(getResources().getString(R.string.username_empty_error));
            return;
        }

        showProgressDialog();

        usernameEditText.setEnabled(false);

        ChatClient.getInstance().connect();
    }

    private void showProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(getResources().getString(R.string.login_authenticating));
        progressDialog.show();
    }

    private void dismissProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    public void onConnectedToSocket() {
        ChatClient.getInstance().registerUser(usernameEditText.getText().toString());

        if (connectionErrorSnackbar != null) {
            connectionErrorSnackbar.dismiss();
            connectionErrorSnackbar = null;
        }
    }

    public void onConnectionError(Exception error) {
        usernameEditText.setEnabled(true);
        dismissProgressDialog();
        connectionErrorSnackbar = Snackbar.make(findViewById(android.R.id.content),
                R.string.no_connection_to_server,
                Snackbar.LENGTH_INDEFINITE);
        connectionErrorSnackbar.show();
    }

    public void onError(Exception error) {
        dismissProgressDialog();
        this.usernameEditText.setEnabled(true);
        this.usernameEditText.setError(error.getMessage());
    }

    public void onUsernameRegistered(String username) {
        ChatClient.getInstance().deregisterObserver(this);
        dismissProgressDialog();
        UserSession.getInstance().open(username);

        setResult(RESULT_OK);
        finish();
    }
}
