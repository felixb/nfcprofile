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
import de.ub0r.android.nfcprofile.data.Profile;

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
		Preference p = this.findPreference("name");
		p.setOnPreferenceChangeListener(this);
		this.onPreferenceChange(p,
				pm.getSharedPreferences().getString(p.getKey(), null));
		if (!Intent.ACTION_INSERT.equals(this.getIntent().getAction())) {
			PreferenceScreen ps = (PreferenceScreen) this
					.findPreference("container");
			ps.removePreference(this.findPreference("unknown_profile"));
		}
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
		}
		return true;
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		this.getMenuInflater().inflate(R.menu.profile_activity, menu);
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
		case R.id.activate_profile:
			new Profile(this.getPreferenceManager().getSharedPreferences())
					.set(this);
			return true;
		case R.id.deactivate_profile:
			new Profile(this.getPreferenceManager().getSharedPreferences())
					.reset(this);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}
