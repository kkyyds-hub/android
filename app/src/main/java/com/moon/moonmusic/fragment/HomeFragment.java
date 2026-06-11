package com.moon.moonmusic.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.moon.moonmusic.R;
import com.moon.moonmusic.adapter.SongListAdapter;
import com.moon.moonmusic.data.SongRepository;
import com.moon.moonmusic.model.Song;
import com.moon.moonmusic.ui.PlayerActivity;

import java.util.List;

/**
 * 首页歌曲列表：展示本地准备好的歌曲数据，点击条目后跳转到播放器页面。
 * 这里适合讲 ListView + Adapter + Intent 传参三个知识点如何连起来。
 */
public class HomeFragment extends Fragment {

    private ListView lvSongs;
    private SongListAdapter adapter;
    private List<Song> data;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        initData();
        initListener();
    }

    private void initView(View v) {
        lvSongs = v.findViewById(R.id.lv_download_songs);
    }

    private void initData() {
        // 首页复用 SongRepository 中的演示歌曲，Adapter 负责把每首歌渲染成 item_song 布局。
        data = SongRepository.getDownloadSongList();
        adapter = new SongListAdapter(requireContext(), data);
        lvSongs.setAdapter(adapter);
    }

    private void initListener() {
        lvSongs.setOnItemClickListener((parent, view, position, id) -> {
            Song s = data.get(position);
            // 用户点击歌曲后，用 Intent 带上歌曲 id，PlayerActivity 再根据 id 找到对应音频和歌词。
            Intent it = new Intent(requireContext(), PlayerActivity.class);
            it.putExtra(PlayerActivity.EXTRA_SONG_ID, s.getId());
            startActivity(it);
        });
    }
}
