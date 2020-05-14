package com.openwatchproject.launcher.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.openwatchproject.launcher.Utils;
import com.openwatchproject.launcher.activity.FullscreenDialog;
import com.openwatchproject.launcher.databinding.ClockskinItemBinding;
import com.openwatchproject.launcher.listener.ClockSkinInfoClickListener;
import com.openwatchproject.watchface.OpenWatchWatchFace;
import com.openwatchproject.watchface.OpenWatchWatchFaceFile;

import java.util.List;

public class ClockSkinPagerAdapter extends PagerAdapter {

    private ClockSkinInfoClickListener clickListener;
    private List<OpenWatchWatchFaceFile> watchFace;

    public ClockSkinPagerAdapter(List<OpenWatchWatchFaceFile> clockSkins, ClockSkinInfoClickListener clickListener) {
        this.watchFace = clockSkins;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        ClockskinItemBinding binding = ClockskinItemBinding.inflate(LayoutInflater.from(container.getContext()), container, false);

        OpenWatchWatchFaceFile watchFace = this.watchFace.get(position);

        final TextView name = binding.name;
        name.setSelected(true);
        final ImageView preview = binding.preview;
        final ImageView removeButton = binding.removeButton;

        name.setText(watchFace.getMetadata().getName());
        preview.setImageBitmap(watchFace.getPreview());
        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new FullscreenDialog(container.getContext())
                        .setTitle("Delete ClockSkin?")
                        .setDescription("Do you really want to delete " + watchFace.getMetadata().getName() + "?")
                        .setPositiveButtonOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //Utils.deleteFile(watchFace.getName());
                                ClockSkinPagerAdapter.this.watchFace.remove(position);
                                notifyDataSetChanged();
                            }
                        })
                        .setNegativeButtonOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                            }
                        })
                        .show();
            }
        });

        final View view = binding.getRoot();
        view.setOnClickListener(v -> clickListener.onClick(watchFace));
        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        return watchFace.size();
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        for (int i = 0; i < watchFace.size(); i++) {
            OpenWatchWatchFaceFile owwf = watchFace.get(i);
            if (owwf.getMetadata().getName().equals(object)) {
                return i;
            }
        }

        return 0;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }
}