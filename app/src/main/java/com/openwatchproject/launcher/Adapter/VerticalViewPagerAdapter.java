package com.openwatchproject.launcher.Adapter;

import android.util.SparseArray;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.openwatchproject.launcher.Fragment.ClockSkinFragment;
import com.openwatchproject.launcher.Fragment.QuickSettingsFragment;

public class VerticalViewPagerAdapter extends FragmentPagerAdapter {
    private SparseArray<Fragment> fragments;

    public VerticalViewPagerAdapter(FragmentManager fm) {
        super(fm);
        fragments = new SparseArray<>();
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new QuickSettingsFragment();
            case 1:
                return new ClockSkinFragment();
        }

        return null; // This is never reached
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        Fragment fragment = (Fragment) super.instantiateItem(container, position);
        fragments.put(position, fragment);
        return fragment;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        fragments.remove(position);
        super.destroyItem(container, position, object);
    }

    @Override
    public int getCount() {
        return 2;
    }
}
