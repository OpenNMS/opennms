/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.vaadin.nodemaps.internal.gwt.client;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * @author Marcus Hellberg (marcus@vaadin.com)
 */
public class MapNode implements Serializable {
    private static final long serialVersionUID = -8153594123122717289L;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final MapNode mapNode = (MapNode) o;
        return Double.compare(mapNode.latitude, latitude) == 0 &&
                Double.compare(mapNode.longitude, longitude) == 0 &&
                unackedCount == mapNode.unackedCount &&
                Objects.equals(nodeId, mapNode.nodeId) &&
                Objects.equals(nodeLabel, mapNode.nodeLabel) &&
                Objects.equals(foreignSource, mapNode.foreignSource) &&
                Objects.equals(foreignId, mapNode.foreignId) &&
                Objects.equals(description, mapNode.description) &&
                Objects.equals(maintcontract, mapNode.maintcontract) &&
                Objects.equals(ipAddress, mapNode.ipAddress) &&
                Objects.equals(severity, mapNode.severity) &&
                Objects.equals(severityLabel, mapNode.severityLabel) &&
                Objects.equals(categories, mapNode.categories);
    }

    @Override
    public int hashCode() {
        return Objects.hash(latitude, longitude, nodeId, nodeLabel, foreignSource, foreignId, description,
                maintcontract, ipAddress, severity, severityLabel, unackedCount, categories);
    }
}
