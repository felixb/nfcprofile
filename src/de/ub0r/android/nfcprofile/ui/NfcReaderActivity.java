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
package de.ub0r.android.nfcprofile.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import de.ub0r.android.lib.Log;
import de.ub0r.android.nfcprofile.data.Profile;

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

	@Override
	protected void onResume() {
		super.onResume();
		Log.d(TAG, "intent: " + this.getIntent());
		Uri u = this.getIntent().getData();
		if (u != null) {
			String key = u.getHost();
			if (Profile.isValidKey(this, key)) {
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
		SharedPreferences p = PreferenceManager
				.getDefaultSharedPreferences(this);
		Profile prof = new Profile(this.getSharedPreferences(key, MODE_PRIVATE));

		if (p.getString(Profile.CURRENT_PROFILE, null) == null
				|| !p.getBoolean("reset_on_second_touch", true)) {
			// set new profile to key
			Log.i(TAG, "switch profile: " + key);
			prof.set(this);

			p.edit().putString(Profile.CURRENT_PROFILE, key).apply();
			if (p.getBoolean("vibrate", true)) {
				Vibrator vibrator = (Vibrator) this
						.getSystemService(VIBRATOR_SERVICE);
				vibrator.vibrate(VIBRATE_ON, -1);
			}
		} else {
			// reset to previous settings
			Log.i(TAG, "reset to previous settings");
			prof.reset(this);
			p.edit().remove(Profile.CURRENT_PROFILE).apply();

			if (p.getBoolean("vibrate", true)) {
				Vibrator vibrator = (Vibrator) this
						.getSystemService(VIBRATOR_SERVICE);
				vibrator.vibrate(VIBRATE_OFF, -1);
			}
		}
	}
}
