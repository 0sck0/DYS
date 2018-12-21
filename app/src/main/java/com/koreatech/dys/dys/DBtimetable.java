package com.koreatech.dys.dys;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBtimetable extends SQLiteOpenHelper {
    private static final String DBName = "timetable.db";
    private static final int DBVer = 2;

    public DBtimetable(Context context) {
        super(context, DBName, null, DBVer);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // 해당 학교 목록 데이터를 다운받으면 이 테이블에 저장한다.
        db.execSQL("CREATE TABLE course ( _id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "title TEXT, " +        // 과목명
                "professor TEXT," +     // 교수명
                "class TEXT," +         // 강의실
                "day TEXT, " +          // 요일
                "start TEXT, " +        // 시작시간
                "finish TEXT, " +       // 종료시간
                "color TEXT" +          // 색상
                ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS course");
        onCreate(db);
    }

}
