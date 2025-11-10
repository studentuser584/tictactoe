package com.example.tictactoe;

import android.app.Application;

public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        try {
            GameDatabaseHelper helper = GameDatabaseHelper.getInstance(this);
            helper.ensureOpen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
