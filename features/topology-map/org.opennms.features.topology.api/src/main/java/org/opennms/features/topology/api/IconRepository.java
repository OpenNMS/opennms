package org.opennms.features.topology.api;

public interface IconRepository {

    boolean contains(String type);

    String getIconUrl(String type);

}
