package com.example.signin;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

public class MainActivity extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int SIGN_IN_REQUEST = 123;

    private TextView details;
    private SignInButton signInButton;
    private Button signOutButton;
    private Button revokeButton;
    private GoogleApiClient googleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        details = (TextView) findViewById(R.id.details);
        signInButton = (SignInButton) findViewById(R.id.signin_button);
        signOutButton = (Button) findViewById(R.id.signout_button);
        revokeButton = (Button) findViewById(R.id.revoke_access_button);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                                        .requestEmail()
                                                        .build();

        googleApiClient = new GoogleApiClient.Builder(this)
                                            .enableAutoManage(this, this)
                                            .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                                            .build();

        // Sign In using an intent
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (googleApiClient.isConnected()) {
                    details.setText(R.string.signing_in);
                    Intent intent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
                    startActivityForResult(intent, SIGN_IN_REQUEST);
                }
            }
        });

        // Sign Out
        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (googleApiClient.isConnected()) {
                    Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(@NonNull Status status) {
                            if (status.isSuccess()) {
                                details.setText(R.string.signed_out);
                                updateButtons(false);
                            }
                            else {
                                details.setText(R.string.unable_to_sign_out);
                            }
                        }
                    });
                }
            }
        });

        // Revoke Access
        revokeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (googleApiClient.isConnected()) {
                    Auth.GoogleSignInApi.revokeAccess(googleApiClient).setResultCallback(new ResultCallback<Status>() {
                        @Override
                        public void onResult(@NonNull Status status) {
                            if (status.isSuccess()) {
                                details.setText(R.string.access_revoked);
                                updateButtons(false);
                            }
                            else {
                                details.setText(R.string.unable_to_revoke_access);
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!googleApiClient.isConnected() || !googleApiClient.isConnecting()) {
            googleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        if (googleApiClient.isConnected() || googleApiClient.isConnecting()) {
            googleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "Connected successfully");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Connection suspended");
        googleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(TAG, "Connection failed. Error code: " + connectionResult.getErrorCode());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SIGN_IN_REQUEST) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleResult(result);
        }
    }

    private void handleResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            GoogleSignInAccount account = result.getSignInAccount();
            details.setText(account.getEmail());
            updateButtons(true);
        }
        else {
            details.setText(R.string.unable_to_sign_in);
        }
    }

    private void updateButtons(boolean b) {
        if (b) {
            signInButton.setEnabled(false);
            signOutButton.setEnabled(true);
            revokeButton.setEnabled(true);
        }
        else {
            signInButton.setEnabled(true);
            signOutButton.setEnabled(false);
            revokeButton.setEnabled(false);
        }
    }
}
