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

import de.ub0r.android.lib.Log;

/**
 * Holding a single setting switchable by NfcReaderActivity.
 * 
 * @author flx
 */
public abstract class Setting implements ISetable {
	/** Tag for Logging. */
	private static final String TAG = Setting.class.getSimpleName();

	/** Unchanged. */
	public static final String UNCHANGED = "unchanged";
	/** Activate {@link Setting}. */
	public static final String ACTIVATE = "activate";
	/** Deactivate {@link Setting}. */
	public static final String DEACTIVATE = "deactivate";

	/** {@link Setting}s name in SharedPreferences. */
	private final String name;

	/**
	 * Set {@link Setting}s name.
	 */
	protected Setting() {
		this(null);
	}

	/**
	 * Set {@link Setting}s name.
	 * 
	 * @param postfix
	 *            add this to the name
	 */
	protected Setting(final int postfix) {
		this(String.valueOf(postfix));
	}

	/**
	 * Set {@link Setting}s name.
	 * 
	 * @param postfix
	 *            add this to the name
	 */
	protected Setting(final String postfix) {
		if (postfix == null) {
			this.name = this.getClass().getSimpleName();
		} else {
			this.name = this.getClass().getSimpleName() + "_" + postfix;
		}
		Log.d(TAG, "new " + this.getName() + "()");
	}

	/**
	 * @return name
	 */
	protected final String getName() {
		return this.name;
	}

	/**
	 * @return reset key
	 */
	protected final String getResetKey() {
		return "RESET_" + this.name;
	}

	/**
	 * @param postfix
	 *            append this
	 * @return reset key
	 */
	protected final String getResetKey(final String postfix) {
		return "RESET_" + this.name + "_" + postfix;
	}
}
