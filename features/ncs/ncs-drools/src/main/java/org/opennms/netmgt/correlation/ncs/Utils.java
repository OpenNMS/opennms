package org.opennms.netmgt.correlation.ncs;

public abstract class Utils {
	
	public static boolean nullSafeEquals(Object a, Object b) {
		return a == null ? b == null : a.equals(b);
	}

}
