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

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.UserHandle;
import android.provider.Settings;
import android.view.MenuItem;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceFragment;
import androidx.preference.SwitchPreference;
import androidx.preference.TwoStatePreference;

import org.aosp.device.DeviceSettings.ModeSwitch.DCModeSwitch;
import org.aosp.device.DeviceSettings.ModeSwitch.HBMModeSwitch;

import org.aosp.device.DeviceSettings.ModeSwitch.GameModeSwitch;
import org.aosp.device.DeviceSettings.ModeSwitch.EdgeTouchSwitch;

public class DeviceSettings extends PreferenceFragment
        implements Preference.OnPreferenceChangeListener {

    private static final String KEY_CATEGORY_CAMERA = "camera";
    public static final String KEY_SRGB_SWITCH = "srgb";
    public static final String KEY_DCI_SWITCH = "dci";
    public static final String KEY_NIGHT_SWITCH = "night";
    public static final String KEY_WIDECOLOR_SWITCH = "widecolor";
    public static final String KEY_NATURAL_SWITCH = "natural";
    public static final String KEY_VIVID_SWITCH = "vivid";

    private static final String KEY_ALWAYS_CAMERA_DIALOG = "always_on_camera_dialog";

    public static final String KEY_SETTINGS_PREFIX = "device_setting_";

    private static final String POPUP_HELPER_PKG_NAME = "org.lineageos.camerahelper";

    private TwoStatePreference mDCModeSwitch;
    private TwoStatePreference mHBMModeSwitch;
    private SwitchPreference mAlwaysCameraSwitch;
    private SwitchPreference mMuteMediaSwitch;
    private TwoStatePreference mGameModeSwitch;
    private TwoStatePreference mEdgeTouchSwitch;

    private boolean mInternalHbmStart = false;
    private boolean mInternalDCStart = false;
    private boolean mInternalGameStart = false;
    private boolean mInternalEdgeStart = false;

    private final BroadcastReceiver mServiceStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case HBMModeSwitch.ACTION_HBM_SERVICE_CHANGED:
                    if (mInternalHbmStart) {
                        mInternalHbmStart = false;
                        return;
                    }
                    if (mHBMModeSwitch == null) return;
                    final boolean hbmStarted = intent.getBooleanExtra(
                            HBMModeSwitch.EXTRA_HBM_STATE, false);
                    mHBMModeSwitch.setChecked(hbmStarted);
                    break;
                case DCModeSwitch.ACTION_DCMODE_CHANGED:
                    if (mInternalDCStart) {
                        mInternalDCStart = false;
                        return;
                    }
                    if (mDCModeSwitch == null) return;
                    final boolean dcEnabled = intent.getBooleanExtra(
                            DCModeSwitch.EXTRA_DCMODE_STATE, false);
                    mDCModeSwitch.setChecked(dcEnabled);
                    break;
                case GameModeSwitch.ACTION_GAMEMODE_CHANGED:
                    if (mInternalGameStart) {
                        mInternalGameStart = false;
                        return;
                    }
                    if (mGameModeSwitch == null) return;
                    final boolean gameEnabled = intent.getBooleanExtra(
                            GameModeSwitch.EXTRA_GAMEMODE_STATE, false);
                    mGameModeSwitch.setChecked(gameEnabled);
                    break;
                case EdgeTouchSwitch.ACTION_EDGETOUCH_CHANGED:
                    if (mInternalEdgeStart) {
                        mInternalEdgeStart = false;
                        return;
                    }
                    if (mEdgeTouchSwitch == null) return;
                    final boolean edgeEnabled = intent.getBooleanExtra(
                            EdgeTouchSwitch.EXTRA_EDGETOUCH_STATE, false);
                    mEdgeTouchSwitch.setChecked(edgeEnabled);
                    break;
            }
        }
    };

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.main);
        
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        ListPreference mTopKeyPref = findPreference(Constants.NOTIF_SLIDER_TOP_KEY);
        mTopKeyPref.setValueIndex(Constants.getPreferenceInt(getContext(), Constants.NOTIF_SLIDER_TOP_KEY));
        mTopKeyPref.setOnPreferenceChangeListener(this);
        ListPreference mMiddleKeyPref = findPreference(Constants.NOTIF_SLIDER_MIDDLE_KEY);
        mMiddleKeyPref.setValueIndex(Constants.getPreferenceInt(getContext(), Constants.NOTIF_SLIDER_MIDDLE_KEY));
        mMiddleKeyPref.setOnPreferenceChangeListener(this);
        ListPreference mBottomKeyPref = findPreference(Constants.NOTIF_SLIDER_BOTTOM_KEY);
        mBottomKeyPref.setValueIndex(Constants.getPreferenceInt(getContext(), Constants.NOTIF_SLIDER_BOTTOM_KEY));
        mBottomKeyPref.setOnPreferenceChangeListener(this);

        mMuteMediaSwitch = findPreference(Constants.NOTIF_SLIDER_MUTE_MEDIA_KEY);
        mMuteMediaSwitch.setChecked(Constants.getIsMuteMediaEnabled(getContext()));
        mMuteMediaSwitch.setOnPreferenceChangeListener(this);

        mDCModeSwitch = findPreference(DCModeSwitch.KEY_DC_SWITCH);
        mDCModeSwitch.setEnabled(DCModeSwitch.isSupported());
        mDCModeSwitch.setChecked(DCModeSwitch.isCurrentlyEnabled());
        mDCModeSwitch.setOnPreferenceChangeListener(this);

        mHBMModeSwitch = findPreference(HBMModeSwitch.KEY_HBM_SWITCH);
        mHBMModeSwitch.setEnabled(HBMModeSwitch.isSupported());
        mHBMModeSwitch.setChecked(HBMModeSwitch.isCurrentlyEnabled());
        mHBMModeSwitch.setOnPreferenceChangeListener(this);

        PreferenceCategory mCameraCategory = findPreference(KEY_CATEGORY_CAMERA);
        boolean hasPopup = Utils.isPackageInstalled(POPUP_HELPER_PKG_NAME, getContext());
        if (hasPopup) {
            mAlwaysCameraSwitch = findPreference(KEY_ALWAYS_CAMERA_DIALOG);
            boolean enabled = Settings.System.getInt(getContext().getContentResolver(),
                    KEY_SETTINGS_PREFIX + KEY_ALWAYS_CAMERA_DIALOG, 0) == 1;
            mAlwaysCameraSwitch.setChecked(enabled);
            mAlwaysCameraSwitch.setOnPreferenceChangeListener(this);
        } else {
            mCameraCategory.setVisible(false);
        }

        mGameModeSwitch = findPreference(GameModeSwitch.KEY_GAME_SWITCH);
        if(GameModeSwitch.isSupported()) {
            mGameModeSwitch.setEnabled(true);
            mGameModeSwitch.setChecked(GameModeSwitch.isCurrentlyEnabled());
            mGameModeSwitch.setOnPreferenceChangeListener(this);
        } else {
            mGameModeSwitch.setEnabled(false);
        }

        mEdgeTouchSwitch = findPreference(EdgeTouchSwitch.KEY_EDGE_SWITCH);
        if(EdgeTouchSwitch.isSupported()) {
            mEdgeTouchSwitch.setEnabled(true);
            mEdgeTouchSwitch.setChecked(EdgeTouchSwitch.isCurrentlyEnabled());
            mEdgeTouchSwitch.setOnPreferenceChangeListener(this);
        } else {
            mEdgeTouchSwitch.setEnabled(false);
        }

        // Registering observers
        IntentFilter filter = new IntentFilter();
        filter.addAction(HBMModeSwitch.ACTION_HBM_SERVICE_CHANGED);
        filter.addAction(DCModeSwitch.ACTION_DCMODE_CHANGED);
        filter.addAction(GameModeSwitch.ACTION_GAMEMODE_CHANGED);
        filter.addAction(EdgeTouchSwitch.ACTION_EDGETOUCH_CHANGED);
        getContext().registerReceiver(mServiceStateReceiver, filter);
    }

    @Override
    public void onResume() {
        super.onResume();
        mHBMModeSwitch.setChecked(HBMModeSwitch.isCurrentlyEnabled());
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        final ContentResolver resolver = getContext().getContentResolver();
        if (preference == mAlwaysCameraSwitch) {
            boolean enabled = (Boolean) newValue;
            Settings.System.putInt(resolver,
                    KEY_SETTINGS_PREFIX + KEY_ALWAYS_CAMERA_DIALOG,
                    enabled ? 1 : 0);
        } else if (preference == mHBMModeSwitch) {
            mInternalHbmStart = true;
            Boolean enabled = (Boolean) newValue;
            HBMModeSwitch.setEnabled(enabled, getContext());            
        } else if (preference == mMuteMediaSwitch) {
            Boolean enabled = (Boolean) newValue;
            Settings.System.putInt(resolver,
                    Constants.NOTIF_SLIDER_MUTE_MEDIA_KEY, enabled ? 1 : 0);
        } else if (preference == mDCModeSwitch) {
            mInternalDCStart = true;
            Boolean enabled = (Boolean) newValue;
            DCModeSwitch.setEnabled(enabled, getContext());
        } else if (preference == mGameModeSwitch) {
            mInternalGameStart = true;
            Boolean enabled = (Boolean) newValue;
            GameModeSwitch.setEnabled(enabled, getContext());
        } else if (preference == mEdgeTouchSwitch) {
            mInternalEdgeStart = true;
            Boolean enabled = (Boolean) newValue;
            EdgeTouchSwitch.setEnabled(enabled, getContext());
        } else if (newValue instanceof String) {
            Constants.setPreferenceInt(getContext(), preference.getKey(),
                    Integer.parseInt((String) newValue));
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Respond to the action bar's Up/Home button
        if (item.getItemId() == android.R.id.home) {
            getActivity().finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getContext().unregisterReceiver(mServiceStateReceiver);
    }
}
