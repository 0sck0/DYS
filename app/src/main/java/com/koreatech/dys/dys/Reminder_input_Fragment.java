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
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;
import java.util.GregorianCalendar;

import static android.content.Context.ALARM_SERVICE;

public class Reminder_input_Fragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    // 프래그먼트 선언
    private Fragment timeFragment;
    private Fragment creditFragment;

    private String total_data;
    private boolean isModify;
    //날짜 데이터를 화면에 표시하는 뷰, 버튼을 눌렀을때 DatePickerDialog를 호출하는 버튼
    private TextView eDateDisplay;
    private ImageButton ePickDate;
    //알림 스위치 뷰와 그 상태를 화면에 출력하는 뷰
    private TextView ring_switch_on_text;
    private Switch ring_switch;
    //현재 저장되어 있는 날짜 데이터 변수들이다..
    private int ehour;
    private int eminute;
    private int eYear;
    private int eMonth;
    private int eDay;
    //제목, 내용의 뷰
    private EditText memo_reminder;
    private EditText title_reminder;
    //현재 액티비티를 취소하거나 저장하는 버튼
    private Button cancel_reminder;
    private Button save_reminder;
    private DBclass reminderDB;
    private SQLiteDatabase db;
    private Cursor cursor;
    private boolean fin = true;
    private String recieve_title;
    private int switch1 = 0;
    static final int TIME_DIALOG_ID = 1;
    static final int DATE_DIALOG_ID = 0;
    private AlarmManager alarm_manager;
    private PendingIntent pendingIntent;

    public Reminder_input_Fragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static Reminder_input_Fragment newInstance(String param1, String param2) {
        Reminder_input_Fragment fragment = new Reminder_input_Fragment();
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reminder_input, container, false);
        alarm_manager = (AlarmManager) getActivity().getSystemService(ALARM_SERVICE);
        final Intent my_intent = new Intent(getActivity().getApplicationContext(), Alarm_Reciver.class);

        reminderDB = new DBclass(getActivity().getApplicationContext());
        try {
            db = reminderDB.getWritableDatabase();
            db = reminderDB.getReadableDatabase();
        } catch (SQLiteException ex) {
            db = reminderDB.getReadableDatabase();
        }


        title_reminder = (EditText) view.findViewById(R.id.title_reminder);
        //텍스트로 선택한 일정 보여주는 변수
        eDateDisplay = (TextView) view.findViewById(R.id.date_reminder);

        //날짜 선택하는 버튼
        ePickDate = (ImageButton) view.findViewById(R.id.ring_reminder);

        memo_reminder = (EditText) view.findViewById(R.id.memo_reminder);

        //현재 액티비티를 취소하거나 저장하는 버튼 //온클릭 리스너 만들어주고, 거기에는 인텐트 처리와 파일처리 코드를 넣을 예정.
        cancel_reminder = (Button) view.findViewById(R.id.cancel_reminder);
        save_reminder = (Button) view.findViewById(R.id.save_reminder);
        Intent intent  = getActivity().getIntent();
        //새로운 일정을 생성하는건지, 기존 일정을 수정하는건지를 판단하는 코드
        if((isModify = intent.getBooleanExtra("isModify",false))) {
            recieve_title = intent.getStringExtra("title");
            cursor = db.rawQuery("SELECT title, memo, end_date FROM reminder " +
                    "WHERE title='" + recieve_title + "';", null);
            // 반환된 커서에 ResultSets의 행의 개수가 0개일 경우
            if(cursor.getCount() == 0) {
                Toast.makeText(getActivity().getApplicationContext(), "해당 이름이 없습니다", Toast.LENGTH_SHORT).show();
                return null;
            }
            cursor.moveToFirst();

            title_reminder.setText(cursor.getString(0));
            memo_reminder.setText(cursor.getString(1));
            eDateDisplay.setText(cursor.getString(2));

        }
        else {
            //처음 날짜 정할때 오늘 날짜로 초기화하는 함수
            set_date();
        }

        //날짜 변경 버튼 이벤트 리스너
        ePickDate.setOnClickListener((new View.OnClickListener(){
            public void onClick(View v)
            {
                switch1 = 1;
                getActivity().showDialog(DATE_DIALOG_ID);
            }
        }));

        //취소 버튼과 저장 버튼 이벤트 리스너 설정
        cancel_reminder = (Button) view.findViewById(R.id.cancel_reminder);
        cancel_reminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                fragmentManager.beginTransaction().remove(Reminder_input_Fragment.this).commit();
                fragmentManager.popBackStack();
            }
        });
        save_reminder = (Button) view.findViewById(R.id.save_reminder);
        save_reminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!check_data()) {
                    String cur_title = title_reminder.getText().toString();
                    String memo = memo_reminder.getText().toString();
                    String end_date = eDateDisplay.getText().toString();
                    if (isModify) {
                        //수정하는경우

                        if(title_reminder.getText().toString().equals(recieve_title)) {
                            if (!end_date.equals(cursor.getString(2))) {
                                stopAlarm();
                                goAlarm();
                            }
                            db.execSQL("UPDATE reminder " +
                                    "SET title = '" + cur_title +
                                    "', memo = '" + memo +
                                    "', end_date = '" + end_date +
                                    "' WHERE title = '" + recieve_title + "';");
                        }
                        else
                        {
                            if (!end_date.equals(cursor.getString(2))) {
                                stopAlarm();
                                goAlarm();
                            }
                            db.execSQL("DELETE FROM reminder " +
                                    "WHERE title = '" + recieve_title + "';");
                            db.execSQL("INSERT INTO reminder " +
                                    "VALUES (null, '" + cur_title + "', '" + memo + "', '"
                                    + end_date + "');");
                        }
                        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                        fragmentManager.beginTransaction().remove(Reminder_input_Fragment.this).commit();
                        fragmentManager.popBackStack();
                    } else {  //수정이 아닌, 새로운 일정을 추가하는 경우는 파일을 생성하고 데이터를 쓴다.
                        cursor = db.rawQuery("SELECT title, memo, end_date FROM reminder " +
                                "WHERE title='" + cur_title + "';", null);
                        // 반환된 커서에 ResultSets의 행의 개수가 0개일 경우
                        if(cursor.getCount() == 0) {
                            db.execSQL("INSERT INTO reminder " +
                                    "VALUES (null, '" + cur_title + "', '" + memo + "', '"
                                    + end_date + "');");
                            goAlarm();
                            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                            fragmentManager.beginTransaction().remove(Reminder_input_Fragment.this).commit();
                            fragmentManager.popBackStack();
                        }
                        else
                        {
                            //메세지 박스 출력 -> 확인 취소, 이때 확인 누르면 덮어쓰기, 취소하면 그대로 액티비티에 머무름.
                            AlertDialog.Builder alert_confirm = new AlertDialog.Builder(getActivity());
                            alert_confirm.setMessage("동일한 제목의 리마인더가 존재합니다. 덮어 쓰시겠습니까?").setCancelable(false).setPositiveButton("덮어쓰기",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            String title = title_reminder.getText().toString();
                                            String memo = memo_reminder.getText().toString();
                                            String end_date = eDateDisplay.getText().toString();
                                            if(!end_date.equals(cursor.getString(2)))
                                            {
                                                stopAlarm();
                                                goAlarm();
                                            }
                                            db.execSQL("UPDATE reminder " +
                                                    "SET title = '" + title +
                                                    "', memo = '" + memo +
                                                    "', end_date = '" + end_date +
                                                    "' WHERE title = '" + title + "';");
                                            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                                            fragmentManager.beginTransaction().remove(Reminder_input_Fragment.this).commit();
                                            fragmentManager.popBackStack();
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

        getActivity().setTitle("리마인더 입력");

        return view;
    }
    private boolean check_data() {
        boolean sw = false;
        if (title_reminder.getText().toString().isEmpty()) {
            Toast.makeText(getActivity().getApplicationContext(), "제목를 입력해주십시오.", Toast.LENGTH_LONG).show();
            sw = true;
        }
        if (memo_reminder.getText().toString().isEmpty()) {
            Toast.makeText(getActivity().getApplicationContext(), "메모를 입력해주십시오.", Toast.LENGTH_LONG).show();
            sw = true;
        }
        if (eDateDisplay.getText().toString().isEmpty()) {
            Toast.makeText(getActivity().getApplicationContext(), "종료 날짜를 입력해주십시오.", Toast.LENGTH_LONG).show();
            sw = true;
        }
        return sw;
    }

    //처음 날짜 정할때 오늘 날짜로 초기화하는 코드.
    private void set_date()
    {

        Calendar c = Calendar.getInstance();

        eYear = c.get(Calendar.YEAR);
        eMonth = c.get(Calendar.MONTH);
        eDay = c.get(Calendar.DAY_OF_MONTH);
        ehour = c.get(Calendar.HOUR_OF_DAY);
        eminute = c.get(Calendar.MINUTE);
    }

    //위의 날짜들의 데이터를 텍스트뷰에 출력
    private void updateDate()
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

                    eYear = year;
                    eMonth = monthOfYear;
                    eDay = dayOfMonth;
                    updateDate();
                }
            };


    // 시간 선택
    TimePickerDialog.OnTimeSetListener mTimeSetListener =
            new TimePickerDialog.OnTimeSetListener() {
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                    ehour = hourOfDay;
                    eminute = minute;
                    updateDate();
                }
            };

    //    @Override
    protected Dialog onCreateDialog(int id) {
        Log.v(Integer.toString(id), " : 1");

        switch (id) {
            case DATE_DIALOG_ID: {
                return new DatePickerDialog(getActivity(),
                        mDateSetListener,
                        eYear, eMonth, eDay);
            }
            case TIME_DIALOG_ID: {
                return new TimePickerDialog(getActivity(),
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