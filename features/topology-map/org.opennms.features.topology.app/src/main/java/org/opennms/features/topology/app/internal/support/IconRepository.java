package org.opennms.features.topology.app.internal.support;

public interface IconRepository {

    boolean contains(String type);

    String getIconUrl(String type);

}
