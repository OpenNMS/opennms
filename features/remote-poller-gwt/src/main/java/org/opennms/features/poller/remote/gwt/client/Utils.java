/**
 * 
 */
package org.opennms.features.poller.remote.gwt.client;

import java.util.Collection;
import java.util.Iterator;

public class Utils {
	public static String join(Collection<?> s, String delimiter) {
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