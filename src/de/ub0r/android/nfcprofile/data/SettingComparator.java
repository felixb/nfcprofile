package de.ub0r.android.nfcprofile.data;

import java.util.Comparator;

/**
 * {@link Comparator} for {@link Setting}s.
 * 
 * @author flx
 */
public final class SettingComparator implements Comparator<Setting> {

	@Override
	public int compare(final Setting lhs, final Setting rhs) {
		if (lhs == rhs || lhs instanceof RingModeSetting
				&& rhs instanceof RingModeSetting) {
			return 0;
		}
		if (lhs instanceof RingModeSetting) {
			return 1;
		}
		if (rhs instanceof RingModeSetting) {
			return -1;
		}
		// ignore the rest
		return 0;
	}

}
