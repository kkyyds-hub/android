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

    private void initData() {
        tvAlbums.setText(NicholasRepository.getAlbumWallIntro());
        tvFeatures.setText(NicholasRepository.getRecommendationIntro());
        songData = SongRepository.getNicholasSongList();
        lvSongs.setAdapter(new SongListAdapter(requireContext(), songData));
        renderLocationRecommendation(LocationPlaylistRepository.getDefaultRecommendation());
        loadNetworkRecommendation();
    }

    private void initListener() {
        btnDetail.setOnClickListener(v -> openDetail());
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
            it.putExtra(PlayerActivity.EXTRA_SONG_ID, song.getId());
            startActivity(it);
        });
    }

    private void loadNetworkRecommendation() {
        renderNetworkLoading();
        pulseNetworkCard();
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

    private void renderNetworkRecommendation(RemoteRecommendation recommendation) {
        if (recommendation == null) return;
        currentRecommendation = recommendation;
        tvNetworkTag.setText(recommendation.getTag());
        tvNetworkTitle.setText(recommendation.getTitle());
        tvNetworkReason.setText(recommendation.getReason());
        String mode = recommendation.isFromNetwork() ? "在线曲库" : "本地精选";
        tvNetworkSource.setText(recommendation.getSource() + " · " + mode);
        renderNetworkCover(recommendation);
    }

    private void renderNetworkCover(RemoteRecommendation recommendation) {
        setLocalNetworkCover(recommendation.getLocalArtworkName());
        String artworkUrl = recommendation.getArtworkUrl();
        if (artworkUrl == null || artworkUrl.trim().isEmpty()) return;
        new Thread(() -> {
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

    private Bitmap downloadBitmap(String urlText) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(urlText);
            connection = (HttpURLConnection) url.openConnection();
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

    private void handleLocationPlaylistRequest() {
        if (hasLocationPermission()) {
            loadLocationPlaylist();
            return;
        }
        requestPermissions(
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                REQUEST_LOCATION_PERMISSION
        );
    }

    @SuppressLint("MissingPermission")
    private void loadLocationPlaylist() {
        LocationManager locationManager = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);
        Location location = getBestLastKnownLocation(locationManager);
        if (location == null) {
            renderLocationRecommendation(LocationPlaylistRepository.getDefaultRecommendation());
            pulseLocationCard();
            toast("暂未获取到设备位置，已使用默认城市歌单");
            return;
        }
        renderLocationRecommendation(LocationPlaylistRepository.buildFromLocation(location.getLatitude(), location.getLongitude()));
        pulseLocationCard();
        toast("已根据当前位置更新歌单");
    }

    @SuppressLint("MissingPermission")
    private Location getBestLastKnownLocation(LocationManager locationManager) {
        if (locationManager == null) return null;
        String[] providers = new String[]{
                LocationManager.GPS_PROVIDER,
                LocationManager.NETWORK_PROVIDER,
                LocationManager.PASSIVE_PROVIDER
        };
        Location bestLocation = null;
        for (String provider : providers) {
            try {
                Location location = locationManager.getLastKnownLocation(provider);
                if (location != null && (bestLocation == null || location.getTime() > bestLocation.getTime())) {
                    bestLocation = location;
                }
            } catch (IllegalArgumentException | SecurityException ignored) {
                // Some devices do not expose every provider; skip unavailable providers.
            }
        }
        return bestLocation;
    }

    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void renderLocationRecommendation(LocationPlaylistRecommendation recommendation) {
        if (recommendation == null) return;
        currentLocationRecommendation = recommendation;
        tvLocationTag.setText(recommendation.getTag());
        tvLocationTitle.setText(recommendation.getTitle());
        tvLocationReason.setText(recommendation.getReason());
        String mode = recommendation.isFromLocation() ? "设备位置" : "本地兜底";
        tvLocationSource.setText("来源：" + recommendation.getSource() + " · " + mode);
        btnLocation.setText(recommendation.isFromLocation() ? "重新定位" : "定位歌单");
    }

    private void openLocationSong() {
        LocationPlaylistRecommendation recommendation = currentLocationRecommendation;
        if (recommendation == null) {
            recommendation = LocationPlaylistRepository.getDefaultRecommendation();
        }
        Intent it = new Intent(requireContext(), PlayerActivity.class);
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
            renderLocationRecommendation(LocationPlaylistRepository.getPermissionDeniedRecommendation());
            pulseLocationCard();
            toast("定位权限未开启，已使用手动体验歌单");
        }
    }

    private void openDetail() {
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

    private void playEntranceAnimation() {
        View[] cards = new View[]{heroCard, scoreCard, exploreCard, albumsCard, songsCard};
        for (int i = 0; i < cards.length; i++) {
            View card = cards[i];
            if (card == null) continue;
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

    private void addPressAnimation(View view) {
        if (view == null) return;
        view.setOnTouchListener((v, event) -> {
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
