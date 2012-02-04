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
package de.ub0r.android.nfcprofile.ui;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.text.TextUtils;
import de.ub0r.android.lib.Log;

/**
 * Reading NFC tags.
 * 
 * @author flx
 */
public final class NfcReaderActivity extends Activity {
	/** Tag for Logging. */
	private static final String TAG = "reader";

	/** Vibrate pattern: switch profile on. */
	private static final long[] VIBRATE_ON = new long[] { 0L, 100L };
	/** Vibrate pattern: switch profile off. */
	private static final long[] VIBRATE_OFF = new long[] { 0L, 100L, 100L, 100L };

	/** Preference's name: current profile. */
	private static final String CURRENT_PROFILE = "current_profile";
	/** Reset state: airplane mode. */
	private static final String RESET_AIRPLANE = "RESET_airplane";
	/** Reset state: airplane radios. */
	private static final String RESET_AIRPLANE_RADIOS = "RESET_airplane_radios";
	/** Reset state: ring mode. */
	private static final String RESET_RINGMODE = "RESET_ringmode";
	/** Reset state: vibrate mode. */
	private static final String RESET_VIBRATEMODE = "RESET_vibratemode";
	/** Reset state: wifi on. */
	private static final String RESET_WIFION = "RESET_wifion";
	/** Reset state: bluetooth on. */
	private static final String RESET_BLUETOOTHON = "RESET_bluetoothon";

	/** Ringtone mode: unchanged. */
	private static final String RINGTONE_UNCHANGED = "unchanged";
	/** Ringtone mode: silent. */
	private static final String RINGTONE_SILENT = "silent";
	/** Ringtone mode: vibrate. */
	private static final String RINGTONE_VIBRATE = "vibrate";
	/** Ringtone mode: ring. */
	private static final String RINGTONE_RING = "ring";
	/** Ringtone mode: ringvibrate. */
	private static final String RINGTONE_RING_VIBRATE = "ring_vibrate";

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.init("NfcProfile");
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d(TAG, "intent: " + this.getIntent());
		Uri u = this.getIntent().getData();
		if (u != null) {
			String key = u.getHost();
			if (ProfileActivity.isValidKey(this, key)) {
				this.invokeProfile(u.getHost());
			} else {
				Intent intent = new Intent(this, ProfileActivity.class);
				intent.putExtra(ProfileActivity.EXTRA_KEY, key);
				intent.setAction(Intent.ACTION_INSERT);
				this.startActivity(intent);
			}

		}
		this.finish();
	}

	/**
	 * Invoke a profile or reset profile.
	 * 
	 * @param key
	 *            profile key
	 */
	private void invokeProfile(final String key) {
		Log.i(TAG, "invokeProfile(" + key + ")");
		ContentResolver cr = this.getContentResolver();
		SharedPreferences p = PreferenceManager
				.getDefaultSharedPreferences(this);
		AudioManager amgr = (AudioManager) this.getSystemService(AUDIO_SERVICE);
		if (p.getString(CURRENT_PROFILE, null) == null
				|| !p.getBoolean("reset_on_second_touch", true)) {
			Log.i(TAG, "switch profile: " + key);
			SharedPreferences sp = this.getSharedPreferences(key, MODE_PRIVATE);
			Editor e = p.edit();

			// save current settings
			e.putInt(RESET_AIRPLANE, Settings.System.getInt(cr,
					Settings.System.AIRPLANE_MODE_ON, 0));
			e.putString(RESET_AIRPLANE_RADIOS, Settings.System.getString(cr,
					Settings.System.AIRPLANE_MODE_RADIOS));
			try {
				e.putInt(RESET_BLUETOOTHON, Settings.Secure.getInt(cr,
						Settings.Secure.BLUETOOTH_ON));
			} catch (SettingNotFoundException e1) {
				Log.w(TAG, "setting not found", e1);
			}
			try {
				e.putInt(RESET_WIFION,
						Settings.Secure.getInt(cr, Settings.Secure.WIFI_ON));
			} catch (SettingNotFoundException e1) {
				Log.w(TAG, "setting not found", e1);
			}
			e.putInt(RESET_RINGMODE, amgr.getRingerMode());
			e.putInt(RESET_VIBRATEMODE,
					amgr.getVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER));

			// set new profile to key
			if (sp.getBoolean("airplane_on", false)) {
				Log.i(TAG, "set airplane mode");
				this.setAirplaneMode(p, true);
			}
			String s = sp.getString("ringtone_settings", RINGTONE_UNCHANGED);
			if (s.equals(RINGTONE_SILENT)) {
				Log.i(TAG, "set silent");
				amgr.setRingerMode(AudioManager.RINGER_MODE_SILENT);
			} else if (s.equals(RINGTONE_VIBRATE)) {
				Log.i(TAG, "set vibrate");
				amgr.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
			} else if (s.equals(RINGTONE_RING)) {
				Log.i(TAG, "set ring");
				amgr.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
				amgr.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER,
						AudioManager.VIBRATE_SETTING_ONLY_SILENT);
			} else if (s.equals(RINGTONE_RING_VIBRATE)) {
				Log.i(TAG, "set ring+vibrate");
				amgr.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
				amgr.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER,
						AudioManager.VIBRATE_SETTING_ON);
			}
			/*
			 * if (p.getBoolean("bluetooth_off", false)) { Log.i(TAG, "switch off bluetooth");
			 * Settings.Secure.putInt(cr, Settings.Secure.BLUETOOTH_ON, 0); } if
			 * (p.getBoolean("wifi_off", false)) { Log.i(TAG, "switch off wifi");
			 * Settings.Secure.putInt(cr, Settings.Secure.WIFI_ON, 0); }
			 */

			e.putString(CURRENT_PROFILE, key).apply();
			if (p.getBoolean("vibrate", true)) {
				Vibrator vibrator = (Vibrator) this
						.getSystemService(VIBRATOR_SERVICE);
				vibrator.vibrate(VIBRATE_ON, -1);
			}
		} else {
			// reset to previous settings
			Log.i(TAG, "reset to previous settings");
			this.setAirplaneMode(p, p.getInt(RESET_AIRPLANE, 0) == 1);
			amgr.setRingerMode(p.getInt(RESET_RINGMODE,
					AudioManager.RINGER_MODE_NORMAL));
			amgr.setVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER, p
					.getInt(RESET_VIBRATEMODE,
							AudioManager.VIBRATE_SETTING_ONLY_SILENT));
			/*
			 * Settings.Secure.putInt(cr, Settings.Secure.BLUETOOTH_ON, p.getInt(RESET_BLUETOOTHON,
			 * 1)); Settings.Secure.putInt(cr, Settings.Secure.WIFI_ON, p.getInt(RESET_WIFION, 1));
			 */
			p.edit().remove(CURRENT_PROFILE).apply();
			if (p.getBoolean("vibrate", true)) {
				Vibrator vibrator = (Vibrator) this
						.getSystemService(VIBRATOR_SERVICE);
				vibrator.vibrate(VIBRATE_OFF, -1);
			}
		}
	}

	/**
	 * Set airplane mode.
	 * 
	 * @param p
	 *            {@link SharedPreferences}
	 * @param turnOff
	 *            true to turn on airplane mode
	 */
	private void setAirplaneMode(final SharedPreferences p,
			final boolean turnOff) {
		Log.d(TAG, "setAirplaneMode(" + turnOff + ")");
		ContentResolver cr = this.getContentResolver();
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
				Settings.System.putString(cr,
						Settings.System.AIRPLANE_MODE_RADIOS, s);
			}
			Settings.System.putInt(cr, Settings.System.AIRPLANE_MODE_ON,
					1 - current);
			Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
			intent.putExtra("state", turnOff);
			this.sendBroadcast(intent);
		}
	}
}
