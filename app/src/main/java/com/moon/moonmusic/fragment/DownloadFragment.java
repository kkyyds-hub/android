package com.moon.moonmusic.fragment;

import android.content.Intent;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.moon.moonmusic.R;
import com.moon.moonmusic.adapter.SongListAdapter;
import com.moon.moonmusic.constant.AppConstants;
import com.moon.moonmusic.data.SongRepository;
import com.moon.moonmusic.model.Song;
import com.moon.moonmusic.ui.PlayerActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DownloadFragment extends Fragment {

    private ListView lvDownloads;
    private final List<Song> data = new ArrayList<>();
    private SongListAdapter adapter;

    private final BroadcastReceiver downloadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(android.content.Context context, Intent intent) {
            if (intent == null || intent.getAction() == null) return;
            if (AppConstants.ACTION_DOWNLOAD_DONE.equals(intent.getAction())) {
                refresh();
                Toast.makeText(requireContext(), "下载完成，已刷新列表", Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_download, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        lvDownloads = view.findViewById(R.id.lv_downloads);
        adapter = new SongListAdapter(requireContext(), data);
        lvDownloads.setAdapter(adapter);
        refresh();
        lvDownloads.setOnItemClickListener((parent, v, position, id) -> {
            Song s = data.get(position);
            Intent it = new Intent(requireContext(), PlayerActivity.class);
            it.putExtra(PlayerActivity.EXTRA_SONG_ID, s.getId());
            it.putExtra(PlayerActivity.EXTRA_PLAY_FROM_DOWNLOAD, true);
            startActivity(it);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
        ContextCompat.registerReceiver(
                requireContext(),
                downloadReceiver,
                new IntentFilter(AppConstants.ACTION_DOWNLOAD_DONE),
                ContextCompat.RECEIVER_NOT_EXPORTED
        );
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            requireContext().unregisterReceiver(downloadReceiver);
        } catch (Exception ignored) {
        }
    }

    private void refresh() {
        data.clear();
        List<Song> all = SongRepository.getPlayableSongList();
        File dir = SongRepository.getDownloadDir(requireContext());
        if (dir == null) {
            Toast.makeText(requireContext(), "下载目录不可用", Toast.LENGTH_SHORT).show();
            return;
        }
        for (Song s : all) {
            File f = new File(dir, "song" + s.getId() + ".mp3");
            if (f.exists()) {
                data.add(s);
            }
        }
        if (adapter != null) adapter.notifyDataSetChanged();
    }
}
