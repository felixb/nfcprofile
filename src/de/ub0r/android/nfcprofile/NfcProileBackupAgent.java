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
package de.ub0r.android.nfcprofile;

import java.io.IOException;

import android.app.backup.BackupAgent;
import android.app.backup.BackupDataInput;
import android.app.backup.BackupDataOutput;
import android.os.ParcelFileDescriptor;

/**
 * {@link BackupAgent} implementation.
 * 
 * @author flx
 */
public final class NfcProileBackupAgent extends BackupAgent {

	@Override
	public void onBackup(final ParcelFileDescriptor oldState,
			final BackupDataOutput data, final ParcelFileDescriptor newState)
			throws IOException {
		// TODO Auto-generated method stub
	}

	@Override
	public void onRestore(final BackupDataInput data, final int appVersionCode,
			final ParcelFileDescriptor newState) throws IOException {
		// TODO Auto-generated method stub
	}
}
