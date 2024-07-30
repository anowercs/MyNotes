package com.anowercs.awsproject;

import static com.amazonaws.services.cognitoidentityprovider.model.AuthFlowType.USER_SRP_AUTH;

import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.AWSStartupHandler;
import com.amazonaws.mobile.client.AWSStartupResult;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.results.SignInResult;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.results.SignInResult;
import com.amazonaws.mobile.client.UserStateDetails;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);

        AWSMobileClient.getInstance().initialize(this, new Callback<UserStateDetails>() {
            @Override
            public void onResult(UserStateDetails userStateDetails) {
                // AWSMobileClient is initialized and you can now make calls to AWS services.
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> Toast.makeText(LoginActivity.this, "Initialization error: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmail.getText().toString();
                String password = etPassword.getText().toString();

                AWSMobileClient.getInstance().signIn(email, password, null, new Callback<SignInResult>() {
                    @Override
                    public void onResult(SignInResult signInResult) {
                        runOnUiThread(() -> {
                            switch (signInResult.getSignInState()) {
                                case DONE:
                                    Toast.makeText(LoginActivity.this, "Sign-in successful!", Toast.LENGTH_LONG).show();
                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                    startActivity(intent);
                                    break;
                                case SMS_MFA:
                                    Toast.makeText(LoginActivity.this, "Please confirm sign-in with SMS.", Toast.LENGTH_LONG).show();
                                    break;
                                case PASSWORD_VERIFIER:
                                    Toast.makeText(LoginActivity.this, "Please confirm sign-in with password verifier.", Toast.LENGTH_LONG).show();
                                    break;
                                case NEW_PASSWORD_REQUIRED:
                                    Toast.makeText(LoginActivity.this, "Please confirm sign-in with new password.", Toast.LENGTH_LONG).show();
                                    break;
                                case CUSTOM_CHALLENGE:
                                    Toast.makeText(LoginActivity.this, "Please complete custom challenge.", Toast.LENGTH_LONG).show();
                                    break;
                                case DEVICE_SRP_AUTH:
                                    Toast.makeText(LoginActivity.this, "Please confirm sign-in with device SRP auth.", Toast.LENGTH_LONG).show();
                                    break;
                                case DEVICE_PASSWORD_VERIFIER:
                                    Toast.makeText(LoginActivity.this, "Please confirm sign-in with device password verifier.", Toast.LENGTH_LONG).show();
                                    break;
                                case ADMIN_NO_SRP_AUTH:
                                    Toast.makeText(LoginActivity.this, "Please confirm sign-in with admin no SRP auth.", Toast.LENGTH_LONG).show();
                                    break;
                                case UNKNOWN:
                                    Toast.makeText(LoginActivity.this, "Unsupported sign-in confirmation: " + signInResult.getSignInState(), Toast.LENGTH_LONG).show();
                                    break;
                                default:
                                    Toast.makeText(LoginActivity.this, "Sign-in failed.", Toast.LENGTH_LONG).show();
                                    break;
                            }
                        });
                    }

                    @Override
                    public void onError(Exception e) {
                        runOnUiThread(() -> Toast.makeText(LoginActivity.this, "Sign-in failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
                    }
                });
            }
        });
    }
}
