package com.openwatchproject.launcher.activity;

import android.Manifest;
import android.animation.ArgbEvaluator;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.openwatchproject.launcher.R;
import com.openwatchproject.launcher.databinding.ActivityOnboardingBinding;
import com.openwatchproject.launcher.databinding.FragmentPagerBinding;

public class OnboardingActivity extends AppCompatActivity {
    public static final String PREF_USER_FIRST_TIME = "user_first_time";

    private ActivityOnboardingBinding binding;

    private ViewPager2 mViewPager;
    private ImageButton mNextBtn;
    private ImageButton mPrevBtn;
    private ImageView[] indicators;
    private SectionsStateAdapter mSectionsPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOnboardingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mNextBtn = binding.introBtnNext;
        mPrevBtn = binding.introBtnSkip;
        indicators = new ImageView[]{binding.introIndicator0, binding.introIndicator1, binding.introIndicator2};

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsStateAdapter(this);

        // Set up the ViewPager with the sections adapter.
        mViewPager = binding.viewPager;
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setCurrentItem(0);
        updateIndicators(0);

        final ConstraintLayout mainContent = binding.mainContent;
        final int color1 = ContextCompat.getColor(this, R.color.cyan);
        final int color2 = ContextCompat.getColor(this, R.color.orange);
        final int color3 = ContextCompat.getColor(this, R.color.green);
        final int[] colorList = new int[]{color1, color2, color3};
        final ArgbEvaluator evaluator = new ArgbEvaluator();

        mViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                int colorUpdate = (int) evaluator.evaluate(positionOffset, colorList[position], colorList[position == 2 ? position : position + 1]);
                mainContent.setBackgroundColor(colorUpdate);
                mViewPager.setBackgroundColor(colorUpdate);
            }

            @Override
            public void onPageSelected(int position) {
                updateIndicators(position);

                if (position == 2) {
                    mNextBtn.setImageDrawable(getDrawable(R.drawable.ic_check_24dp));
                } else {
                    mNextBtn.setImageDrawable(getDrawable(R.drawable.ic_chevron_right_24dp));
                }

                mPrevBtn.setVisibility(position != 0 ? View.VISIBLE : View.INVISIBLE);
            }
        });

        mNextBtn.setOnClickListener(v -> {
            if (mViewPager.getCurrentItem() != 2) {
                mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1);
            } else {
                finish();
                // update 1st time pref
                //Utils.saveSharedSetting(OnboardingActivity.this, PREF_USER_FIRST_TIME, "false");
            }
        });

        mPrevBtn.setOnClickListener(v -> {
            mViewPager.setCurrentItem(mViewPager.getCurrentItem() - 1, true);
        });
    }

    void updateIndicators(int position) {
        for (int i = 0; i < indicators.length; i++) {
            indicators[i].setBackgroundResource(
                    i == position ? R.drawable.indicator_selected : R.drawable.indicator_unselected
            );
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_LABEL = "SECTION_LABEL";
        private static final String ARG_SECTION_DESCRIPTION = "SECTION_DESCRIPTION";
        private static final String ARG_SECTION_PERMISSION = "SECTION_PERMISSION";
        private static final String ARG_SECTION_IMG = "SECTION_IMG";

        private FragmentPagerBinding binding;

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            switch (sectionNumber) {
                case 0:
                    args.putString(ARG_SECTION_LABEL, "Welcome!");
                    args.putString(ARG_SECTION_DESCRIPTION, "Before using OpenWatch Launcher, you'll need to grant us some permissions.");
                    args.putInt(ARG_SECTION_IMG, R.drawable.ic_access_time_24dp);
                    break;
                case 1:
                    args.putString(ARG_SECTION_LABEL, "External Storage Permission");
                    args.putString(ARG_SECTION_DESCRIPTION, "This permission is needed to access the \"clockskin\" folder, where all your ClockSkins are saved.");
                    args.putString(ARG_SECTION_PERMISSION, Manifest.permission.READ_EXTERNAL_STORAGE);
                    args.putInt(ARG_SECTION_IMG, R.drawable.ic_folder_24dp);
                    break;
                case 2:
                    args.putString(ARG_SECTION_LABEL, "That's it!");
                    args.putString(ARG_SECTION_DESCRIPTION, "Now that you've granted all the permissions we're ready to go! Enjoy.");
                    args.putInt(ARG_SECTION_IMG, R.drawable.ic_explore_24dp);
                    break;
                default:
                    throw new IllegalStateException("Unknown fragment requested!");
            }

            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            binding = FragmentPagerBinding.inflate(inflater, container, false);
            return binding.getRoot();
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            TextView label = binding.sectionLabel;
            label.setText(getArguments().getString(ARG_SECTION_LABEL));

            TextView description = binding.sectionDescription;
            description.setText(getArguments().getString(ARG_SECTION_DESCRIPTION));

            ImageView img = binding.sectionImg;
            img.setBackgroundResource(getArguments().getInt(ARG_SECTION_IMG));
        }
    }

    public static class SectionsStateAdapter extends FragmentStateAdapter {

        public SectionsStateAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return PlaceholderFragment.newInstance(position);
        }

        @Override
        public int getItemCount() {
            return 3;
        }
    }
}
