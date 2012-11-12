package org.opennms.features.topology.app.internal.gwt.client.d3;

public interface Tween<T, D> {

    T call(D d, int index, String a);
}
