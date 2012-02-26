/*
 * Copyright (C) 2012 Felix Bechstein
 * 
 * This file is part of NfcProfile.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; If not, see <http://www.gnu.org/licenses/>.
 */
package de.ub0r.android.nfcprofile.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.AudioManager;
import android.preference.PreferenceManager;
import de.ub0r.android.lib.Log;

/**
 * {@link Setting} switching ring mode.
 * 
 * @author flx
 */
public final class VibratorSetting extends Setting {
	/** Tag for Logging. */
	private static final String TAG = VibratorSetting.class.getSimpleName();

	/** Vibrator mode: vibrate only when silent. */
	private static final String SILENT = "silent";

	/** Type of ringer. */
	private final int vibratorType;
	/** Desired state. */
	private String desiredState;

	/**
	 * Default constructor.
	 * 
	 * @param vt
	 *            AudioManager.VIBRATE_TYPE_RINGER or
	 *            AudioManager.VIBRATE_TYPE_NOTIFICATON
	 */
	public VibratorSetting(final int vt) {
		super(vt);
		this.vibratorType = vt;
	}

	@Override
	public void load(final SharedPreferences p) {
		this.desiredState = p.getString(this.getName(), null);
		if (this.desiredState == UNCHANGED) {
			this.desiredState = null;
		}
	}

	@Override
	public void set(final Context context) {
		AudioManager amgr = (AudioManager) context
				.getSystemService(Context.AUDIO_SERVICE);
		// save current settings
		Editor e = PreferenceManager.getDefaultSharedPreferences(context)
				.edit();
		e.putInt(this.getResetKey(), amgr.getVibrateSetting(this.vibratorType));
		e.apply();

		// set to desired state
		if (this.desiredState == null) {
			Log.d(TAG, "ignore desiredState == null");
		} else if (this.desiredState.equals(ACTIVATE)) {
			Log.i(TAG, "set on");
			amgr.setVibrateSetting(this.vibratorType,
					AudioManager.VIBRATE_SETTING_ON);
		} else if (this.desiredState.equals(DEACTIVATE)) {
			Log.i(TAG, "set off");
			amgr.setVibrateSetting(this.vibratorType,
					AudioManager.VIBRATE_SETTING_OFF);
		} else if (this.desiredState.equals(SILENT)) {
			Log.i(TAG, "set on only when silent");
			amgr.setVibrateSetting(this.vibratorType,
					AudioManager.VIBRATE_SETTING_ONLY_SILENT);
		} else {
			Log.e(TAG, "unknown desired state");
		}
	}

	@Override
	public void reset(final Context context) {
		if (this.desiredState != null) {
			AudioManager amgr = (AudioManager) context
					.getSystemService(Context.AUDIO_SERVICE);
			SharedPreferences p = PreferenceManager
					.getDefaultSharedPreferences(context);
			amgr.setVibrateSetting(this.vibratorType, p.getInt(
					this.getResetKey(),
					AudioManager.VIBRATE_SETTING_ONLY_SILENT));
		}
	}
}
