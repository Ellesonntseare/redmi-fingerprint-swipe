package com.triggerpo.fingerswipe;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.FingerprintGestureController;
import android.os.Handler;
import android.os.Looper;
import android.view.accessibility.AccessibilityEvent;

import static android.accessibilityservice.FingerprintGestureController.*;

public final class SwipeService extends AccessibilityService {
    static SwipeService active;
    private FingerprintGestureController controller;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
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
        if (controller == null) {
            controller = getFingerprintGestureController();
            controller.registerFingerprintGestureCallback(gestures, mainHandler);
        }
        active = this;
    }

    void openNotifications() {
        lastAction = performGlobalAction(GLOBAL_ACTION_NOTIFICATIONS)
                ? "Android accepted the request"
                : "Android could not open the panel";
    }

    String sensorStatus() {
        return controller != null && controller.isGestureDetectionAvailable()
                ? "Available — swipe the sensor to test"
                : "Unavailable — unlock the phone and try again";
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
            controller.unregisterFingerprintGestureCallback(gestures);
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
