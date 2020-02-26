package com.openwatchproject.launcher.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.openwatchproject.launcher.adapter.VerticalViewPagerAdapter;
import com.openwatchproject.launcher.R;
import com.openwatchproject.launcher.view.HorizontalViewPager;
import com.openwatchproject.launcher.view.VerticalViewPager;

public class VerticalViewPagerFragment extends Fragment {

    private VerticalViewPager verticalViewPager;
    private VerticalViewPagerAdapter verticalViewPagerAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_vertical_view_pager, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        verticalViewPager = view.findViewById(R.id.vertical_view_pager);
        verticalViewPagerAdapter = new VerticalViewPagerAdapter(getActivity().getSupportFragmentManager());
        verticalViewPager.setAdapter(verticalViewPagerAdapter);
        verticalViewPager.setCurrentItem(1);
        ViewParent parent = getView().getParent();
        if (parent instanceof HorizontalViewPager) {
            ((HorizontalViewPager) getView().getParent()).setVerticalViewPager(verticalViewPager);
        }
    }
}
