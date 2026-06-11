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

/**
 * 登录/注册页面：负责收集用户信息，写入 SQLite，并把登录态保存到 SharedPreferences。
 * 这是进入主功能前的入口页面，也体现了 Activity 跳转到 MainActivity 的流程。
 */
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

    /**
     * 准备用户数据库访问对象，并检查是否已有登录态。
     * 如果 SharedPreferences 中存在账号，就直接进入主页面。
     */
    private void initData() {
        userDao = new UserDao(this);
        // 如果已登录，直接进入主页，避免用户每次打开 App 都重新登录。
        String qq = SpUtil.getLoginQq(this);
        if (!TextUtils.isEmpty(qq)) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    /**
     * 绑定登录页的两个主要操作。
     * 注册按钮会先写入数据库再登录，登录按钮会校验已有账号。
     */
    private void initListener() {
        btnRegisterLogin.setOnClickListener(v -> doRegisterAndLogin());
        btnLogin.setOnClickListener(v -> doLogin());
    }

    /**
     * 处理注册并登录流程。
     * 方法会校验必填项、检查 QQ 是否重复，写库成功后保存登录态并跳转主页。
     */
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
        // 注册信息写入 SQLite，成功后再保存登录态并进入主页面。
        long id = userDao.insertUser(u);
        if (id > 0) {
            // 新账号注册后固定使用 Zack 头像，用来区分新注册用户和已有用户。
            SpUtil.saveLogin(this, qq, nickname, SpUtil.AVATAR_ZACK);
            toast("注册成功，已登录");
            startActivity(new Intent(this, MainActivity.class));
            finish();
        } else {
            toast("注册失败（可能 QQ 重复）");
        }
    }

    /**
     * 处理已有账号登录流程。
     * 登录成功后把账号、昵称和头像类型保存起来，后续我的页面会读取这些信息。
     */
    private void doLogin() {
        String qq = etQq.getText().toString().trim();
        String pwd = etPwd.getText().toString();

        if (TextUtils.isEmpty(qq) || TextUtils.isEmpty(pwd)) {
            toast("请输入 QQ 和密码");
            return;
        }

        User u = userDao.login(qq, pwd);
        if (u != null) {
            // 登录成功后把关键信息放进 SharedPreferences，我的页面会读取这些信息展示用户栏。
            SpUtil.saveLogin(this, u.getQq(), u.getNickname(), SpUtil.AVATAR_AERITH);
            toast("登录成功");
            startActivity(new Intent(this, MainActivity.class));
            finish();
        } else {
            toast("账号或密码错误");
        }
    }

    /**
     * 汇总用户勾选的音乐偏好。
     * 多个复选框会拼成逗号分隔的字符串，注册时一起保存到用户表。
     */
    private String buildHobbies() {
        StringBuilder sb = new StringBuilder();
        // 把多个复选框选择拼成一个字符串存库，便于后续一次性读取用户偏好。
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
