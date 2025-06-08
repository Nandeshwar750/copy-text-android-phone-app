package com.example.txt_reader_flut;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MyAccessibilityService extends AccessibilityService {
    private static final String TAG = "MyAccessibilityService";
    private List<String> detectedTextList = new ArrayList<>();

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.d(TAG, "onAccessibilityEvent: Event received");

        if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED ||
            event.getEventType() == AccessibilityEvent.TYPE_VIEW_SCROLLED ||
            event.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {

            Log.d(TAG, "Event Type: " + event.getEventType());
            AccessibilityNodeInfo source = event.getSource();
            if (source != null) {
                traverseNodeHierarchy(source);
            } else {
                Log.d(TAG, "Event source is null");
            }
        } else {
            Log.d(TAG, "Unhandled event type: " + event.getEventType());
        }
    }

    private void traverseNodeHierarchy(AccessibilityNodeInfo node) {
        if (node == null) return;

        Log.d(TAG, "Node class: " + node.getClassName());

        if (node.getText() != null) {
            String text = node.getText().toString().trim();
            Log.d(TAG, "Node text: " + text);
            if (!text.isEmpty()) {
                detectedTextList.add(text);
                MainActivity.sendTextToFlutter(text);
                Log.d(TAG, "Text Detected: " + text);
            }
        } else {
            Log.d(TAG, "Node text is null");
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo childNode = node.getChild(i);
            if (childNode != null) {
                traverseNodeHierarchy(childNode);
            }
        }
    }

    @Override
    public void onInterrupt() {
        Log.d(TAG, "onInterrupt: Accessibility service interrupted");
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.d(TAG, "onServiceConnected: Accessibility service connected");

        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED |
                          AccessibilityEvent.TYPE_VIEW_SCROLLED |
                          AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        info.notificationTimeout = 100;
        setServiceInfo(info);

        Log.d(TAG, "onServiceConnected: Service info set");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: Accessibility service destroyed");
        saveTextToFile();
    }

    private void saveTextToFile() {
        Log.d(TAG, "saveTextToFile: Saving text to file");
    
        String directoryPath = getApplicationContext().getExternalFilesDir(null).getAbsolutePath();
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File file = new File(directoryPath, "detected_text_" + timestamp + ".txt");
    
        try (FileOutputStream fos = new FileOutputStream(file, true)) {
            for (String text : detectedTextList) {
                fos.write((text + "\n").getBytes());
            }
            Log.d(TAG, "saveTextToFile: Text saved to " + file.getAbsolutePath());
        } catch (IOException e) {
            Log.e(TAG, "saveTextToFile: Failed to save text", e);
        }
    }
}
