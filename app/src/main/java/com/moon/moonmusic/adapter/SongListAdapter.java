package com.moon.moonmusic.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.moon.moonmusic.R;
import com.moon.moonmusic.model.Song;

import java.util.List;

/**
 * 歌曲列表适配器：把 Song 数据对象转换成 ListView 里能看到的一行歌曲卡片。
 * Adapter 是“数据”和“界面控件”之间的桥梁，列表页只需要提供 Song 数据。
 */
public class SongListAdapter extends BaseAdapter {

    private final Context context;
    private final List<Song> data;

    public SongListAdapter(Context context, List<Song> data) {
        this.context = context;
        this.data = data;
    }

    @Override
    public int getCount() {
        return data == null ? 0 : data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    static class ViewHolder {
        ImageView ivCover;
        TextView tvTitle;
        TextView tvSubtitle;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder vh;
        if (convertView == null) {
            // convertView 为空时才加载 XML；滑动列表时复用旧 View，避免频繁创建控件导致卡顿。
            convertView = LayoutInflater.from(context).inflate(R.layout.item_song, parent, false);
            vh = new ViewHolder();
            vh.ivCover = convertView.findViewById(R.id.iv_cover);
            vh.tvTitle = convertView.findViewById(R.id.tv_title);
            vh.tvSubtitle = convertView.findViewById(R.id.tv_subtitle);
            convertView.setTag(vh);
        } else {
            // ViewHolder 保存子控件引用，复用时不用重复 findViewById，这是 ListView 常见优化写法。
            vh = (ViewHolder) convertView.getTag();
        }

        Song s = data.get(position);
        vh.ivCover.setImageResource(s.getCoverResId());
        vh.tvTitle.setText(s.getTitle());
        vh.tvSubtitle.setText(s.getSubtitle());
        return convertView;
    }
}
