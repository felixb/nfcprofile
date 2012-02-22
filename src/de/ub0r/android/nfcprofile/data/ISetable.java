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

import android.content.Context;
import android.content.SharedPreferences;

/**
 * A setting.
 * 
 * @author flx
 */
public interface ISetable {
	/**
	 * Load {@link Setting} from {@link SharedPreferences}.
	 * 
	 * @param p
	 *            {@link SharedPreferences}
	 */
	void load(final SharedPreferences p);

	/**
	 * Set {@link Setting} and save current state.
	 * 
	 * @param context
	 *            {@link Context}
	 */
	void set(final Context context);

	/**
	 * Reset {@link Setting} to previously saved state.
	 * 
	 * @param context
	 *            {@link Context}
	 */
	void reset(final Context context);
}
