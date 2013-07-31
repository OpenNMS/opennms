package org.opennms.features.vaadin.nodemaps.internal.gwt.client;

import java.util.List;

/**
 * @author Marcus Hellberg (marcus@vaadin.com)
 */
public class MapNode {
    private double latitude;
    private double longitude;
    private String nodeId;
    private String nodeLabel;
    private String foreignSource;
    private String foreignId;
    private String description;
    private String maintcontract;
    private String ipAddress;
    private String severity;
    private String severityLabel;
    private int unackedCount;
    private List<String> categories;

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getNodeLabel() {
        return nodeLabel;
    }

    public void setNodeLabel(String nodeLabel) {
        this.nodeLabel = nodeLabel;
    }

    public String getForeignSource() {
        return foreignSource;
    }

    public void setForeignSource(String foreignSource) {
        this.foreignSource = foreignSource;
    }

    public String getForeignId() {
        return foreignId;
    }

    public void setForeignId(String foreignId) {
        this.foreignId = foreignId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMaintcontract() {
        return maintcontract;
    }

    public void setMaintcontract(String maintcontract) {
        this.maintcontract = maintcontract;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getSeverityLabel() {
        return severityLabel;
    }

    public void setSeverityLabel(String severityLabel) {
        this.severityLabel = severityLabel;
    }

    public int getUnackedCount() {
        return unackedCount;
    }

    public void setUnackedCount(int unackedCount) {
        this.unackedCount = unackedCount;
    }

    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }
}
