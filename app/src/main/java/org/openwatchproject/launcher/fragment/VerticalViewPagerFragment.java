package org.openwatchproject.launcher.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.openwatchproject.launcher.R;
import org.openwatchproject.launcher.adapter.VerticalViewPagerAdapter;
import org.openwatchproject.launcher.view.HorizontalViewPager;
import org.openwatchproject.launcher.view.VerticalViewPager;

public class VerticalViewPagerFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_vertical_view_pager, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final VerticalViewPager verticalViewPager = view.findViewById(R.id.vertical_view_pager);
        final VerticalViewPagerAdapter verticalViewPagerAdapter = new VerticalViewPagerAdapter(getActivity().getSupportFragmentManager());
        verticalViewPager.setAdapter(verticalViewPagerAdapter);
        verticalViewPager.setCurrentItem(1);

        ((HorizontalViewPager) getView().getParent()).setVerticalViewPager(verticalViewPager);
    }
}
