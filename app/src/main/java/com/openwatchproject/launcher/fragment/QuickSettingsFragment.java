package com.openwatchproject.launcher.fragment;

import android.animation.ArgbEvaluator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Point;
import android.os.BatteryManager;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.openwatchproject.launcher.R;
import com.openwatchproject.launcher.databinding.FragmentQuickSettingsBinding;
import com.openwatchproject.launcher.databinding.FragmentVerticalViewPagerBinding;

import static android.telephony.PhoneStateListener.LISTEN_DATA_CONNECTION_STATE;
import static android.telephony.PhoneStateListener.LISTEN_NONE;
import static android.telephony.PhoneStateListener.LISTEN_SERVICE_STATE;
import static android.telephony.PhoneStateListener.LISTEN_SIGNAL_STRENGTHS;
import static android.telephony.TelephonyManager.DATA_CONNECTED;
import static android.telephony.TelephonyManager.DATA_CONNECTING;
import static android.telephony.TelephonyManager.DATA_DISCONNECTED;
import static android.telephony.TelephonyManager.DATA_SUSPENDED;

public class QuickSettingsFragment extends Fragment {
    private static final String TAG = "QuickSettingsFragment";

    private FragmentQuickSettingsBinding binding;
    private ProgressBar batteryProgress;

    private TextView batteryStatus;
    private TextView carrierName;
    private TextView signalStrength;
    private TextView connectionState;

    private TelephonyManager telephonyManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentQuickSettingsBinding.inflate(inflater, container, false);
        // Inflate the layout for this fragment
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        batteryStatus = binding.batteryStatus;
        batteryProgress = binding.progressBar;

        int minDisplaySize = getMinDisplaySize();
        batteryProgress.getLayoutParams().height = minDisplaySize;
        batteryProgress.getLayoutParams().width = minDisplaySize;

        carrierName = binding.carrierName;
        signalStrength = binding.signalStrength;
        connectionState = binding.connectionState;

        telephonyManager = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);

        IntentFilter batteryIntentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent intent = getContext().registerReceiver(batteryStatusBroadcastReceiver, batteryIntentFilter);
        updateBatteryLevel(intent);

        telephonyManager.listen(phoneStateListener,
                LISTEN_DATA_CONNECTION_STATE | LISTEN_SERVICE_STATE | LISTEN_SIGNAL_STRENGTHS);

        updateTelephonyInfo();
    }

    private int getMinDisplaySize() {
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getRealSize(size);
        return Math.min(size.x, size.y);
    }

    private void updateTelephonyInfo() {
        carrierName.setText(telephonyManager.getSimOperatorName());
    }

    private void updateBatteryLevel(Intent intent) {
        int batteryLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 50);
        int batteryScale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100);
        int batteryPercentage = batteryLevel * 100 / batteryScale;
        batteryStatus.setText(batteryPercentage + "%");
        batteryProgress.setProgress(batteryPercentage);
    }

    @Override
    public void onDestroyView() {
        getContext().unregisterReceiver(batteryStatusBroadcastReceiver);
        telephonyManager.listen(phoneStateListener, LISTEN_NONE);

        super.onDestroyView();
    }

    private BroadcastReceiver batteryStatusBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateBatteryLevel(intent);
        }
    };

    private PhoneStateListener phoneStateListener = new PhoneStateListener() {
        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);
            QuickSettingsFragment.this.signalStrength.setText("Strength: " + signalStrength.getLevel());
        }

        @Override
        public void onServiceStateChanged(ServiceState serviceState) {
            super.onServiceStateChanged(serviceState);
            Log.d(TAG, "onServiceStateChanged: " + serviceState.toString());

        }

        @Override
        public void onDataConnectionStateChanged(int state, int networkType) {
            super.onDataConnectionStateChanged(state, networkType);
            switch (state) {
                case DATA_DISCONNECTED:
                    connectionState.setText("DATA_DISCONNECTED");
                    break;
                case DATA_CONNECTING:
                    connectionState.setText("DATA_CONNECTING");
                    break;
                case DATA_CONNECTED:
                    connectionState.setText("DATA_CONNECTED");
                    break;
                case DATA_SUSPENDED:
                    connectionState.setText("DATA_SUSPENDED");
                    break;
            }
        }
    };
}
