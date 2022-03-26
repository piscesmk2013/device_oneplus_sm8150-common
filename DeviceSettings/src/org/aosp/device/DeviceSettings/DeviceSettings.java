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

public class DeviceSettings extends PreferenceFragment
        implements Preference.OnPreferenceChangeListener {

    public static final String KEY_SRGB_SWITCH = "srgb";
    public static final String KEY_HBM_SWITCH = "hbm";
    public static final String KEY_AUTO_HBM_SWITCH = "auto_hbm";
    public static final String KEY_AUTO_HBM_THRESHOLD = "auto_hbm_threshold";
    public static final String KEY_DCI_SWITCH = "dci";
    public static final String KEY_NIGHT_SWITCH = "night";
    public static final String KEY_WIDECOLOR_SWITCH = "widecolor";
    public static final String KEY_NATURAL_SWITCH = "natural";
    public static final String KEY_VIVID_SWITCH = "vivid";

    public static final String KEY_SETTINGS_PREFIX = "device_setting_";

    private TwoStatePreference mDCModeSwitch;
    private TwoStatePreference mHBMModeSwitch;
    private static TwoStatePreference mAutoHBMSwitch;
    private SwitchPreference mMuteMediaSwitch;

    private boolean mInternalHbmStart = false;
    private boolean mInternalDCStart = false;

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
            }
        }
    };

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.main);

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

        mHBMModeSwitch = findPreference(KEY_HBM_SWITCH);
        mHBMModeSwitch.setEnabled(HBMModeSwitch.isSupported());
        mHBMModeSwitch.setChecked(HBMModeSwitch.isCurrentlyEnabled());
        mHBMModeSwitch.setOnPreferenceChangeListener(this);

        mAutoHBMSwitch = (TwoStatePreference) findPreference(KEY_AUTO_HBM_SWITCH);
        mAutoHBMSwitch.setChecked(PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean(DeviceSettings.KEY_AUTO_HBM_SWITCH, false));
        mAutoHBMSwitch.setOnPreferenceChangeListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        mHBMModeSwitch.setChecked(HBMModeSwitch.isCurrentlyEnabled());
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        final ContentResolver resolver = getContext().getContentResolver();
        if (preference == mHBMModeSwitch) {
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
        } else if (preference == mAutoHBMSwitch) {
            Boolean enabled = (Boolean) newValue;
            SharedPreferences.Editor prefChange = PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
            prefChange.putBoolean(KEY_AUTO_HBM_SWITCH, enabled).commit();
            Utils.enableService(getContext());
        } else if (newValue instanceof String) {
            Constants.setPreferenceInt(getContext(), preference.getKey(),
                    Integer.parseInt((String) newValue));
        }
        return true;
    }

    public static boolean isAUTOHBMEnabled(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(DeviceSettings.KEY_AUTO_HBM_SWITCH, false);
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
