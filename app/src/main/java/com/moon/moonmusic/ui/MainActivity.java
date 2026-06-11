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

/**
 * 项目主页面：负责把首页、谢霆锋专区、我的页面三个 Fragment 整合到同一个 Activity。
 * 底部导航切换 Fragment，通知权限用于配合音频前台服务显示播放通知。
 */
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

        // 默认先显示首页，后面点击底部导航时只替换容器里的 Fragment，不重新打开 Activity。
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
            // 用户点不同 tab 时，FragmentManager 把对应页面放进 fl_container。
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
                // Android 13 以后通知权限需要运行时申请；音乐播放服务的前台通知会用到它。
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        1001);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // 不强依赖通知权限；拒绝也能播放，只是可能无前台通知。
    }
}
