package org.openwatchproject.launcher.notification;

import android.app.Notification;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Parcel;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.text.style.TextAppearanceSpan;

import org.openwatchproject.launcher.R;

import java.util.Arrays;

import static androidx.core.graphics.ColorUtils.HSLToColor;
import static androidx.core.graphics.ColorUtils.LABToColor;
import static androidx.core.graphics.ColorUtils.calculateContrast;
import static androidx.core.graphics.ColorUtils.calculateLuminance;
import static androidx.core.graphics.ColorUtils.colorToHSL;
import static androidx.core.graphics.ColorUtils.colorToLAB;

public class ContrastColorUtil {
    private static final ThreadLocal<double[]> TEMP_ARRAY = new ThreadLocal<>();

    public static int resolveDefaultColor(Context context, int backgroundColor,
                                          boolean defaultBackgroundIsDark) {
        boolean useDark = shouldUseDark(backgroundColor, defaultBackgroundIsDark);
        if (useDark) {
            return context.getColor(
                    R.color.notification_default_color_light);
        } else {
            return context.getColor(
                    R.color.notification_default_color_dark);
        }
    }

    /**
     * Finds a suitable color such that there's enough contrast.
     *
     * @param color the color to start searching from.
     * @param other the color to ensure contrast against. Assumed to be darker than {@param color}
     * @param findFg if true, we assume {@param color} is a foreground, otherwise a background.
     * @param minRatio the minimum contrast ratio required.
     * @return a color with the same hue as {@param color}, potentially darkened to meet the
     *          contrast ratio.
     */
    public static int findContrastColorAgainstDark(int color, int other, boolean findFg,
                                                   double minRatio) {
        int fg = findFg ? color : other;
        int bg = findFg ? other : color;
        if (calculateContrast(fg, bg) >= minRatio) {
            return color;
        }
        float[] hsl = new float[3];
        colorToHSL(findFg ? fg : bg, hsl);
        float low = hsl[2], high = 1;
        for (int i = 0; i < 15 && high - low > 0.00001; i++) {
            final float l = (low + high) / 2;
            hsl[2] = l;
            if (findFg) {
                fg = HSLToColor(hsl);
            } else {
                bg = HSLToColor(hsl);
            }
            if (calculateContrast(fg, bg) > minRatio) {
                high = l;
            } else {
                low = l;
            }
        }
        return findFg ? fg : bg;
    }

    public static double[] getTempDouble3Array() {
        double[] result = TEMP_ARRAY.get();
        if (result == null) {
            result = new double[3];
            TEMP_ARRAY.set(result);
        }
        return result;
    }

    /**
     * Change a color by a specified value
     * @param baseColor the base color to lighten
     * @param amount the amount to lighten the color from 0 to 100. This corresponds to the L
     *               increase in the LAB color space. A negative value will darken the color and
     *               a positive will lighten it.
     * @return the changed color
     */
    public static int changeColorLightness(int baseColor, int amount) {
        final double[] result = getTempDouble3Array();
        colorToLAB(baseColor, result);
        result[0] = Math.max(Math.min(100, result[0] + amount), 0);
        return LABToColor(result[0], result[1], result[2]);
    }

    /**
     * Finds a suitable color such that there's enough contrast.
     *
     * @param color the color to start searching from.
     * @param other the color to ensure contrast against. Assumed to be lighter than {@param color}
     * @param findFg if true, we assume {@param color} is a foreground, otherwise a background.
     * @param minRatio the minimum contrast ratio required.
     * @return a color with the same hue as {@param color}, potentially darkened to meet the
     *          contrast ratio.
     */
    public static int findContrastColor(int color, int other, boolean findFg, double minRatio) {
        int fg = findFg ? color : other;
        int bg = findFg ? other : color;
        if (calculateContrast(fg, bg) >= minRatio) {
            return color;
        }
        double[] lab = new double[3];
        colorToLAB(findFg ? fg : bg, lab);
        double low = 0, high = lab[0];
        final double a = lab[1], b = lab[2];
        for (int i = 0; i < 15 && high - low > 0.00001; i++) {
            final double l = (low + high) / 2;
            if (findFg) {
                fg = LABToColor(l, a, b);
            } else {
                bg = LABToColor(l, a, b);
            }
            if (calculateContrast(fg, bg) > minRatio) {
                low = l;
            } else {
                high = l;
            }
        }
        return LABToColor(low, a, b);
    }

    public static boolean satisfiesTextContrast(int backgroundColor, int foregroundColor) {
        return calculateContrast(foregroundColor, backgroundColor) >= 4.5;
    }

    /**
     * Finds a suitable alpha such that there's enough contrast.
     *
     * @param color the color to start searching from.
     * @param backgroundColor the color to ensure contrast against.
     * @param minRatio the minimum contrast ratio required.
     * @return the same color as {@param color} with potentially modified alpha to meet contrast
     */
    public static int findAlphaToMeetContrast(int color, int backgroundColor, double minRatio) {
        int fg = color;
        int bg = backgroundColor;
        if (calculateContrast(fg, bg) >= minRatio) {
            return color;
        }
        int startAlpha = Color.alpha(color);
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        int low = startAlpha, high = 255;
        for (int i = 0; i < 15 && high - low > 0; i++) {
            final int alpha = (low + high) / 2;
            fg = Color.argb(alpha, r, g, b);
            if (calculateContrast(fg, bg) > minRatio) {
                high = alpha;
            } else {
                low = alpha;
            }
        }
        return Color.argb(high, r, g, b);
    }

    public static int resolveSecondaryColor(Context context, int backgroundColor,
                                            boolean defaultBackgroundIsDark) {
        boolean useDark = shouldUseDark(backgroundColor, defaultBackgroundIsDark);
        if (useDark) {
            return context.getColor(
                    R.color.notification_secondary_text_color_light);
        } else {
            return context.getColor(
                    R.color.notification_secondary_text_color_dark);
        }
    }

    private static boolean shouldUseDark(int backgroundColor, boolean defaultBackgroundIsDark) {
        if (backgroundColor == Notification.COLOR_DEFAULT) {
            return !defaultBackgroundIsDark;
        }
        return calculateLuminance(backgroundColor) > 0.5;
    }

    public static int resolvePrimaryColor(Context context, int backgroundColor,
                                          boolean defaultBackgroundIsDark) {
        boolean useDark = shouldUseDark(backgroundColor, defaultBackgroundIsDark);
        if (useDark) {
            return context.getColor(
                    R.color.notification_primary_text_color_light);
        } else {
            return context.getColor(
                    R.color.notification_primary_text_color_dark);
        }
    }

    /**
     * Clears all color spans of a text
     * @param charSequence the input text
     * @return the same text but without color spans
     */
    public static CharSequence clearColorSpans(CharSequence charSequence) {
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
                    TextAppearanceSpan originalSpan = (TextAppearanceSpan) resultSpan;
                    if (originalSpan.getTextColor() != null) {
                        resultSpan = new TextAppearanceSpan(
                                originalSpan.getFamily(),
                                originalSpan.getTextStyle(),
                                originalSpan.getTextSize(),
                                null,
                                originalSpan.getLinkTextColor());
                    }
                } else if (resultSpan instanceof ForegroundColorSpan
                        || (resultSpan instanceof BackgroundColorSpan)) {
                    continue;
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
