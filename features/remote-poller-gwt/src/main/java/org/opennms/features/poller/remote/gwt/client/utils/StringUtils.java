/**
 * 
 */
package org.opennms.features.poller.remote.gwt.client.utils;

import java.util.Collection;
import java.util.Iterator;

public class StringUtils {
	public static String join(Collection<?> s, String delimiter) {
		if (s == null) {
			return "null";
		}
		StringBuilder builder = new StringBuilder();
		Iterator<?> iter = s.iterator();
		while (iter.hasNext()) {
			builder.append(iter.next());
			if (!iter.hasNext()) {
				break;
			}
			builder.append(delimiter);
		}
		return builder.toString();
	}
}