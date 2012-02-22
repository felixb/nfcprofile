/*
 * Copyright (C) 2012 Felix Bechstein
 * 
 * This file is part of NfcProfile.
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; If
 * not, see <http://www.gnu.org/licenses/>.
 */
package de.ub0r.android.nfcprofile.data;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.TextUtils;
import de.ub0r.android.lib.Log;

/**
 * {@link Setting} switching airplane mode.
 * 
 * @author flx
 */
public final class AirplaneModeSetting extends Setting {
	/** Tag for Logging. */
	private static final String TAG = AirplaneModeSetting.class.getSimpleName();

	/** Reset state: airplane mode. */
	private static final String RESET_AIRPLANE = "RESET_airplane";
	/** Reset state: airplane radios. */
	private static final String RESET_AIRPLANE_RADIOS = "RESET_airplane_radios";

	/** Desired state. */
	private Boolean desiredState;

	/**
	 * Default constructor.
	 */
	public AirplaneModeSetting() {
		super(AirplaneModeSetting.class.getSimpleName());
	}

	@Override
	public void load(final SharedPreferences p) {
		String s = p.getString(this.getName(), null);
		if (s != null && s.equals(Setting.ACTIVATE)) {
			this.desiredState = true;
		} else if (s != null && s.equals(Setting.DEACTIVATE)) {
			this.desiredState = false;
		} else {
			this.desiredState = null;
		}
	}

	@Override
	public void set(final Context context) {
		// save current settings
		ContentResolver cr = context.getContentResolver();
		Editor e = PreferenceManager.getDefaultSharedPreferences(context)
				.edit();
		e.putInt(RESET_AIRPLANE,
				Settings.System.getInt(cr, Settings.System.AIRPLANE_MODE_ON, 0));
		e.putString(RESET_AIRPLANE_RADIOS, Settings.System.getString(cr,
				Settings.System.AIRPLANE_MODE_RADIOS));
		e.apply();

		// set to desired state
		if (this.desiredState != null) {
			this.setAirplaneMode(context, this.desiredState);
		}
	}

	@Override
	public void reset(final Context context) {
		if (this.desiredState != null) {
			SharedPreferences p = PreferenceManager
					.getDefaultSharedPreferences(context);
			this.setAirplaneMode(context, p.getInt(RESET_AIRPLANE, 0) == 1);
		}
	}

	/**
	 * Set airplane mode.
	 * 
	 * @param context
	 *            {@link Context}
	 * @param turnOff
	 *            true to turn on airplane mode
	 */
	private void setAirplaneMode(final Context context, final boolean turnOff) {
		Log.d(TAG, "setAirplaneMode(ctx, " + turnOff + ")");
		ContentResolver cr = context.getContentResolver();
		SharedPreferences p = PreferenceManager
				.getDefaultSharedPreferences(context);
		int current = Settings.System.getInt(cr,
				Settings.System.AIRPLANE_MODE_ON, 0);

		if (current == 0 && turnOff || current == 1 && !turnOff) {
			if (turnOff) {
				String s = Settings.System.getString(cr,
						Settings.System.AIRPLANE_MODE_RADIOS);
				if (!TextUtils.isEmpty(s)
						&& s.contains(Settings.System.RADIO_NFC)) {
					s = s.replace(Settings.System.RADIO_NFC, "").replace(",,",
							",");
				}
				Log.d(TAG, "set " + Settings.System.AIRPLANE_MODE_RADIOS + "="
						+ s);
				Settings.System.putString(cr,
						Settings.System.AIRPLANE_MODE_RADIOS, s);
			} else {
				String s = p.getString(RESET_AIRPLANE_RADIOS, null);
				Log.d(TAG, "set " + Settings.System.AIRPLANE_MODE_RADIOS + "="
						+ s);
				if (s != null) {
					Settings.System.putString(cr,
							Settings.System.AIRPLANE_MODE_RADIOS, s);
				}
			}
			Settings.System.putInt(cr, Settings.System.AIRPLANE_MODE_ON,
					1 - current);
			Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
			intent.putExtra("state", turnOff);
			context.sendBroadcast(intent);
		}
	}
}
