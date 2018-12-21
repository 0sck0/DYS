package com.koreatech.dys.dys;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class TimetableSelected extends AppCompatActivity {
    // 인텐트
    static final int GET_DAY = 1;
    static final int GET_START_TIME = 2;
    static final int GET_END_TIME = 3;
    static final int GET_COLOR = 4;
    int requestCode;

    // 입력 뷰
    EditText subject;
    EditText professor;
    EditText classroom;

    // 요일 및 시간
    TextView weekOfDay;
    TextView startTime;
    TextView endTime;

    // 색상
    TextView backColor;
    int color;

    // DB
    private DBtimetable dbHelper;
    private SQLiteDatabase db;
    //private Cursor c;
    DBThread dbThread;
    boolean conditionToAdd = false;

    // Data
    String s_subject;
    String s_professor;
    String s_classroom;
    String s_weekOfDay;
    String s_startTime;
    String s_endTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timetable_selected);
        this.setTitle(R.string.title_timetable_selected);

        // 뷰 연결
        subject = (EditText) findViewById(R.id.subject);
        professor = (EditText) findViewById(R.id.professor);
        classroom = (EditText) findViewById(R.id.classroom);

        weekOfDay = (TextView) findViewById(R.id.weekOfDay);
        startTime = (TextView) findViewById(R.id.startTime);
        endTime = (TextView) findViewById(R.id.endTime);

        backColor = (TextView) findViewById(R.id.color);

        // 메뉴 왼쪽에 back button 넣기
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // 이미지 변경
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_keyboard_arrow_left_black_24dp);

        subject.setFilters(new InputFilter[] { new InputFilter.LengthFilter(20) });
        professor.setFilters(new InputFilter[] { new InputFilter.LengthFilter(20) });
        classroom.setFilters(new InputFilter[] { new InputFilter.LengthFilter(20) });

        // 기본 설정
        weekOfDay.setText("월요일");
        startTime.setText("8시");
        endTime.setText("9시");
        backColor.setBackgroundColor(getResources().getColor(R.color.color_table_1_light));
        color = R.color.color_table_1_light;

        // DB
        dbHelper = new DBtimetable(this);

        try {
            db = dbHelper.getWritableDatabase();
        } catch(SQLiteException e) {
            db = dbHelper.getReadableDatabase();
        }

        dbThread = new DBThread();
    }

    // Actionbar_Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_timetable_selected, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            // back button
            case android.R.id.home:
                finish();
                return true;
            // complete button
            case R.id.menu_complete:

                s_subject = subject.getText().toString();
                s_professor = professor.getText().toString();
                s_classroom = classroom.getText().toString();
                s_weekOfDay = weekOfDay.getText().toString();
                s_startTime = startTime.getText().toString();
                s_endTime = endTime.getText().toString();

                if(checkConditionToAdd()) {
                    dbThread.start();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    //Toast.makeText(getApplicationContext(), "추가할 수 없습니다.", Toast.LENGTH_SHORT).show();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void select(View view) {
        String[] a = {"월요일", "화요일", "수요일", "목요일", "금요일"};
        String[] b = {"8시", "9시", "10시", "11시", "12시", "13시", "14시", "15시", "16시", "17시", "18시", "19시", "20시"};

        Intent intent = new Intent(this, TimetableSelectedListView.class);

        if(view.getId() == R.id.color) {
            startActivityForResult(new Intent(this, TimetableSelectedColor.class), GET_COLOR);
        } else {
            switch (view.getId()) {
                case R.id.weekOfDay:
                    intent.putExtra("day", a);
                    requestCode = GET_DAY;
                    break;
                case R.id.startTime:
                    intent.putExtra("time", b);
                    requestCode = GET_START_TIME;
                    break;
                case R.id.endTime:
                    intent.putExtra("time", b);
                    requestCode = GET_END_TIME;
                    break;
                default:
                    break;
            }

            intent.putExtra("requestCode", requestCode);
            startActivityForResult(intent, requestCode);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        String result;
        int resultColor;

        if(resultCode == RESULT_OK) {
            switch (requestCode) {
                case 1:
                    result = data.getStringExtra("result");
                    weekOfDay.setText(result);
                    break;
                case 2:
                    result = data.getStringExtra("result");
                    startTime.setText(result);
                    break;
                case 3:
                    result = data.getStringExtra("result");
                    endTime.setText(result);
                    break;
                case 4:
                    resultColor = data.getIntExtra("result", 1);
                    color = getColorSelected(resultColor);
                    backColor.setBackgroundColor(getResources().getColor(color));
                    break;
                default:
                    break;
            }
            //hexColor = String.format("%X", color);
        }
    }

    class DBThread extends Thread {
        public void run() {
            // Insert 실행
            Log.d("Subject", s_subject);
            Log.d("StartTime", s_startTime);
            Log.d("EndTime", s_endTime);

            db.execSQL("INSERT INTO course VALUES(null, '" + s_subject + "', '" + s_professor + "', '"
                    +  s_classroom + "', '" + s_weekOfDay + "', '" + s_startTime + "', '"
                    + s_endTime + "', '" + color + "');" );
        }
    }


    private boolean checkConditionToAdd() {
        conditionToAdd = false;

        // 모든 입력 값들이 모두 채워져 있으면
        if(!s_subject.equals("") && !s_professor.equals("") && !s_classroom.equals("")
                && !s_startTime.equals("") && !s_endTime.equals("") && !s_weekOfDay.equals("") && color != 0) {

            // 시작 시간이 종료 시간보다 작으면
            if(Integer.valueOf(s_startTime.split("시")[0]) < Integer.valueOf(s_endTime.split("시")[0])) {
                conditionToAdd = true;
            } else {
                Toast.makeText(getApplicationContext(), "시간이 올바르지 않습니다.", Toast.LENGTH_SHORT).show();
                conditionToAdd = false;
            }
        } else {
            Toast.makeText(getApplicationContext(), "입력해야 되는 곳을 채워주세요.", Toast.LENGTH_SHORT).show();
            conditionToAdd = false;
        }

        return conditionToAdd;
    }

    public int getColorSelected(int color) {
        switch (color) {
            case 1:
                return R.color.color_table_1_light;
            case 2:
                return R.color.color_table_2_light;
            case 3:
                return R.color.color_table_3_light;
            case 4:
                return R.color.color_table_4_light;
            case 5:
                return R.color.color_table_5_light;
            case 6:
                return R.color.color_table_6_light;
            case 7:
                return R.color.color_table_7_light;
            case 8:
                return R.color.color_table_8_light;
            default:
                break;
        }
        return R.color.color_table_1_light;
    }

}