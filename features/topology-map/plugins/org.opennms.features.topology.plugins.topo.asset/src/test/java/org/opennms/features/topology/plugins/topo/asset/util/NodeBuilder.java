/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
