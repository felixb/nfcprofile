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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Map;

import android.app.backup.BackupAgent;
import android.app.backup.BackupDataInput;
import android.app.backup.BackupDataOutput;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.ParcelFileDescriptor;
import android.preference.PreferenceManager;
import de.ub0r.android.lib.Log;
import de.ub0r.android.nfcprofile.data.Profile;

/**
 * {@link BackupAgent} implementation.
 * 
 * @author flx
 */
public final class NfcProfileBackupAgent extends BackupAgent {
	/** Tag for Logging. */
	private static final String TAG = "backup";

	/** Preference name: last change of something. */
	public static final String PREF_LAST_CHANGE = "_last_change";
	/** Header for main prefs. */
	private static final String HEADER_MAIN = "main";
	/** Header for profile. */
	private static final String HEADER_PROFILE = "profile_";

	/**
	 * Backup data..
	 * 
	 * @param data
	 *            {@link BackupDataOutput} from onBackup()
	 * @throws IOException
	 *             IOException
	 */
	private void doBackup(final BackupDataOutput data) throws IOException {
		Log.d(TAG, "doBackup()");
		// backup default prefs
		ByteArrayOutputStream bufStream = new ByteArrayOutputStream();
		ObjectOutputStream out = new ObjectOutputStream(bufStream);
		SharedPreferences p = PreferenceManager
				.getDefaultSharedPreferences(this);
		writePrefsToStream(p, out);
		byte[] buf = bufStream.toByteArray();
		Log.d(TAG, "backup main: " + buf.length);
		data.writeEntityHeader(HEADER_MAIN, buf.length);
		data.writeEntityData(buf, buf.length);

		// backup profiles
		List<String[]> keys = Profile.getValidKeys(this);
		for (String[] k : keys) {
			String key = k[0];
			p = this.getSharedPreferences(key, MODE_PRIVATE);
			bufStream = new ByteArrayOutputStream();
			out = new ObjectOutputStream(bufStream);
			writePrefsToStream(p, out);
			buf = bufStream.toByteArray();
			Log.d(TAG, "backup profile: " + key + " " + buf.length);
			data.writeEntityHeader(HEADER_PROFILE + key, buf.length);
			data.writeEntityData(buf, buf.length);
		}
	}

	@Override
	public void onBackup(final ParcelFileDescriptor oldState,
			final BackupDataOutput data, final ParcelFileDescriptor newState)
			throws IOException {
		Log.d(TAG, "onBackup()");
		FileInputStream instream = new FileInputStream(
				oldState.getFileDescriptor());
		DataInputStream in = new DataInputStream(instream);
		long lastModified = 0;
		try {
			// Get the last modified timestamp from the state file and data file
			long stateModified = in.readLong();
			SharedPreferences p = PreferenceManager
					.getDefaultSharedPreferences(this);
			lastModified = p.getLong(PREF_LAST_CHANGE, -1);
			Log.d(TAG, "stateModified: " + stateModified);
			Log.d(TAG, "lastModified: " + lastModified);
			if (stateModified != lastModified) {
				// we need a backup
				this.doBackup(data);
				lastModified = System.currentTimeMillis();
			} else {
				return;
			}
		} catch (IOException e) {
			this.doBackup(data);
			lastModified = System.currentTimeMillis();
		}
		FileOutputStream outstream = new FileOutputStream(
				newState.getFileDescriptor());
		DataOutputStream out = new DataOutputStream(outstream);
		out.writeLong(lastModified);
	}

	@Override
	public void onRestore(final BackupDataInput data, final int appVersionCode,
			final ParcelFileDescriptor newState) throws IOException {
		Log.d(TAG, "onRestore()");
		while (data.readNextHeader()) {
			String header = data.getKey();
			int dataSize = data.getDataSize();

			if (HEADER_MAIN.equals(header)) {
				// restore default prefs
				Log.d(TAG, "restore main: " + dataSize);
				byte[] dataBuf = new byte[dataSize];
				data.readEntityData(dataBuf, 0, dataSize);
				ByteArrayInputStream baStream = new ByteArrayInputStream(
						dataBuf);
				ObjectInputStream in = new ObjectInputStream(baStream);
				SharedPreferences p = PreferenceManager
						.getDefaultSharedPreferences(this);
				try {
					readPrefsFromStream(p, in);
				} catch (ClassNotFoundException e) {
					Log.e(TAG, "error restoring default SharedPreferences");
				}
			} else if (header.startsWith(HEADER_PROFILE)) {
				// restore profiles
				String k = header.substring(header.indexOf("_") + 1);
				Log.d(TAG, "restore profile: " + k + " " + dataSize);
				byte[] dataBuf = new byte[dataSize];
				data.readEntityData(dataBuf, 0, dataSize);
				ByteArrayInputStream baStream = new ByteArrayInputStream(
						dataBuf);
				ObjectInputStream in = new ObjectInputStream(baStream);
				SharedPreferences p = this
						.getSharedPreferences(k, MODE_PRIVATE);
				try {
					readPrefsFromStream(p, in);
				} catch (ClassNotFoundException e) {
					Log.e(TAG, "error restoring default SharedPreferences");
				}
			} else {
				data.skipEntityData();
			}
		}

		long l = System.currentTimeMillis();
		PreferenceManager.getDefaultSharedPreferences(this).edit()
				.putLong(PREF_LAST_CHANGE, l);
		FileOutputStream outstream = new FileOutputStream(
				newState.getFileDescriptor());
		DataOutputStream out = new DataOutputStream(outstream);
		out.writeLong(l);
	}

	/**
	 * Read {@link SharedPreferences} from {@link ObjectInputStream}.
	 * 
	 * @param p
	 *            {@link SharedPreferences}
	 * @param in
	 *            {@link ObjectInputStream}
	 * @throws IOException
	 *             IOException
	 * @throws ClassNotFoundException
	 *             Class not found
	 */
	private static void readPrefsFromStream(final SharedPreferences p,
			final ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		Editor e = p.edit();
		e.clear();
		while (in.available() > 0) {
			String k = in.readUTF();
			Object o = in.readObject();
			if (o instanceof String) {
				e.putString(k, (String) o);
			} else if (o instanceof Integer) {
				e.putInt(k, (Integer) o);
			} else if (o instanceof Boolean) {
				e.putBoolean(k, (Boolean) o);
			}
		}
		e.apply();
	}

	/**
	 * Write {@link SharedPreferences} to {@link ObjectOutputStream}.
	 * 
	 * @param p
	 *            {@link SharedPreferences}
	 * @param out
	 *            {@link ObjectOutputStream}
	 * @throws IOException
	 *             IOException
	 */
	private static void writePrefsToStream(final SharedPreferences p,
			final ObjectOutputStream out) throws IOException {
		Map<String, ?> map = p.getAll();
		for (String k : map.keySet()) {
			out.writeUTF(k);
			out.writeObject(map.get(k));
		}
	}
}
