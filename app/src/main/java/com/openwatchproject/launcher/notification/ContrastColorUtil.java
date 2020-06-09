package com.openwatchproject.launcher.notification;

import android.app.Notification;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Parcel;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.text.style.TextAppearanceSpan;

import com.openwatchproject.launcher.R;

import java.util.Arrays;

public class ContrastColorUtil {
    /**
     * Resolves {@param color} to an actual color if it is {@link Notification#COLOR_DEFAULT}
     */
    public static int resolveColor(Context context, int color, boolean defaultBackgroundIsDark) {
        if (color == Notification.COLOR_DEFAULT) {
            int res = defaultBackgroundIsDark
                    ? R.color.notification_default_color_dark
                    : R.color.notification_default_color_light;
            return context.getColor(res);
        }
        return color;
    }

    private static TextAppearanceSpan processTextAppearanceSpan(TextAppearanceSpan span) {
        ColorStateList colorStateList = span.getTextColor();
        if (colorStateList != null) {
            int[] colors = getColorStateListColors(colorStateList);
            boolean changed = false;
            for (int i = 0; i < colors.length; i++) {
                if (isGrayscale(colors[i])) {
                    // Allocate a new array so we don't change the colors in the old color state
                    // list.
                    if (!changed) {
                        colors = Arrays.copyOf(colors, colors.length);
                    }
                    colors[i] = processColor(colors[i]);
                    changed = true;
                }
            }
            if (changed) {
                return new TextAppearanceSpan(
                        span.getFamily(), span.getTextStyle(), span.getTextSize(),
                        new ColorStateList(getColorStateListStates(colorStateList), colors),
                        span.getLinkTextColor());
            }
        }
        return span;
    }

    /**
     * Inverts all the grayscale colors set by {@link android.text.style.TextAppearanceSpan}s on
     * the text.
     *
     * @param charSequence The text to process.
     * @return The color inverted text.
     */
    public static CharSequence invertCharSequenceColors(CharSequence charSequence) {
        if (charSequence instanceof Spanned) {
            Spanned ss = (Spanned) charSequence;
            Object[] spans = ss.getSpans(0, ss.length(), Object.class);
            SpannableStringBuilder builder = new SpannableStringBuilder(ss.toString());
            for (Object span : spans) {
                Object resultSpan = span;
                if (resultSpan instanceof CharacterStyle) {
                    resultSpan = ((CharacterStyle) span).getUnderlying();
                }
                if (resultSpan instanceof TextAppearanceSpan) {
                    TextAppearanceSpan processedSpan = processTextAppearanceSpan(
                            (TextAppearanceSpan) span);
                    if (processedSpan != resultSpan) {
                        resultSpan = processedSpan;
                    } else {
                        // we need to still take the orgininal for wrapped spans
                        resultSpan = span;
                    }
                } else if (resultSpan instanceof ForegroundColorSpan) {
                    ForegroundColorSpan originalSpan = (ForegroundColorSpan) resultSpan;
                    int foregroundColor = originalSpan.getForegroundColor();
                    resultSpan = new ForegroundColorSpan(processColor(foregroundColor));
                } else {
                    resultSpan = span;
                }
                builder.setSpan(resultSpan, ss.getSpanStart(span), ss.getSpanEnd(span),
                        ss.getSpanFlags(span));
            }
            return builder;
        }
        return charSequence;
    }

    private static int processColor(int color) {
        return Color.argb(Color.alpha(color),
                255 - Color.red(color),
                255 - Color.green(color),
                255 - Color.blue(color));
    }

    private static int[] getColorStateListColors(ColorStateList colorStateList) {
        Parcel parcel = Parcel.obtain();
        colorStateList.writeToParcel(parcel, 0);

        int N = parcel.readInt();
        for (int i = 0; i < N; i++)
            parcel.createIntArray();

        return parcel.createIntArray();
    }

    private static int[][] getColorStateListStates(ColorStateList colorStateList) {
        Parcel parcel = Parcel.obtain();
        colorStateList.writeToParcel(parcel, 0);

        int N = parcel.readInt();
        int[][] states = new int[N][];
        for (int i = 0; i < N; i++)
            states[i] = parcel.createIntArray();

        return states;
    }

    // Amount (max is 255) that two channels can differ before the color is no longer "gray".
    private static final int TOLERANCE = 20;

    // Alpha amount for which values below are considered transparent.
    private static final int ALPHA_TOLERANCE = 50;

    /**
     * Classifies a color as grayscale or not. Grayscale here means "very close to a perfect
     * gray"; if all three channels are approximately equal, this will return true.
     *
     * Note that really transparent colors are always grayscale.
     */
    public static boolean isGrayscale(int color) {
        int alpha = 0xFF & (color >> 24);
        if (alpha < ALPHA_TOLERANCE) {
            return true;
        }
        int r = 0xFF & (color >> 16);
        int g = 0xFF & (color >> 8);
        int b = 0xFF & color;
        return Math.abs(r - g) < TOLERANCE
                && Math.abs(r - b) < TOLERANCE
                && Math.abs(g - b) < TOLERANCE;
    }
}
