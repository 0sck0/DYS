package com.koreatech.dys.dys;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.eunsiljo.timetablelib.data.TimeData;
import com.github.eunsiljo.timetablelib.data.TimeTableData;
import com.github.eunsiljo.timetablelib.view.TimeTableView;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class TimetableFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final int RESULT_OK = -1;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    // DB
    DBtimetable dbHelper;
    private SQLiteDatabase db;
    Cursor c;

    // timetable
    private TimeTableView timeTable;
    private ArrayList<TimeTableData> mTableData = new ArrayList<>();
    private long mNow = 0;

    static final int monkey = 0;
    static final int tuekey = 1;
    static final int wedkey = 2;
    static final int thukey = 3;
    static final int frikey = 4;

    public TimetableFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static TimetableFragment newInstance(String param1, String param2) {
        TimetableFragment fragment = new TimetableFragment();
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

        // DB 작동
        dbHelper = new DBtimetable(getContext());
        db = dbHelper.getReadableDatabase();
        c = db.rawQuery("SELECT * FROM course", null);

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getActivity().setTitle(R.string.title_timetable);

        View v = inflater.inflate(R.layout.fragment_timetable, container, false);

        // 날짜 표시
        weekPrintToTimeTable(v);

        if(c.getCount() != 0) {
            addTimeTable();
        }
        initLayout(v);
        initData();

        // Inflate the layout for this fragment
        return v;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_timetable, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_add:
                Intent intent = new Intent(getActivity(), TimetableSelected.class);
                startActivityForResult(intent, 100);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 100) {
            if(resultCode == RESULT_OK) {
                c = db.rawQuery("SELECT * FROM course", null);

                // 초기화를 시켜준다.
                mTableData.clear();

                addTimeTable();
                initData();
            }
        }
    }

    public void weekPrintToTimeTable(View v) {
        Calendar mCalendar = Calendar.getInstance();
        Date[] thisWeek = new Date[5];
        String[] thisWeekString = new String[5];

        // 1 = 일요일, 2 = 월요일
        int day_of_week = mCalendar.get(Calendar.DAY_OF_WEEK);

        // 오늘 날짜가 일요일이면 월요일부터 출력하기 위해 offset 이동
        int monday_offset;
        if (day_of_week == 1) {
            monday_offset = -7;
        } else
            monday_offset = (1 - day_of_week); // 음수로 설정

        //Log.v("Today_Monday:", String.valueOf(monday_offset));

        // 월요일부터 시작
        mCalendar.add(Calendar.DAY_OF_YEAR, monday_offset);

        // 화, 수, 목, 금 출력하기
        for(int i=0; i<5; i++) {
            mCalendar.add(Calendar.DAY_OF_YEAR, 1);
            //Log.v("Today2:", String.valueOf(mCalendar.get(Calendar.DAY_OF_YEAR)));
            thisWeek[i] = mCalendar.getTime();
        }

        // 출력할 포맷 만들기
        String strDateFormat = "MM/dd";
        SimpleDateFormat sdf = new SimpleDateFormat(strDateFormat, Locale.KOREA);

        for(int i=0; i<5; i++) {
            thisWeekString[i] = sdf.format(thisWeek[i]);
        }

        // TextView에 넣기
        TextView[] days = new TextView[5];
        days[0] = (TextView)v.findViewById(R.id.mondayDate);
        days[1] = (TextView)v.findViewById(R.id.tusedayDate);
        days[2] = (TextView)v.findViewById(R.id.wednesdayDate);
        days[3] = (TextView)v.findViewById(R.id.thursdayDate);
        days[4] = (TextView)v.findViewById(R.id.fridayDate);
        for(int i=0; i<5; i++) {
            days[i].setText(thisWeekString[i]);
        }
    }

    public void addTimeTable() {
        // 0 : subject
        // 1 : professor
        // 2 : classroom
        // 3 : weekOfDay
        // 4 : startTime
        // 5 : endTime
        // 6 : color

        // 변수
        ArrayList<String> data = new ArrayList<>();
        ArrayList<TimeTableData> tables = new ArrayList<>();

        ArrayList<TimeData> mon = new ArrayList<>();
        ArrayList<TimeData> tue = new ArrayList<>();
        ArrayList<TimeData> wed = new ArrayList<>();
        ArrayList<TimeData> thu = new ArrayList<>();
        ArrayList<TimeData> fri = new ArrayList<>();

        String day;
        String stime, etime;
        String d_stime, d_etime;

        // 모든 레코드에 있는 정보를 테이블에 넣는다.
        c.moveToFirst();

        Log.v("Cursor", String.valueOf(c.getColumnCount()));

        do {
            for (int i = 1; i < c.getColumnCount(); i++) {
                data.add(c.getString(i));
            }

            // 데이터를 시간표에 넣기
            day = data.get(3);
            Log.v("day", day);

            stime = data.get(4).split("시")[0];
            etime = data.get(5).split("시")[0];

            Log.v("stime", stime);
            Log.v("etime", etime);

            d_stime = getDateFormatter(day, stime);
            d_etime = getDateFormatter(day, etime);

            Log.v("Subject", data.get(1));
            Log.v("Color", data.get(6));

            int myColor = Integer.valueOf(data.get(6));

            if(day.equals("월요일")) {
                mon.add(new TimeData(monkey, data.get(1), myColor, R.color.black, getMillis(d_stime), getMillis(d_etime)));
            }
            else if(day.equals("화요일")) {
                Log.d("OK", "진입 성공");
                tue.add(new TimeData(tuekey, data.get(1), myColor, R.color.black, getMillis(d_stime), getMillis(d_etime)));
            }
            else if(day.equals("수요일")) {
                wed.add(new TimeData(wedkey, data.get(1), myColor, R.color.black, getMillis(d_stime), getMillis(d_etime)));
            }
            else if(day.equals("목요일")) {
                thu.add(new TimeData(thukey, data.get(1), myColor, R.color.black, getMillis(d_stime), getMillis(d_etime)));
            }
            else if(day.equals("금요일")) {
                fri.add(new TimeData(frikey, data.get(1), myColor, R.color.black, getMillis(d_stime), getMillis(d_etime)));
            }

            data.clear();
        } while(c.moveToNext());

        tables.add(new TimeTableData("Mon", mon));
        tables.add(new TimeTableData("Tue", tue));
        tables.add(new TimeTableData("Wed", wed));
        tables.add(new TimeTableData("Thu", thu));
        tables.add(new TimeTableData("Fri", fri));

        mTableData.addAll(tables);
    }

    private void initLayout(View v) {
        timeTable = (TimeTableView)v.findViewById(R.id.timeTable);
    }

    private void initData(){
        timeTable.setStartHour(6);
        timeTable.setShowHeader(false);
        timeTable.setTableMode(TimeTableView.TableMode.SHORT);

        Calendar calendar = getMonday();
        mNow = calendar.getTimeInMillis();
        timeTable.setTimeTable(mNow, mTableData);
    }


    private long getMillis(String day){
        DateTime date = getDateTimePattern().parseDateTime(day);
        return date.getMillis();
    }

    private DateTimeFormatter getDateTimePattern(){
        return DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
    }

    private String getDateFormatter(String day, String time) {
        Calendar mCalendar = Calendar.getInstance();
        String thisDate;

        // 1 = 일요일, 2 = 월요일
        int day_of_week = mCalendar.get(Calendar.DAY_OF_WEEK);
        int offset;
        int monday_offset;

        if (day_of_week == 1) {
            monday_offset = -6;
        } else
            monday_offset = (2 - day_of_week);

        offset = monday_offset;

        if(day.equals("화요일")) ++offset;
        else if(day.equals("수요일")) offset += 2;
        else if(day.equals("목요일")) offset += 3;
        else if(day.equals("금요일")) offset += 4;

        // 해당 날짜로 이동
        mCalendar.add(Calendar.DAY_OF_YEAR, offset);

        // 출력할 포맷 만들기
        String strDateFormat = "yyyy-MM-dd";
        SimpleDateFormat sdf = new SimpleDateFormat(strDateFormat);

        thisDate = sdf.format(mCalendar.getTime());
        if(Integer.valueOf(time) < 10) {
            thisDate += (" 0" + time + ":00:00");
        } else {
            thisDate += (" " + time + ":00:00");
        }

        Log.v("thisDate:", thisDate);

        return thisDate;
    }

    private Calendar getMonday() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.DAY_OF_WEEK,Calendar.MONDAY);
        return c;
    }


    /*
    private List<String> mTitles = Arrays.asList("Korean", "English", "Math", "Science", "Physics", "Chemistry", "Biology");
    private List<String> mLongHeaders =  Arrays.asList("Plan", "Do");
    private List<String> mShortHeaders = Arrays.asList("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat");

    private ArrayList<TimeTableData> getSamples(long date, List<String> headers, List<String> titles){
        TypedArray colors_table = getResources().obtainTypedArray(R.array.colors_table);
        TypedArray colors_table_light = getResources().obtainTypedArray(R.array.colors_table_light);

        ArrayList<TimeTableData> tables = new ArrayList<>();
        for(int i=0; i<headers.size(); i++){
            ArrayList<TimeData> values = new ArrayList<>();
            DateTime start = new DateTime(date);
            DateTime end = start.plusMinutes((int)((Math.random() * 10) + 1) * 30);
            for(int j=0; j<titles.size(); j++){
                int color = colors_table_light.getResourceId(j, 0);
                int textColor = R.color.black;
                //TEST
                if(headers.size() == 2 && i == 1){
                    color = colors_table.getResourceId(j, 0);
                    textColor = R.color.white;
                }

                TimeData timeData = new TimeData(j, titles.get(j), color, textColor, start.getMillis(), end.getMillis());

                //TEST
                if(headers.size() == 2 && j == 2){
                    timeData.setShowError(true);
                }
                values.add(timeData);

                start = end.plusMinutes((int)((Math.random() * 10) + 1) * 10);
                end = start.plusMinutes((int)((Math.random() * 10) + 1) * 30);
            }

            tables.add(new TimeTableData(headers.get(i), values));
        }
        return tables;
    }

    */
}
