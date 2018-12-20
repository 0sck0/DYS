package com.koreatech.dys.dys;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TimetableFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

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

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getActivity().setTitle(R.string.title_timetable);

        View v = inflater.inflate(R.layout.fragment_timetable, container, false);

        // 날짜 표시
        Calendar cal = Calendar.getInstance();
        weekPrintToTimeTable(cal, v);

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
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction  = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.container, TimetableSelectedFragment.newInstance("", ""));
                fragmentTransaction.commit();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // 날짜 표기하기
    public void weekPrintToTimeTable(Calendar mCalendar, View v) {
        Date date = new Date();
        Date[] thisWeek = new Date[5];
        String[] thisWeekString = new String[5];
        mCalendar.setTime(date);

        // 1 = 일요일, 2 = 월요일
        int day_of_week = mCalendar.get(Calendar.DAY_OF_WEEK);

        int monday_offset;
        if (day_of_week == 1) {
            monday_offset = -6;
        } else
            monday_offset = (2 - day_of_week); // 음수로 설정

        // 월요일부터 시작
        mCalendar.set(Calendar.DAY_OF_YEAR, monday_offset);

        mCalendar.add(Calendar.MONTH, -1);
        // 화, 수, 목, 금 출력하기
        for(int i=0; i<5; i++) {
            mCalendar.add(Calendar.DAY_OF_YEAR, 1);
            thisWeek[i] = mCalendar.getTime();
        }

        // 출력할 포맷 만들기
        String strDateFormat = "MM/dd";
        SimpleDateFormat sdf = new SimpleDateFormat(strDateFormat);

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
}
