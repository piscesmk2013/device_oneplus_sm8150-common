/*
 * Copyright (C) 2022 PixelExperience Project
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

package org.aosp.device.DeviceSettings;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.UserHandle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.SparseIntArray;
import android.util.Log;
import android.view.KeyEvent;

import com.android.internal.os.DeviceKeyHandler;

public class KeyHandler implements DeviceKeyHandler {

    private static final String TAG = KeyHandler.class.getSimpleName();

    private static final SparseIntArray sSupportedSliderZenModes = new SparseIntArray();
    private static final SparseIntArray sSupportedSliderRingModes = new SparseIntArray();
    private static final SparseIntArray sSupportedSliderHaptics = new SparseIntArray();
    static {
        sSupportedSliderZenModes.put(Constants.KEY_VALUE_TOTAL_SILENCE, Settings.Global.ZEN_MODE_NO_INTERRUPTIONS);
        sSupportedSliderZenModes.put(Constants.KEY_VALUE_SILENT, Settings.Global.ZEN_MODE_OFF);
        sSupportedSliderZenModes.put(Constants.KEY_VALUE_PRIORTY_ONLY, Settings.Global.ZEN_MODE_IMPORTANT_INTERRUPTIONS);
        sSupportedSliderZenModes.put(Constants.KEY_VALUE_VIBRATE, Settings.Global.ZEN_MODE_OFF);
        sSupportedSliderZenModes.put(Constants.KEY_VALUE_NORMAL, Settings.Global.ZEN_MODE_OFF);

        sSupportedSliderRingModes.put(Constants.KEY_VALUE_TOTAL_SILENCE, AudioManager.RINGER_MODE_NORMAL);
        sSupportedSliderRingModes.put(Constants.KEY_VALUE_SILENT, AudioManager.RINGER_MODE_SILENT);
        sSupportedSliderRingModes.put(Constants.KEY_VALUE_PRIORTY_ONLY, AudioManager.RINGER_MODE_NORMAL);
        sSupportedSliderRingModes.put(Constants.KEY_VALUE_VIBRATE, AudioManager.RINGER_MODE_VIBRATE);
        sSupportedSliderRingModes.put(Constants.KEY_VALUE_NORMAL, AudioManager.RINGER_MODE_NORMAL);

        sSupportedSliderHaptics.put(Constants.KEY_VALUE_TOTAL_SILENCE, VibrationEffect.EFFECT_THUD);
        sSupportedSliderHaptics.put(Constants.KEY_VALUE_SILENT, VibrationEffect.EFFECT_DOUBLE_CLICK);
        sSupportedSliderHaptics.put(Constants.KEY_VALUE_PRIORTY_ONLY, VibrationEffect.EFFECT_POP);
        sSupportedSliderHaptics.put(Constants.KEY_VALUE_VIBRATE, VibrationEffect.EFFECT_HEAVY_CLICK);
        sSupportedSliderHaptics.put(Constants.KEY_VALUE_NORMAL, -1);
    }

    private final Context mContext;
    private final NotificationManager mNotificationManager;
    private final AudioManager mAudioManager;
    private Vibrator mVibrator;
    private int mPrevKeyCode = 0;

    public KeyHandler(Context context) {
        mContext = context;
        mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        mVibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (mVibrator == null || !mVibrator.hasVibrator()) {
            mVibrator = null;
        }
    }

    private boolean hasSetupCompleted() {
        return Settings.Secure.getInt(mContext.getContentResolver(),
                Settings.Secure.USER_SETUP_COMPLETE, 0) != 0;
    }

    public KeyEvent handleKeyEvent(KeyEvent event) {
        final int scanCode = event.getScanCode();
        final String keyCode = Constants.sKeyMap.get(scanCode);
        int keyCodeValue;

        try {
            keyCodeValue = Constants.getPreferenceInt(mContext, keyCode);
        } catch (Exception e) {
            return event;
        }

        if (!hasSetupCompleted()) {
            return event;
        }

        // We only want ACTION_UP event
        if (event.getAction() != KeyEvent.ACTION_UP) {
            return null;
        }

        doHapticFeedback(sSupportedSliderHaptics.get(keyCodeValue));
        mAudioManager.setRingerModeInternal(sSupportedSliderRingModes.get(keyCodeValue));
        if (mPrevKeyCode == Constants.KEY_VALUE_TOTAL_SILENCE)
            doHapticFeedback(sSupportedSliderHaptics.get(keyCodeValue));
        mNotificationManager.setZenMode(sSupportedSliderZenModes.get(keyCodeValue), null, TAG);

        if (Constants.getIsMuteMediaEnabled(mContext)) {
            final int max = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            if (keyCodeValue == Constants.KEY_VALUE_SILENT) {
                final int curr = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                Constants.setLastMediaLevel(mContext, Math.round((float)curr * 100f / (float)max));
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                        0, AudioManager.FLAG_SHOW_UI);
            } else if (mPrevKeyCode == Constants.KEY_VALUE_SILENT) {
                final int last = Constants.getLastMediaLevel(mContext);
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                        Math.round((float)max * (float)last / 100f), AudioManager.FLAG_SHOW_UI);
            }
        }

        int position = scanCode == 601 ? 2 : scanCode == 602 ? 1 : 0;
        mPrevKeyCode = keyCodeValue;
        int positionValue = 0;
        int key = sSupportedSliderRingModes.keyAt(
                sSupportedSliderRingModes.indexOfKey(keyCodeValue));
        switch (key) {
            case Constants.KEY_VALUE_TOTAL_SILENCE: // DND - no int'
                positionValue = Constants.MODE_TOTAL_SILENCE;
                break;
            case Constants.KEY_VALUE_SILENT: // Ringer silent
                positionValue = Constants.MODE_SILENT;
                break;
            case Constants.KEY_VALUE_PRIORTY_ONLY: // DND - priority
                positionValue = Constants.MODE_PRIORITY_ONLY;
                break;
            case Constants.KEY_VALUE_VIBRATE: // Ringer vibrate
                positionValue = Constants.MODE_VIBRATE;
                break;
            default:
            case Constants.KEY_VALUE_NORMAL: // Ringer normal DND off
                positionValue = Constants.MODE_RING;
                break;
        }

        sendUpdateBroadcast(position, positionValue);
        return null;
    }
    
    private void sendUpdateBroadcast(int position, int position_value) {
        Intent intent = new Intent(Constants.ACTION_UPDATE_SLIDER_POSITION);
        intent.putExtra(Constants.EXTRA_SLIDER_POSITION, position);
        intent.putExtra(Constants.EXTRA_SLIDER_POSITION_VALUE, position_value);
        mContext.sendBroadcastAsUser(intent, UserHandle.CURRENT);
        intent.setFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY);
        Log.d(TAG, "slider change to positon " + position
                            + " with value " + position_value);
    }

    private void doHapticFeedback(int effect) {
        if (mVibrator != null && mVibrator.hasVibrator() && effect != -1) {
            mVibrator.vibrate(VibrationEffect.get(effect));
        }
    }
}
