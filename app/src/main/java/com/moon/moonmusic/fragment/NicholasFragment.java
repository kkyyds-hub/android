package com.moon.moonmusic.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.moon.moonmusic.R;
import com.moon.moonmusic.adapter.SongListAdapter;
import com.moon.moonmusic.data.LocationPlaylistRepository;
import com.moon.moonmusic.data.NicholasRepository;
import com.moon.moonmusic.data.RecommendationRepository;
import com.moon.moonmusic.data.SongRepository;
import com.moon.moonmusic.model.LocationPlaylistRecommendation;
import com.moon.moonmusic.model.RemoteRecommendation;
import com.moon.moonmusic.model.Song;
import com.moon.moonmusic.ui.PlayerActivity;
import com.moon.moonmusic.ui.RecommendationDetailActivity;
import com.moon.moonmusic.ui.VideoActivity;

import java.util.List;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 谢霆锋专区：把网络推荐、定位歌单、歌曲列表、详情页和视频页入口集中到一个页面。
 * 包含网络推荐、网络图片下载、定位歌单、ListView 歌曲跳转、WebView 详情页和 VideoView 视频页入口。
 */
public class NicholasFragment extends Fragment {

    private static final int REQUEST_LOCATION_PERMISSION = 701;

    private TextView tvAlbums;
    private TextView tvFeatures;
    private TextView tvNetworkTag;
    private TextView tvNetworkTitle;
    private TextView tvNetworkReason;
    private TextView tvNetworkSource;
    private ImageView ivNetworkCover;
    private TextView tvLocationTag;
    private TextView tvLocationTitle;
    private TextView tvLocationReason;
    private TextView tvLocationSource;
    private ListView lvSongs;
    private View heroCard;
    private View scoreCard;
    private View albumsCard;
    private View songsCard;
    private View networkCard;
    private View locationCard;
    private View exploreCard;
    private View btnDetail;
    private View btnVideo;
    private Button btnNetwork;
    private Button btnLocation;
    private List<Song> songData;
    private RemoteRecommendation currentRecommendation;
    private LocationPlaylistRecommendation currentLocationRecommendation;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_nicholas, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        initData();
        initListener();
        playEntranceAnimation();
    }

    private void initView(View view) {
        heroCard = view.findViewById(R.id.ll_nicholas_hero);
        scoreCard = view.findViewById(R.id.ll_nicholas_score);
        albumsCard = view.findViewById(R.id.ll_nicholas_albums);
        songsCard = view.findViewById(R.id.ll_nicholas_songs);
        networkCard = view.findViewById(R.id.ll_nicholas_network_card);
        locationCard = view.findViewById(R.id.ll_nicholas_location_card);
        exploreCard = view.findViewById(R.id.ll_nicholas_explore);
        tvAlbums = view.findViewById(R.id.tv_nicholas_albums);
        tvFeatures = view.findViewById(R.id.tv_nicholas_features);
        tvNetworkTag = view.findViewById(R.id.tv_nicholas_network_tag);
        tvNetworkTitle = view.findViewById(R.id.tv_nicholas_network_title);
        tvNetworkReason = view.findViewById(R.id.tv_nicholas_network_reason);
        tvNetworkSource = view.findViewById(R.id.tv_nicholas_network_source);
        ivNetworkCover = view.findViewById(R.id.iv_nicholas_network_cover);
        tvLocationTag = view.findViewById(R.id.tv_nicholas_location_tag);
        tvLocationTitle = view.findViewById(R.id.tv_nicholas_location_title);
        tvLocationReason = view.findViewById(R.id.tv_nicholas_location_reason);
        tvLocationSource = view.findViewById(R.id.tv_nicholas_location_source);
        lvSongs = view.findViewById(R.id.lv_nicholas_songs);
        btnDetail = view.findViewById(R.id.btn_nicholas_detail);
        btnVideo = view.findViewById(R.id.btn_nicholas_video);
        btnNetwork = view.findViewById(R.id.btn_nicholas_network);
        btnLocation = view.findViewById(R.id.btn_nicholas_location);
    }

    /**
     * 准备专区页初始数据。
     * 这里会加载静态介绍、专区歌曲列表、默认定位推荐，并立即发起一次网络推荐请求。
     */
    private void initData() {
        // 静态文案来自 Repository，页面只负责展示，数据层和界面层保持分工。
        tvAlbums.setText(NicholasRepository.getAlbumWallIntro());
        tvFeatures.setText(NicholasRepository.getRecommendationIntro());
        songData = SongRepository.getNicholasSongList();
        lvSongs.setAdapter(new SongListAdapter(requireContext(), songData));
        // 定位推荐先显示默认内容，用户点按钮授权后再尝试按真实位置更新。
        renderLocationRecommendation(LocationPlaylistRepository.getDefaultRecommendation());
        loadNetworkRecommendation();
    }

    /**
     * 注册专区页中的跳转、刷新和播放入口。
     * 用户点击不同卡片时，会分别进入详情页、视频页、播放器或触发定位推荐。
     */
    private void initListener() {
        btnDetail.setOnClickListener(v -> openDetail());
        // 视频页入口：从专区跳转到 VideoActivity，播放本地视频并联动音浪 View。
        btnVideo.setOnClickListener(v -> startActivity(new Intent(requireContext(), VideoActivity.class)));
        btnNetwork.setOnClickListener(v -> loadNetworkRecommendation());
        btnLocation.setOnClickListener(v -> handleLocationPlaylistRequest());
        locationCard.setOnClickListener(v -> openLocationSong());
        addPressAnimation(btnDetail);
        addPressAnimation(btnVideo);
        addPressAnimation(btnNetwork);
        addPressAnimation(btnLocation);
        addPressAnimation(networkCard);
        addPressAnimation(locationCard);
        lvSongs.setOnItemClickListener((parent, view, position, id) -> {
            Song song = songData.get(position);
            Intent it = new Intent(requireContext(), PlayerActivity.class);
            // 点击专区歌曲后跳到播放器，PlayerActivity 再通过 songId 找到 assets 中的音频和歌词。
            it.putExtra(PlayerActivity.EXTRA_SONG_ID, song.getId());
            startActivity(it);
        });
    }

    /**
     * 加载线上推荐歌曲。
     * 请求由 RecommendationRepository 在子线程执行，拿到结果后切回主线程刷新推荐卡片。
     */
    private void loadNetworkRecommendation() {
        renderNetworkLoading();
        pulseNetworkCard();
        // HTTP 请求在 Repository 的子线程里执行，返回后必须切回主线程更新 TextView/ImageView。
        RecommendationRepository.loadTodayRecommendation(recommendation -> {
            if (getActivity() == null) return;
            getActivity().runOnUiThread(() -> renderNetworkRecommendation(recommendation));
        });
    }

    private void renderNetworkLoading() {
        tvNetworkTag.setText("在线精选");
        tvNetworkTitle.setText("正在挑选今日推荐...");
        tvNetworkReason.setText("从线上曲库里挑一首适合现在播放的谢霆锋。");
        tvNetworkSource.setText("稍后呈现");
    }

    /**
     * 把推荐对象渲染到网络推荐卡片。
     * 文案更新完成后会继续处理封面，网络封面不可用时保留本地封面。
     */
    private void renderNetworkRecommendation(RemoteRecommendation recommendation) {
        if (recommendation == null) return;
        currentRecommendation = recommendation;
        // 网络 JSON 解析后的结果统一映射成 RemoteRecommendation，页面不直接处理原始 JSON。
        tvNetworkTag.setText(recommendation.getTag());
        tvNetworkTitle.setText(recommendation.getTitle());
        tvNetworkReason.setText(recommendation.getReason());
        String mode = recommendation.isFromNetwork() ? "在线曲库" : "本地精选";
        tvNetworkSource.setText(recommendation.getSource() + " · " + mode);
        renderNetworkCover(recommendation);
    }

    /**
     * 渲染推荐封面。
     * 先放本地封面保证页面稳定，再尝试下载网络封面并回到主线程替换图片。
     */
    private void renderNetworkCover(RemoteRecommendation recommendation) {
        // 先显示本地封面，保证网络慢或断网时卡片也不空白。
        setLocalNetworkCover(recommendation.getLocalArtworkName());
        String artworkUrl = recommendation.getArtworkUrl();
        if (artworkUrl == null || artworkUrl.trim().isEmpty()) return;
        new Thread(() -> {
            // 网络图片下载同样不能放主线程；Bitmap 下载完成后再回到主线程设置 ImageView。
            Bitmap bitmap = downloadBitmap(artworkUrl);
            if (bitmap == null || getActivity() == null) return;
            getActivity().runOnUiThread(() -> ivNetworkCover.setImageBitmap(bitmap));
        }).start();
    }

    private void setLocalNetworkCover(String artworkName) {
        int resId = getLocalArtworkResId(artworkName);
        ivNetworkCover.setImageResource(resId);
    }

    private int getLocalArtworkResId(String artworkName) {
        if ("nicholas_album_understand".equals(artworkName)) return R.drawable.nicholas_album_understand;
        if ("nicholas_album_thanks_1999".equals(artworkName)) return R.drawable.nicholas_album_thanks_1999;
        if ("nicholas_album_jade_butterfly".equals(artworkName)) return R.drawable.nicholas_album_jade_butterfly;
        if ("nicholas_album_me".equals(artworkName)) return R.drawable.nicholas_album_me;
        return R.drawable.nicholas_album_viva;
    }

    /**
     * 下载并解码网络封面图片。
     * renderNetworkCover 会在子线程调用它，失败时返回 null 并继续使用本地封面。
     */
    private Bitmap downloadBitmap(String urlText) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(urlText);
            connection = (HttpURLConnection) url.openConnection();
            // 这里先通过 HTTP 拿到图片流，再用 BitmapFactory 解码成 Bitmap。
            connection.setConnectTimeout(4000);
            connection.setReadTimeout(4000);
            connection.setRequestProperty("Accept", "image/*");
            return BitmapFactory.decodeStream(connection.getInputStream());
        } catch (Exception e) {
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * 处理定位歌单按钮点击。
     * 已有定位权限时直接读取位置，否则发起运行时权限申请。
     */
    private void handleLocationPlaylistRequest() {
        if (hasLocationPermission()) {
            loadLocationPlaylist();
            return;
        }
        // 定位属于危险权限，必须运行时申请；用户同意后回调 onRequestPermissionsResult。
        requestPermissions(
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                REQUEST_LOCATION_PERMISSION
        );
    }

    /**
     * 读取设备最近位置并生成定位歌单。
     * 如果暂时拿不到位置，就回退到默认推荐，保证卡片仍有可点击内容。
     */
    @SuppressLint("MissingPermission")
    private void loadLocationPlaylist() {
        LocationManager locationManager = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);
        Location location = getBestLastKnownLocation(locationManager);
        if (location == null) {
            // 真机可能暂时没有缓存定位，使用默认歌单保证页面仍有可用内容。
            renderLocationRecommendation(LocationPlaylistRepository.getDefaultRecommendation());
            pulseLocationCard();
            toast("暂未获取到设备位置，已使用默认城市歌单");
            return;
        }
        renderLocationRecommendation(LocationPlaylistRepository.buildFromLocation(location.getLatitude(), location.getLongitude()));
        pulseLocationCard();
        toast("已根据当前位置更新歌单");
    }

    /**
     * 从多个定位来源中选择最近一次可用位置。
     * loadLocationPlaylist 会调用这里，优先使用时间最新的定位结果。
     */
    @SuppressLint("MissingPermission")
    private Location getBestLastKnownLocation(LocationManager locationManager) {
        if (locationManager == null) return null;
        String[] providers = new String[]{
                LocationManager.GPS_PROVIDER,
                LocationManager.NETWORK_PROVIDER,
                LocationManager.PASSIVE_PROVIDER
        };
        Location bestLocation = null;
        // 同时尝试 GPS、网络和被动定位，取时间最新的一条，比只查一个来源更稳定。
        for (String provider : providers) {
            try {
                Location location = locationManager.getLastKnownLocation(provider);
                if (location != null && (bestLocation == null || location.getTime() > bestLocation.getTime())) {
                    bestLocation = location;
                }
            } catch (IllegalArgumentException | SecurityException ignored) {
                // 有些设备不支持所有 provider，跳过不可用来源即可。
            }
        }
        return bestLocation;
    }

    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * 把定位推荐结果渲染到定位歌单卡片。
     * 同时记录当前推荐，用户点击卡片时会根据它跳转到播放器。
     */
    private void renderLocationRecommendation(LocationPlaylistRecommendation recommendation) {
        if (recommendation == null) return;
        currentLocationRecommendation = recommendation;
        tvLocationTag.setText(recommendation.getTag());
        tvLocationTitle.setText(recommendation.getTitle());
        tvLocationReason.setText(recommendation.getReason());
        String mode = recommendation.isFromLocation() ? "设备位置" : "本地兜底";
        // 页面上显示来源，让用户知道当前是定位结果还是兜底推荐。
        tvLocationSource.setText("来源：" + recommendation.getSource() + " · " + mode);
        btnLocation.setText(recommendation.isFromLocation() ? "重新定位" : "定位歌单");
    }

    /**
     * 打开当前定位歌单对应的主打歌曲。
     * 如果当前位置推荐还没准备好，就使用默认推荐作为兜底。
     */
    private void openLocationSong() {
        LocationPlaylistRecommendation recommendation = currentLocationRecommendation;
        if (recommendation == null) {
            recommendation = LocationPlaylistRepository.getDefaultRecommendation();
        }
        Intent it = new Intent(requireContext(), PlayerActivity.class);
        // 点击定位歌单卡片时，直接播放推荐结果里配置的主打歌曲。
        it.putExtra(PlayerActivity.EXTRA_SONG_ID, recommendation.getPrimarySongId());
        startActivity(it);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != REQUEST_LOCATION_PERMISSION) return;
        boolean granted = false;
        for (int result : grantResults) {
            if (result == PackageManager.PERMISSION_GRANTED) {
                granted = true;
                break;
            }
        }
        if (granted) {
            loadLocationPlaylist();
        } else {
            // 用户拒绝权限时仍给出可体验的本地歌单，不让页面停在空状态。
            renderLocationRecommendation(LocationPlaylistRepository.getPermissionDeniedRecommendation());
            pulseLocationCard();
            toast("定位权限未开启，已使用手动体验歌单");
        }
    }

    /**
     * 打开艺人档案页。
     * 详情页内部会加载本地 H5，用于承载更完整的图文资料。
     */
    private void openDetail() {
        // 详情页使用 WebView 加载本地 H5，承载更完整的图文资料。
        startActivity(new Intent(requireContext(), RecommendationDetailActivity.class));
    }

    private String joinWithDot(List<String> values) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) sb.append(" · ");
            sb.append(values.get(i));
        }
        return sb.toString();
    }

    private void toast(String text) {
        Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT).show();
    }

    /**
     * 播放专区页卡片的入场动画。
     * onViewCreated 初始化完成后调用，让主要内容依次出现。
     */
    private void playEntranceAnimation() {
        View[] cards = new View[]{heroCard, scoreCard, exploreCard, albumsCard, songsCard};
        for (int i = 0; i < cards.length; i++) {
            View card = cards[i];
            if (card == null) continue;
            // 页面进入时让卡片依次上浮，属于普通 View 动画，不改变业务数据。
            card.setAlpha(0f);
            card.setTranslationY(dp(18));
            card.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setStartDelay(i * 90L)
                    .setDuration(320)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
        }
    }

    /**
     * 给可点击区域添加按压缩放反馈。
     * initListener 会给按钮和卡片调用它，让触摸反馈保持一致。
     */
    private void addPressAnimation(View view) {
        if (view == null) return;
        view.setOnTouchListener((v, event) -> {
            // 给可点击卡片加轻微缩放反馈，用户能直观看到自己点到了哪个功能入口。
            if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                v.animate().scaleX(0.97f).scaleY(0.97f).setDuration(90).start();
            } else if (event.getAction() == android.view.MotionEvent.ACTION_UP
                    || event.getAction() == android.view.MotionEvent.ACTION_CANCEL) {
                v.animate().scaleX(1f).scaleY(1f).setDuration(120).start();
            }
            return false;
        });
    }

    private void pulseNetworkCard() {
        if (networkCard == null) return;
        networkCard.animate()
                .scaleX(1.02f)
                .scaleY(1.02f)
                .setDuration(140)
                .withEndAction(() -> networkCard.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(160)
                        .start())
                .start();
    }

    private void pulseLocationCard() {
        if (locationCard == null) return;
        locationCard.animate()
                .scaleX(1.02f)
                .scaleY(1.02f)
                .setDuration(140)
                .withEndAction(() -> locationCard.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(160)
                        .start())
                .start();
    }

    private float dp(float value) {
        if (getResources() == null) return value;
        return value * getResources().getDisplayMetrics().density;
    }
}
