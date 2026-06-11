package com.moon.moonmusic.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.moon.moonmusic.R;
import com.moon.moonmusic.db.UserDao;
import com.moon.moonmusic.model.User;
import com.moon.moonmusic.util.SpUtil;

public class LoginActivity extends AppCompatActivity {

    private EditText etNickname, etQq, etPwd;
    private RadioGroup rgGender;
    private RadioButton rbMale, rbFemale;
    private CheckBox cbPop, cbRock, cbFolk, cbClassic;
    private Button btnRegisterLogin, btnLogin;

    private UserDao userDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        inis();
    }

    /**
     * 对齐你以往项目写法：统一初始化入口。
     */
    private void inis() {
        initView();
        initData();
        initListener();
    }

    private void initView() {
        etNickname = findViewById(R.id.et_nickname);
        etQq = findViewById(R.id.et_qq);
        etPwd = findViewById(R.id.et_pwd);
        rgGender = findViewById(R.id.rg_gender);
        rbMale = findViewById(R.id.rb_male);
        rbFemale = findViewById(R.id.rb_female);
        cbPop = findViewById(R.id.cb_pop);
        cbRock = findViewById(R.id.cb_rock);
        cbFolk = findViewById(R.id.cb_folk);
        cbClassic = findViewById(R.id.cb_classic);
        btnRegisterLogin = findViewById(R.id.btn_register_login);
        btnLogin = findViewById(R.id.btn_login);
    }

    private void initData() {
        userDao = new UserDao(this);
        // 如果已登录，直接进入主页
        String qq = SpUtil.getLoginQq(this);
        if (!TextUtils.isEmpty(qq)) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    private void initListener() {
        btnRegisterLogin.setOnClickListener(v -> doRegisterAndLogin());
        btnLogin.setOnClickListener(v -> doLogin());
    }

    private void doRegisterAndLogin() {
        String nickname = etNickname.getText().toString().trim();
        String qq = etQq.getText().toString().trim();
        String pwd = etPwd.getText().toString();

        if (TextUtils.isEmpty(nickname) || TextUtils.isEmpty(qq) || TextUtils.isEmpty(pwd)) {
            toast("请把昵称/QQ/密码填写完整");
            return;
        }

        if (userDao.existsQq(qq)) {
            toast("该 QQ 已注册，请直接登录");
            return;
        }

        String gender = rbMale.isChecked() ? "男" : (rbFemale.isChecked() ? "女" : "未选择");
        String hobbies = buildHobbies();

        User u = new User(nickname, qq, pwd, gender, hobbies);
        long id = userDao.insertUser(u);
        if (id > 0) {
            // 新账号注册 -> 固定 Zack 头像
            SpUtil.saveLogin(this, qq, nickname, SpUtil.AVATAR_ZACK);
            toast("注册成功，已登录");
            startActivity(new Intent(this, MainActivity.class));
            finish();
        } else {
            toast("注册失败（可能 QQ 重复）");
        }
    }

    private void doLogin() {
        String qq = etQq.getText().toString().trim();
        String pwd = etPwd.getText().toString();

        if (TextUtils.isEmpty(qq) || TextUtils.isEmpty(pwd)) {
            toast("请输入 QQ 和密码");
            return;
        }

        User u = userDao.login(qq, pwd);
        if (u != null) {
            // 已有账号登录 -> 固定 Aerith 头像
            SpUtil.saveLogin(this, u.getQq(), u.getNickname(), SpUtil.AVATAR_AERITH);
            toast("登录成功");
            startActivity(new Intent(this, MainActivity.class));
            finish();
        } else {
            toast("账号或密码错误");
        }
    }

    private String buildHobbies() {
        StringBuilder sb = new StringBuilder();
        if (cbPop.isChecked()) sb.append("流行,");
        if (cbRock.isChecked()) sb.append("摇滚,");
        if (cbFolk.isChecked()) sb.append("民谣,");
        if (cbClassic.isChecked()) sb.append("经典,");
        if (sb.length() == 0) return "无";
        return sb.substring(0, sb.length() - 1);
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
