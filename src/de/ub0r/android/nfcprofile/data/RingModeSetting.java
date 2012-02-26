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
public final class RingModeSetting extends Setting {
	/** Tag for Logging. */
	private static final String TAG = RingModeSetting.class.getSimpleName();

	/** Desired state. */
	private String desiredState;

	/**
	 * Default constructor.
	 */
	public RingModeSetting() {
		super();
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
		e.putInt(this.getResetKey(), amgr.getRingerMode());
		e.apply();

		// set to desired state
		if (this.desiredState == null) {
			Log.d(TAG, "ignore desiredState == null");
		} else if (this.desiredState.equals(ACTIVATE)) {
			Log.i(TAG, "set on");
			amgr.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
		} else if (this.desiredState.equals(DEACTIVATE)) {
			Log.i(TAG, "set off");
			int vm = amgr.getVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER);
			if (vm == AudioManager.VIBRATE_SETTING_OFF) {
				amgr.setRingerMode(AudioManager.RINGER_MODE_SILENT);
			} else {
				amgr.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
			}
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
			amgr.setRingerMode(p.getInt(this.getResetKey(),
					AudioManager.RINGER_MODE_NORMAL));
		}
	}
}
