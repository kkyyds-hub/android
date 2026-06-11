package com.moon.moonmusic.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.moon.moonmusic.R;
import com.moon.moonmusic.adapter.SongListAdapter;
import com.moon.moonmusic.constant.AppConstants;
import com.moon.moonmusic.data.SongRepository;
import com.moon.moonmusic.model.Song;

import java.util.List;

public class FavoriteFragment extends Fragment {

    private ListView lvFavorites;
    private List<Song> data;
    private SongListAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_favorite, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        lvFavorites = view.findViewById(R.id.lv_favorites);

        data = SongRepository.getFavoriteList();
        adapter = new SongListAdapter(requireContext(), data);
        lvFavorites.setAdapter(adapter);

        lvFavorites.setOnItemClickListener((parent, v, position, id) -> {
            Song s = data.get(position);
            Toast.makeText(requireContext(), "演示歌曲：" + s.getTitle() + "（未提供音频资源）", Toast.LENGTH_SHORT).show();
        });

        // 长按删除（仅内存删除）：用于演示 ListView + Adapter + 广播通知
        lvFavorites.setOnItemLongClickListener((parent, v, position, id) -> {
            Song s = data.remove(position);
            adapter.notifyDataSetChanged();
            Toast.makeText(requireContext(), "已移除：" + s.getTitle(), Toast.LENGTH_SHORT).show();
            requireContext().sendBroadcast(new android.content.Intent(AppConstants.ACTION_FAVORITE_CHANGED));
            return true;
        });
    }
}
