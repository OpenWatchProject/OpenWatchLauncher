package org.openwatchproject.launcher.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import org.openwatchproject.launcher.R;
import org.openwatchproject.launcher.Utils;
import org.openwatchproject.launcher.activity.FullscreenDialogActivity;
import org.openwatchproject.launcher.listener.ClockSkinInfoClickListener;
import org.openwatchproject.openwatchfaceview.OpenWatchFaceFile;

import java.util.List;

public class ClockSkinPagerAdapter extends PagerAdapter {

    private ClockSkinInfoClickListener clickListener;
    private List<OpenWatchFaceFile> watchFace;

    public ClockSkinPagerAdapter(List<OpenWatchFaceFile> clockSkins, ClockSkinInfoClickListener clickListener) {
        this.watchFace = clockSkins;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        View view = LayoutInflater.from(container.getContext()).inflate(R.layout.clockskin_item, container, false);

        OpenWatchFaceFile watchFace = this.watchFace.get(position);

        final TextView name = view.findViewById(R.id.name);
        name.setSelected(true);
        Utils.fixRoundScreenWidth(name);
        final TextView metadata = view.findViewById(R.id.metadata);
        Utils.fixRoundScreenWidth(metadata);
        final ImageView preview = view.findViewById(R.id.preview);
        final ImageView removeButton = view.findViewById(R.id.remove_button);

        name.setText(watchFace.getMetadata().getName());
        if (watchFace.getMetadata().getAuthor().isEmpty()) {
            metadata.setText("v" + watchFace.getMetadata().getVersion());
        } else {
            metadata.setText("v" + watchFace.getMetadata().getVersion() + " - " + watchFace.getMetadata().getAuthor());
        }
        preview.setImageBitmap(watchFace.getPreview());
        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new FullscreenDialogActivity(container.getContext())
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
            OpenWatchFaceFile owwf = watchFace.get(i);
            if (owwf.getFile().getUri().toString().equals(object)) {
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