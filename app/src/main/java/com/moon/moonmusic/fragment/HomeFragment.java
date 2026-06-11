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
        data = SongRepository.getDownloadSongList();
        adapter = new SongListAdapter(requireContext(), data);
        lvSongs.setAdapter(adapter);
    }

    private void initListener() {
        lvSongs.setOnItemClickListener((parent, view, position, id) -> {
            Song s = data.get(position);
            Intent it = new Intent(requireContext(), PlayerActivity.class);
            it.putExtra(PlayerActivity.EXTRA_SONG_ID, s.getId());
            startActivity(it);
        });
    }
}
