package com.triggerpo.fingerswipe;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.FingerprintGestureController;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import static android.accessibilityservice.FingerprintGestureController.*;

public final class SwipeService extends AccessibilityService {
    private static final String TAG = "FingerprintSwipe";
    static SwipeService active;
    private FingerprintGestureController controller;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private String sensorProblem;
    private int swipeCount;
    private String lastSwipe = "None yet";
    private String lastAction = "Not tried";

    private final FingerprintGestureController.FingerprintGestureCallback gestures =
            new FingerprintGestureController.FingerprintGestureCallback() {
                @Override
                public void onGestureDetected(int gesture) {
                    switch (gesture) {
                        case FINGERPRINT_GESTURE_SWIPE_DOWN:
                            lastSwipe = "Down";
                            openNotifications();
                            break;
                        case FINGERPRINT_GESTURE_SWIPE_UP:
                            lastSwipe = "Up";
                            break;
                        case FINGERPRINT_GESTURE_SWIPE_LEFT:
                            lastSwipe = "Left";
                            break;
                        case FINGERPRINT_GESTURE_SWIPE_RIGHT:
                            lastSwipe = "Right";
                            break;
                        default:
                            return;
                    }
                    swipeCount++;
                }
            };

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        active = this;
        if (controller != null) {
            return;
        }
        sensorProblem = null;
        try {
            FingerprintGestureController candidate = getFingerprintGestureController();
            if (candidate == null) {
                sensorProblem = "This phone did not provide a fingerprint gesture controller.";
                return;
            }
            candidate.registerFingerprintGestureCallback(gestures, mainHandler);
            controller = candidate;
        } catch (RuntimeException failure) {
            reportSensorFailure("Fingerprint gesture setup failed", failure);
        }
    }

    void openNotifications() {
        try {
            lastAction = performGlobalAction(GLOBAL_ACTION_NOTIFICATIONS)
                    ? "Android accepted the request"
                    : "Android could not open the panel";
        } catch (RuntimeException failure) {
            Log.w(TAG, "Notification panel action failed", failure);
            lastAction = "Android could not open the panel";
        }
    }

    String sensorStatus() {
        if (sensorProblem != null) {
            return sensorProblem;
        }
        if (controller == null) {
            return "Waiting for the fingerprint gesture controller";
        }
        try {
            return controller.isGestureDetectionAvailable()
                    ? "Available — swipe the sensor to test"
                    : "Android reports gestures unavailable. Try again while unlocked.";
        } catch (RuntimeException failure) {
            reportSensorFailure("Fingerprint gesture status check failed", failure);
            return sensorProblem;
        }
    }

    private void reportSensorFailure(String message, RuntimeException failure) {
        Log.w(TAG, message, failure);
        sensorProblem = message + " (" + failure.getClass().getSimpleName() + ").";
    }

    String swipeStatus() {
        return lastSwipe + " (" + swipeCount + " detected)";
    }

    String actionStatus() {
        return lastAction;
    }

    @Override
    public void onDestroy() {
        if (controller != null) {
            try {
                controller.unregisterFingerprintGestureCallback(gestures);
            } catch (RuntimeException failure) {
                Log.w(TAG, "Fingerprint gesture cleanup failed", failure);
            }
            controller = null;
        }
        mainHandler.removeCallbacksAndMessages(null);
        if (active == this) {
            active = null;
        }
        super.onDestroy();
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {}

    @Override
    public void onInterrupt() {}
}
