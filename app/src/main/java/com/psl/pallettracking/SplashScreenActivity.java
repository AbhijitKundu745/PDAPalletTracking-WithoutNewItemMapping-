package com.psl.pallettracking;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.appcompat.app.AppCompatActivity;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();

        // Set the layout for splash screen
        setContentView(R.layout.activity_splash_screen);

        // Find the splash screen logo view in the layout (make sure you have an ImageView with this id in your layout)
        final View animeImage = findViewById(R.id.splash_logo);

        // Start the zoom animation
        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.zoom);
        animeImage.startAnimation(animation);

        // Set a listener to start scale-up animation after fade-in ends
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                // Optional: Do something when fade-in starts
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                                // Start the main activity
                                Intent intent = new Intent(SplashScreenActivity.this, LoginActivity.class);
                                startActivity(intent);
                                finish(); // Close the splash screen activity
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // Not used
            }
        });
    }
}