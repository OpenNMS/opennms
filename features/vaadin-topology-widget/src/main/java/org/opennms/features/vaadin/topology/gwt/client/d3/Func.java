package org.opennms.features.vaadin.topology.gwt.client.d3;

public interface Func<T, D> {
	T call(D datum, int index);
}