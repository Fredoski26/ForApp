package com.fredrick.forapp;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

public class TheApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
//         enable firebase offline
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        //after enabling it , all data loaded  will be able to see offline
    }
}
