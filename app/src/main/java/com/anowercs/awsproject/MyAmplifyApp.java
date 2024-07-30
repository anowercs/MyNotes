package com.anowercs.awsproject;


import android.app.Application;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin;
import com.amplifyframework.auth.options.AuthSignUpOptions;
import com.amplifyframework.auth.result.AuthSignUpResult;
import android.app.Application;
import android.util.Log;
import android.app.Application;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.auth.*;
import com.amplifyframework.AmplifyException;
import com.amplifyframework.core.Amplify;
import android.app.Application;
import android.util.Log;


public class MyAmplifyApp extends Application {

   /* @Override
    public void onCreate() {
        super.onCreate();
        try {
            Amplify.addPlugin(new AWSCognitoAuthPlugin());
            Amplify.configure(getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/


    @Override
    public void onCreate() {
        super.onCreate();
        AWSMobileClient.getInstance().initialize(this, new Callback<UserStateDetails>() {
            @Override
            public void onResult(UserStateDetails userStateDetails) {
                Log.i("INIT", "AWSMobileClient initialized. User State is " + userStateDetails.getUserState());
            }

            @Override
            public void onError(Exception e) {
                Log.e("INIT", "Initialization error.", e);
            }
        });
    }
}
