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

import java.io.IOException;
import java.nio.charset.Charset;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;
import de.ub0r.android.lib.Log;
import de.ub0r.android.lib.Utils;
import de.ub0r.android.nfcprofile.R;

/**
 * Writing NFC tags.
 * 
 * @author flx
 */
public final class NfcWriterActivity extends Activity implements
		OnClickListener {
	/** Tag for Logging. */
	private static final String TAG = "writer";
	/** Prefix for NFC tag's uri. */
	private static final String URI_PREFIX = "nfcprofile://";
	/** Skip http://www. prefix in Uri. */
	private static final byte[] URI_SKIP_WWW = new byte[] { 0x00 };

	/** Array of {@link IntentFilter}. */
	private IntentFilter[] intentFilters;
	/** {@link PendingIntent} to launch this activity. */
	private PendingIntent pendingIntent;
	/** Array of allowed TECHs. */
	private String[][] techLists;
	/** Active {@link NfcAdapter}. */
	private NfcAdapter nfcAdapter;

	/** Profile's key. */
	private String key;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setTitle(R.string.write_tag);
		this.setContentView(R.layout.nfc_writer_activity);
		if (Build.VERSION.SDK_INT != Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			this.findViewById(R.id.android_40_bug).setVisibility(View.GONE);
		}

		if (savedInstanceState == null) {
			this.key = this.getIntent().getStringExtra(
					ProfileActivity.EXTRA_KEY);
		} else {
			this.key = savedInstanceState.getString(ProfileActivity.EXTRA_KEY);
		}
		this.pendingIntent = PendingIntent.getActivity(this, 0, new Intent(
				this, this.getClass())
				.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
		IntentFilter filter = new IntentFilter(
				NfcAdapter.ACTION_NDEF_DISCOVERED);
		try {
			filter.addDataType("*/*");
		} catch (MalformedMimeTypeException e) {
			throw new RuntimeException(
					"could not add data type */* to IntentFilter", e);
		}
		this.intentFilters = new IntentFilter[] { filter };
		this.techLists = new String[][] { new String[] { Ndef.class.getName() } };
		this.nfcAdapter = NfcAdapter.getDefaultAdapter(this);
	}

	@Override
	protected void onSaveInstanceState(final Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(ProfileActivity.EXTRA_KEY, this.key);
	}

	@Override
	public void onPause() {
		super.onPause();
		if (this.nfcAdapter != null) {
			this.nfcAdapter.disableForegroundDispatch(this);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		if (this.nfcAdapter == null) {
			this.nfcAdapter = NfcAdapter.getDefaultAdapter(this);
		}
		if (this.nfcAdapter == null || !this.nfcAdapter.isEnabled()) {
			Log.e(TAG, "no NFC adapter found");
			this.findViewById(R.id.turn_on_nfc).setVisibility(View.VISIBLE);
			this.findViewById(R.id.turn_on_nfc).setOnClickListener(this);
		} else {
			this.findViewById(R.id.turn_on_nfc).setVisibility(View.GONE);
			this.nfcAdapter.enableForegroundDispatch(this, this.pendingIntent,
					this.intentFilters, this.techLists);
		}

	}

	@Override
	public void onNewIntent(final Intent intent) {
		Log.d(TAG, "onNewIntent(" + intent + ")");
		AsyncTask<Intent, Void, Boolean> task = new AsyncTask<Intent, Void, Boolean>() {
			private ProgressDialog d = null;

			@Override
			protected void onPreExecute() {
				ProgressDialog pd = new ProgressDialog(NfcWriterActivity.this);
				this.d = pd;
				pd.setIndeterminate(true);
				pd.setMessage(NfcWriterActivity.this
						.getString(R.string.writing));
				pd.show();
			};

			@Override
			protected Boolean doInBackground(final Intent... params) {
				boolean success = false;
				try {
					success = NfcWriterActivity.this.writeNfcTag(params[0],
							URI_PREFIX + NfcWriterActivity.this.key);
				} catch (IOException e) {
					Log.e(TAG, "error writing tag", e);
				} catch (FormatException e) {
					Log.e(TAG, "error writing tag", e);
				}
				return success;
			}

			@Override
			protected void onPostExecute(final Boolean success) {
				super.onPostExecute(success);
				this.d.dismiss();
				if (success) {
					Toast.makeText(NfcWriterActivity.this,
							R.string.tag_written, Toast.LENGTH_LONG).show();
				} else {
					Toast.makeText(NfcWriterActivity.this,
							R.string.error_writing_tag, Toast.LENGTH_LONG)
							.show();
				}
				NfcWriterActivity.this.finish();
			}
		};
		task.execute(intent);
	}

	@Override
	public void onClick(final View v) {
		switch (v.getId()) {
		case R.id.turn_on_nfc:
			try {
				this.startActivity(new Intent("android.settings.NFC_SETTINGS"));
			} catch (ActivityNotFoundException e) {
				Log.e(TAG, "no NFC settings found", e);
			}
			break;
		default:
			break;
		}
	}

	/**
	 * Write NFC tag.
	 * 
	 * @param intent
	 *            {@link Intent}
	 * @param uri
	 *            URI which should be written to tag
	 * @return true if NFC tag was written
	 * @throws IOException
	 *             IOException
	 * @throws FormatException
	 *             FormatException
	 */
	private boolean writeNfcTag(final Intent intent, final String uri)
			throws IOException, FormatException {
		Log.d(TAG, "writeUriToTag(" + intent + "," + uri + ")");
		String action = intent.getAction();
		if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
				|| NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
			Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
			Ndef ndef = Ndef.get(tag);
			final byte[] uriBytes = Utils.concatByteArrays(new byte[][] {
					URI_SKIP_WWW, uri.getBytes(Charset.forName("US-ASCII")) });
			NdefRecord uriRecord = new NdefRecord(NdefRecord.TNF_WELL_KNOWN,
					NdefRecord.RTD_URI, new byte[0], uriBytes);
			try {
				NdefRecord[] records = {
						uriRecord,
						NdefRecord.createApplicationRecord(this
								.getApplication().getPackageName()) };
				NdefMessage message = new NdefMessage(records);
				ndef.connect();
				ndef.writeNdefMessage(message);
				Log.i(TAG, "NFC tag written");
				return true;
			} catch (Exception e) {
				Log.e(TAG, "error", e);
			}
		}
		return false;
	}
}
