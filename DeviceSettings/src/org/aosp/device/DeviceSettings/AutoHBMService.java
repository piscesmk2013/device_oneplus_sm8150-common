/*
 * Copyright (C) 2022 PixelExperience Project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.aosp.device.DeviceSettings;

import android.app.KeyguardManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.os.PowerManager;
import androidx.preference.PreferenceManager;

public class AutoHBMService extends Service {
    private static final String HBM_FILE = "/sys/devices/platform/soc/ae00000.qcom,mdss_mdp/drm/card0/card0-DSI-1/hbm";

    private static boolean mAutoHBMActive = false;

    private SensorManager mSensorManager;
    Sensor mLightSensor;

    private SharedPreferences mSharedPrefs;

    public void activateLightSensorRead() {
        mSensorManager = (SensorManager) getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
        mLightSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        mSensorManager.registerListener(mSensorEventListener, mLightSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void deactivateLightSensorRead() {
        mSensorManager.unregisterListener(mSensorEventListener);
        mAutoHBMActive = false;
        enableHBM(false);
    }

    private void enableHBM(boolean enable) {
        if (enable) {
            Utils.writeValue(HBM_FILE, "5");
        } else {
            Utils.writeValue(HBM_FILE, "0");
        }
    }

    private boolean isCurrentlyEnabled() {
        return Utils.getFileValueAsBoolean(HBM_FILE, false);
    }

    SensorEventListener mSensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            float lux = event.values[0];
            KeyguardManager km =
                    (KeyguardManager) getSystemService(getApplicationContext().KEYGUARD_SERVICE);
            boolean keyguardShowing = km.inKeyguardRestrictedInputMode();
            float threshold = Float.parseFloat(mSharedPrefs.getString(DeviceSettings.KEY_AUTO_HBM_THRESHOLD, "20000"));
            if (lux > threshold) {
                if ((!mAutoHBMActive | !isCurrentlyEnabled()) && !keyguardShowing) {
                    mAutoHBMActive = true;
                    enableHBM(true);
                }
            }
            if (lux < threshold) {
                if (mAutoHBMActive) {
                    mAutoHBMActive = false;
                    enableHBM(false);
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // do nothing
        }
    };

    private BroadcastReceiver mScreenStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                activateLightSensorRead();
            } else if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                deactivateLightSensorRead();
            }
        }
    };

    @Override
    public void onCreate() {
        IntentFilter screenStateFilter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        screenStateFilter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(mScreenStateReceiver, screenStateFilter);
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (pm.isInteractive()) {
            activateLightSensorRead();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mScreenStateReceiver);
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (pm.isInteractive()) {
            deactivateLightSensorRead();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
