/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.openwatchproject.launcher.notification.view;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.os.Handler;
import android.util.AttributeSet;
import android.widget.RemoteViews.RemoteView;

import androidx.appcompat.widget.AppCompatTextView;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import static android.text.format.DateUtils.DAY_IN_MILLIS;
import static android.text.format.DateUtils.HOUR_IN_MILLIS;
import static android.text.format.DateUtils.MINUTE_IN_MILLIS;
import static android.text.format.DateUtils.YEAR_IN_MILLIS;
import static android.text.format.Time.getJulianDay;

/**
 * Displays a given time in a convenient human-readable foramt.
 */
@RemoteView
public class DateTimeView extends AppCompatTextView {
    private static final ThreadLocal<ReceiverInfo> sReceiverInfo = new ThreadLocal<>();
    Date mTime;
    long mTimeMillis;
    DateFormat mLastFormat;
    private long mUpdateTimeMillis;

    public DateTimeView(Context context) {
        this(context, null);
    }

    public DateTimeView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    // Return the date difference for the two times in a given timezone.
    private static int dayDistance(TimeZone timeZone, long startTime,
                                   long endTime) {
        return getJulianDay(endTime, timeZone.getOffset(endTime) / 1000)
                - getJulianDay(startTime, timeZone.getOffset(startTime) / 1000);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        ReceiverInfo ri = sReceiverInfo.get();
        if (ri == null) {
            ri = new ReceiverInfo();
            sReceiverInfo.set(ri);
        }
        ri.addView(this);
        // The view may not be added to the view hierarchy immediately right after setTime()
        // is called which means it won't get any update from intents before being added.
        // In such case, the view might show the incorrect relative time after being added to the
        // view hierarchy until the next update intent comes.
        // So we update the time here to prevent this case.
        update();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        final ReceiverInfo ri = sReceiverInfo.get();
        if (ri != null) {
            ri.removeView(this);
        }
    }

    public void setTime(long time) {
        Calendar c = new GregorianCalendar();
        c.setTimeInMillis(time);
        mTimeMillis = time;
        c.add(Calendar.YEAR, -1900);
        mTime = c.getTime();
        update();
    }

    @Override
    public void setVisibility(int visibility) {
        boolean gotVisible = visibility != GONE && getVisibility() == GONE;
        super.setVisibility(visibility);
        if (gotVisible) {
            update();
        }
    }

    void update() {
        if (mTime == null || getVisibility() == GONE) {
            return;
        }

        updateRelativeTime();
    }

    private void updateRelativeTime() {
        long now = System.currentTimeMillis();
        long duration = Math.abs(now - mTimeMillis);
        int count;
        long millisIncrease;
        boolean past = (now >= mTimeMillis);
        String result;
        if (duration < MINUTE_IN_MILLIS) {
            setText("now");
            mUpdateTimeMillis = mTimeMillis + MINUTE_IN_MILLIS + 1;
            return;
        } else if (duration < HOUR_IN_MILLIS) {
            count = (int) (duration / MINUTE_IN_MILLIS);
            result = String.format(past
                            ? "%dm"
                            : "in %dm",
                    count);
            millisIncrease = MINUTE_IN_MILLIS;
        } else if (duration < DAY_IN_MILLIS) {
            count = (int) (duration / HOUR_IN_MILLIS);
            result = String.format(past
                            ? "%dh"
                            : "in %dh",
                    count);
            millisIncrease = HOUR_IN_MILLIS;
        } else if (duration < YEAR_IN_MILLIS) {
            // In weird cases it can become 0 because of daylight savings
            TimeZone timeZone = TimeZone.getDefault();
            count = Math.max(Math.abs(dayDistance(timeZone, mTimeMillis, now)), 1);
            result = String.format(past
                            ? "%dd"
                            : "in %dd",
                    count);
            if (past || count != 1) {
                mUpdateTimeMillis = computeNextMidnight(timeZone);
                millisIncrease = -1;
            } else {
                millisIncrease = DAY_IN_MILLIS;
            }
        } else {
            count = (int) (duration / YEAR_IN_MILLIS);
            result = String.format(past
                            ? "%dy"
                            : "in %dy",
                    count);
            millisIncrease = YEAR_IN_MILLIS;
        }
        if (millisIncrease != -1) {
            if (past) {
                mUpdateTimeMillis = mTimeMillis + millisIncrease * (count + 1) + 1;
            } else {
                mUpdateTimeMillis = mTimeMillis - millisIncrease * count + 1;
            }
        }
        setText(result);
    }

    /**
     * @param timeZone the timezone we are in
     * @return the timepoint in millis at UTC at midnight in the current timezone
     */
    private long computeNextMidnight(TimeZone timeZone) {
        Calendar c = Calendar.getInstance();
        c.setTimeZone(timeZone);
        c.add(Calendar.DAY_OF_MONTH, 1);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        update();
    }

    void clearFormatAndUpdate() {
        mLastFormat = null;
        update();
    }

    private static class ReceiverInfo {
        private final ArrayList<DateTimeView> mAttachedViews = new ArrayList<>();
        private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (Intent.ACTION_TIME_TICK.equals(action)) {
                    if (System.currentTimeMillis() < getSoonestUpdateTime()) {
                        // The update() function takes a few milliseconds to run because of
                        // all of the time conversions it needs to do, so we can't do that
                        // every minute.
                        return;
                    }
                }
                // ACTION_TIME_CHANGED can also signal a change of 12/24 hr. format.
                updateAll();
            }
        };
        private final ContentObserver mObserver = new ContentObserver(new Handler()) {
            @Override
            public void onChange(boolean selfChange) {
                updateAll();
            }
        };
        private Handler mHandler = new Handler();

        static final Context getApplicationContextIfAvailable(Context context) {
            final Context ac = context.getApplicationContext();
            return ac != null ? ac : context;
        }

        void addView(DateTimeView v) {
            synchronized (mAttachedViews) {
                final boolean register = mAttachedViews.isEmpty();
                mAttachedViews.add(v);
                if (register) {
                    register(getApplicationContextIfAvailable(v.getContext()));
                }
            }
        }

        void removeView(DateTimeView v) {
            synchronized (mAttachedViews) {
                final boolean removed = mAttachedViews.remove(v);
                // Only unregister once when we remove the last view in the list otherwise we risk
                // trying to unregister a receiver that is no longer registered.
                if (removed && mAttachedViews.isEmpty()) {
                    unregister(getApplicationContextIfAvailable(v.getContext()));
                }
            }
        }

        void updateAll() {
            synchronized (mAttachedViews) {
                final int count = mAttachedViews.size();
                for (int i = 0; i < count; i++) {
                    DateTimeView view = mAttachedViews.get(i);
                    view.post(view::clearFormatAndUpdate);
                }
            }
        }

        long getSoonestUpdateTime() {
            long result = Long.MAX_VALUE;
            synchronized (mAttachedViews) {
                final int count = mAttachedViews.size();
                for (int i = 0; i < count; i++) {
                    final long time = mAttachedViews.get(i).mUpdateTimeMillis;
                    if (time < result) {
                        result = time;
                    }
                }
            }
            return result;
        }

        void register(Context context) {
            final IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_TIME_TICK);
            filter.addAction(Intent.ACTION_TIME_CHANGED);
            filter.addAction(Intent.ACTION_CONFIGURATION_CHANGED);
            filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
            context.registerReceiver(mReceiver, filter, null, mHandler);
        }

        void unregister(Context context) {
            context.unregisterReceiver(mReceiver);
        }
    }
}