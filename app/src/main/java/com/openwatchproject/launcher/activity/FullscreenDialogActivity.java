package com.openwatchproject.launcher.activity;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatDialog;

import com.openwatchproject.launcher.databinding.DialogFullscreenBinding;

public class FullscreenDialogActivity extends AppCompatDialog {

    private String title;
    private String description;
    private View.OnClickListener positiveClickListener;
    private View.OnClickListener negativeClickListener;

    private TextView titleTv;
    private TextView descriptionTv;
    private ImageButton positiveButtonView;
    private ImageButton negativeButtonView;

    public FullscreenDialogActivity(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final DialogFullscreenBinding binding = DialogFullscreenBinding.inflate(getLayoutInflater());
        binding.getRoot().setBackgroundColor(Color.BLACK);
        setContentView(binding.getRoot());

        titleTv = binding.title;
        if (title != null) titleTv.setText(title);
        descriptionTv = binding.description;
        if (description != null) descriptionTv.setText(description);
        positiveButtonView = binding.positiveButton;
        if (positiveClickListener != null) positiveButtonView.setOnClickListener(positiveClickListener);
        negativeButtonView = binding.negativeButton;
        if (negativeClickListener != null) negativeButtonView.setOnClickListener(negativeClickListener);
    }

    public FullscreenDialogActivity setTitle(String title) {
        this.title = title;
        return this;
    }

    public FullscreenDialogActivity setDescription(String description) {
        this.description = description;
        return this;
    }

    public FullscreenDialogActivity setPositiveButtonOnClickListener(View.OnClickListener l) {
        this.positiveClickListener = l;
        return this;
    }

    public FullscreenDialogActivity setNegativeButtonOnClickListener(View.OnClickListener l) {
        this.negativeClickListener = l;
        return this;
    }
}
