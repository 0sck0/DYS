package com.koreatech.dys.dys;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.EventDay;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import static android.content.Context.ALARM_SERVICE;

public class StudyPlanner extends Fragment {
    public static final String RESULT = "result";
    public static final String EVENT = "event";
    private static final int ADD_NOTE = 44;
    private List<EventDay> mEventDays = new ArrayList<>();
    private SQLiteDatabase db;
    private DBclass studyDB;

    //스터디 인풋창
    LinearLayout study_input;

    //제목, 내용의 뷰
    private EditText title_study;
    private EditText content_study;

    //현재 액티비티를 취소하거나 저장하는 버튼
    private Button cancel_study;
    private Button save_study;

    //날짜 데이터를 화면에 표시하는 뷰, 버튼을 눌렀을때 DatePickerDialog를 호출하는 버튼
    private TextView sTimeDisplay, eTimeDisplay;
    private Button sPickTime, ePickTime;

    //알림 스위치 뷰와 그 상태를 화면에 출력하는 뷰
    private TextView ring_switch_on_text;
    private Switch ring_switch;
    private StringBuilder db_Date;

    //현재 저장되어 있는 날짜 데이터 변수들이다..
    private int shour, ehour;
    private Calendar clickedDayCalendar;
    private int sminute, eminute;
    private int cYear;
    private int cMonth;
    private int cDay;
    private int switch1 = 0;

    private AlarmManager alarm_manager;
    private Context context;
    private PendingIntent pendingIntent;
    static final int TIME_DIALOG_ID = 1;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public StudyPlanner() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static StudyPlanner newInstance(String param1, String param2) {
        StudyPlanner fragment = new StudyPlanner();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup contatiner,
                             Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_study, contatiner, false);

        getActivity().setTitle(R.string.title_study);
        alarm_manager = (AlarmManager) getActivity().getSystemService(ALARM_SERVICE);
        Intent my_intent = new Intent(getActivity().getApplicationContext(), Alarm_Reciver.class);
        studyDB = new DBclass(getActivity().getApplicationContext());
        try {
            db = studyDB.getWritableDatabase();
        } catch (SQLiteException ex) {
            db = studyDB.getReadableDatabase();
        }
        //studyDB.onUpgrade(db, 1, 2);

        // 달력 출력 코드
        Calendar calendar = Calendar.getInstance();
        //calendar.add(Calendar.DAY_OF_MONTH, 2);
        List<EventDay> events = new ArrayList<>();
        CalendarView calendarView;
        onSetIcon(events);
        calendarView = view.findViewById(R.id.calendarView);

        //처음 날짜를 오늘로 초기화하는 함수
        set_date();

        //사용자가 날짜를 클릭하였을때 스터디 입력창을 띄운다.
        calendarView.setOnDayClickListener((EventDay eventDay) ->
        {
            clear();
            clickedDayCalendar = eventDay.getCalendar();
            Log.d("clickedDayCalendar", clickedDayCalendar.toString());
            cYear = clickedDayCalendar.get(Calendar.YEAR);
            cMonth = clickedDayCalendar.get(Calendar.MONTH);
            cDay = clickedDayCalendar.get(Calendar.DAY_OF_MONTH);
            db_date();
            Cursor cursor = db.rawQuery("SELECT * FROM study_planner WHERE date='" + db_Date + "';", null);
            cursor.moveToFirst();
            if(cursor.getCount() != 0)
            {
                content_study.setText(cursor.getString(5));
                sTimeDisplay.setText(cursor.getString(2));
                eTimeDisplay.setText(cursor.getString(3));
                ring_switch_on_text.setText(cursor.getString(4));
                if(ring_switch_on_text.getText().toString().equals("On"))
                    ring_switch.setChecked(true);
                else
                    ring_switch.setChecked(false);
                title_study.setText(cursor.getString(1));
                shour = Integer.parseInt(sTimeDisplay.getText().toString().split(":")[0]);
                sminute = Integer.parseInt(sTimeDisplay.getText().toString().split(":")[1]);
            }
            //사용자가 클릭한 날짜의 값을 인트로 받아 올수 있음!
            study_input.setVisibility(View.VISIBLE);
        });

        //입력창 선언******************************************************************************
        study_input = (LinearLayout) view.findViewById(R.id.study_input);
        sTimeDisplay = (TextView) view.findViewById(R.id.start_time_study);
        eTimeDisplay = (TextView) view.findViewById(R.id.end_time_study);
        title_study = (EditText) view.findViewById(R.id.title_study);
        content_study = (EditText) view.findViewById((R.id.content_study));

        //현재 액티비티를 취소하거나 저장하는 버튼 //온클릭 리스너 만들어주고, 거기에는 인텐트 처리와 파일처리 코드를 넣을 예정.
        cancel_study = (Button) view.findViewById(R.id.cancel_study);
        cancel_study.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clear();
                study_input.setVisibility(View.GONE);
            }
        });
        save_study = (Button) view.findViewById(R.id.save_study);
        save_study.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!check_data()) {
                    String cur_title = title_study.getText().toString();
                    String content = content_study.getText().toString();
                    String start_time = sTimeDisplay.getText().toString();
                    String end_time = eTimeDisplay.getText().toString();
                    String ring_switch1 = ring_switch_on_text.getText().toString();
                    Cursor cursor = db.rawQuery("SELECT title, alarm, start_time FROM study_planner WHERE date='" + db_Date + "';", null);
                    cursor.moveToFirst();
                    if (cursor.getCount() == 0) {
                        db.execSQL("INSERT INTO study_planner " +
                                "VALUES ('" + db_Date + "', '" + cur_title + "', '" + start_time + "', '"
                                + end_time + "', '" + ring_switch1 + "', '" + content + "');");
                        if(ring_switch.isChecked() == true)
                        {
                            goAlarm();
                            Toast.makeText(getActivity().getApplicationContext(), "알람이 설정되었습니다.", Toast.LENGTH_SHORT);
                        }
                    } else {
                        if(cursor.getString(1).equals("On") && ring_switch1.equals("Off"))
                        {
                            stopAlarm();
                            Toast.makeText(getActivity().getApplicationContext(), "알람이 취소 되었습니다.", Toast.LENGTH_SHORT);
                        }
                        else if(cursor.getString(1).equals("On") && ring_switch1.equals("On"))
                        {
                            String str[] = cursor.getString(2).split(":");
                            if(shour != Integer.parseInt(str[0]) || sminute != Integer.parseInt(str[1]))
                            {
                                stopAlarm();
                                goAlarm();
                                Toast.makeText(getActivity().getApplicationContext(), "알람이 재설정되었습니다.", Toast.LENGTH_SHORT);
                            }
                        }
                        db.execSQL("UPDATE study_planner " +
                                "SET title = '" + cur_title +
                                "', start_time = '" + start_time +
                                "', end_time = '" + end_time +
                                "', alarm = '" + ring_switch1 +
                                "', content = '" + content +
                                "' WHERE date = '" + db_Date + "';");

                    }

                    events.add(new EventDay(clickedDayCalendar, R.drawable.plan_16));
                    calendarView.setEvents(events);
                    clear();
                    study_input.setVisibility(View.GONE);
                }
            }
        });

        //시간 선택하는 버튼
        sPickTime = (Button) view.findViewById(R.id.start_timepicker);
        sPickTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch1 = 0;
                getActivity().showDialog(TIME_DIALOG_ID);
            }
        });
        ePickTime = (Button) view.findViewById(R.id.end_timepicker);
        ePickTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch1 = 1;
                getActivity().showDialog(TIME_DIALOG_ID);
            }
        });

        //알림 스위치와 스위치 상태를 표시해주는 텍스트뷰
        ring_switch = (Switch) view.findViewById(R.id.switch_study);
        ring_switch_on_text = (TextView) view.findViewById(R.id.switch_condition_study);
        //스위치 버튼 이벤트 리스너
        ring_switch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ring_switch_checkState();
            }
        });
        ring_switch_checkState();
        //*****************************************************************************************

        return view;
    }

    //스위치 상태를 텍스트에 출력해주는 함수 ( 알림 기능을 추가해주어야함.
    private void ring_switch_checkState() {
        if (ring_switch.isChecked())
            ring_switch_on_text.setText("On");
        else
            ring_switch_on_text.setText("Off");
    }

    //날짜를 오늘로 초기화해주는 함수
    private void set_date() {
        Calendar c = Calendar.getInstance();

        cYear = c.get(Calendar.YEAR);
        cMonth = c.get(Calendar.MONTH);
        cDay = c.get(Calendar.DAY_OF_MONTH);
        shour = c.get(Calendar.HOUR_OF_DAY);
        sminute = c.get(Calendar.MINUTE);
        ehour = c.get(Calendar.HOUR_OF_DAY);
        eminute = c.get(Calendar.MINUTE);
    }
    private void db_date()
    {
        db_Date = new StringBuilder().append(cYear).append("/")
                .append(cMonth + 1).append("/")
                .append(cDay);
    }

    //위의 날짜들의 데이터를 텍스트뷰에 출력
    private void updateDate()
    {
        if(switch1 == 0)
        {
            sTimeDisplay.setText(
                    new StringBuilder()
                            .append(pad(shour)).append(":")
                            .append(pad(sminute)));
        }
        else
        {
            eTimeDisplay.setText(
                    new StringBuilder()
                            .append(pad(ehour)).append(":")
                            .append(pad(eminute)));
        }

    }

    // 시간 선택
    TimePickerDialog.OnTimeSetListener mTimeSetListener =
            new TimePickerDialog.OnTimeSetListener() {
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    if (switch1 == 0) {
                        shour = hourOfDay;
                        sminute = minute;
                    } else {
                        ehour = hourOfDay;
                        eminute = minute;
                    }

                    updateDate();
                }
            };

    //시간을 1분이면 01로 바꿔서 문자열로 바꿔주는 함수
    private static String pad(int c)
    {
        if (c >= 10)
            return String.valueOf(c);
        else
            return "0" + String.valueOf(c);
    }
    protected Dialog onCreateDialog(int id) {
        if (switch1 == 0)
            return new TimePickerDialog(getActivity().getApplicationContext(),
                    mTimeSetListener, shour, sminute, false);
        else
            return new TimePickerDialog(getActivity().getApplicationContext(),
                    mTimeSetListener, ehour, eminute, false);
    }
    private void clear()
    {
        content_study.setText("");
        sTimeDisplay.setText("");
        eTimeDisplay.setText("");
        ring_switch.setChecked(false);
        ring_switch_checkState();
        title_study.setText("");
    }
    private void onSetIcon(List<EventDay> events)
    {
        CalendarView calendarView = getActivity().findViewById(R.id.calendarView);
        Cursor cursor = db.rawQuery("SELECT date FROM study_planner;", null);
        cursor.moveToFirst();
        if(cursor.getCount() != 0) {
            while (cursor.moveToNext()) {
                String str[] = cursor.getString(0).split("/");
                Calendar temp = new GregorianCalendar();
                temp.set(Integer.parseInt(str[0]), Integer.parseInt(str[1]) - 1, Integer.parseInt(str[2]));
                events.add(new EventDay(temp, R.drawable.plan_16));
            }
            calendarView.setEvents(events);
        }
    }
    private boolean check_data() {
        boolean sw = false;
        if (title_study.getText().toString().isEmpty()) {
            Toast.makeText(getActivity().getApplicationContext(), "제목를 입력해주십시오.", Toast.LENGTH_LONG).show();
            sw = true;
        }
        if (sTimeDisplay.getText().toString().isEmpty()) {
            Toast.makeText(getActivity().getApplicationContext(), "시작 날짜를 입력해주십시오.", Toast.LENGTH_LONG).show();
            sw = true;
        }
        if (eTimeDisplay.getText().toString().isEmpty()) {
            Toast.makeText(getActivity().getApplicationContext(), "종료 날짜를 입력해주십시오.", Toast.LENGTH_LONG).show();
            sw = true;
        }
        if (ring_switch_on_text.getText().toString().isEmpty()) {
            Toast.makeText(getActivity().getApplicationContext(), "알림을 설정해주십시오.", Toast.LENGTH_LONG).show();
            sw = true;
        }
        if (content_study.getText().toString().isEmpty()) {
            Toast.makeText(getActivity().getApplicationContext(), "내용을 입력해주십시오.", Toast.LENGTH_LONG).show();
            sw = true;
        }
        return sw;
    }
    private void goAlarm()
    {
        Intent my_intent = new Intent(getActivity().getApplicationContext(), Alarm_Reciver.class);
        my_intent.putExtra("state","alarm on");
        Calendar calendar = new GregorianCalendar();
        Calendar cur_cal = Calendar.getInstance();

        Log.d("cur_cal y, m, d, h, s", ":" + String.valueOf(cur_cal.get(Calendar.YEAR)) +
                String.valueOf(cur_cal.get(Calendar.MONTH)) +
                String.valueOf(cur_cal.get(Calendar.DAY_OF_MONTH)) +
                String.valueOf(shour) +
                String.valueOf(sminute));

        calendar.set(cur_cal.get(Calendar.YEAR), cur_cal.get(Calendar.MONTH), cur_cal.get(Calendar.DAY_OF_MONTH), shour, sminute);
        pendingIntent = PendingIntent.getBroadcast(getActivity().getApplicationContext(), 0, my_intent,
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
        getActivity().getApplicationContext().sendBroadcast(my_intent);
    }
}