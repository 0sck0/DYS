package com.koreatech.dys.dys;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class TimetableSelectedColor extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timetable_selected_color);
    }


    public void colorClick(View view) {
        Intent intent = new Intent();

        switch(view.getId()) {
            case R.id.color1:
                intent.putExtra("result", 1);
                break;
            case R.id.color2:
                intent.putExtra("result", 2);
                break;
            case R.id.color3:
                intent.putExtra("result", 3);
                break;
            case R.id.color4:
                intent.putExtra("result", 4);
                break;
            case R.id.color5:
                intent.putExtra("result", 5);
                break;
            case R.id.color6:
                intent.putExtra("result", 6);
                break;
            case R.id.color7:
                intent.putExtra("result", 7);
                break;
            case R.id.color8:
                intent.putExtra("result", 8);
                break;
        }

        setResult(RESULT_OK, intent);
        finish();
    }

}
