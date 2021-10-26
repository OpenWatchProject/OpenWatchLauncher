package org.openwatchproject.launcher.adapter;

import android.util.SparseArray;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import org.openwatchproject.launcher.fragment.LauncherFragment;
import org.openwatchproject.launcher.fragment.NotificationsFragment;
import org.openwatchproject.launcher.fragment.SettingsFragment;
import org.openwatchproject.launcher.fragment.VerticalViewPagerFragment;

public class HorizontalViewPagerAdapter extends FragmentPagerAdapter {
    private SparseArray<Fragment> fragments;

    public HorizontalViewPagerAdapter(FragmentManager fm) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        fragments = new SparseArray<>();
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = fragments.get(position);
        if (fragment == null) {
            switch (position) {
                case 0:
                    return new NotificationsFragment();
                case 1:
                    return new VerticalViewPagerFragment();
                case 2:
                    return new LauncherFragment();
                case 3:
                    return new SettingsFragment();
                default:
                    throw new IllegalStateException();
            }
        }

        return fragment;
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
        return 4;
    }
}