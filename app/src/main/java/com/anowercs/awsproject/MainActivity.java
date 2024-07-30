package com.anowercs.awsproject;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import androidx.activity.EdgeToEdge;

public class MainActivity extends AppCompatActivity {

    VideoView videoView;
    TextView tvDisplay;
    Button decrease, reset, increase;

    int count = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        videoView = findViewById(R.id.videoView);

        // Set the video path
        Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.walpaper);
        videoView.setVideoURI(videoUri);

        // Start the video and loop it
        videoView.setOnPreparedListener(mp -> {
            mp.setLooping(true);
            videoView.start();
        });

        tvDisplay = findViewById(R.id.tvDisplay);
        decrease = findViewById(R.id.decrease);
        reset = findViewById(R.id.reset);
        increase = findViewById(R.id.addCount);

        increase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                count++;
                tvDisplay.setText(""+count);
                if(count == 50){
                    Toast.makeText(MainActivity.this,"50 times ", Toast.LENGTH_LONG).show();

                }
            }
        });

        decrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                count--;
                tvDisplay.setText(""+count);
            }
        });
        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                count = 0;
                tvDisplay.setText(""+count);
            }
        });


    }
}