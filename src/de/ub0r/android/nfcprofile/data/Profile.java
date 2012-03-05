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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import de.ub0r.android.lib.Log;
import de.ub0r.android.lib.Utils;

/**
 * A set of {@link Setting}s.
 * 
 * @author flx
 */
public final class Profile implements ISetable {

	/** Tag for Logging. */
	private static final String TAG = "Profile";

	/** Preference's name: current profile. */
	public static final String CURRENT_PROFILE = "current_profile";

	/** Preference's name: valid keys. */
	private static final String PREF_VALIDKEYS = "valid_keys";
	/** Separate keys with this. */
	private static final String SEPARATOR = " ##§## ";

	/** Internal List of {@link Setting}s. */
	private final ArrayList<Setting> settings;

	/**
	 * Create and load {@link Profile}.
	 * 
	 * @param p
	 *            {@link SharedPreferences}
	 */
	public Profile(final SharedPreferences p) {
		Log.d(TAG, "new Profile(" + p.getString("name", null) + ")");
		ArrayList<Setting> s = new ArrayList<Setting>(p.getAll().size());
		s.add(new AirplaneModeSetting());
		s.add(new ScreenTimeoutSetting());
		s.add(new ScreenBrightnessSetting());
		s.add(new VibratorSetting(0));
		s.add(new VibratorSetting(1));
		s.add(new RingModeSetting());
		this.settings = s;
		this.load(p);
	}

	@Override
	public void load(final SharedPreferences p) {
		Log.d(TAG, "load()");
		for (Setting s : this.settings) {
			s.load(p);
		}
	}

	@Override
	public void set(final Context context) {
		Log.d(TAG, "set()");
		for (Setting s : this.settings) {
			s.set(context);
		}
	}

	@Override
	public void reset(final Context context) {
		Log.d(TAG, "reset()");
		for (Setting s : this.settings) {
			s.reset(context);
		}
	}

	/**
	 * Add a key to list of keys.
	 * 
	 * @param context
	 *            {@link Context}
	 * @param key
	 *            key
	 */
	public static void addKey(final Context context, final String key) {
		SharedPreferences p = PreferenceManager
				.getDefaultSharedPreferences(context);
		ArrayList<String> keys = parseKeys(p.getString(PREF_VALIDKEYS, null));
		if (keys.contains(key)) {
			// nothing to do
			return;
		}
		keys.add(key);
		p.edit().putString(PREF_VALIDKEYS, concatKeys(keys)).apply();
	}

	/**
	 * Concatenate keys to safe them in {@link SharedPreferences}.
	 * 
	 * @param keys
	 *            array of keys
	 * @return keys as String
	 */
	private static String concatKeys(final ArrayList<String> keys) {
		StringBuilder sb = new StringBuilder();
		for (String k : keys) {
			if (sb.length() > 0) {
				sb.append(SEPARATOR);
			}
			sb.append(k);

		}
		return sb.toString();
	}

	/**
	 * Generate a new key.
	 * 
	 * @param context
	 *            {@link Context}
	 * @return new key
	 */
	public static String genKey(final Context context) {
		String key = Utils.md5(String.valueOf(System.currentTimeMillis()));
		addKey(context, key);
		return key;
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
		ArrayList<String> keys = parseKeys(p.getString(PREF_VALIDKEYS, null));
		ArrayList<String[]> ret = new ArrayList<String[]>(keys.size());
		HashSet<String> remove = new HashSet<String>(0);
		for (String k : keys) {
			SharedPreferences sp = context.getSharedPreferences(k,
					Context.MODE_PRIVATE);
			if (sp.contains("name")) {
				ret.add(new String[] { k, sp.getString("name", null) });
			} else {
				remove.add(k);
			}
		}
		if (remove.size() > 0) {
			keys.removeAll(remove);
			p.edit().putString(PREF_VALIDKEYS, concatKeys(keys)).apply();
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
		SharedPreferences sp = context.getSharedPreferences(key,
				Context.MODE_PRIVATE);
		return sp.contains("name");
	}

	/**
	 * Parse keys read from {@link SharedPreferences}.
	 * 
	 * @param keys
	 *            keys as String
	 * @return array of keys
	 */
	private static ArrayList<String> parseKeys(final String keys) {
		if (keys == null) {
			return new ArrayList<String>(0);
		}
		String[] s = keys.split(SEPARATOR);
		ArrayList<String> ret = new ArrayList<String>(s.length);
		for (String k : s) {
			ret.add(k);
		}
		return ret;
	}
}
