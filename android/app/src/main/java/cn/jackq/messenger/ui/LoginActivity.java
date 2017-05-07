package cn.jackq.messenger.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.jackq.messenger.R;

public class LoginActivity extends AbstractMessengerActivity {
    private static final String TAG = "LoginActivity";

    @BindView(R.id.user_name)
    EditText mUserNameEdit;
    @BindView(R.id.password)
    EditText mPasswordEdit;
    @BindView(R.id.host)
    EditText mHostEdit;
    @BindView(R.id.port)
    EditText mPortEdit;
    @BindView(R.id.is_create_new)
    CheckBox mIsCreateNewCheckBox;

    @BindView(R.id.login_progress)
    View mProgressView;
    @BindView(R.id.login_form)
    View mLoginFormView;

    private boolean mConnecting = false;
    private int state = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ButterKnife.bind(this);
    }

    @Override
    public void onServerStateChange() {
        super.onServerStateChange();

        if (Looper.myLooper() != Looper.getMainLooper()) {
            this.runOnUiThread(this::syncServerState);
        }

    }

    private void syncServerState() {
        switch (getMainService().getStatus()) {
            case IN_CALL:
            case LOGIN_IDLE:
                // login success, redirect activity
                mConnecting = false;
                startActivity(new Intent(this, MainActivity.class));
                return;
            case NOT_LOGIN:
                if (state > 1) {
                    state = 1;
                    mConnecting = false;
                    mUserNameEdit.setError(getMainService().getErrorMessage());
                    mUserNameEdit.requestFocus();
                    showProgress(false);
                } else if (state == 1) {
                    state++;
                    login();
                }
                break;
            case NOT_CONNECTED:
                if (state > 0) {
                    mConnecting = false;
                    mHostEdit.setError(getMainService().getErrorMessage());
                    mHostEdit.requestFocus();
                    state = 0;
                    showProgress(false);
                }
                break;
            case CONNECTING:
            case LOGGING_IN:
            default:
                // nothing
        }
        showProgress(false);
    }

    private void login() {
        if (mIsCreateNewCheckBox.isChecked()) {
            getMainService().userRegister(mUserNameEdit.getText().toString(),
                    mPasswordEdit.getText().toString());
        } else {
            getMainService().userLogin(mUserNameEdit.getText().toString(),
                    mPasswordEdit.getText().toString());
        }
    }

    @OnClick(R.id.email_sign_in_button)
    void attemptLogin() {
        if (mConnecting)
            return;

        // Reset errors.
        mUserNameEdit.setError(null);
        mPasswordEdit.setError(null);
        mHostEdit.setError(null);
        mPortEdit.setError(null);

        // Store values at the time of the login attempt.
        String username = mUserNameEdit.getText().toString();
        String password = mPasswordEdit.getText().toString();
        String host = mHostEdit.getText().toString();
        int port = Integer.parseInt(mPortEdit.getText().toString());

        boolean cancel = false;
        View focusView = null;


        Log.d(TAG, "attemptLogin: " + username + password + host + password);
        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            mPasswordEdit.setError(getString(R.string.error_field_required));
            focusView = mPasswordEdit;
            cancel = true;
        }

        // Check for a valid username.
        if (TextUtils.isEmpty(username)) {
            mUserNameEdit.setError(getString(R.string.error_field_required));
            focusView = mUserNameEdit;
            cancel = true;
        }
        // Check for a valid username.
        if (port < 1 || port > 65535) {
            mPortEdit.setError("port should in range of 1 to 65535");
            focusView = mPortEdit;
            cancel = true;
        }
        // Check for a valid username.
        if (TextUtils.isEmpty(host)) {
            mHostEdit.setError(getString(R.string.error_field_required));
            focusView = mHostEdit;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            showProgress(true);
            mConnecting = true;
            if (state == 0) {
                state = 1;
                getMainService().connectToServer(host, port);
            } else {
                state = 2;
                login();
            }
        }
    }

    private void showProgress(final boolean show) {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        mProgressView.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

}

