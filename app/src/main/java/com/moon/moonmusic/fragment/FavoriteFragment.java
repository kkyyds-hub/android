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

/**
 * 喜欢页：使用 ListView 展示收藏歌曲，长按删除后通知其它页面刷新。
 * 当前收藏列表主要用于页面展示，所以点击时只提示未提供音频资源。
 */
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
            // 收藏页的部分歌曲没有放入音频文件，点击时用 Toast 说明，避免误跳播放器报错。
            Toast.makeText(requireContext(), "演示歌曲：" + s.getTitle() + "（未提供音频资源）", Toast.LENGTH_SHORT).show();
        });

        // 长按删除（仅内存删除）：删除后刷新 Adapter，并发广播通知其它页面。
        lvFavorites.setOnItemLongClickListener((parent, v, position, id) -> {
            Song s = data.remove(position);
            adapter.notifyDataSetChanged();
            Toast.makeText(requireContext(), "已移除：" + s.getTitle(), Toast.LENGTH_SHORT).show();
            requireContext().sendBroadcast(new android.content.Intent(AppConstants.ACTION_FAVORITE_CHANGED));
            return true;
        });
    }
}
