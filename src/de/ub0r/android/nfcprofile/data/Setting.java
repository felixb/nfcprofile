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


/**
 * Holding a single setting switchable by NfcReaderActivity.
 * 
 * @author flx
 */
public abstract class Setting implements ISetable {
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
	 * 
	 * @param n
	 *            name
	 */
	protected Setting(final String n) {
		this.name = n;
	}

	/**
	 * @return name
	 */
	public final String getName() {
		return this.name;
	}
}
