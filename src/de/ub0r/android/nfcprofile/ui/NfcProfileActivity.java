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

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import de.ub0r.android.lib.Log;
import de.ub0r.android.nfcprofile.R;

/**
 * Default Activity showing preferences and list of profiles.
 * 
 * @author flx
 */
public final class NfcProfileActivity extends PreferenceActivity implements
		OnPreferenceClickListener {
	/** Tag for Logging. */
	// private static final String TAG = "main";

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.init("NfcProfile");
		this.setTitle(R.string.app_name);
		this.addPreferencesFromResource(R.xml.nfc_profile_activity);
		Preference p = this.findPreference("add_profile");
		p.setOnPreferenceClickListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		PreferenceCategory ps = (PreferenceCategory) this
				.findPreference("profiles");
		int l = ps.getPreferenceCount();
		for (int i = l - 1; i > 0; i--) {
			ps.removePreference(ps.getPreference(i));
		}
		for (String[] k : ProfileActivity.getValidKeys(this)) {
			Preference profile = new Preference(this);
			profile.setKey(k[0]);
			profile.setTitle(k[1]);
			profile.setOnPreferenceClickListener(this);
			ps.addPreference(profile);
		}
	}

	@Override
	public boolean onPreferenceClick(final Preference preference) {
		String key = preference.getKey();
		if (key.equals("add_profile")) {
			this.startActivity(new Intent(this, ProfileActivity.class));
			return true;
		} else if (key.startsWith(ProfileActivity.PREFIX)) {
			Intent intent = new Intent(this, ProfileActivity.class);
			intent.putExtra(ProfileActivity.EXTRA_KEY, key);
			this.startActivity(intent);
			return true;
		}
		return false;
	}
}