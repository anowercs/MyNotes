/*
package com.anowercs.awsproject.AWSAmplify;

import android.app.Application;
import android.util.Log;
import java.util.concurrent.TimeUnit;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.storage.s3.AWSS3StoragePlugin;
import com.anowercs.awsproject.database.DynamoDBHelper;

public class MyAmplifyApp extends Application {
    private static DynamoDBMapper dynamoDBMapper;
    private static DynamoDBHelper dbHelper;

    public static final String S3_REGION = "eu-north-1";  // Change to public or use a getter
    public static final String S3_BUCKET_NAME = "notespadpro";

    @Override
    public void onCreate() {
        super.onCreate();

        AWSMobileClient.getInstance().initialize(getApplicationContext(), new Callback<UserStateDetails>() {
            @Override
            public void onResult(UserStateDetails result) {
                Log.i("AWS", "Initialization successful: " + result.getUserState());
                initializeAWSComponents();
            }

            @Override
            public void onError(Exception e) {
                Log.e("AWS", "Initialization error", e);
            }
        });
    }

    private void initializeAWSComponents() {
        try {
            // Initialize DynamoDB client
            AmazonDynamoDBClient dynamoDBClient = new AmazonDynamoDBClient(AWSMobileClient.getInstance());
            dynamoDBClient.setRegion(Region.getRegion(Regions.fromName(S3_REGION)));

            dynamoDBMapper = DynamoDBMapper.builder()
                    .dynamoDBClient(dynamoDBClient)
                    .build();

            dbHelper = new DynamoDBHelper(dynamoDBMapper);

            // Add Auth plugin first
            Amplify.addPlugin(new AWSCognitoAuthPlugin());
            // Then add Storage plugin
            Amplify.addPlugin(new AWSS3StoragePlugin());
            // Add this line back
            Amplify.configure(getApplicationContext());

            Log.i("AWS", "All AWS services initialized successfully!");
        } catch (Exception e) {
            Log.e("AWS", "Error initializing AWS components", e);
        }
    }

    public static DynamoDBMapper getDynamoDBMapper() {
        return dynamoDBMapper;
    }

    public static DynamoDBHelper getDbHelper() {
        return dbHelper;
    }
}
*/


package com.anowercs.notespadpro.AWSAmplify;

import android.app.Application;
import android.util.Log;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amplifyframework.AmplifyException;
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.storage.s3.AWSS3StoragePlugin;
import com.anowercs.notespadpro.database.DynamoDBHelper;

public class MyAmplifyApp extends Application {
    private static volatile boolean isAmplifyInitialized = false;
    private static final Object initLock = new Object();
    private static DynamoDBMapper dynamoDBMapper;
    private static DynamoDBHelper dbHelper;
    public static final String S3_REGION = "eu-north-1";
    public static final String S3_BUCKET_NAME = "notespadpro";

    @Override
    public void onCreate() {
        super.onCreate();

        try {
            synchronized (initLock) {
                if (!isAmplifyInitialized) {
                    // Add plugins
                    Amplify.addPlugin(new AWSCognitoAuthPlugin());
                    Amplify.addPlugin(new AWSS3StoragePlugin());
                    // Configure Amplify
                    Amplify.configure(getApplicationContext());
                    isAmplifyInitialized = true;
                    initLock.notifyAll();
                    Log.i("AmplifyInit", "Initialized Amplify directly");
                }
            }

            // Then initialize AWSMobileClient
            AWSMobileClient.getInstance().initialize(getApplicationContext(), new Callback<UserStateDetails>() {
                @Override
                public void onResult(UserStateDetails result) {
                    Log.i("AWS", "AWSMobileClient Initialization successful: " + result.getUserState());
                    initializeAWSComponents();
                }

                @Override
                public void onError(Exception e) {
                    Log.e("AWS", "AWSMobileClient Initialization error", e);
                }
            });

        } catch (AmplifyException e) {
            Log.e("AmplifyInit", "Could not initialize Amplify", e);
            // Set initialization flag to false if it fails
            synchronized (initLock) {
                isAmplifyInitialized = false;
                initLock.notifyAll();
            }
        }
    }

    public static boolean waitForAmplifyInit(long timeoutMillis) {
        if (isAmplifyInitialized) return true;

        synchronized (initLock) {
            if (!isAmplifyInitialized) {
                try {
                    initLock.wait(timeoutMillis);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return false;
                }
            }
        }
        return isAmplifyInitialized;
    }
    private void initializeAWSComponents() {
        try {
            // Initialize DynamoDB client
            AmazonDynamoDBClient dynamoDBClient = new AmazonDynamoDBClient(AWSMobileClient.getInstance());
            dynamoDBClient.setRegion(Region.getRegion(Regions.fromName(S3_REGION)));

            dynamoDBMapper = DynamoDBMapper.builder()
                    .dynamoDBClient(dynamoDBClient)
                    .build();

            dbHelper = new DynamoDBHelper(dynamoDBMapper);

            Log.i("AWS", "All AWS services initialized successfully!");
        } catch (Exception e) {
            Log.e("AWS", "Error initializing AWS components", e);
        }
    }

    public static DynamoDBMapper getDynamoDBMapper() {
        return dynamoDBMapper;
    }

    public static DynamoDBHelper getDbHelper() {
        return dbHelper;
    }
}