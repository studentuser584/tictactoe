package com.example.tictactoe;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class GameDatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "tictactoe.db";
    private static final int DB_VERSION = 1;

    public static final String TABLE_NAME = "games";
    public static final String COL_ID = "id";
    public static final String COL_USERNAME = "username";
    public static final String COL_ROUND = "round";
    public static final String COL_WINNER = "winner";
    public static final String COL_MODE = "mode";
    public static final String COL_TIMESTAMP = "timestamp";

    private static GameDatabaseHelper instance;
    private SQLiteDatabase openWritableDatabase;

    private GameDatabaseHelper(Context context) {
        super(context.getApplicationContext(), DB_NAME, null, DB_VERSION);
    }

    public static synchronized GameDatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new GameDatabaseHelper(context);
        }
        return instance;
    }

    public synchronized void ensureOpen() {
        if (openWritableDatabase == null || !openWritableDatabase.isOpen()) {
            openWritableDatabase = getWritableDatabase();
        }
    }

    public synchronized void safeClose() {
        if (openWritableDatabase != null && openWritableDatabase.isOpen()) {
            try { openWritableDatabase.close(); } catch (Exception ignored) {}
            openWritableDatabase = null;
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_USERNAME + " TEXT NOT NULL, " +
                COL_ROUND + " INTEGER NOT NULL, " +
                COL_WINNER + " TEXT NOT NULL, " +
                COL_MODE + " TEXT NOT NULL, " +
                COL_TIMESTAMP + " TEXT NOT NULL" +
                ")";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVer, int newVer) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    /**
     * Returns the maximum stored round number for the given username (0 if none).
     */
    public synchronized int getMaxRound(String username) {
        int max = 0;
        Cursor cursor = null;
        try {
            SQLiteDatabase db = (openWritableDatabase != null && openWritableDatabase.isOpen())
                    ? openWritableDatabase : getReadableDatabase();
            cursor = db.rawQuery(
                    "SELECT MAX(" + COL_ROUND + ") FROM " + TABLE_NAME + " WHERE " + COL_USERNAME + "=?",
                    new String[]{username});
            if (cursor != null && cursor.moveToFirst()) {
                max = cursor.isNull(0) ? 0 : cursor.getInt(0);
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return max;
    }

    /**
     * Insert a completed game record.
     */
    public synchronized void insertGame(String username, int round, String winner, String mode, String timestamp) {
        SQLiteDatabase db = (openWritableDatabase != null && openWritableDatabase.isOpen())
                ? openWritableDatabase : getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_USERNAME, username);
        values.put(COL_ROUND, round);
        values.put(COL_WINNER, winner);
        values.put(COL_MODE, mode);
        values.put(COL_TIMESTAMP, timestamp);
        db.insert(TABLE_NAME, null, values);
    }

    /**
     * Delete all saved games for a given username.
     * (Optional; previously used — not needed to reset round now.)
     */
    public synchronized void deleteGamesForUser(String username) {
        SQLiteDatabase db = (openWritableDatabase != null && openWritableDatabase.isOpen())
                ? openWritableDatabase : getWritableDatabase();
        db.delete(TABLE_NAME, COL_USERNAME + "=?", new String[]{username});
    }
}
