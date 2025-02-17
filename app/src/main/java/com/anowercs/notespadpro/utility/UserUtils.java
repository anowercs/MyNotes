package com.anowercs.notespadpro.utility;

import android.content.Context;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GetDetailsHandler;
import com.amazonaws.regions.Regions;

import java.util.Map;

public class UserUtils {

    public static void fetchUserDetails(Context context, OnUserDetailsFetchedListener listener) {
        AWSMobileClient.getInstance().initialize(context, new Callback<UserStateDetails>() {
            @Override
            public void onResult(UserStateDetails result) {
                CognitoUserPool userPool = new CognitoUserPool(
                        context,
                        "eu-north-1_A19IVNUgz",
                        "5k6ba23sp51v5t3qest4e8s200",
                        null,
                        Regions.EU_NORTH_1);
                CognitoUser user = userPool.getCurrentUser();

                user.getDetailsInBackground(new GetDetailsHandler() {
                    @Override
                    public void onSuccess(CognitoUserDetails cognitoUserDetails) {
                        Map<String, String> attributes = cognitoUserDetails.getAttributes().getAttributes();
                        listener.onUserDetailsFetched(attributes);
                    }

                    @Override
                    public void onFailure(Exception exception) {
                        listener.onError(exception);
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                listener.onError(e);
            }
        });
    }

    public interface OnUserDetailsFetchedListener {
        void onUserDetailsFetched(Map<String, String> attributes);
        void onError(Exception exception);
    }

}