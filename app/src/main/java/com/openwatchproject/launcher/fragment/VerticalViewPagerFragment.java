package com.openwatchproject.launcher.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.openwatchproject.launcher.activity.MainActivity;
import com.openwatchproject.launcher.adapter.VerticalViewPagerAdapter;
import com.openwatchproject.launcher.R;
import com.openwatchproject.launcher.databinding.FragmentVerticalViewPagerBinding;
import com.openwatchproject.launcher.view.HorizontalViewPager;
import com.openwatchproject.launcher.view.VerticalViewPager;

public class VerticalViewPagerFragment extends Fragment {

    private FragmentVerticalViewPagerBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentVerticalViewPagerBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final VerticalViewPager verticalViewPager = binding.verticalViewPager;
        final VerticalViewPagerAdapter verticalViewPagerAdapter = new VerticalViewPagerAdapter(getActivity().getSupportFragmentManager());
        verticalViewPager.setAdapter(verticalViewPagerAdapter);
        verticalViewPager.setCurrentItem(1);

        ((HorizontalViewPager) getView().getParent()).setVerticalViewPager(verticalViewPager);
    }
}
