package com.triggerpo.fingerswipe;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public final class MainActivity extends Activity {
    private static final int BACKGROUND = 0xff121916;
    private static final int CARD = 0xff202b25;
    private static final int TEXT = 0xfff0f6f1;
    private static final int MUTED = 0xffbccdc2;
    private static final int ACCENT = 0xffacf5c8;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private TextView sensor;
    private TextView swipe;
    private TextView action;
    private Button test;

    private final Runnable refresh = new Runnable() {
        @Override
        public void run() {
            updateStatus();
            handler.postDelayed(this, 300);
        }
    };

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(BACKGROUND);
        getWindow().setNavigationBarColor(BACKGROUND);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(BACKGROUND);
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(22), dp(24), dp(22), dp(28));
        scroll.addView(content);
        setContentView(scroll);

        TextView title = label("Fingerprint Swipe", 31, TEXT);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        if (android.os.Build.VERSION.SDK_INT >= 28) {
            title.setAccessibilityHeading(true);
        }
        add(content, title, 0);
        add(content, label("Swipe down on the fingerprint sensor to open notifications.",
                17, MUTED), 8);

        LinearLayout status = new LinearLayout(this);
        status.setOrientation(LinearLayout.VERTICAL);
        status.setPadding(dp(16), dp(16), dp(16), dp(16));
        GradientDrawable surface = new GradientDrawable();
        surface.setColor(CARD);
        surface.setCornerRadius(dp(18));
        status.setBackground(surface);
        add(content, status, 24);

        sensor = statusRow(status, "Sensor gestures", 0);
        swipe = statusRow(status, "Last swipe", 16);
        action = statusRow(status, "Notification panel", 16);

        Button settings = button("Open accessibility settings");
        settings.setOnClickListener(v ->
                startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));
        add(content, settings, 16);

        test = button("Test notification panel");
        test.setOnClickListener(v -> {
            SwipeService service = SwipeService.active;
            if (service != null) {
                service.openNotifications();
                updateStatus();
            }
        });
        add(content, test, 4);

        add(content, label("Enable Fingerprint Swipe in Accessibility, then return here and slide your finger down the sensor without pressing the power button.", 15, MUTED), 13);
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.post(refresh);
    }

    @Override
    protected void onPause() {
        handler.removeCallbacks(refresh);
        super.onPause();
    }

    private void updateStatus() {
        SwipeService service = SwipeService.active;
        sensor.setText(service == null
                ? "Service disconnected — if enabled, turn it off and back on."
                : service.sensorStatus());
        swipe.setText(service == null ? "—" : service.swipeStatus());
        action.setText(service == null ? "—" : service.actionStatus());
        test.setEnabled(service != null);
    }

    private TextView statusRow(LinearLayout parent, String title, int gap) {
        add(parent, label(title, 14, ACCENT), gap);
        TextView value = label("", 17, TEXT);
        add(parent, value, 3);
        return value;
    }

    private TextView label(String text, int size, int color) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextSize(size);
        view.setTextColor(color);
        return view;
    }

    private Button button(String text) {
        Button button = new Button(this);
        button.setText(text);
        button.setAllCaps(false);
        button.setTextSize(16);
        button.setMinimumHeight(dp(54));
        button.setBackgroundTintList(new ColorStateList(
                new int[][] {new int[] {-android.R.attr.state_enabled}, new int[] {}},
                new int[] {CARD, ACCENT}));
        button.setTextColor(new ColorStateList(
                new int[][] {new int[] {-android.R.attr.state_enabled}, new int[] {}},
                new int[] {MUTED, Color.BLACK}));
        return button;
    }

    private void add(LinearLayout parent, android.view.View child, int gap) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topMargin = dp(gap);
        parent.addView(child, params);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
