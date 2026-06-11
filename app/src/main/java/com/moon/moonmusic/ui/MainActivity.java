package com.moon.moonmusic.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.moon.moonmusic.R;
import com.moon.moonmusic.fragment.HomeFragment;
import com.moon.moonmusic.fragment.MyFragment;
import com.moon.moonmusic.fragment.NicholasFragment;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;

    private Fragment homeFragment;
    private Fragment nicholasFragment;
    private Fragment myFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initData();
        initListener();
        requestNotificationPermissionIfNeeded();
    }

    private void initView() {
        bottomNav = findViewById(R.id.bottom_nav);
    }

    private void initData() {
        homeFragment = new HomeFragment();
        nicholasFragment = new NicholasFragment();
        myFragment = new MyFragment();

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fl_container, homeFragment)
                .commit();

        bottomNav.setSelectedItemId(R.id.nav_home);
    }

    private void initListener() {
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment target;
            if (item.getItemId() == R.id.nav_home) {
                target = homeFragment;
            } else if (item.getItemId() == R.id.nav_nicholas) {
                target = nicholasFragment;
            } else {
                target = myFragment;
            }
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fl_container, target)
                    .commit();
            return true;
        });
    }

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        1001);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // 演示项目：不强依赖通知权限；拒绝也能播放，只是可能无前台通知
    }
}
