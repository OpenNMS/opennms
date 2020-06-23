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

package org.opennms.features.topology.plugins.topo.asset.util;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.opennms.netmgt.dao.api.MonitoringLocationDao;
import org.opennms.netmgt.model.OnmsAssetRecord;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation;

public class NodeBuilder {
    
    private OnmsNode node = new OnmsNode();

    private AssetBuilder assetBuilder;

    public NodeBuilder withId(String id) {
        node.setNodeId(id);
        return this;
    }
    
    public NodeBuilder withLabel(String label) {
        node.setLabel(label);
        return this;
    }

    public NodeBuilder withParentId(int parentId) {
        getParent().setId(parentId);
        return this;
    }

    public NodeBuilder withParentId(String parentId) {
        getParent().setNodeId(parentId);
        return this;
    }

    public NodeBuilder withForeignSource(String foreignSource) {
        node.setForeignSource(foreignSource);
        return this;
    }

    public NodeBuilder withForeignId(String foreignId) {
        node.setForeignId(foreignId);
        return this;
    }

    public NodeBuilder withSyslocation(String syslocation) {
        node.setSysLocation(syslocation);
        return this;
    }

    public NodeBuilder withOperatingSystem(String operatingSystem) {
        node.setOperatingSystem(operatingSystem);
        return this;
    }

    public NodeBuilder withCategories(String categories) {
        Set<OnmsCategory> categorySet = Arrays.stream(categories.split(","))
                .map(c -> c.trim())
                .filter(c -> c != null && c.length() > 1)
                .map(c -> new OnmsCategory(c))
                .collect(Collectors.toSet());
        node.setCategories(categorySet);
        return this;
    }

    public NodeBuilder withParentLabel(String label) {
        getParent().setLabel(label);
        return this;
    }

    public NodeBuilder withParentForeignSource(String foreignSource) {
        getParent().setForeignSource(foreignSource);
        return this;
    }

    public NodeBuilder withParentForeignId(String foreignId) {
        getParent().setForeignId(foreignId);
        return this;
    }

    public AssetBuilder withAssets() {
        if (assetBuilder != null) {
            return assetBuilder;
        }
        assetBuilder = new AssetBuilder(this);
        return assetBuilder;
    }

    public OnmsNode getNode() {
        if (assetBuilder != null) {
            OnmsAssetRecord assetRecord = assetBuilder.getAssetRecord();
            assetRecord.setNode(node);
            node.setAssetRecord(assetRecord);
        }
        if (node.getLocation() == null) {
            node.setLocation(new OnmsMonitoringLocation(MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID, MonitoringLocationDao.DEFAULT_MONITORING_LOCATION_ID));
        }
        return node;
    }

    private OnmsNode getParent() {
        if (node.getParent() != null) {
            return node.getParent();
        }
        node.setParent(new OnmsNode());
        return node.getParent();
    }

    public NodeBuilder withId(int nodeId) {
        node.setId(nodeId);
        return this;
    }

    public NodeBuilder withSysname(String sysname) {
        node.setSysName(sysname);
        return this;
    }
}
