package org.opennms.features.poller.remote.gwt.client.utils;

import java.util.ArrayList;
import java.util.Set;



/**
 * <p>CompareToBuilder class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class CompareToBuilder {
	private int comparison = 0;

	/**
	 * <p>append</p>
	 *
	 * @param a a {@link java.lang.Object} object.
	 * @param b a {@link java.lang.Object} object.
	 * @return a {@link org.opennms.features.poller.remote.gwt.client.utils.CompareToBuilder} object.
	 */
	@SuppressWarnings("unchecked")
	public CompareToBuilder append(Object a, Object b) {
		if (comparison != 0) return this;
		if (a == b) return this;

		if (a == null) {
			comparison = -1;
		} else if (b == null) {
			comparison = 1;
		} else {
			if (a instanceof Set<?>) {
				Object[] aL = new ArrayList((Set)a).toArray();
				Object[] bL = new ArrayList((Set)b).toArray();
				append(aL, bL);
			}
			if (a.getClass().isArray()) {
				append((Object[])a, (Object[])b);
			} else {
				comparison = ((Comparable)a).compareTo(b);
			}
		}
		return this;
	}

	/**
	 * <p>append</p>
	 *
	 * @param a an array of {@link java.lang.Object} objects.
	 * @param b an array of {@link java.lang.Object} objects.
	 * @return a {@link org.opennms.features.poller.remote.gwt.client.utils.CompareToBuilder} object.
	 */
	public CompareToBuilder append(Object[] a, Object[] b) {
		if (comparison != 0) return this;
		if (a == b) return this;
		if (a == null) {
			comparison = -1;
		} else if (b == null) {
			comparison = 1;
		} else {
	        if (a.length != b.length) {
	            comparison = (a.length < b.length) ? -1 : +1;
	            return this;
	        }
	        for (int i = 0; i < a.length && comparison == 0; i++) {
	            append(a[i], b[i]);
	        }
		}
		return this;
	}

	/**
	 * <p>toComparison</p>
	 *
	 * @return a int.
	 */
	public int toComparison() {
		return comparison;
	}
}
