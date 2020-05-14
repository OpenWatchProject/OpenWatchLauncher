package com.openwatchproject.launcher.activity;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatDialog;

import com.openwatchproject.launcher.databinding.DialogFullscreenBinding;

public class FullscreenDialog extends AppCompatDialog {

    private String title;
    private String description;
    private View.OnClickListener positiveClickListener;
    private View.OnClickListener negativeClickListener;

    private TextView titleTv;
    private TextView descriptionTv;
    private ImageButton positiveButtonView;
    private ImageButton negativeButtonView;

    public FullscreenDialog(Context context) {
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

    public FullscreenDialog setTitle(String title) {
        this.title = title;
        return this;
    }

    public FullscreenDialog setDescription(String description) {
        this.description = description;
        return this;
    }

    public FullscreenDialog setPositiveButtonOnClickListener(View.OnClickListener l) {
        this.positiveClickListener = l;
        return this;
    }

    public FullscreenDialog setNegativeButtonOnClickListener(View.OnClickListener l) {
        this.negativeClickListener = l;
        return this;
    }
}
