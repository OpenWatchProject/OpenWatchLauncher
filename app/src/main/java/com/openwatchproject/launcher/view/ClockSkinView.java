package com.openwatchproject.launcher.view;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.BatteryManager;
import android.os.SystemClock;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Xml;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import com.openwatchproject.launcher.ClockSkin;
import com.openwatchproject.launcher.ClockSkinConstants;
import com.openwatchproject.launcher.ClockSkinItem;
import com.openwatchproject.launcher.SystemHelper;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

public class ClockSkinView extends View {
    private static final String TAG = "ClockSkinView";

    private Context context;
    private Calendar calendar;
    private ClockSkin clockSkin;
    private List<ClockSkinItem> touchItems;

    private boolean registered;

    private boolean isBatteryCharging;
    private int batteryPercentage;
    private int viewCenterX;
    private int viewCenterY;

    private boolean shouldRunTicker;

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case Intent.ACTION_BATTERY_CHANGED:
                    updateBatteryStatus(intent);
                    break;
                case Intent.ACTION_TIMEZONE_CHANGED:
                    updateTimeZone();
                    break;
            }
        }
    };

    public ClockSkinView(Context context) {
        super(context);
        constructView(context);
    }

    public ClockSkinView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        constructView(context);
    }

    public ClockSkinView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        constructView(context);
    }

    public ClockSkinView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        constructView(context);
    }

    private void constructView(Context context) {
        this.context = context;
        this.calendar = Calendar.getInstance();

        setOnTouchListener(onTouchListener);
    }

    public void setClockSkin(ClockSkin clockSkin) {
        this.touchItems = new ArrayList<>();
        this.clockSkin = parseClockSkin(clockSkin);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        if (!registered) {
            registered = true;

            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
            intentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
            Intent batteryStatus = context.registerReceiver(receiver, intentFilter);
            updateBatteryStatus(batteryStatus);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        if (registered) {
            registered = false;

            context.unregisterReceiver(receiver);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        this.viewCenterX = w / 2;
        this.viewCenterY = h / 2;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    private final View.OnTouchListener onTouchListener = new OnTouchListener() {
        @SuppressLint("WrongConstant")
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                v.performClick();

                float x = event.getX();
                float y = event.getY();

                if (clockSkin != null) {
                    List<ClockSkinItem> touchClockSkinItems = clockSkin.getTouchClockSkinItems();
                    if (touchClockSkinItems != null) {
                        for (ClockSkinItem item : touchClockSkinItems) {
                            int centerX = viewCenterX + item.getCenterX();
                            int centerY = viewCenterY + item.getCenterY();
                            int range = item.getRange();

                            if (distance(x, y, centerX, centerY) <= range) {
                                String packageName = item.getPackageName();
                                String className = item.getClassName();

                                if (packageName != null && className != null) {
                                    Intent i = new Intent();
                                    i.setComponent(new ComponentName(packageName, className));
                                    i.setFlags(270532608); // TODO: Fix me!
                                    try {
                                        context.startActivity(i);
                                    } catch (ActivityNotFoundException e) {
                                        Log.d(TAG, "onTouch: tried to open a non-existant activity: packageName = " + packageName + ", className = " + className);
                                    }
                                    return true;
                                }

                                break;
                            }
                        }
                    }
                }
            }

            return false;
        }
    };

    private int distance(float x1, float y1, float x2, float y2) {
        float x = x2 - x1;
        float y = y2 - y1;

        return (int) Math.round(Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2)));
    }

    private void updateBatteryStatus(Intent batteryStatus) {
        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        this.isBatteryCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;

        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        this.batteryPercentage =  Math.round((float) (level * 100) / (float) scale);
    }

    private void updateTimeZone() {
        TimeZone timeZone = TimeZone.getDefault();
        calendar.setTimeZone(timeZone);
        for (ClockSkinItem clockSkinItem : clockSkin.getClockSkinItems()) {
            clockSkinItem.setTimeZone(timeZone);
        }
    }

    private final Runnable ticker = new Runnable() {
        public void run() {
            onTimeChanged();

            long now = SystemClock.uptimeMillis();
            long next = now + ((1000 / 60) - now % (1000 / 60));

            getHandler().postAtTime(ticker, next);
        }
    };

    @Override
    public void onVisibilityAggregated(boolean isVisible) {
        super.onVisibilityAggregated(isVisible);

        if (!shouldRunTicker && isVisible) {
            shouldRunTicker = true;
            ticker.run();
        } else if (shouldRunTicker && !isVisible) {
            shouldRunTicker = false;
            getHandler().removeCallbacks(ticker);
        }
    }

    private void onTimeChanged() {
        postInvalidateOnAnimation();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (clockSkin != null) {
            calendar.setTimeInMillis(System.currentTimeMillis());
            for (ClockSkinItem item : clockSkin.getClockSkinItems()) {
                switch (item.getArrayType()) {
                    case ClockSkinConstants.ARRAY_NONE:
                        drawNoArrayType(canvas, item);
                        break;
                    case ClockSkinConstants.ARRAY_YEAR_MONTH_DAY:
                        drawArrayYearMonthDay(canvas, item);
                        break;
                    case ClockSkinConstants.ARRAY_MONTH_DAY:
                        drawArrayMonthDay(canvas, item);
                        break;
                    case ClockSkinConstants.ARRAY_MONTH:
                        int month = calendar.get(Calendar.MONTH);
                        drawArraySingle(canvas, item, month);
                        break;
                    case ClockSkinConstants.ARRAY_DAY:
                        int day = calendar.get(Calendar.DAY_OF_MONTH);
                        drawArrayDouble(canvas, item, day / 10, day % 10);
                        break;
                    case ClockSkinConstants.ARRAY_WEEKDAY:
                        int weekDay = calendar.get(Calendar.DAY_OF_WEEK) - 1;
                        drawArraySingle(canvas, item, weekDay);
                        break;
                    case ClockSkinConstants.ARRAY_HOUR_MINUTE:
                        drawArrayHourMinute(canvas, item);
                        break;
                    case ClockSkinConstants.ARRAY_HOUR:
                        int hour = calendar.get(Calendar.HOUR_OF_DAY);
                        if (!DateFormat.is24HourFormat(context)) {
                            hour = hour % 12;
                            if (hour == 0) {
                                hour = 12;
                            }
                        }
                        drawArrayDouble(canvas, item, hour / 10, hour % 10);
                        break;
                    case ClockSkinConstants.ARRAY_MINUTE:
                        int minute = calendar.get(Calendar.MINUTE);
                        drawArrayDouble(canvas, item, minute / 10, minute % 10);
                        break;
                    case ClockSkinConstants.ARRAY_SECOND:
                        int second = calendar.get(Calendar.SECOND);
                        drawArrayDouble(canvas, item, second / 10, second % 10);
                        break;
                    case ClockSkinConstants.ARRAY_WEATHER:
                        int weatherIcon = SystemHelper.getWeatherIcon(context);
                        int weatherIconValue = 0;
                        switch (weatherIcon) {
                            case 6:
                            case 7:
                            case 8:
                            case 38:
                                weatherIconValue = 1;
                                break;
                            case 11:
                                weatherIconValue = 3;
                                break;
                            case 18:
                                weatherIconValue = 4;
                                break;
                            case 12:
                            case 13:
                            case 14:
                            case 39:
                            case 40:
                                weatherIconValue = 5;
                                break;
                            case 26:
                            case 29:
                                weatherIconValue = 6;
                                break;
                            case 15:
                            case 16:
                            case 17:
                            case 41:
                            case 42:
                                weatherIconValue = 7;
                                break;
                            case 19:
                            case 20:
                            case 21:
                            case 22:
                            case 23:
                            case 24:
                            case 25:
                            case 43:
                            case 44:
                                weatherIconValue = 8;
                                break;
                            case 30:
                                weatherIconValue = 10;
                                break;
                            case 31:
                                weatherIconValue = 11;
                                break;
                            case 32:
                                weatherIconValue = 12;
                                break;
                        }
                        drawArraySingle(canvas, item, weatherIconValue);
                        break;
                    case ClockSkinConstants.ARRAY_TEMPERATURE:
                        drawArrayTemperature(canvas, item);
                        break;
                    case ClockSkinConstants.ARRAY_STEPS:
                        int steps = SystemHelper.getSteps(context);
                        Log.d(TAG, "onDraw: steps = " + steps);

                        break;
                    case ClockSkinConstants.ARRAY_HEART_RATE:
                        int heartRate = SystemHelper.getHeartRate(context);
                        Log.d(TAG, "onDraw: heartRate = " + heartRate);

                        break;
                    case ClockSkinConstants.ARRAY_BATTERY:
                        drawArrayBattery(canvas, item);
                        break;
                    case ClockSkinConstants.ARRAY_SPECIAL_SECOND:

                        break;
                    case ClockSkinConstants.ARRAY_YEAR:
                        drawArrayYear(canvas, item);
                        break;
                    case ClockSkinConstants.ARRAY_BATTERY_CIRCLE:

                        break;
                    case ClockSkinConstants.ARRAY_STEPS_CIRCLE:

                        break;
                    case ClockSkinConstants.ARRAY_MOON_PHASE:

                        break;
                    case ClockSkinConstants.ARRAY_BATTERY_CIRCLE_PIC:

                        break;
                    case ClockSkinConstants.ARRAY_KCAL:

                        break;
                    case ClockSkinConstants.ARRAY_DISTANCE:

                        break;
                    case ClockSkinConstants.ARRAY_TEXT_PEDOMETER:

                        break;
                    case ClockSkinConstants.ARRAY_TEXT_HEARTRATE:

                        break;
                    case ClockSkinConstants.ARRAY_CHARGING:

                        break;
                    case ClockSkinConstants.ARRAY_TAP_ACTION:
                        // Handled on parse
                        break;
                    case ClockSkinConstants.ARRAY_DISTANCE_2:

                        break;
                    case ClockSkinConstants.ARRAY_DISTANCE_UNIT:

                        break;
                    case ClockSkinConstants.ARRAY_TEMP_UNIT:

                        break;
                }
            }
        }
    }

    private void drawNoArrayType(Canvas canvas, ClockSkinItem item) {
        switch (item.getRotate()) {
            case ClockSkinConstants.ROTATE_NONE:
                drawDial(canvas, item);
                break;
            case ClockSkinConstants.ROTATE_HOUR:
                float analogHour = (float) calendar.get(Calendar.HOUR) + ((float) calendar.get(Calendar.MINUTE) / (float) 60) + ((float) calendar.get(Calendar.SECOND) / (float) 60 / (float) 60);
                float hourAngle = item.getAngle() + (item.getMulRotate() * ((float) analogHour * (float) 360 / (float) 12));
                drawHand(canvas, item, hourAngle);
                break;
            case ClockSkinConstants.ROTATE_MINUTE:
                float analogMinute = (float) calendar.get(Calendar.MINUTE) + ((float) calendar.get(Calendar.SECOND) / (float) 60);
                float minuteAngle = item.getAngle() + (item.getMulRotate() * ((float) analogMinute / (float) 60 * 360));
                drawHand(canvas, item, minuteAngle);
                break;
            case ClockSkinConstants.ROTATE_SECOND:
                float analogSecond = (float) calendar.get(Calendar.SECOND) + ((float) calendar.get(Calendar.MILLISECOND) / (float) 1000);
                int mulRotate = item.getMulRotate();
                if (mulRotate > 0) {
                    //analogSecond *= mulRotate;
                } else if (mulRotate < 0){
                    //analogSecond /= (float) mulRotate;
                }
                if (item.getDirection() == ClockSkinConstants.DIRECTION_REVERSE) {
                    analogSecond = -analogSecond;
                }
                float secondAngle = item.getAngle() + (item.getMulRotate() * ((float) analogSecond / (float) 60 * 360));
                drawHand(canvas, item, secondAngle);
                break;
            case ClockSkinConstants.ROTATE_MONTH:
                int month = calendar.get(Calendar.MONTH) + 1; // Calendar.MONTH starts at 0
                if (item.getDirection() == ClockSkinConstants.DIRECTION_REVERSE) {
                    month = -month;
                }
                float monthAngle = item.getAngle() + (item.getMulRotate() * ((float) month / (float) 12 * 360));
                drawHand(canvas, item, monthAngle);
                break;
            case ClockSkinConstants.ROTATE_DAY_OF_WEEK:
                int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 2; // Sunday = -2, Monday = -1, Tuesday = 0, etc
                if (item.getDirection() == ClockSkinConstants.DIRECTION_REVERSE) {
                    dayOfWeek = -dayOfWeek;
                }
                float dayOfWeekAngle = item.getAngle() + (item.getMulRotate() * ((float) dayOfWeek / (float) 7 * 360));
                drawHand(canvas, item, dayOfWeekAngle);
                break;
            case ClockSkinConstants.ROTATE_BATTERY:
                int battery = batteryPercentage;
                if (item.getDirection() == ClockSkinConstants.DIRECTION_REVERSE) {
                    battery = -battery;
                }
                float batteryAngle = item.getAngle() + (item.getMulRotate() * ((float) battery / (float) 100 * 180));
                drawHand(canvas, item, batteryAngle);
                break;
            case ClockSkinConstants.ROTATE_DAY_NIGHT:
                float hour24 = calendar.get(Calendar.HOUR_OF_DAY) + ((float) calendar.get(Calendar.MINUTE) / (float) 60) + ((float) calendar.get(Calendar.SECOND) / (float) 60 / (float) 60);
                if (item.getDirection() == ClockSkinConstants.DIRECTION_REVERSE) {
                    hour24 = -hour24;
                }
                float hour24Angle = item.getAngle() + (item.getMulRotate() * ((float) hour24 / (float) 24 * 360));
                drawHand(canvas, item, hour24Angle);
                break;
            case ClockSkinConstants.ROTATE_HOUR_SHADOW: // TODO: Fix me!
                float analogHourShadow = (float) calendar.get(Calendar.HOUR) + ((float) calendar.get(Calendar.MINUTE) / (float) 60) + ((float) calendar.get(Calendar.SECOND) / (float) 60 / (float) 60);
                float hourShadowAngle = item.getAngle() + (item.getMulRotate() * ((float) analogHourShadow * (float) 360 / (float) 12));
                drawHand(canvas, item, hourShadowAngle);
                break;
            case ClockSkinConstants.ROTATE_MINUTE_SHADOW: // TODO: Fix me!
                float analogMinuteShadow = (float) calendar.get(Calendar.MINUTE) + ((float) calendar.get(Calendar.SECOND) / (float) 60);
                float minuteShadowAngle = item.getAngle() + (item.getMulRotate() * ((float) analogMinuteShadow / (float) 60 * 360));
                drawHand(canvas, item, minuteShadowAngle);
                break;
            case ClockSkinConstants.ROTATE_SECOND_SHADOW: // TODO: Fix me!
                float analogSecondShadow = (float) calendar.get(Calendar.SECOND) + ((float) calendar.get(Calendar.MILLISECOND) / 1000);
                if (item.getDirection() == ClockSkinConstants.DIRECTION_REVERSE) {
                    analogSecondShadow = -analogSecondShadow;
                }
                float secondShadowAngle = item.getAngle() + (item.getMulRotate() * ((float) analogSecondShadow / (float) 60 * 360));
                drawHand(canvas, item, secondShadowAngle);
                break;
            case ClockSkinConstants.ROTATE_BATTERY_CIRCLE:
                // Unknown
                break;
            case ClockSkinConstants.ROTATE_DAY:
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                if (item.getDirection() == ClockSkinConstants.DIRECTION_REVERSE) {
                    day = -day;
                }
                float dayAngle = item.getAngle() + (item.getMulRotate() * ((float) day / (float) 31 * 360));
                drawHand(canvas, item, dayAngle);
                break;
            case ClockSkinConstants.ROTATE_STEPS_TARGET:
                int steps = SystemHelper.getSteps(context);
                int targetSteps = SystemHelper.getTargetSteps(context);
                if (item.getDirection() == ClockSkinConstants.DIRECTION_REVERSE) {
                    steps = -steps;
                }
                float stepsAngle = item.getAngle() + (item.getMulRotate() * ((float) steps / (float) targetSteps * 360));
                drawHand(canvas, item, stepsAngle);
                break;
        }
    }

    private void drawArrayYearMonthDay(Canvas canvas, ClockSkinItem item) {
        List<Drawable> drawables = item.getDrawables();
        if (drawables != null) {
            int year = calendar.get(Calendar.YEAR);
            Drawable y1000 = drawables.get(year / 1000);
            Drawable y100 = drawables.get((year % 1000) / 1000);
            Drawable y10 = drawables.get(((year % 1000) % 100) / 10);
            Drawable y1 = drawables.get(((year % 1000) % 100) % 10);
            int month = calendar.get(Calendar.MONTH) + 1;
            Drawable m10 = drawables.get(month / 10);
            Drawable m1 = drawables.get(month % 10);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            Drawable d10 = drawables.get(day / 10);
            Drawable d1 = drawables.get(day % 10);
            Drawable separator = drawables.get(10);

            int centerX = viewCenterX + item.getCenterX();
            int centerY = viewCenterY + item.getCenterY();
            int width = y1000.getIntrinsicWidth();
            int halfHeight = y1000.getIntrinsicHeight() / 2;
            int top = centerY - halfHeight;
            int bottom = centerY + halfHeight;
            y1000.setBounds(centerX - (width * 5), top, centerX - (width * 4), bottom);
            y1000.draw(canvas);
            y100.setBounds(centerX - (width * 4), top, centerX - (width * 3), bottom);
            y100.draw(canvas);
            y10.setBounds(centerX - (width * 3), top, centerX - (width * 2), bottom);
            y10.draw(canvas);
            y1.setBounds(centerX - (width * 2), top, centerX - width, bottom);
            y1.draw(canvas);
            separator.setBounds(centerX - width, top, centerX, bottom);
            separator.draw(canvas);
            m10.setBounds(centerX, top, centerX + width, bottom);
            m10.draw(canvas);
            m1.setBounds(centerX + width, top, centerX + (width * 2), bottom);
            m1.draw(canvas);
            separator.setBounds(centerX + (width * 2), top, centerX + (width * 3), bottom);
            separator.draw(canvas);
            d10.setBounds(centerX + (width * 3), top, centerX + (width * 4), bottom);
            d10.draw(canvas);
            d1.setBounds(centerX + (width * 4), top, centerX + (width * 5), bottom);
            d1.draw(canvas);
        }
    }

    private void drawArrayMonthDay(Canvas canvas, ClockSkinItem item) {
        List<Drawable> drawables = item.getDrawables();
        int month = calendar.get(Calendar.MONTH) + 1;
        Drawable m10 = drawables.get(month / 10);
        Drawable m1 = drawables.get(month % 10);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        Drawable d10 = drawables.get(day / 10);
        Drawable d1 = drawables.get(day % 10);
        Drawable separator = drawables.get(10);

        int centerX = viewCenterX + item.getCenterX();
        int centerY = viewCenterY + item.getCenterY();
        int numberWidth = m10.getIntrinsicWidth();
        int numberHalfHeight = m10.getIntrinsicHeight() / 2;
        int top = centerY - numberHalfHeight;
        int bottom = centerY + numberHalfHeight;
        int separatorHalfWidth = separator.getIntrinsicWidth() / 2;
        m10.setBounds((centerX - separatorHalfWidth) - (numberWidth * 2), top, (centerX - separatorHalfWidth) - numberWidth, bottom);
        m10.draw(canvas);
        m1.setBounds((centerX - separatorHalfWidth) - numberWidth, top, (centerX - separatorHalfWidth), bottom);
        m1.draw(canvas);
        separator.setBounds(centerX - separatorHalfWidth, top, centerX + separatorHalfWidth, bottom);
        separator.draw(canvas);
        d10.setBounds((centerX + separatorHalfWidth), top, (centerX + separatorHalfWidth) + numberWidth, bottom);
        d10.draw(canvas);
        d1.setBounds((centerX + separatorHalfWidth) + numberWidth, top, (centerX + separatorHalfWidth) + (numberWidth * 2), bottom);
        d1.draw(canvas);
    }

    private void drawArraySingle(Canvas canvas, ClockSkinItem item, int value) {
        List<Drawable> drawables = item.getDrawables();
        Drawable drawable = drawables.get(value);

        int centerX = viewCenterX + item.getCenterX();
        int centerY = viewCenterY + item.getCenterY();
        int halfWidth = drawable.getIntrinsicWidth() / 2;
        int halfHeight = drawable.getIntrinsicHeight() / 2;
        int top = centerY - halfHeight;
        int bottom = centerY + halfHeight;
        drawable.setBounds(centerX - halfWidth, top, centerX + halfWidth, bottom);
        drawable.draw(canvas);
    }

    private void drawArrayDouble(Canvas canvas, ClockSkinItem item, int value1, int value2) {
        List<Drawable> drawables = item.getDrawables();
        Drawable drawable1 = drawables.get(value1);
        Drawable drawable2 = drawables.get(value2);

        int centerX = viewCenterX + item.getCenterX();
        int centerY = viewCenterY + item.getCenterY();
        int width = drawable1.getIntrinsicWidth();
        int halfHeight = drawable1.getIntrinsicHeight() / 2;
        int top = centerY - halfHeight;
        int bottom = centerY + halfHeight;
        drawable1.setBounds(centerX - width, top, centerX, bottom);
        drawable1.draw(canvas);
        drawable2.setBounds(centerX, top, centerX + width, bottom);
        drawable2.draw(canvas);
    }

    private void drawArrayHourMinute(Canvas canvas, ClockSkinItem item) {
        List<Drawable> drawables = item.getDrawables();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        Drawable periodIndicator = null;
        if (!DateFormat.is24HourFormat(context)) {
            if (drawables.size() > 11) {
                if (hour < 12) {
                    periodIndicator = drawables.get(11);
                } else {
                    periodIndicator = drawables.get(12);
                }
            }

            hour = hour % 12;
            if (hour == 0) {
                hour = 12;
            }
        }
        Drawable h10 = drawables.get(hour / 10);
        Drawable h1 = drawables.get(hour % 10);
        Drawable separator = drawables.get(10);
        Drawable m10 = drawables.get(minute / 10);
        Drawable m1 = drawables.get(minute % 10);

        int centerX = viewCenterX + item.getCenterX();
        int centerY = viewCenterY + item.getCenterY();
        int numberWidth = h10.getIntrinsicWidth();
        int numberHalfHeight = h10.getIntrinsicHeight() / 2;
        int separatorWidth = separator.getIntrinsicWidth();
        int periodWidth = 0;
        if (periodIndicator != null) {
            periodWidth = periodIndicator.getIntrinsicWidth();
        }
        int startX = centerX - ((((numberWidth * 4) + separatorWidth) + periodWidth) / 2);

        int top = centerY - numberHalfHeight;
        int bottom = centerY + numberHalfHeight;

        h10.setBounds(startX, top, startX + numberWidth, bottom);
        h10.draw(canvas);
        h1.setBounds(startX + numberWidth, top, (numberWidth * 2) + startX, bottom);
        h1.draw(canvas);
        if (calendar.get(Calendar.SECOND) % 2 == 0) {
            separator.setBounds((numberWidth * 2) + startX, top, (numberWidth * 2) + startX + separatorWidth, bottom);
            separator.draw(canvas);
        }
        m10.setBounds((numberWidth * 2) + startX + separatorWidth, top, (numberWidth * 3) + startX + separatorWidth, bottom);
        m10.draw(canvas);
        m1.setBounds((numberWidth * 3) + startX + separatorWidth, top, (numberWidth * 4) + startX + separatorWidth, bottom);
        m1.draw(canvas);
        if (periodIndicator != null) {
            periodIndicator.setBounds((numberWidth * 4) + startX + separatorWidth, top, (numberWidth * 4) + startX + separatorWidth + periodWidth, bottom);
            periodIndicator.draw(canvas);
        }
    }

    private void drawArrayBattery(Canvas canvas, ClockSkinItem item) {
        List<Drawable> drawables = item.getDrawables();
        int battery = batteryPercentage;
        Drawable b100;
        if ((battery / 100) == 0) {
            b100 = drawables.get(10);
        } else {
            b100 = drawables.get(battery / 100);
        }
        Drawable b10 = drawables.get((battery % 100) / 10);
        Drawable b1 = drawables.get((battery % 100) % 10);
        Drawable symbol = null;
        if (drawables.size() == 12) {
            symbol = drawables.get(11);
        }

        int centerX = viewCenterX + item.getCenterX();
        int centerY = viewCenterY + item.getCenterY();
        int width = b1.getIntrinsicWidth();
        int halfHeight = b1.getIntrinsicHeight() / 2;
        int top = centerY - halfHeight;
        int bottom = centerY + halfHeight;
        b100.setBounds(centerX - (width * 2), top, centerX - width, bottom);
        b100.draw(canvas);
        b10.setBounds(centerX - width, top, centerX, bottom);
        b10.draw(canvas);
        b1.setBounds(centerX, top, centerX + width, bottom);
        b1.draw(canvas);
        if (symbol != null) {
            symbol.setBounds(centerX + width, top, centerX + (width * 2), bottom);
            symbol.draw(canvas);
        }
    }

    private void drawArrayYear(Canvas canvas, ClockSkinItem item) {
        List<Drawable> drawables = item.getDrawables();
        int year = calendar.get(Calendar.YEAR);
        Drawable y1000 = drawables.get(year / 1000);
        Drawable y100 = drawables.get((year % 1000) / 100);
        Drawable y10 = drawables.get(((year % 1000) % 100) / 10);
        Drawable y1 = drawables.get(((year % 1000) % 100) % 10);

        int centerX = viewCenterX + item.getCenterX();
        int centerY = viewCenterY + item.getCenterY();
        int width = y1000.getIntrinsicWidth();
        int halfHeight = y1000.getIntrinsicHeight() / 2;
        int top = centerY - halfHeight;
        int bottom = centerY + halfHeight;
        y1000.setBounds(centerX - (width * 2), top, centerX - width, bottom);
        y1000.draw(canvas);
        y100.setBounds(centerX - width, top, centerX, bottom);
        y100.draw(canvas);
        y10.setBounds(centerX, top, centerX + width, bottom);
        y10.draw(canvas);
        y1.setBounds(centerX + width, top, centerX + (width * 2), bottom);
        y1.draw(canvas);
    }

    private void drawArrayTemperature(Canvas canvas, ClockSkinItem item) {
        List<Drawable> drawables = item.getDrawables();
        int weatherTemp = SystemHelper.getWeatherTemp(context);
        boolean negative = false;
        if (weatherTemp < 0) {
            weatherTemp = -weatherTemp;
            negative = true;
        }

        Drawable sign = drawables.get(10);
        Drawable t10 = drawables.get(weatherTemp / 10);
        Drawable t1 = drawables.get(weatherTemp % 10);
        Drawable unit = drawables.get(11);

        int centerX = viewCenterX + item.getCenterX();
        int centerY = viewCenterY + item.getCenterY();
        int numberWidth = t10.getIntrinsicWidth();
        int halfHeight = t10.getIntrinsicHeight() / 2;
        int top = centerY - halfHeight;
        int bottom = centerY + halfHeight;
        if (negative) {
            int signWidth = sign.getIntrinsicWidth();
            sign.setBounds(centerX - numberWidth - signWidth, top, centerX - numberWidth, bottom);
            sign.draw(canvas);
        }
        t10.setBounds(centerX - numberWidth, top, centerX, bottom);
        t10.draw(canvas);
        t1.setBounds(centerX, top, centerX + numberWidth, bottom);
        t1.draw(canvas);
        int unitWidth = unit.getIntrinsicWidth();
        int unitHeight = unit.getIntrinsicHeight();
        unit.setBounds(centerX + numberWidth, bottom - unitHeight, centerX + numberWidth + unitWidth, bottom);
        unit.draw(canvas);
    }

    private void drawDial(Canvas canvas, ClockSkinItem item) {
        Drawable drawable = item.getDrawable();
        if (drawable != null) {
            int centerX = viewCenterX + item.getCenterX();
            int centerY = viewCenterY + item.getCenterY();
            int halfWidth = drawable.getIntrinsicWidth() / 2;
            int halfHeight = drawable.getIntrinsicHeight() / 2;
            drawable.setBounds(centerX - halfWidth, centerY - halfHeight, centerX + halfWidth, centerY + halfHeight);
            drawable.draw(canvas);
        }
    }

    private void drawHand(Canvas canvas, ClockSkinItem item, float angle) {
        Drawable drawable = item.getDrawable();
        if (drawable != null) {
            int centerX = viewCenterX + item.getCenterX();
            int centerY = viewCenterY + item.getCenterY();
            int halfWidth = drawable.getIntrinsicWidth() / 2;
            int halfHeight = drawable.getIntrinsicHeight() / 2;
            drawable.setBounds(centerX - halfWidth, centerY - halfHeight, centerX + halfWidth, centerY + halfHeight);
            canvas.save();
            canvas.rotate(angle, (float) centerX, (float) centerY);
            drawable.draw(canvas);
            canvas.restore();
        }
    }

    private ClockSkin parseClockSkin(ClockSkin clockSkin) {
        try (InputStream is = clockSkin.getClockSkinFile(ClockSkinConstants.CLOCK_SKIN_XML)) {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(is, null);
            parser.nextTag();
            parser.require(XmlPullParser.START_TAG, null, "clockskin");

            String width = parser.getAttributeValue(null, "width");
            String height = parser.getAttributeValue(null, "height");
            if (width != null) {
                Log.d(TAG, "parseClockSkin: width = " + width);
                clockSkin.setWidth(Integer.valueOf(width));
            }
            if (height != null) {
                Log.d(TAG, "parseClockSkin: height = " + height);
                clockSkin.setHeight(Integer.valueOf(height));
            }

            boolean invertDirection = false;
            while (parser.next() != XmlPullParser.END_DOCUMENT) {
                if (parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }

                if (parser.getName().equals(ClockSkinConstants.TAG_DRAWABLE)) {
                    Log.d(TAG, "parseClockSkin: new drawable!");
                    ClockSkinItem clockSkinItem = parseDrawableTag(parser, clockSkin);
                    if (clockSkinItem.getDirection() == ClockSkinConstants.DIRECTION_REVERSE) {
                        if (invertDirection) {
                            clockSkinItem.setDirection(ClockSkinConstants.DIRECTION_NORMAL);
                            invertDirection = false;
                        } else {
                            invertDirection = true;
                        }
                    } else if (invertDirection) {
                        clockSkinItem.setDirection(ClockSkinConstants.DIRECTION_REVERSE);
                    }
                    clockSkinItem.setTimeZone(calendar.getTimeZone());
                    clockSkin.addClockSkinItem(clockSkinItem);
                    Log.d(TAG, "parseClockSkin: finished parsing drawable");
                }
            }
        } catch (XmlPullParserException | IOException e) {
            Log.e(TAG, "parseClockSkin: error while parsing the ClockSkin!", e);
            e.printStackTrace();
        }
        Log.d(TAG, "parseClockSkin: finished parsing!");

        return clockSkin;
    }

    private void parseDrawableArray(ClockSkin clockSkin, ClockSkinItem clockSkinItem, String name) {
        try (InputStream is = clockSkin.getClockSkinFile(name)) {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(is, null);
            parser.nextTag();
            parser.require(XmlPullParser.START_TAG, null, ClockSkinConstants.TAG_DRAWABLES);
            List<Drawable> drawables = new ArrayList<>();
            while (parser.next() != XmlPullParser.END_DOCUMENT) {
                if (parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }

                if (parser.getName().equals(ClockSkinConstants.TAG_IMAGE)) {
                    String image = parser.nextText();
                    Log.d(TAG, "parseDrawableArray: image = " + image);
                    drawables.add(clockSkin.getDrawable(context, image));
                }
            }
            clockSkinItem.setDrawables(drawables);
        } catch (IOException | XmlPullParserException e) {
            e.printStackTrace();
        }
    }

    private ClockSkinItem parseDrawableTag(XmlPullParser parser, ClockSkin clockSkin) throws IOException, XmlPullParserException {
        ClockSkinItem clockSkinItem = new ClockSkinItem();

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }

            switch (parser.getName()) {
                case ClockSkinConstants.TAG_NAME:
                    String repeat = parser.getAttributeValue(null, "repeat");
                    if (repeat != null) {
                        Log.d(TAG, "parseDrawableTag: repeat = " + repeat);
                        clockSkinItem.setRepeat(Integer.valueOf(repeat));
                    }
                    String name = parser.nextText();
                    Log.d(TAG, "parseDrawableTag: name = " + name);
                    clockSkinItem.setName(name);
                    if (name.endsWith(".xml")) {
                        parseDrawableArray(clockSkin, clockSkinItem, name);
                    } else {
                        clockSkinItem.setDrawable(clockSkin.getDrawable(context, name));
                    }
                    break;
                case ClockSkinConstants.TAG_ARRAY_TYPE:
                    String arrayType = parser.nextText();
                    Log.d(TAG, "parseDrawableTag: arrayType = " + arrayType);
                    clockSkinItem.setArrayType(Integer.valueOf(arrayType));
                    break;
                case ClockSkinConstants.TAG_CENTERX:
                    String centerX = parser.nextText();
                    Log.d(TAG, "parseDrawableTag: centerX = " + centerX);
                    clockSkinItem.setCenterX(Integer.valueOf(centerX));
                    break;
                case ClockSkinConstants.TAG_CENTERY:
                    String centerY = parser.nextText();
                    Log.d(TAG, "parseDrawableTag: centerY = " + centerY);
                    clockSkinItem.setCenterY(Integer.valueOf(centerY));
                    break;
                case ClockSkinConstants.TAG_ROTATE:
                    String rotate = parser.nextText();
                    Log.d(TAG, "parseDrawableTag: rotate = " + rotate);
                    clockSkinItem.setRotate(Integer.valueOf(rotate));
                    break;
                case ClockSkinConstants.TAG_OFFSET_ANGLE:
                    String offsetAngle = parser.nextText();
                    Log.d(TAG, "parseDrawableTag: offsetAngle = " + offsetAngle);
                    clockSkinItem.setOffsetAngle(Double.valueOf(offsetAngle));
                    break;
                case ClockSkinConstants.TAG_MUL_ROTATE:
                    String mulRotate = parser.nextText();
                    Log.d(TAG, "parseDrawableTag: mulRotate = " + mulRotate);
                    clockSkinItem.setMulRotate(Integer.valueOf(mulRotate));
                    break;
                case ClockSkinConstants.TAG_START_ANGLE:
                    String startAngle = parser.nextText();
                    Log.d(TAG, "parseDrawableTag: startAngle = " + startAngle);
                    clockSkinItem.setStartAngle(Integer.valueOf(startAngle));
                    break;
                case ClockSkinConstants.TAG_DIRECTION:
                    String direction = parser.nextText();
                    Log.d(TAG, "parseDrawableTag: direction = " + direction);
                    clockSkinItem.setDirection(Integer.valueOf(direction));
                    break;
                case ClockSkinConstants.TAG_TEXT_SIZE:
                    String textSize = parser.nextText();
                    Log.d(TAG, "parseDrawableTag: textSize = " + textSize);
                    clockSkinItem.setTextSize(Integer.valueOf(textSize));
                    break;
                case ClockSkinConstants.TAG_TEXT_COLOR:
                    String textColor = parser.nextText();
                    Log.d(TAG, "parseDrawableTag: textColor = " + textColor);
                    //clockSkinItem.setTextColor(textColor);
                    break;
                case ClockSkinConstants.TAG_COLOR_ARRAY:
                    String colorArray = parser.nextText();
                    Log.d(TAG, "parseDrawableTag: colorArray = " + colorArray);
                    clockSkinItem.setColorArray(colorArray);
                    break;
                case ClockSkinConstants.TAG_COLOR:
                    String color = parser.nextText();
                    Log.d(TAG, "parseDrawableTag: color = " + color);
                    clockSkinItem.setColor(Integer.valueOf(color));
                    break;
                case ClockSkinConstants.TAG_WIDTH:
                    String width = parser.nextText();
                    Log.d(TAG, "parseDrawableTag: width = " + width);
                    //clockSkinItem.setWidth(Integer.valueOf(width));
                    break;
                case ClockSkinConstants.TAG_RADIUS:
                    String radius = parser.nextText();
                    Log.d(TAG, "parseDrawableTag: radius = " + radius);
                    //clockSkinItem.setRadius(radius);
                    break;
                case ClockSkinConstants.TAG_ROTATE_MODE:
                    String rotateMode = parser.nextText();
                    Log.d(TAG, "parseDrawableTag: rotateMode = " + rotateMode);
                    //clockSkinItem.setRotateMode(Integer.valueOf(rotateMode));
                    break;
                case ClockSkinConstants.TAG_CLASS_NAME:
                    String className = parser.nextText();
                    Log.d(TAG, "parseDrawableTag: className = " + className);
                    clockSkinItem.setClassName(className);
                    break;
                case ClockSkinConstants.TAG_PACKAGE_NAME:
                    String packageName = parser.nextText();
                    Log.d(TAG, "parseDrawableTag: packageName = " + packageName);
                    clockSkinItem.setPackageName(packageName);
                    break;
                case ClockSkinConstants.TAG_RANGE:
                    String range = parser.nextText();
                    Log.d(TAG, "parseDrawableTag: range = " + range);
                    clockSkinItem.setRange(Integer.valueOf(range));
                    break;
                case ClockSkinConstants.TAG_FRAMERATE:
                    String framerate = parser.nextText();
                    Log.d(TAG, "parseDrawableTag: framerate = " + framerate);
                    clockSkinItem.setFramerate(Double.parseDouble(framerate));
            }
        }

        return clockSkinItem;
    }
}
