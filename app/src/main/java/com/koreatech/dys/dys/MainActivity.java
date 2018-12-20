package com.koreatech.dys.dys;

import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    // 프래그먼트 선언
    private Fragment mainFragment;
    private Fragment timeFragment;
    private Fragment creditFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mainFragment = new MainFragment();
        timeFragment = new TimetableFragment();
        creditFragment = new CreditFragment();

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.container, mainFragment);
        transaction.addToBackStack(null);
        transaction.commit();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    // Back 버튼을 눌렀을 때 Navigation drawer가 닫히는 기능을 구현한 메소드
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            for (Fragment fragment: getSupportFragmentManager().getFragments()) {
                if (fragment.isVisible()) {
                    if(fragment instanceof MainFragment)
                        finish();
                    else
                        super.onBackPressed();
                }
            }
        }
    }

    // Navigation drawer 메뉴 선택 시 프래그먼트 이동 메소드
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        if (id == R.id.nav_home) {
            transaction.replace(R.id.container, mainFragment);
        } else if (id == R.id.nav_schedule) {

        } else if (id == R.id.nav_timetable) {
            transaction.replace(R.id.container, timeFragment);
        } else if (id == R.id.nav_study_planner) {

        } else if (id == R.id.nav_reminder) {

        } else if (id == R.id.nav_lab) {

        } else if (id == R.id.nav_settings) {

        } else if (id == R.id.nav_credit) {
            transaction.replace(R.id.container, creditFragment);
        }

        transaction.addToBackStack(null);
        transaction.commit();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
