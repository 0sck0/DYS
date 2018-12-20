package com.koreatech.dys.dys;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBclass extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "DYS.db";
    private static final int DATABASE_VERSION = 1;

    public DBclass(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL("CREATE TABLE reminder (num INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT UNIQUE," +
                "memo TEXT, end_date TEXT);");
        db.execSQL("CREATE TABLE todo (num INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT UNIQUE," +
                " start_date TEXT, end_date TEXT, alarm TEXT, content TEXT);");
        db.execSQL("CREATE TABLE study_planner (date TEXT PRIMARY KEY, title TEXT UNIQUE," +
                "start_time TEXT, end_time TEXT, alarm TEXT, content TEXT);");
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        db.execSQL("DROP TABLE IF EXISTS reminder");
        db.execSQL("DROP TABLE IF EXISTS todo");
        db.execSQL("DROP TABLE IF EXISTS study_planner");
        onCreate(db);
    }
}
