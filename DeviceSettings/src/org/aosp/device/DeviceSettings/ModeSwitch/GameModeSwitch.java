/*
 * Copyright (C) 2023 PixelExperience Project
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
package org.aosp.device.DeviceSettings.ModeSwitch;

import android.content.SharedPreferences;
import android.content.Context;
import android.content.Intent;
import android.os.UserHandle;

import androidx.preference.PreferenceManager;

import org.aosp.device.DeviceSettings.Utils;

public class GameModeSwitch {

    private static final String FILE = "/proc/touchpanel/game_switch_enable";
    
    public static final String ACTION_GAMEMODE_CHANGED = "org.aosp.device.DeviceSettings.ModeSwitch.GAMEMODE_CHANGED";
    public static final String EXTRA_GAMEMODE_STATE = "enabled";
    public static final String KEY_GAME_SWITCH = "game_mode";

    public static String getFile() {
        if (Utils.fileWritable(FILE)) {
            return FILE;
        }
        return null;
    }

    public static boolean isSupported() {
        return Utils.fileWritable(getFile());
    }

    public static boolean isCurrentlyEnabled() {
        return Utils.getFileValueAsBoolean(getFile(), false);
    }

    public static void setEnabled(boolean enabled, Context context) {
        Utils.writeValue(getFile(), enabled ? "1" : "0");
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putBoolean(KEY_GAME_SWITCH, enabled).commit();
        Intent intent = new Intent(ACTION_GAMEMODE_CHANGED);
        intent.putExtra(EXTRA_GAMEMODE_STATE, enabled);
        intent.setFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY);
        context.sendBroadcastAsUser(intent, UserHandle.CURRENT);
    }
}
