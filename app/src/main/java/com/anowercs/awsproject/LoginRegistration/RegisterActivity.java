package com.anowercs.awsproject.LoginRegistration;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amazonaws.mobile.client.results.SignUpResult;
import com.amazonaws.mobile.client.results.UserCodeDeliveryDetails;
import com.anowercs.awsproject.R;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText etUsername, etEmail, etPassword, etConfirmationCode;
    private Button btnRegister, btnConfirm, R_login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmationCode = findViewById(R.id.etConfirmationCode);
        btnRegister = findViewById(R.id.btnRegister);
        btnConfirm = findViewById(R.id.btnConfirm);

        R_login = findViewById(R.id.R_login);


        AWSMobileClient.getInstance().initialize(this, new Callback<UserStateDetails>() {
            @Override
            public void onResult(UserStateDetails userStateDetails) {
                // AWSMobileClient is initialized and you can now make calls to AWS services.
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> showToastLong("Initialization error: " + e.getMessage()));
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = etUsername.getText().toString();
                String email = etEmail.getText().toString();
                String password = etPassword.getText().toString();

                if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                    showToastLong("All fields are required.");
                    return;
                }

                Map<String, String> attributes = new HashMap<>();
                attributes.put("email", email);

                AWSMobileClient.getInstance().signUp(username, password, attributes, null, new Callback<SignUpResult>() {
                    @Override
                    public void onResult(SignUpResult signUpResult) {
                        runOnUiThread(() -> {
                            UserCodeDeliveryDetails details = signUpResult.getUserCodeDeliveryDetails();
                            showToastLong("Sign-up successful! Please confirm your registration through: " + details.getDestination());


                            btnRegister.setVisibility(View.GONE);
                            // Show confirmation code input and button
                            etConfirmationCode.setVisibility(View.VISIBLE);
                            btnConfirm.setVisibility(View.VISIBLE);
                        });
                    }

                    @Override
                    public void onError(Exception e) {
                        runOnUiThread(() -> showToastLong("Sign-up failed: " + e.getMessage()));
                    }
                });
            }



        });


        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = etUsername.getText().toString();
                String confirmationCode = etConfirmationCode.getText().toString();

                if (confirmationCode.isEmpty()) {
                    showToastLong("Confirmation code is required.");
                    return;
                }

                AWSMobileClient.getInstance().confirmSignUp(username, confirmationCode, new Callback<SignUpResult>() {
                    @Override
                    public void onResult(SignUpResult confirmSignUpResult) {
                        runOnUiThread(() -> {
                            showToastLong("Confirmation successful!");

                            // Redirect to LoginActivity after successful confirmation
                            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                            startActivity(intent);
                            finish();
                        });
                    }

                    @Override
                    public void onError(Exception e) {
                        runOnUiThread(() -> showToastLong("Confirmation failed: " + e.getMessage()));
                    }
                });
            }


        });


        R_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }

    private void showToastLong(String message) {
        final Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG);
        toast.show();

        // Duration for which the toast will be shown in milliseconds
        final int duration = 99900; // 3.5 seconds
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                toast.cancel();
            }
        }, duration);
    }
}
