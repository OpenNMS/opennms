package org.opennms.features.poller.remote.gwt.client.utils;

public class CompareToBuilder {
	private int comparison = 0;

	@SuppressWarnings("unchecked")
	public CompareToBuilder append(Object a, Object b) {
		if (comparison != 0) {
			return this;
		}
		if (a == null) {
			comparison = -1;
		} else if (b == null) {
			comparison = 1;
		} else {
			comparison = ((Comparable)a).compareTo(b);
		}
		return this;
	}

	public int toComparison() {
		return comparison;
	}
}
