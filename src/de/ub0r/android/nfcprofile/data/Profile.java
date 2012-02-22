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

import android.content.Context;
import android.content.SharedPreferences;
import de.ub0r.android.lib.Log;

/**
 * A set of {@link Setting}s.
 * 
 * @author flx
 */
public final class Profile implements ISetable {
	/** Tag for Logging. */
	private static final String TAG = "Profile";

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
		this.settings = new ArrayList<Setting>(p.getAll().size());
		this.load(p);
	}

	@Override
	public void load(final SharedPreferences p) {
		Log.d(TAG, "load()");
		this.settings.clear();
		for (String k : p.getAll().keySet()) {
			String v = null;
			try {
				v = p.getString(k, null);
			} catch (Exception e) {
				// nothing to do
				Log.d(TAG, "got no String..");
			}
			if (v == null || !v.equals(Setting.UNCHANGED)) {
				if (k.equals(AirplaneModeSetting.class.getSimpleName())) {
					this.settings.add(new AirplaneModeSetting());
				} else if (k.equals(RingModeSetting.class.getSimpleName())) {
					this.settings.add(new RingModeSetting());
				} // else if ..
			}
		}

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

}
