package com.anowercs.notespadpro.activities;

import static android.content.ContentValues.TAG;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.results.SignInResult;

import android.content.Intent;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.mobile.client.results.SignInState;
import com.amazonaws.mobile.client.results.SignUpResult;
import com.amazonaws.mobile.client.results.UserCodeDeliveryDetails;
import com.anowercs.notespadpro.R;

public class LoginActivity extends AppCompatActivity {

    private EditText EDT_usernameField, EDT_login_passwordField;
    private Button BTN_loginButton;
    TextView TV_signupLink, TV_forgot_password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        //################################ Initialize views  ####################################
        EDT_usernameField = findViewById(R.id.EDT_usernameField);
        EDT_login_passwordField = findViewById(R.id.EDT_login_passwordField);
        BTN_loginButton = findViewById(R.id.BTN_loginButton);
        TV_signupLink = findViewById(R.id.TV_signupLink);
        TV_forgot_password = findViewById(R.id.TV_forgot_password);
        //######################################################################################


        BTN_loginButton.setOnClickListener(v -> {
            String email = EDT_usernameField.getText().toString().trim();
            String password = EDT_login_passwordField.getText().toString().trim();

            if (validateInputs(email, password)) {
                loginUser(email, password);
            } else {
                BTN_loginButton.setEnabled(true);
            }
        });

        TV_signupLink.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });

        TV_forgot_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));

            }
        });




    }

    private boolean validateInputs(String email, String password) {
        if (email.isEmpty()) {
            EDT_usernameField.setError("Email cannot be empty");
            return false;
        }
        if (password.isEmpty()) {
            EDT_login_passwordField.setError("Password cannot be empty");
            return false;
        }
        return true;
    }

    private void loginUser(String email, String password) {
        AWSMobileClient.getInstance().signIn(email, password, null, new Callback<SignInResult>() {
            @Override
            public void onResult(SignInResult signInResult) {
                runOnUiThread(() -> {
                    BTN_loginButton.setEnabled(true);
                    if (signInResult.getSignInState() == SignInState.DONE) {
                        handleSuccessfulLogin(email);
                    } else {
                        Toast.makeText(LoginActivity.this,
                                "Login failed: Invalid credentials",
                                Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> {
                    BTN_loginButton.setEnabled(true);
                    Log.e(TAG, "Login error", e);
                    if (e.getMessage().contains("User is not confirmed")) {
                        showConfirmationDialog(email);
                    } else {
                        Toast.makeText(LoginActivity.this,
                                "Login error: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private void showConfirmationDialog(String email) {

        new AlertDialog.Builder(this)
                .setTitle("Confirm Email")
                .setMessage("Your account is not confirmed. Would you like to resend the confirmation email?")
                .setPositiveButton("Resend OTP", (dialog, which) -> resendConfirmationOTP(email))
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void resendConfirmationOTP(String email) {


       AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Create an EditText for OTP input
        builder.setTitle("Verify Email");
        builder.setMessage("A verification code will be sent to: " + email + "\nDo you want to proceed?");


        // Set buttons
        // Set buttons
        builder.setPositiveButton("Confirm", (dialog, which) -> {
            // Send OTP to the email when user confirms
            sendVerificationCode(email);
            showOTPInputDialog(email);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> {
            // Dismiss the dialog when user cancels
            dialog.dismiss();
        });

        // Create and show the dialog
       AlertDialog dialog = builder.create();
        dialog.show();


    }

    private void sendVerificationCode(String email) {
        AWSMobileClient.getInstance().resendSignUp(email, new Callback<SignUpResult>() {
            @Override
            public void onResult(SignUpResult signUpResult) {
                Log.i(TAG, "A verification code has been sent via" +
                        signUpResult.getUserCodeDeliveryDetails().getDeliveryMedium()
                        + " at " +
                        signUpResult.getUserCodeDeliveryDetails().getDestination());
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, String.valueOf(e));
            }
        });
    }


    private void showOTPInputDialog(String email) {
        // Create AlertDialog.Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter OTP");

        // Create an EditText for OTP input
        final EditText inputresentOTP = new EditText(this);
        inputresentOTP.setInputType(InputType.TYPE_CLASS_NUMBER); // Only numeric input
        builder.setView(inputresentOTP);


        builder.setPositiveButton("Confirm", (dialog, which) -> {
            String otpInput = inputresentOTP.getText().toString().trim();
            if (!otpInput.isEmpty()) {
                ProgressDialog progressDialog = new ProgressDialog(this);
                progressDialog.setMessage("Verifying OTP...");
                progressDialog.setCancelable(false);
                progressDialog.show();

                try {
                    confirmUser(email, otpInput, new Callback<SignUpResult>() {
                        @Override
                        public void onResult(SignUpResult result) {
                            if (result.getConfirmationState()) {
                                handleSuccessfulLogin(email);
                                progressDialog.dismiss();

                            }
                        }

                        @Override
                        public void onError(Exception e) {
                        }
                    });

                    progressDialog.dismiss();
                } catch (Exception e) {
                    progressDialog.dismiss();
                    // Log error or show user-friendly error message
                    Toast.makeText(this, "Verification failed", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Please enter OTP", Toast.LENGTH_SHORT).show();
            }
        });

        // Create and show the dialog
        AlertDialog dialog = builder.create();
        dialog.show();


    }
    private void confirmUser(String email, String otp, Callback<SignUpResult> callback) {
        showLoadingDialog("Confirming account...");

        AWSMobileClient.getInstance().confirmSignUp(email, otp, new Callback<SignUpResult>() {
            @Override
            public void onResult(final SignUpResult signUpResult) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "Sign-up callback state: " + signUpResult.getConfirmationState());
                        if (!signUpResult.getConfirmationState()) {
                            final UserCodeDeliveryDetails details = signUpResult.getUserCodeDeliveryDetails();
                            Log.d(TAG, "Destination: " + details.getDestination());
                            Log.d(TAG, "DeliveryMedium: " + details.getDeliveryMedium());
                            Toast.makeText(LoginActivity.this, "Confirm sign-up with: " + details.getDestination(), Toast.LENGTH_SHORT).show();

                        } else {
                            Toast.makeText(LoginActivity.this, "Verification  done: ", Toast.LENGTH_SHORT).show();

                            dismissLoadingDialog();
                        }
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Confirm sign-up error", e);
            }
        });
    }

    private void handleSuccessfulLogin(String email) {


        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private ProgressDialog progressDialog;

    private void showLoadingDialog(String message) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setCancelable(false);
        }
        progressDialog.setMessage(message);
        progressDialog.show();
    }

    private void dismissLoadingDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

}
