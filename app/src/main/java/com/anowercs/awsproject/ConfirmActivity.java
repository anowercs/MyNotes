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

public class ConfirmActivity extends AppCompatActivity {

    private EditText etUsername, etConfirmationCode;
    private Button btnConfirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm);


        etUsername = findViewById(R.id.etUsername);
        etConfirmationCode = findViewById(R.id.etConfirmationCode);
        btnConfirm = findViewById(R.id.btnConfirm);

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = etUsername.getText().toString();
                String confirmationCode = etConfirmationCode.getText().toString();

                AWSMobileClient.getInstance().confirmSignUp(username, confirmationCode, new Callback<SignUpResult>() {
                    @Override
                    public void onResult(SignUpResult signUpResult) {
                        runOnUiThread(() -> {
                            showToastLong("Confirmation successful!");

                            // Redirect to LoginActivity after successful confirmation
                            Intent intent = new Intent(ConfirmActivity.this, LoginActivity.class);
                            startActivity(intent);
                            finish(); // Optional: Call finish() if you don't want to keep ConfirmActivity in the back stack
                        });
                    }

                    @Override
                    public void onError(Exception e) {
                        runOnUiThread(() -> showToastLong("Confirmation failed: " + e.getMessage()));
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
