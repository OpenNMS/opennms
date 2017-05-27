/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.service;

public class NodeScanScheduleData {
    private int nodeId;
    private String foreignSource;
    private String foreignId;
    private String nodeLabel;
    private long scanIntervalInSeconds;
    private long delayInMilliseconds;

    public void setNodeId(int nodeId) {
        this.nodeId = nodeId;
    }

    public int getNodeId() {
        return nodeId;
    }

    public void setForeignSource(String foreignSource) {
        this.foreignSource = foreignSource;
    }

    public String getForeignSource() {
        return foreignSource;
    }

    public void setForeignId(String foreignId) {
        this.foreignId = foreignId;
    }

    public String getForeignId() {
        return foreignId;
    }

    public void setNodeLabel(String nodeLabel) {
        this.nodeLabel = nodeLabel;
    }

    public String getNodeLabel() {
        return nodeLabel;
    }

    public void setScanIntervalInSeconds(long scanIntervalInSeconds) {
        this.scanIntervalInSeconds = scanIntervalInSeconds;
    }

    public long getScanIntervalInSeconds() {
        return scanIntervalInSeconds;
    }

    public void setDelayInMilliseconds(long delayInMilliseconds) {
        this.delayInMilliseconds = delayInMilliseconds;
    }

    public long getDelayInMilliseconds() {
        return delayInMilliseconds;
    }
}
