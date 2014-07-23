package org.opennms.netmgt.snmp;


class ToStringBuilder {
	
	final StringBuilder buf;
	boolean first = true;
	boolean finished = false;

	public ToStringBuilder(Object o) {
		buf = new StringBuilder(512);
		
		buf.append(o.getClass().getSimpleName());
		buf.append('@');
		buf.append(String.format("%x", System.identityHashCode(o)));
		buf.append('[');
		
	}
	
	public ToStringBuilder append(String label, String value) {
		assertNotFinished();
		if (!first) {
			buf.append(", ");
		} else {
			first = false;
		}
		buf.append(label).append('=').append(value);
		return this;
	}
	
	public ToStringBuilder append(String label, Object value) {
		return append(label, value == null ? null : value.toString());
	}
	
        @Override
	public String toString() {
		if (!finished) {
			buf.append(']');
			finished = true;
		}
		
		return buf.toString();
	}
	
	private void assertNotFinished() {
		if (finished) {
			throw new IllegalStateException("This builder has already been completed by calling toString");
		}
	}
	
}
