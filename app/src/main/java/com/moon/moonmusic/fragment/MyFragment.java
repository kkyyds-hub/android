package com.moon.moonmusic.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.moon.moonmusic.R;
import com.moon.moonmusic.service.PlayerService;
import com.moon.moonmusic.ui.LoginActivity;
import com.moon.moonmusic.util.SpUtil;

/**
 * 我的页面：仿 QQ 音乐的“头像 + 昵称 + 下拉详情”
 * - 点击用户栏展开/收起详情（账号 + 退出登录）
 * - 已有账号登录：固定 Aerith 头像
 * - 新账号注册：固定 Zack 头像
 */
public class MyFragment extends Fragment {

    private Button btnFavorite, btnDownload;

    private LinearLayout llUserHeader;
    private LinearLayout llUserDetail;
    private ImageView ivUserAvatar;
    private ImageView ivUserArrow;
    private TextView tvUserNick;
    private TextView tvUserAccount;
    private Button btnLogout;

    private boolean detailShown = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        initData();
        initListener();
    }

    private void initView(View v) {
        btnFavorite = v.findViewById(R.id.btn_favorite);
        btnDownload = v.findViewById(R.id.btn_download);

        llUserHeader = v.findViewById(R.id.ll_user_header);
        llUserDetail = v.findViewById(R.id.ll_user_detail);
        ivUserAvatar = v.findViewById(R.id.iv_user_avatar);
        ivUserArrow = v.findViewById(R.id.iv_user_arrow);
        tvUserNick = v.findViewById(R.id.tv_user_nick);
        tvUserAccount = v.findViewById(R.id.tv_user_account);
        btnLogout = v.findViewById(R.id.btn_logout);
    }

    private void initData() {
        refreshUserBar(false);
        // 默认显示“喜欢”
        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.fl_my_container, new FavoriteFragment())
                .commit();
    }

    private void initListener() {
        btnFavorite.setOnClickListener(v -> getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.fl_my_container, new FavoriteFragment())
                .commit());

        btnDownload.setOnClickListener(v -> getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.fl_my_container, new DownloadFragment())
                .commit());

        llUserHeader.setOnClickListener(v -> {
            if (getContext() == null) return;
            String qq = SpUtil.getLoginQq(requireContext());
            if (qq == null || qq.trim().isEmpty()) {
                Toast.makeText(requireContext(), "未登录", Toast.LENGTH_SHORT).show();
                return;
            }
            toggleUserDetail();
        });

        btnLogout.setOnClickListener(v -> doLogout());
    }

    @Override
    public void onResume() {
        super.onResume();
        // 从登录页返回或切换页面后，确保信息最新
        refreshUserBar(true);
    }

    private void refreshUserBar(boolean closeDetail) {
        if (getContext() == null) return;

        String qq = SpUtil.getLoginQq(requireContext());
        String nick = SpUtil.getLoginNick(requireContext());
        String avatarType = SpUtil.getAvatarType(requireContext());

        if (qq == null || qq.trim().isEmpty()) {
            tvUserNick.setText("未登录");
            tvUserAccount.setText("账号：-");
            ivUserAvatar.setImageResource(R.drawable.avatar_aerith);
            detailShown = false;
            llUserDetail.setVisibility(View.GONE);
            ivUserArrow.setImageResource(R.drawable.ic_chevron_down);
            return;
        }

        nick = nick == null ? "" : nick.trim();
        tvUserNick.setText(nick.isEmpty() ? "用户" : nick);
        tvUserAccount.setText("账号：" + qq);

        int avatarRes = SpUtil.AVATAR_ZACK.equals(avatarType)
                ? R.drawable.avatar_zack
                : R.drawable.avatar_aerith;
        ivUserAvatar.setImageResource(avatarRes);

        if (closeDetail) {
            detailShown = false;
            llUserDetail.setVisibility(View.GONE);
            ivUserArrow.setImageResource(R.drawable.ic_chevron_down);
        } else {
            ivUserArrow.setImageResource(detailShown ? R.drawable.ic_chevron_up : R.drawable.ic_chevron_down);
            llUserDetail.setVisibility(detailShown ? View.VISIBLE : View.GONE);
        }
    }

    private void toggleUserDetail() {
        if (llUserDetail == null) return;

        detailShown = !detailShown;
        if (detailShown) {
            ivUserArrow.setImageResource(R.drawable.ic_chevron_up);
            llUserDetail.setVisibility(View.VISIBLE);
            llUserDetail.setAlpha(0f);
            llUserDetail.setTranslationY(-dp(6));
            llUserDetail.animate()
                    .alpha(1f)
                    .translationY(0)
                    .setDuration(180)
                    .start();
        } else {
            ivUserArrow.setImageResource(R.drawable.ic_chevron_down);
            llUserDetail.animate()
                    .alpha(0f)
                    .translationY(-dp(6))
                    .setDuration(160)
                    .withEndAction(() -> {
                        llUserDetail.setVisibility(View.GONE);
                        llUserDetail.setAlpha(1f);
                        llUserDetail.setTranslationY(0);
                    })
                    .start();
        }
    }

    private float dp(float dp) {
        if (getResources() == null) return dp;
        return dp * getResources().getDisplayMetrics().density;
    }

    private void doLogout() {
        if (getContext() == null) return;
        // 清理登录态
        SpUtil.clearLogin(requireContext());
        // 退出时停止播放，避免“退出登录后还在播放”造成验收质疑
        try {
            requireContext().stopService(new Intent(requireContext(), PlayerService.class));
        } catch (Exception ignored) {
        }

        Toast.makeText(requireContext(), "已退出登录", Toast.LENGTH_SHORT).show();

        // 回到登录页，并清空回退栈
        Intent it = new Intent(requireContext(), LoginActivity.class);
        it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(it);
        if (getActivity() != null) {
            getActivity().finish();
        }
    }
}
