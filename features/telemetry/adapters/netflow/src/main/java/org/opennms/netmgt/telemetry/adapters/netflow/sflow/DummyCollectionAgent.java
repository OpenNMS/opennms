/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018-2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.telemetry.adapters.netflow.sflow;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Objects;
import java.util.Set;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.model.ResourcePath;

// TODO MVR for now this is used to get the sflowtelemetryadapter running inside OSGi. A better approach must be found
public class DummyCollectionAgent implements CollectionAgent {

    private final InetAddress inetAddress;

    private final HashMap<String, Object> properties = new HashMap<>();
    private final Integer nodeId;
    private final String nodeLabel;
    private final String foreignSource;
    private final String foreignId;
    private long sysUpTime;
    private String locationName;

    public DummyCollectionAgent(final InetAddress inetAddress,
                                final int nodeId,
                                final String nodeLabel,
                                final String foreignSource,
                                final String foreignId,
                                final String locationName) {
        this.inetAddress = Objects.requireNonNull(inetAddress);
        this.nodeId = Objects.requireNonNull(nodeId);
        this.nodeLabel = Objects.requireNonNull(nodeLabel);
        this.foreignSource = foreignSource;
        this.foreignId = Objects.requireNonNull(foreignId);
        this.locationName = Objects.requireNonNull(locationName);
    }

    @Override
    public InetAddress getAddress() {
        return inetAddress;
    }

    @Override
    public Set<String> getAttributeNames() {
        return properties.keySet();
    }

    @Override
    public <V> V getAttribute(String property) {
        return (V) properties.get(property);
    }

    @Override
    public Object setAttribute(String property, Object value) {
        return properties.put(property, value);
    }

    @Override
    public Boolean isStoreByForeignSource() {
        return false;
    }

    @Override
    public String getHostAddress() {
        return InetAddressUtils.str(getAddress());
    }

    @Override
    public int getNodeId() {
        return nodeId;
    }

    @Override
    public String getNodeLabel() {
        return nodeLabel;
    }

    @Override
    public String getForeignSource() {
        return foreignSource;
    }

    @Override
    public String getForeignId() {
        return foreignId;
    }

    @Override
    public String getLocationName() {
        return locationName;
    }

    @Override
    public ResourcePath getStorageResourcePath() {
        return null;
    }

    @Override
    public long getSavedSysUpTime() {
        return sysUpTime;
    }

    @Override
    public void setSavedSysUpTime(long sysUpTime) {
        this.sysUpTime = sysUpTime;
    }
}
