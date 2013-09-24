package org.opennms.features.vaadin.nodemaps.internal.gwt.client;

import java.util.List;
import java.util.Map;

import org.opennms.features.geocoder.Coordinates;

public interface NodeMarker {
    public Integer getNodeId();
    public String getForeignSource();
    public String getForeignId();
    public String getNodeLabel();
    public String getDescription();
    public String getIpAddress();
    public Integer getSeverity();
    public String getSeverityLabel();
    public Integer getUnackedCount();
    public String getMaintContract();
    public List<String> getCategoryList();
    public Coordinates getCoordinates();

    public Map<String, String> getProperties();
}
