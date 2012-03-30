package org.opennms.features.vaadin.topology.gwt.client.d3;

public interface Func<T, P> {
	T call(P param);
}