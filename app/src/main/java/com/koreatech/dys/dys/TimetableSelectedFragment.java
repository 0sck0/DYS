package com.koreatech.dys.dys;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

public class TimetableSelectedFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    // 시간 설정 관련 변수
    private int mHour, mMinute;
    private String ampm;
    View lastView;
    TextView startTime;
    TextView endTime;

    public TimetableSelectedFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static TimetableSelectedFragment newInstance(String param1, String param2) {
        TimetableSelectedFragment fragment = new TimetableSelectedFragment();
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

        // 텍스트 뷰 연결
        startTime = (TextView)getView().findViewById(R.id.startTime);
        endTime = (TextView)getView().findViewById(R.id.endTime);

        // Calendar instance 선언
        Calendar cal = new GregorianCalendar();
        mHour = cal.get(Calendar.HOUR_OF_DAY);
        mMinute = cal.get(Calendar.MINUTE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_timetable_selected, container, false);
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    public void setTime(View v) {
        lastView = v;

        TimePickerDialog tp = new TimePickerDialog(getContext(),
                TimePickerDialog.THEME_HOLO_LIGHT, mTimeSetListner, mHour, mMinute, false);

        tp.show();
    }

    TimePickerDialog.OnTimeSetListener mTimeSetListner = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            mHour = hourOfDay;
            mMinute = minute;

            // 최대 최소치 제한
            if(mHour < 8) {
                mHour = 8; mMinute = 0;
                Toast.makeText(getActivity().getApplicationContext(), "최소 8시까지 설정이 가능합니다.", Toast.LENGTH_SHORT).show();
            } else if(mHour >= 18 && mMinute != 0) {
                mHour = 18; mMinute = 0;
                Toast.makeText(getActivity().getApplicationContext(), "최대 18시까지 설정이 가능합니다.", Toast.LENGTH_SHORT).show();
            } else {

            }

            // 오전 오후 구분
            if(mHour > 11) {
                ampm = "오후";
            } else {
                ampm = "오전";
            }

            // 오후일 시 시간 12시간 감소 (13시 → 오후 1시)
            if(mHour >= 13) {
                mHour -= 12;
            }

            switch (lastView.getId()) {
                case R.id.setStartTime:
                    startTime.setText(String.format("%s %d:%02d", ampm, mHour, mMinute));
                    break;
                case R.id.setEndTime:
                    endTime.setText(String.format("%s %d:%02d", ampm, mHour, mMinute));
                    break;
                default:
                    break;
            }
        }
    };

    public void checkTheTime() {

    }
}
