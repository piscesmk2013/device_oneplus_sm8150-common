/*
* Copyright (C) 2018 The OmniROM Project
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

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Icon;
import android.provider.Settings;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import androidx.preference.PreferenceManager;


@TargetApi(24)
public class RefreshRateTileService extends TileService {
    private boolean enabled = false;
    private boolean autoRefreshEnabled;

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onTileAdded() {
        super.onTileAdded();
    }

    @Override
    public void onTileRemoved() {
        super.onTileRemoved();
    }

    @Override
    public void onStartListening() {
        super.onStartListening();
        autoRefreshEnabled = Settings.System.getInt(this.getContentResolver(),
                AutoRefreshRateSwitch.SETTINGS_KEY, 1) == 1;
        if (autoRefreshEnabled) {
            getQsTile().setState(Tile.STATE_UNAVAILABLE);
        } else {
            enabled = RefreshRateSwitch.isCurrentlyEnabled(this);
            RefreshRateSwitch.setPeakRefresh(this, enabled);

            getQsTile().setIcon(Icon.createWithResource(this,
                    enabled ? R.drawable.ic_refresh_tile_90 : R.drawable.ic_refresh_tile_60));
            getQsTile().setState(enabled ? Tile.STATE_ACTIVE : Tile.STATE_INACTIVE);
        }
        getQsTile().updateTile();
    }

    @Override
    public void onStopListening() {
        super.onStopListening();
    }

    @Override
    public void onClick() {
        super.onClick();
        if (!autoRefreshEnabled) {
            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
            enabled = RefreshRateSwitch.isCurrentlyEnabled(this);
            RefreshRateSwitch.setPeakRefresh(this, enabled);
            sharedPrefs.edit().putBoolean(DeviceSettings.KEY_REFRESH_RATE, enabled ? false : true).commit();
            Settings.System.putFloat(this.getContentResolver(),
                    Settings.System.PEAK_REFRESH_RATE, enabled ? 60f : 90f);
            Settings.System.putFloat(this.getContentResolver(),
                    Settings.System.MIN_REFRESH_RATE, enabled ? 60f : 90f);
            getQsTile().setIcon(Icon.createWithResource(this,
                    enabled ? R.drawable.ic_refresh_tile_60 : R.drawable.ic_refresh_tile_90));
            getQsTile().setState(enabled ? Tile.STATE_INACTIVE : Tile.STATE_ACTIVE);
            getQsTile().updateTile();
        }
    }
}
