package com.openwatchproject.launcher.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.openwatchproject.launcher.ClockSkin;
import com.openwatchproject.launcher.listener.ClockSkinInfoClickListener;
import com.openwatchproject.launcher.R;

import java.util.List;

public class ClockSkinPagerAdapter extends PagerAdapter {

    private ClockSkinInfoClickListener clickListener;
    private List<ClockSkin> clockSkins;

    public ClockSkinPagerAdapter(List<ClockSkin> clockSkins, ClockSkinInfoClickListener clickListener) {
        this.clockSkins = clockSkins;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View view = LayoutInflater.from(container.getContext())
                .inflate(R.layout.clockskin_item, container, false);

        ClockSkin clockSkin = clockSkins.get(position);

        TextView name = view.findViewById(R.id.name);
        name.setSelected(true);
        ImageView preview = view.findViewById(R.id.preview);
        ImageView removeButton = view.findViewById(R.id.remove_button);

        name.setText(clockSkin.getFile().getName());
        preview.setImageBitmap(clockSkin.getPreview());
        view.setOnClickListener(v -> clickListener.onClick(clockSkin));

        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        return clockSkins.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }
}