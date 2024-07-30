package com.anowercs.awsproject;

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
import com.amazonaws.mobile.client.results.SignUpResult;
import com.amazonaws.mobile.client.results.UserCodeDeliveryDetails;
import com.amazonaws.mobile.client.UserStateDetails;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText etUsername, etEmail, etPassword;
    private Button btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnRegister = findViewById(R.id.btnRegister);

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

                Map<String, String> attributes = new HashMap<>();
                attributes.put("email", email);

                AWSMobileClient.getInstance().signUp(username, password, attributes, null, new Callback<SignUpResult>() {
                    @Override
                    public void onResult(SignUpResult signUpResult) {
                        runOnUiThread(() -> {
                            UserCodeDeliveryDetails details = signUpResult.getUserCodeDeliveryDetails();
                            showToastLong("Sign-up successful! Please confirm your registration through: " + details.getDestination());

                            // Redirect to ConfirmActivity after successful registration
                            Intent intent = new Intent(RegisterActivity.this, ConfirmActivity.class);
                            startActivity(intent);
                            finish(); // Optional: Call finish() if you don't want to keep RegisterActivity in the back stack
                        });
                    }

                    @Override
                    public void onError(Exception e) {
                        runOnUiThread(() -> showToastLong("Sign-up failed: " + e.getMessage()));

                        System.out.println(e.getMessage());
                    }
                });
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
