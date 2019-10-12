package com.openwatchproject.launcher.Fragment;

import android.graphics.Point;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.openwatchproject.launcher.R;

public class ClockSkinFragment extends Fragment {
    private static final String TAG = "ClockSkinFragment";

    private AnimationDrawable animationDrawable;

    private ImageView imageView;
    private TextView textView;
    private LoadAnimation loadAnimation;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_clock_skin, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        imageView = view.findViewById(R.id.imageView);
        textView = view.findViewById(R.id.loading);
        Point displaySize = getDisplaySize();
        imageView.getLayoutParams().height = displaySize.x;
        imageView.getLayoutParams().width = displaySize.y;
        imageView.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.clock_skin_model));
        textView.setVisibility(View.GONE);
    }

    @Override
    public void onDestroyView() {
        imageView = null;
        textView = null;
        super.onDestroyView();
    }

    private Point getDisplaySize() {
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getRealSize(size);
        return size;
    }

    private int getMinDisplaySize() {
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getRealSize(size);
        return Math.min(size.x, size.y);
    }

    @DrawableRes
    public int getDrawableRes(String name) {
        try {
            return R.drawable.class.getField(name).getInt(null);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            return getResources().getIdentifier(name, "drawable", getContext().getPackageName());
        }
    }

    public class LoadAnimation extends AsyncTask<Void, Void, AnimationDrawable> {
        @Override
        protected AnimationDrawable doInBackground(Void... voids) {
            AnimationDrawable animation = new AnimationDrawable();
            animation.setOneShot(false);
            for (int i = 0; i < 25; i++) {
                BitmapDrawable f = (BitmapDrawable) ContextCompat.getDrawable(getContext(),
                        getDrawableRes("animation_1_" + i));
                animation.addFrame(f, 100);
            }
            return animation;
        }

        @Override
        protected void onPostExecute(AnimationDrawable animationDrawable) {
            ClockSkinFragment.this.animationDrawable = animationDrawable;
            imageView.setBackground(animationDrawable);
            animationDrawable.start();
            textView.setVisibility(View.GONE);
        }
    }
}
