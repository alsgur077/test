package com.example.hstalk;

import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;

import android.graphics.Color;
import android.os.Build;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v7.app.AppCompatActivity;

import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.example.hstalk.Fragment.BoardFragment;
import com.example.hstalk.Fragment.LiveMatchingFragment;
import com.example.hstalk.util.Constants;
import com.example.hstalk.Fragment.SettingPreferenceFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        BottomNavigationView bottomNavigationView = (BottomNavigationView)findViewById(R.id.mainactivity_drawer);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                switch(id){
                    case R.id.action_livematching:
                        getFragmentManager().beginTransaction().replace(R.id.content_main, new LiveMatchingFragment()).commit();
                        break;
                    case R.id.action_board :
                        getFragmentManager().beginTransaction().replace(R.id.content_main, new BoardFragment()).commit();
                        break;
                    case R.id.action_setting :
                        getFragmentManager().beginTransaction().replace(R.id.content_main, new SettingPreferenceFragment()).commit();
                        break;
                }
                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                drawer.closeDrawer(GravityCompat.START);
                return true;
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

//네비게이션바에 이름과 이메일을 표시하는 기능을 동작하는 함수
        infoViewer(navigationView);

        passPushTokenToServer();
    }

    public void infoViewer(NavigationView navigationView){
        TextView userName;
        TextView userEmail;
        String user;
        String email;
        FirebaseRemoteConfig firebaseRemoteConfig;
        FirebaseAuth firebaseAuth;
        SharedPreferences sharedPreferences;

        View nav_header_view = navigationView.getHeaderView(0);
        userName = (TextView) nav_header_view.findViewById(R.id.nav_textview_name);
        userEmail = (TextView) nav_header_view.findViewById(R.id.nav_textview_email);

        firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        String splash_background = firebaseRemoteConfig.getString(getString(R.string.rc_color));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.parseColor(splash_background));
        }

        sharedPreferences = getSharedPreferences(Constants.SHARED_PREFS, MODE_PRIVATE);
        user = sharedPreferences.getString(Constants.USER_NAME,"");

        FirebaseUser users = firebaseAuth.getCurrentUser();
        email = users.getEmail();

        userName.setText(user);
        userEmail.setText(email);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
//         Handle navigation view item clicks here.
        Fragment fragment = null;
        int id = item.getItemId();
        switch(id){
            case R.id.button_logout :
                FirebaseAuth.getInstance().signOut();
                SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREFS,MODE_PRIVATE);
                SharedPreferences.Editor edit = sharedPreferences.edit();
                edit.remove(Constants.USER_NAME);
                edit.apply();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                MainActivity.this.finish();
                break;
            case R.id.nav_action_chat:
//                fragment = new
                break;
            case R.id.nav_action_livematching:
                fragment = new LiveMatchingFragment();
                break;
            case R.id.nav_action_board:
                fragment = new BoardFragment();
                break;
            case R.id.nav_action_setting:
                fragment = new SettingPreferenceFragment();
                break;
            case R.id.nav_action_matchingInfo:
                Intent intent2 = new Intent(MainActivity.this, MatchingInfoActivity.class);
                startActivity(intent2);
                break;
        }

        if(fragment != null){
            getFragmentManager().beginTransaction().replace(R.id.content_main, fragment).commit();
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    void passPushTokenToServer(){

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String token = FirebaseInstanceId.getInstance().getToken();
        Map<String, Object> map = new HashMap<>();
        map.put("pushToken",token);

        FirebaseDatabase.getInstance().getReference().child("users").child(uid).updateChildren(map);
    }
}