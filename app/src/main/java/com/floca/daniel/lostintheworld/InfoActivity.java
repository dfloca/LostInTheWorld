package com.floca.daniel.lostintheworld;

/*
    Authors:    Daniel Floca, Garrett Fraser
    Purpose:    Simple info activity
    Date:       2018-11-21
 */

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class InfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
    }

    public void onCloseClick(View view) {
        this.finish();
    }
}
