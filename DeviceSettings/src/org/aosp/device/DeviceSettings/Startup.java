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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.Settings;
import androidx.preference.PreferenceManager;

import org.aosp.device.DeviceSettings.ModeSwitch.*;

public class Startup extends BroadcastReceiver {

    private void restore(String file, boolean enabled) {
        if (file == null) return;
        if (enabled) Utils.writeValue(file, "1");
    }

    @Override
    public void onReceive(final Context context, final Intent bootintent) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

        // DC Dimming
        restore(DCModeSwitch.getFile(),
                sharedPrefs.getBoolean(DCModeSwitch.KEY_DC_SWITCH, false));

        // Panel modes
        restore(SRGBModeSwitch.getFile(),
                sharedPrefs.getBoolean(DeviceSettings.KEY_SRGB_SWITCH, false));
        restore(DCIModeSwitch.getFile(),
                sharedPrefs.getBoolean(DeviceSettings.KEY_DCI_SWITCH, false));
        restore(WideColorModeSwitch.getFile(),
                sharedPrefs.getBoolean(DeviceSettings.KEY_WIDECOLOR_SWITCH, false));
        restore(NaturalModeSwitch.getFile(),
                sharedPrefs.getBoolean(DeviceSettings.KEY_NATURAL_SWITCH, false));
        restore(VividModeSwitch.getFile(),
                sharedPrefs.getBoolean(DeviceSettings.KEY_VIVID_SWITCH, false));
        restore(NightModeSwitch.getFile(),
                sharedPrefs.getBoolean(DeviceSettings.KEY_NIGHT_SWITCH, false));
    }
}
