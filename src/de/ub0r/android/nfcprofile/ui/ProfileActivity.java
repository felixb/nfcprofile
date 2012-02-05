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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.view.Menu;
import android.view.MenuItem;
import de.ub0r.android.lib.Log;
import de.ub0r.android.lib.Utils;
import de.ub0r.android.nfcprofile.R;

/**
 * Set preferences for a single profile.
 * 
 * @author flx
 */
public final class ProfileActivity extends PreferenceActivity implements
		OnPreferenceChangeListener {
	/** Tag for Logging. */
	private static final String TAG = "profile";
	/** Preference's name: valid keys. */
	private static final String PREF_VALIDKEYS = "valid_keys";
	/** Extra: key. */
	public static final String EXTRA_KEY = "key";
	/** Profile's key. */
	private String key;

	/**
	 * Generate a new key.
	 * 
	 * @param context
	 *            {@link Context}
	 * @return new key
	 */
	private static String genKey(final Context context) {
		String key = Utils.md5(String.valueOf(System.currentTimeMillis()));
		addKey(context, key);
		return key;
	}

	/**
	 * Add a key to list of keys.
	 * 
	 * @param context
	 *            {@link Context}
	 * @param key
	 *            key
	 */
	private static void addKey(final Context context, final String key) {
		SharedPreferences p = PreferenceManager
				.getDefaultSharedPreferences(context);
		Set<String> set = p.getStringSet(PREF_VALIDKEYS, null);
		if (set == null) {
			set = new HashSet<String>();
		} else if (set.contains(key)) {
			// nothing to do
			return;
		}
		set.add(key);
		p.edit().putStringSet(PREF_VALIDKEYS, set).apply();
	}

	/**
	 * Get a {@link List} of key/name pairs of valid profiles.
	 * 
	 * @param context
	 *            {@link Context}
	 * @return {@link List} of key/name pairs
	 */
	public static List<String[]> getValidKeys(final Context context) {
		SharedPreferences p = PreferenceManager
				.getDefaultSharedPreferences(context);
		Set<String> set = p.getStringSet(PREF_VALIDKEYS, null);
		if (set == null) {
			return new ArrayList<String[]>(0);
		}
		ArrayList<String[]> ret = new ArrayList<String[]>(set.size());
		HashSet<String> remove = new HashSet<String>(0);
		for (String k : set) {
			SharedPreferences sp = context
					.getSharedPreferences(k, MODE_PRIVATE);
			if (sp.contains("name")) {
				ret.add(new String[] { k, sp.getString("name", null) });
			} else {
				remove.add(k);
			}
		}
		if (remove.size() > 0) {
			set.removeAll(remove);
			p.edit().putStringSet(PREF_VALIDKEYS, set).apply();
		}
		return ret;
	}

	/**
	 * Check if a key is valid.
	 * 
	 * @param context
	 *            {@link Context}
	 * @param key
	 *            key
	 * @return true, if profile exists
	 */
	public static boolean isValidKey(final Context context, final String key) {
		SharedPreferences sp = context.getSharedPreferences(key, MODE_PRIVATE);
		return sp.contains("name");
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState == null) {
			this.key = this.getIntent().getStringExtra(EXTRA_KEY);
			if (this.key == null) {
				this.key = genKey(this);
			} else {
				addKey(this, this.key);
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
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}
