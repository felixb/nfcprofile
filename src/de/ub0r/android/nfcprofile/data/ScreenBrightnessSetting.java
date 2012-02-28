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

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import de.ub0r.android.lib.Log;

/**
 * {@link Setting} switching ring mode.
 * 
 * @author flx
 */
public final class ScreenBrightnessSetting extends Setting {
	/** Tag for Logging. */
	private static final String TAG = ScreenBrightnessSetting.class
			.getSimpleName();
	/** Default value. */
	private static final int DEFAULT = -1;

	/** Brightness mode. */
	private static final String MODE = "mode";
	/** Brightness value. */
	private static final String VALUE = "value";

	/** Desired state. */
	private Integer desiredState;

	/**
	 * Default constructor.
	 */
	public ScreenBrightnessSetting() {
		super();
	}

	@Override
	public void load(final SharedPreferences p) {
		this.desiredState = null;
		String s = p.getString(this.getName(), null);
		if (s != null && !s.equals(UNCHANGED)) {
			try {
				this.desiredState = Integer.parseInt(s);
			} catch (NumberFormatException e) {
				Log.e(TAG, "illegal number format: " + s, e);
			}
		}
	}

	@Override
	public void set(final Context context) {
		ContentResolver cr = context.getContentResolver();
		// save current settings
		Editor editor = PreferenceManager.getDefaultSharedPreferences(context)
				.edit();
		try {
			editor.putInt(this.getResetKey(MODE), Settings.System.getInt(cr,
					Settings.System.SCREEN_BRIGHTNESS_MODE));
		} catch (SettingNotFoundException e) {
			Log.e(TAG, "setting not found.. ", e);
		}
		try {
			editor.putInt(this.getResetKey(VALUE), Settings.System.getInt(cr,
					Settings.System.SCREEN_BRIGHTNESS));
		} catch (SettingNotFoundException e) {
			Log.e(TAG, "setting not found.. ", e);
		}
		editor.apply();

		// set to desired state
		if (this.desiredState == null) {
			Log.d(TAG, "ignore desiredState == null");
			return;
		}
		if (this.desiredState < 0) {
			Log.d(TAG, "set " + Settings.System.SCREEN_BRIGHTNESS_MODE + " to "
					+ Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
			Settings.System.putInt(cr, Settings.System.SCREEN_BRIGHTNESS_MODE,
					Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
		} else {
			Log.d(TAG, "set " + Settings.System.SCREEN_BRIGHTNESS + " to "
					+ this.desiredState);
			Settings.System.putInt(cr, Settings.System.SCREEN_BRIGHTNESS_MODE,
					Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
			Settings.System.putInt(cr, Settings.System.SCREEN_BRIGHTNESS,
					this.desiredState); // FIXME
		}
	}

	@Override
	public void reset(final Context context) {
		if (this.desiredState != null) {
			ContentResolver cr = context.getContentResolver();
			SharedPreferences p = PreferenceManager
					.getDefaultSharedPreferences(context);
			Settings.System.putInt(cr, Settings.System.SCREEN_BRIGHTNESS,
					p.getInt(this.getResetKey(VALUE), DEFAULT)); // FIXME
			Settings.System.putInt(cr, Settings.System.SCREEN_BRIGHTNESS_MODE,
					p.getInt(this.getResetKey(MODE),
							Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC));
		}
	}
}
