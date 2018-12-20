package com.koreatech.dys.dys;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class ReminderFragment extends Fragment
        implements AdapterView.OnItemLongClickListener, ActionMode.Callback {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    //리스트 뷰를 이용하기 위해 전역변수로 선언하였다.
    private ListView m_ListView;
    //리스트 뷰와 item들의 연결점인 ArrayAdapter를 선언하였다.
    private SimpleCursorAdapter adapter;
    //컨텍스트 액션모드를 이용하기 위한 변수이다.
    private ActionMode mActionMode;
    //리스트 뷰에서 롱클릭을 하였을때, 롱클릭된 뷰의 이름을 저장하는 변수이다
    private String renm;
    private DBclass reminderDB;
    private SQLiteDatabase db;
    private Cursor cursor;

    public ReminderFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static ReminderFragment newInstance(String param1, String param2) {
        ReminderFragment fragment = new ReminderFragment();
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
        View view = inflater.inflate(R.layout.fragment_reminder, container, false);

        reminderDB = new DBclass(getActivity().getApplicationContext());
        try {
            db = reminderDB.getWritableDatabase();
        } catch (SQLiteException ex) {
            db = reminderDB.getReadableDatabase();
        }
        //xml파일에는 id가 list인 리스트뷰가 코딩되어 있으므로 그 뷰를 변수에 이어준다.
        m_ListView = (ListView) view.findViewById(R.id.list_reminder);
        //처음 앱이 실행될때, 정해진 경로의 폴더(나는 내부저장소로 정햇다)에 있는 파일들을 File 배열
        m_ListView = (ListView) view.findViewById(R.id.list_reminder);
        refresh_list();
        //이때 item들을 눌러서 웹사이트를 연결하기 위한 이벤트리스너를 설정한다.
        m_ListView.setOnItemClickListener(onClickListItem);
        //item들이 길게 눌렸을 때 반응하는 이벤트리스너 또한 설정하는데, 이때는 implements로 Adapter
        // View.OnItemLongClickListener를 해줬기 때문에 이벤트리스너의 인자에는 this가 들어간다.
        m_ListView.setOnItemLongClickListener(this);

        getActivity().setTitle(R.string.title_reminder);

        return view;
    }

    //콘텍스트 액션 모드 구현
    //ActionMode.Callback implements 했기 때문에 클래스 안에 해당하는 함수를 만든다
    // 이 함수는 컨텍스트 액션 모드를 생성하는 함수이다.
    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.menu_reminder_context, menu);
        return true;
    }

    //이 함수는 준비하는? 함수인거 같은데 정확히 하는 일은 잘 모르겠다. 액션모드 객체를 초기화해주는?
    //그런 용도인 것 같다.
    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    //롱클릭하여 액션모드가 보이게 되었을때, 사용자가 액션모드의 메뉴 아이템을 눌렀을때 이벤트함수이다.
    //여기서 액션모드가 보였을때, remove라는 item이 보이고 그것을 눌렀을때 롱클릭되었던 아이템뷰가
    //사라져야 한다.
    //그리고 삭제여부를 토스트 메세지로 사용자에게 알린다. 그리고 컨텍스트 액션모드를 종료한다.
    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.remove_reminder:
                String cur_title = renm;
                Log.d("item:", renm);
                db.execSQL("DELETE FROM reminder WHERE title = '" + renm + "';");
                mode.finish();
                refresh_list();
                return true;
            default:
                return false;
        }
    }

    // 사용자가 컨텍스트 액션 모드를 빠져나갈 때 호출하는 함수이다.
    @Override
    public void onDestroyActionMode(ActionMode mode) {
        mActionMode = null;
    }

    //사용자가 item을 길게 눌렀을때 호출되는 함수이다.
    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        if (mActionMode != null) {
            return false;
        }
        renm = adapter.getCursor().getString(2);
        // 컨텍스트 액션 모드 시작
        mActionMode = ((AppCompatActivity) getActivity()).startSupportActionMode(this);
        view.setSelected(true);
        return true;
    }
    //콘텍스트 액션 모드 구현 끝


    //기본 화면에서 item들을 짧게 눌렀을 때 호출되는 함수이다. url을 이용하여 웹사이트를 연결한다.
    private AdapterView.OnItemClickListener onClickListItem = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//            // 클릭 이벤트 발생시 수정 작업으로써, intent호출하여 Reminder_input실행
//            Intent reminderintent = new Intent(getActivity().getApplicationContext(), Reminder_input_Fragment.class);
//            reminderintent.putExtra("isModify", true);
//            reminderintent.putExtra("title", adapter.getCursor().getString(2));
//            startActivity(reminderintent);

            Fragment fragment = new Reminder_input_Fragment();
            Bundle bundle = new Bundle(2);
            bundle.putBoolean("isModify", true);
            bundle.putString("title", adapter.getCursor().getString(2));
            fragment.setArguments(bundle);

            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.replace(R.id.container, fragment);
            transaction.addToBackStack(null);
            transaction.commit();
        }
    };

    //기본 화면에서의 옵션 메뉴를 생성하는 함수이다.
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_add_reminder, menu);
    }

    //기본 화면에서의 옵션 메뉴를 눌렀을때 즉, add를 눌렀을 때 Reminder_input를 호출하는 함수이다.
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_reminder:
//                //서브 액티비티 호출, 인텐트로 데이터 소통,
//                Intent reminderintent = new Intent(getActivity().getApplicationContext(), Reminder_input_Fragment.class);
//                reminderintent.putExtra("isModify", false);
//                startActivity(reminderintent);

                Fragment fragment = new Reminder_input_Fragment();
                Bundle bundle = new Bundle(1);
                bundle.putBoolean("isModify", false);
                fragment.setArguments(bundle);

                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.container, fragment);
                transaction.addToBackStack(null);
                transaction.commit();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return false;
    }
    private void refresh_list()
    {
        cursor = db.rawQuery("SELECT rowid _id, * FROM reminder;", null);
        getActivity().startManagingCursor(cursor);
        String[] from = {"title"};
        int[] to = {android.R.id.text1};
        adapter = new SimpleCursorAdapter(getActivity(), android.R.layout.simple_list_item_1,cursor,from,to);
        m_ListView.setAdapter(adapter);
    }
}