package com.floca.daniel.lostintheworld;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ScoreActivity extends AppCompatActivity {

    int score;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score);

        Intent mIntent = getIntent();
        score = mIntent.getIntExtra("score", 0);

        ProgressBar p = findViewById(R.id.progressBarScore);
        p.setMax(5);
        p.setProgress(score);

        TextView t = findViewById(R.id.scoreText);
        t.setText("Your score is: " + score + "/5");
    }
}
