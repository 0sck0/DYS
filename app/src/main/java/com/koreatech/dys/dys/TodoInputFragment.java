package com.koreatech.dys.dys;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;
import java.util.GregorianCalendar;

import static android.app.Activity.RESULT_CANCELED;
import static android.content.Context.ALARM_SERVICE;

public class TodoInputFragment extends Fragment {
    private String total_data;
    private boolean isModify;
    //날짜 데이터를 화면에 표시하는 뷰, 버튼을 눌렀을때 DatePickerDialog를 호출하는 버튼
    private TextView sDateDisplay, eDateDisplay;
    private Button sPickDate, ePickDate;
    //알림 스위치 뷰와 그 상태를 화면에 출력하는 뷰
    private TextView ring_switch_on_text;
    private Switch ring_switch;
    //현재 저장되어 있는 날짜 데이터 변수들이다..
    private int shour, ehour;
    private int sminute, eminute;
    private int sYear, eYear;
    private int sMonth, eMonth;
    private int sDay, eDay;
    private DBclass todoDB;
    private SQLiteDatabase db;
    private Cursor cursor;
    //제목, 내용의 뷰
    private EditText title_todo;
    private EditText content_todo;
    //현재 액티비티를 취소하거나 저장하는 버튼
    private Button cancel_todo;
    private Button save_todo;
    private boolean fin = true;
    private String recieve_title;
    private int switch1 = 0;
    static final int TIME_DIALOG_ID = 1;
    static final int DATE_DIALOG_ID = 0;
    private AlarmManager alarm_manager;
    private PendingIntent pendingIntent;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public TodoInputFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static TodoInputFragment newInstance(String param1, String param2) {
        TodoInputFragment fragment = new TodoInputFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        setHasOptionsMenu(true);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_todo_input, container, false);

        alarm_manager = (AlarmManager) getActivity().getSystemService(ALARM_SERVICE);
        final Intent my_intent = new Intent(getActivity().getApplicationContext(), Alarm_Reciver.class);
        todoDB = new DBclass(getActivity().getApplicationContext());
        try {
            db = todoDB.getWritableDatabase();
            db = todoDB.getReadableDatabase();
        } catch (SQLiteException ex) {
            db = todoDB.getReadableDatabase();
        }
        Intent intent  = getActivity().getIntent();
        //텍스트로 선택한 일정 보여주는 변수
        sDateDisplay = (TextView) view.findViewById(R.id.start_date_todo);
        eDateDisplay = (TextView) view.findViewById(R.id.end_date_todo);

        //날짜 선택하는 버튼
        sPickDate = (Button) view.findViewById(R.id.start_datepicker);
        ePickDate = (Button) view.findViewById(R.id.end_datepicker);
        //알림 스위치와 스위치 상태를 표시해주는 텍스트뷰
        ring_switch = (Switch) view.findViewById(R.id.switch_todo);
        ring_switch_on_text = (TextView) view.findViewById(R.id.switch_condition_todo);
        title_todo = (EditText) view.findViewById(R.id.title_todo);
        content_todo = (EditText) view.findViewById((R.id.content_todo));
        //현재 액티비티를 취소하거나 저장하는 버튼 //온클릭 리스너 만들어주고, 거기에는 인텐트 처리와 파일처리 코드를 넣을 예정.
        cancel_todo = (Button) view.findViewById(R.id.cancel_todo);
        save_todo = (Button) view.findViewById(R.id.save_todo);
        //날짜 변경 버튼 이벤트 리스너
        sPickDate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                switch1 = 0;
                getActivity().showDialog(DATE_DIALOG_ID);
            }
        });
        ePickDate.setOnClickListener((new View.OnClickListener(){
            public void onClick(View v)
            {
                switch1 = 1;
                getActivity().showDialog(DATE_DIALOG_ID);
            }
        }));
        //스위치 버튼 이벤트 리스너
        ring_switch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ring_switch_checkState();
            }
        });
        //취소 버튼과 저장 버튼 이벤트 리스너 설정
        cancel_todo = (Button) view.findViewById(R.id.cancel_todo);
        cancel_todo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().setResult(RESULT_CANCELED);
//                getActivity().finish();
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                fragmentManager.beginTransaction().remove(TodoInputFragment.this).commit();
                fragmentManager.popBackStack();
            }
        });
        save_todo = (Button) view.findViewById(R.id.save_todo);
        save_todo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!check_data()) {
                    String cur_title = title_todo.getText().toString();
                    String content = content_todo.getText().toString();
                    String start_date = sDateDisplay.getText().toString();
                    String end_date = eDateDisplay.getText().toString();
                    String ring_switch = ring_switch_on_text.getText().toString();
                    if (isModify) {
                        //수정하는경우

                        if(title_todo.getText().toString().equals(recieve_title))
                        {
                            if(!start_date.equals(cursor.getString(1)))
                            {
                                stopAlarm();
                                goAlarm();
                            }
                            db.execSQL("UPDATE todo " +
                                    "SET title = '" + cur_title +
                                    "', start_date = '" + start_date +
                                    "', end_date = '" + end_date +
                                    "', alarm = '" + ring_switch +
                                    "', content = '" + content +
                                    "' WHERE title = '" + recieve_title + "';");
                        }
                        else
                        {
                            if(!start_date.equals(cursor.getString(1)))
                            {
                                stopAlarm();
                                goAlarm();
                            }
                            db.execSQL("DELETE FROM todo " +
                                    "WHERE title = '" + recieve_title + "';");
                            db.execSQL("INSERT INTO todo " +
                                    "VALUES (null, '" + cur_title + "', '" + start_date + "', '"
                                    + end_date + "', '" + ring_switch + "', '" + content + "');");
                        }
//                        getActivity().finish();
                        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                        fragmentManager.beginTransaction().remove(TodoInputFragment.this).commit();
                        fragmentManager.popBackStack();
                    } else {  //수정이 아닌, 새로운 일정을 추가하는 경우는 데이터베이스에 추가
                        cursor = db.rawQuery("SELECT * FROM todo " +
                                "WHERE title='" + cur_title + "';", null);
                        // 반환된 커서에 ResultSets의 행의 개수가 0개일 경우
                        if(cursor.getCount() == 0) {
                            db.execSQL("INSERT INTO todo " +
                                    "VALUES (null, '" + cur_title + "', '" + start_date + "', '"
                                    + end_date + "', '" + ring_switch + "', '" + content + "');");
                            goAlarm();
//                            getActivity().finish();
                            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                            fragmentManager.beginTransaction().remove(TodoInputFragment.this).commit();
                            fragmentManager.popBackStack();
                        }
                        else
                        {
                            //메세지 박스 출력 -> 확인 취소, 이때 확인 누르면 덮어쓰기, 취소하면 그대로 액티비티에 머무름.
                            AlertDialog.Builder alert_confirm = new AlertDialog.Builder(getActivity().getApplicationContext());
                            alert_confirm.setMessage("동일한 제목의 일정이 존재합니다. 덮어 쓰시겠습니까?").setCancelable(false).setPositiveButton("덮어쓰기",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            String cur_title = title_todo.getText().toString();
                                            String content = content_todo.getText().toString();
                                            String start_date = sDateDisplay.getText().toString();
                                            String end_date = eDateDisplay.getText().toString();
                                            String ring_switch = ring_switch_on_text.getText().toString();
                                            if(!start_date.equals(cursor.getString(1)))
                                            {
                                                stopAlarm();
                                                goAlarm();
                                            }
                                            db.execSQL("UPDATE todo " +
                                                    "SET title = '" + cur_title +
                                                    "', start_date = '" + start_date +
                                                    "', end_date = '" + end_date +
                                                    "', alarm = '" + ring_switch +
                                                    "', content = '" + content +
                                                    "' WHERE title = '" + cur_title + "';");
                                            getActivity().finish();
                                        }
                                    }).setNegativeButton("취소",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            fin = false;
                                            return;
                                        }
                                    });
                            AlertDialog alert = alert_confirm.create();
                            alert.show();
                        }
                        if(fin == false)
                            return;
                    }
                }
                else
                    Toast.makeText(getActivity().getApplicationContext(), "입력을 제대로 해주십시오.", Toast.LENGTH_LONG).show();
            }
        });
        //새로운 일정을 생성하는건지, 기존 일정을 수정하는건지를 판단하는 코드
        if((isModify = intent.getBooleanExtra("isModify",false))) {
            recieve_title = intent.getStringExtra("title");
            cursor = db.rawQuery("SELECT * FROM todo " +
                    "WHERE title='" + recieve_title + "';", null);
            // 반환된 커서에 ResultSets의 행의 개수가 0개일 경우
            if(cursor.getCount() == 0) {
                Toast.makeText(getActivity().getApplicationContext(), "해당 이름이 없습니다", Toast.LENGTH_SHORT).show();
                return inflater.inflate(R.layout.fragment_todo_input, container, false);
            }
            cursor.moveToFirst();
            title_todo.setText(cursor.getString(1));
            sDateDisplay.setText(cursor.getString(2));
            eDateDisplay.setText(cursor.getString(3));
            content_todo.setText(cursor.getString(5));
            if(cursor.getString(4).equals("ON"))
                ring_switch.setChecked(true);
            else
                ring_switch.setChecked(false);
        }
        else
        {
            //처음 날짜 정할때 오늘 날짜로 초기화하는 함수
            set_date();
        }
        getActivity().setTitle("일정 입력");

        return view;
    }

    private boolean check_data() {
        boolean sw = false;
        if (title_todo.getText().toString().isEmpty()) {
            Toast.makeText(getActivity().getApplicationContext(), "제목를 입력해주십시오.", Toast.LENGTH_LONG).show();
            sw = true;
        }
        if (sDateDisplay.getText().toString().isEmpty()) {
            Toast.makeText(getActivity().getApplicationContext(), "시작 날짜를 입력해주십시오.", Toast.LENGTH_LONG).show();
            sw = true;
        }
        if (eDateDisplay.getText().toString().isEmpty()) {
            Toast.makeText(getActivity().getApplicationContext(), "종료 날짜를 입력해주십시오.", Toast.LENGTH_LONG).show();
            sw = true;
        }
        if (ring_switch_on_text.getText().toString().isEmpty()) {
            Toast.makeText(getActivity().getApplicationContext(), "알림을 설정해주십시오.", Toast.LENGTH_LONG).show();
            sw = true;
        }
        if (content_todo.getText().toString().isEmpty()) {
            Toast.makeText(getActivity().getApplicationContext(), "내용을 입력해주십시오.", Toast.LENGTH_LONG).show();
            sw = true;
        }
        return sw;
    }
    //처음 날짜 정할때 오늘 날짜로 초기화하는 코드.
    private void set_date()
    {

        Calendar c = Calendar.getInstance();

        sYear = c.get(Calendar.YEAR);
        sMonth = c.get(Calendar.MONTH);
        sDay = c.get(Calendar.DAY_OF_MONTH);
        shour = c.get(Calendar.HOUR_OF_DAY);
        sminute = c.get(Calendar.MINUTE);
        eYear = c.get(Calendar.YEAR);
        eMonth = c.get(Calendar.MONTH);
        eDay = c.get(Calendar.DAY_OF_MONTH);
        ehour = c.get(Calendar.HOUR_OF_DAY);
        eminute = c.get(Calendar.MINUTE);
    }
    //스위치 상태를 텍스트에 출력 + 알림해주는 기능 추가해야함

    private void ring_switch_checkState()
    {
        if(ring_switch.isChecked())
            ring_switch_on_text.setText("On");
        else
            ring_switch_on_text.setText("Off");
    }
    //위의 날짜들의 데이터를 텍스트뷰에 출력
    private void updateDate()
    {
        if(switch1 == 0)
        {
            sDateDisplay.setText(
                    new StringBuilder()
                            // Month is 0 based so add 1
                            .append(sYear).append(" ")
                            .append(sMonth + 1).append("/")
                            .append(sDay).append("/")
                            .append(pad(shour)).append(":")
                            .append(pad(sminute)));
            getActivity().showDialog(TIME_DIALOG_ID);
        }
        else
        {
            eDateDisplay.setText(
                    new StringBuilder()
                            // Month is 0 based so add 1
                            .append(eYear).append(" ")
                            .append(eMonth + 1).append("/")
                            .append(eDay).append("/")
                            .append(pad(ehour)).append(":")
                            .append(pad(eminute)));
            getActivity().showDialog(TIME_DIALOG_ID);
        }

    }


    //일의 자리 숫자를 01 02 03 이런식으로 문자열로 변환하여 주는 함수
    private static String pad(int c)
    {
        if (c >= 10)
            return String.valueOf(c);
        else
            return "0" + String.valueOf(c);
    }

    //날짜를 선택했을 때 그에 따른

    DatePickerDialog.OnDateSetListener mDateSetListener =
            new DatePickerDialog.OnDateSetListener() {

                public void onDateSet(DatePicker view, int year,
                                      int monthOfYear, int dayOfMonth) {
                    if(switch1 == 0)
                    {
                        sYear = year;
                        sMonth = monthOfYear;
                        sDay = dayOfMonth;
                    }
                    else
                    {
                        eYear = year;
                        eMonth = monthOfYear;
                        eDay = dayOfMonth;
                    }
                    updateDate();
                }
            };


    // 시간 선택
    TimePickerDialog.OnTimeSetListener mTimeSetListener =
            new TimePickerDialog.OnTimeSetListener() {
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    if(switch1 == 0) {
                        shour = hourOfDay;
                        sminute = minute;
                    }
                    else
                    {
                        ehour = hourOfDay;
                        eminute = minute;
                    }

                    updateDate();
                }
            };

    protected Dialog onCreateDialog(int id) {
        Log.v(Integer.toString(id), " : 1");

        switch (id) {
            case DATE_DIALOG_ID: {
                if (switch1 == 0)
                    return new DatePickerDialog(getActivity().getApplicationContext(),
                            mDateSetListener,
                            sYear, sMonth, sDay);
                else
                    return new DatePickerDialog(getActivity().getApplicationContext(),
                            mDateSetListener,
                            eYear, eMonth, eDay);
            }
            case TIME_DIALOG_ID: {
                if (switch1 == 0)
                    return new TimePickerDialog(getActivity().getApplicationContext(),
                            mTimeSetListener, shour, sminute, false);
                else
                    return new TimePickerDialog(getActivity().getApplicationContext(),
                            mTimeSetListener, ehour, eminute, false);
            }
        }
        return null;
    }
    private void goAlarm()
    {
        Intent my_intent = new Intent(getActivity().getApplicationContext(), Alarm_Reciver.class);
        my_intent.putExtra("state","alarm on");
        Calendar calendar = new GregorianCalendar();

        Log.d("cur_cal y, m, d, h, s", ":" + String.valueOf(eYear) +
                String.valueOf(eMonth) +
                String.valueOf(eDay) +
                String.valueOf(ehour) +
                String.valueOf(eminute));

        calendar.set(eYear, eMonth, eDay, ehour, eminute-4);
        Log.d("cur_cal y, m, d, h, s", ":" + String.valueOf(calendar.get(Calendar.YEAR)) +
                String.valueOf(calendar.get(Calendar.MONTH)) +
                String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)) +
                String.valueOf(calendar.get(Calendar.HOUR_OF_DAY)) +
                String.valueOf(calendar.get(Calendar.MINUTE)));
        pendingIntent = PendingIntent.getBroadcast(getActivity().getApplicationContext(),
                0, my_intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        alarm_manager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                pendingIntent);
    }
    private void stopAlarm()
    {

        Intent my_intent = new Intent(getActivity().getApplicationContext(),  Alarm_Reciver.class);
        my_intent.putExtra("state","alarm off");
        pendingIntent = PendingIntent.getBroadcast(getActivity().getApplicationContext(), 0, my_intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        alarm_manager.cancel(pendingIntent);

        // 알람취소
        getActivity().sendBroadcast(my_intent);
    }
}

