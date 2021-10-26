package org.openwatchproject.launcher.activity;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatDialog;

import org.openwatchproject.launcher.R;

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

    public FullscreenDialogActivity(Context context, int theme) {
        super(context, theme);
    }

    protected FullscreenDialogActivity(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_fullscreen);
        findViewById(R.id.content).setBackgroundColor(Color.BLACK);

        titleTv = findViewById(R.id.title);
        if (title != null) titleTv.setText(title);
        descriptionTv = findViewById(R.id.description);
        if (description != null) descriptionTv.setText(description);
        positiveButtonView = findViewById(R.id.positive_button);
        if (positiveClickListener != null)
            positiveButtonView.setOnClickListener(positiveClickListener);
        negativeButtonView = findViewById(R.id.negative_button);
        if (negativeClickListener != null)
            negativeButtonView.setOnClickListener(negativeClickListener);
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
