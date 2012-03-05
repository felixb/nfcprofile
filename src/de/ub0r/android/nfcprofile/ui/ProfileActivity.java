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

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.view.Menu;
import android.view.MenuItem;
import de.ub0r.android.lib.Log;
import de.ub0r.android.nfcprofile.R;
import de.ub0r.android.nfcprofile.data.AirplaneModeSetting;
import de.ub0r.android.nfcprofile.data.Profile;
import de.ub0r.android.nfcprofile.data.RingModeSetting;
import de.ub0r.android.nfcprofile.data.ScreenBrightnessSetting;
import de.ub0r.android.nfcprofile.data.ScreenTimeoutSetting;
import de.ub0r.android.nfcprofile.data.VibratorSetting;

/**
 * Set preferences for a single profile.
 * 
 * @author flx
 */
public final class ProfileActivity extends PreferenceActivity implements
		OnPreferenceChangeListener {
	/** Tag for Logging. */
	private static final String TAG = "profile";
	/** Extra: key. */
	public static final String EXTRA_KEY = "key";
	/** Profile's key. */
	private String key;

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState == null) {
			this.key = this.getIntent().getStringExtra(EXTRA_KEY);
			if (this.key == null) {
				this.key = Profile.genKey(this);
			} else {
				Profile.addKey(this, this.key);
			}
		} else {
			this.key = savedInstanceState.getString(EXTRA_KEY);
		}
		Log.d(TAG, "key: " + this.key);

		PreferenceManager pm = this.getPreferenceManager();
		pm.setSharedPreferencesName(this.key);
		this.addPreferencesFromResource(R.xml.profile_activity);
		this.setAndInvokeOnPreferenceChangeListener(pm,
				this.findPreference("name"), this);
		this.setAndInvokeOnPreferenceChangeListener(pm,
				AirplaneModeSetting.class.getSimpleName(), this);
		this.setAndInvokeOnPreferenceChangeListener(pm,
				ScreenTimeoutSetting.class.getSimpleName(), this);
		this.setAndInvokeOnPreferenceChangeListener(pm,
				ScreenBrightnessSetting.class.getSimpleName(), this);
		this.setAndInvokeOnPreferenceChangeListener(pm,
				RingModeSetting.class.getSimpleName(), this);
		this.setAndInvokeOnPreferenceChangeListener(pm,
				VibratorSetting.class.getSimpleName() + "_0", this);
		this.setAndInvokeOnPreferenceChangeListener(pm,
				VibratorSetting.class.getSimpleName() + "_1", this);
		if (!Intent.ACTION_INSERT.equals(this.getIntent().getAction())) {
			PreferenceScreen ps = (PreferenceScreen) this
					.findPreference("container");
			ps.removePreference(this.findPreference("unknown_profile"));
		}
	}

	/**
	 * Set and invoke {@link OnPreferenceChangeListener}.
	 * 
	 * @param pm
	 *            {@link PreferenceManager}
	 * @param p
	 *            key of {@link Preference}
	 * @param opcl
	 *            {@link OnPreferenceChangeListener}
	 */
	private void setAndInvokeOnPreferenceChangeListener(
			final PreferenceManager pm, final String p,
			final OnPreferenceChangeListener opcl) {
		this.setAndInvokeOnPreferenceChangeListener(pm, this.findPreference(p),
				opcl);
	}

	/**
	 * Set and invoke {@link OnPreferenceChangeListener}.
	 * 
	 * @param pm
	 *            {@link PreferenceManager}
	 * @param p
	 *            {@link Preference}
	 * @param opcl
	 *            {@link OnPreferenceChangeListener}
	 */
	private void setAndInvokeOnPreferenceChangeListener(
			final PreferenceManager pm, final Preference p,
			final OnPreferenceChangeListener opcl) {
		if (p == null || pm == null || opcl == null) {
			return;
		}
		p.setOnPreferenceChangeListener(this);
		opcl.onPreferenceChange(p,
				pm.getSharedPreferences().getString(p.getKey(), null));
	}

	@Override
	protected void onSaveInstanceState(final Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(EXTRA_KEY, this.key);
	}

	@Override
	public boolean onPreferenceChange(final Preference preference,
			final Object newValue) {
		String k = preference.getKey();
		if (k.equals("name")) {
			preference.setSummary((CharSequence) newValue);
		} else if (k.equals(AirplaneModeSetting.class.getSimpleName())
				|| k.equals(RingModeSetting.class.getSimpleName())) {
			preference.setSummary(this.translateStringList(
					R.array.onoff_values, R.array.onoff_settings,
					(String) newValue));
		} else if (k.startsWith(VibratorSetting.class.getSimpleName())) {
			preference.setSummary(this.translateStringList(
					R.array.vibrator_values, R.array.vibrator_settings,
					(String) newValue));
		} else if (k.equals(ScreenTimeoutSetting.class.getSimpleName())) {
			preference.setSummary(this.translateStringList(
					R.array.screentimeout_values,
					R.array.screentimeout_settings, (String) newValue));
		} else if (k.equals(ScreenBrightnessSetting.class.getSimpleName())) {
			preference.setSummary(this.translateStringList(
					R.array.screenbrightness_values,
					R.array.screenbrightness_settings, (String) newValue));
		}
		return true;
	}

	/**
	 * Translate selected key to value.
	 * 
	 * @param resIdKeys
	 *            keys
	 * @param resIdValues
	 *            values
	 * @param k
	 *            selected key
	 * @return selected value
	 */
	private String translateStringList(final int resIdKeys,
			final int resIdValues, final String k) {
		Resources r = this.getResources();
		String[] stringk = r.getStringArray(resIdKeys);
		String[] stringv = r.getStringArray(resIdValues);
		for (int i = 0; i < stringk.length; i++) {
			if (k.equals(stringk[i])) {
				return stringv[i].replaceAll("%", "%%");
			}
		}
		return null;
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		this.getMenuInflater().inflate(R.menu.profile_activity, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(final Menu menu) {
		String current = PreferenceManager.getDefaultSharedPreferences(this)
				.getString(Profile.CURRENT_PROFILE, null);
		if (current != null && current.equals(this.key)) {
			menu.findItem(R.id.activate_profile).setVisible(false);
			menu.findItem(R.id.deactivate_profile).setVisible(true);
		} else {
			menu.findItem(R.id.activate_profile).setVisible(true);
			menu.findItem(R.id.deactivate_profile).setVisible(false);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		Intent intent;
		switch (item.getItemId()) {
		case android.R.id.home:
			intent = new Intent(this, NfcProfileActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			this.startActivity(intent);
			return true;
		case R.id.delete_profile:
			this.getPreferenceManager().getSharedPreferences().edit().clear()
					.apply();
			this.finish();
			return true;
		case R.id.write_tag:
			intent = new Intent(this, NfcWriterActivity.class);
			intent.putExtra(EXTRA_KEY, this.key);
			this.startActivity(intent);
			return true;
		case R.id.share_profile:
			intent = new Intent(Intent.ACTION_SEND);
			intent.setType("text/plain");
			intent.putExtra(Intent.EXTRA_TEXT, NfcWriterActivity.URI_PREFIX
					+ this.key);
			this.startActivity(Intent.createChooser(intent,
					this.getString(R.string.share)));
			return true;
		case R.id.activate_profile:
			new Profile(this.getPreferenceManager().getSharedPreferences())
					.set(this);
			PreferenceManager.getDefaultSharedPreferences(this).edit()
					.putString(Profile.CURRENT_PROFILE, this.key).apply();
			return true;
		case R.id.deactivate_profile:
			new Profile(this.getPreferenceManager().getSharedPreferences())
					.reset(this);
			PreferenceManager.getDefaultSharedPreferences(this).edit()
					.remove(Profile.CURRENT_PROFILE).apply();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}
