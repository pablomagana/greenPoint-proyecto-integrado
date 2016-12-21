package com.app.greenpoint;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

public class SplashActivity extends Activity {
    TextView green;
    TextView point;
    ImageView logo;
    Animation animGreen;
    Animation animPoint;
    Animation animLogo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);

        green = (TextView) findViewById(R.id.ico_green);
        point = (TextView) findViewById(R.id.ico_point);
        logo = (ImageView) findViewById(R.id.ico_logo);


        animGreen = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.splah1);
        animPoint = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.splash2);
        animLogo = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.splash3);

        green.startAnimation(animGreen);
        green.setVisibility(View.VISIBLE);
        animGreen.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                point.startAnimation(animPoint);
                point.setVisibility(View.VISIBLE);
                logo.startAnimation(animLogo);
                logo.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        animLogo.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                finish();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }
}
