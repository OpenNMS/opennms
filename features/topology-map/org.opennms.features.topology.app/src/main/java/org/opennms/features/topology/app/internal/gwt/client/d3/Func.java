package org.opennms.features.topology.app.internal.gwt.client.d3;

public interface Func<T, D> {
	T call(D datum, int index);
}