package com.koreatech.dys.dys;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class TimetableSelectedListView extends Activity {
    // 인텐트 변수
    String[] values;
    int requestCode;

    // Listview
    TextView tv;
    ListView lv;
    ArrayAdapter adapter;

    // result 값
    String result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timetable_selected_listview);

        tv = (TextView) findViewById(R.id.lvText);
        lv = (ListView) findViewById(R.id.list);

        Intent intent = getIntent();
        requestCode = intent.getIntExtra("requestCode", 0);

        switch (requestCode) {
            case 1:
                tv.setText("요일 선택");
                values = intent.getStringArrayExtra("day");
                break;
            case 2:
                tv.setText("시작 시간 선택");
                values = intent.getStringArrayExtra("time");
                break;
            case 3:
                tv.setText("종료 시간 선택");
                values = intent.getStringArrayExtra("time");
                break;
            default:
                break;
        }

        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, values);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(onClickListItem);
    }

    private AdapterView.OnItemClickListener onClickListItem = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String result = "";

            switch (requestCode) {
                case 1:
                    result = values[position];
                    break;
                case 2:
                    result = values[position];
                    break;
                case 3:
                    result = values[position];
                    break;
                default:
                    break;
            }

            Intent in = new Intent();
            in.putExtra("result", result);
            setResult(RESULT_OK, in);
            finish();
        }
    };
}
