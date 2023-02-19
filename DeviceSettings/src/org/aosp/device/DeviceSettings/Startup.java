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

import static android.content.Intent.ACTION_BOOT_COMPLETED;
import static android.content.Intent.ACTION_LOCKED_BOOT_COMPLETED;

import static org.aosp.device.DeviceSettings.DeviceSettings.KEY_SRGB_SWITCH;
import static org.aosp.device.DeviceSettings.DeviceSettings.KEY_DCI_SWITCH;
import static org.aosp.device.DeviceSettings.DeviceSettings.KEY_WIDECOLOR_SWITCH;
import static org.aosp.device.DeviceSettings.DeviceSettings.KEY_NATURAL_SWITCH;
import static org.aosp.device.DeviceSettings.DeviceSettings.KEY_VIVID_SWITCH;
import static org.aosp.device.DeviceSettings.ModeSwitch.DCModeSwitch.KEY_DC_SWITCH;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.Settings;
import androidx.preference.PreferenceManager;

import org.aosp.device.DeviceSettings.ModeSwitch.*;

import java.util.Map;

public class Startup extends BroadcastReceiver {

    private static final String KEY_MIGRATION_DONE = "migration_done";

    private static final Map<String, String> sKeyFileMap = Map.of(
        // DC Dimming
        KEY_DC_SWITCH, DCModeSwitch.getFile(),
        // Panel modes
        KEY_SRGB_SWITCH, SRGBModeSwitch.getFile(),
        KEY_DCI_SWITCH, DCIModeSwitch.getFile(),
        KEY_WIDECOLOR_SWITCH, WideColorModeSwitch.getFile(),
        KEY_NATURAL_SWITCH, NaturalModeSwitch.getFile(),
        KEY_VIVID_SWITCH, VividModeSwitch.getFile()
    );

    private void restore(String file, boolean enabled) {
        if (file == null) return;
        if (enabled) Utils.writeValue(file, "1");
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {
        final SharedPreferences dePrefs = Constants.getDESharedPrefs(context);

        final boolean migrationDone = dePrefs.getBoolean(KEY_MIGRATION_DONE, false);
        if (!migrationDone && intent.getAction().equals(ACTION_BOOT_COMPLETED)) {
            // migration of old user encrypted preferences
            final SharedPreferences oldPrefs = PreferenceManager.getDefaultSharedPreferences(context);
            final SharedPreferences.Editor oldPrefsEditor = oldPrefs.edit();
            final SharedPreferences.Editor dePrefsEditor = dePrefs.edit();

            for (String prefKey : sKeyFileMap.keySet()) {
                if (!oldPrefs.contains(prefKey)) continue;
                dePrefsEditor.putBoolean(prefKey, oldPrefs.getBoolean(prefKey, false));
                oldPrefsEditor.remove(prefKey);
            }

            dePrefsEditor.putBoolean(KEY_MIGRATION_DONE, true);
            // must use commit (and not apply) because of what follows!
            dePrefsEditor.commit();
            oldPrefsEditor.commit();
        }

        // restoring state from DE shared preferences
        for (Map.Entry<String, String> set : sKeyFileMap.entrySet()) {
            final String prefKey = set.getKey();
            final String file = set.getValue();
            restore(file, dePrefs.getBoolean(prefKey, false));
        }
    }
}
