package com.anowercs.notespadpro.services;


import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.anowercs.notespadpro.activities.FitnessActivity.FitnessDataItem;

public class FitnessUploadService extends Service {
    private static final int UPLOAD_INTERVAL = 15 * 60 * 1000; // 15 minutes
    private Handler handler = new Handler();
    private DynamoDBMapper dynamoDBMapper;

    private Runnable uploadRunnable = new Runnable() {
        @Override
        public void run() {
            uploadFitnessData();
            handler.postDelayed(this, UPLOAD_INTERVAL);
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        setupDynamoDB();
        handler.post(uploadRunnable);
        return START_STICKY;
    }

    private void setupDynamoDB() {
        AmazonDynamoDBClient dynamoDBClient = new AmazonDynamoDBClient(AWSMobileClient.getInstance());
        dynamoDBClient.setRegion(Region.getRegion(Regions.EU_NORTH_1));
        this.dynamoDBMapper = DynamoDBMapper.builder()
                .dynamoDBClient(dynamoDBClient)
                .build();
    }

    private void uploadFitnessData() {
        // Get current steps and distance (implement your step counting logic here)
        int currentSteps = getCurrentSteps();
        float currentDistance = currentSteps * 0.7f; // approximate distance

        FitnessDataItem item = new FitnessDataItem();
        item.setUserId(AWSMobileClient.getInstance().getUsername());
        item.setTimestamp(System.currentTimeMillis());
        item.setSteps(currentSteps);
        item.setDistance(currentDistance);

        new Thread(() -> {
            try {
                dynamoDBMapper.save(item);
                Log.d("UPLOAD", "Data uploaded successfully: Steps=" + currentSteps);
            } catch (Exception e) {
                Log.e("UPLOAD", "Error uploading data: " + e.getMessage());
            }
        }).start();
    }

    private int getCurrentSteps() {
        // Implement step counting logic here
        // For now, returning random number for testing
        return (int) (Math.random() * 1000);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}