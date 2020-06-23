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

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.opennms.core.xml.JaxbUtils;
import org.opennms.features.topology.plugins.topo.asset.NodeProvider;
import org.opennms.features.topology.plugins.topo.asset.layers.LayerDefinition;
import org.opennms.features.topology.plugins.topo.asset.layers.NodeParamLabels;
import org.opennms.netmgt.model.OnmsNode;

public class TestNodeProvider implements NodeProvider {

    private static final String NODE_TEST_DATA_FILE_NAME="/mock-testdata.xml";

    @Override
    public List<OnmsNode> getNodes(List<LayerDefinition> definitions) {
            final InputStream nodeTestDataStream = getClass().getResourceAsStream(NODE_TEST_DATA_FILE_NAME);
            NodeInfoRepositoryXML nodeInfoRepositoryXML = JaxbUtils.unmarshal(NodeInfoRepositoryXML.class, nodeTestDataStream);
            final List<OnmsNode> nodes = nodeInfoRepositoryXML.getNodeInfoList().stream().map(eachEntry -> {
                final NodeBuilder nodeBuilder = new NodeBuilder().withId(eachEntry.getNodeId());
                final Map<String, String> parameters = eachEntry.getParameters();

                // Apply Node parameters
                apply(parameters, NodeParamLabels.NODE_NODELABEL, value -> nodeBuilder.withLabel(value));
                apply(parameters, NodeParamLabels.NODE_NODEID, value -> nodeBuilder.withId(value));
                apply(parameters, NodeParamLabels.NODE_FOREIGNSOURCE, value -> nodeBuilder.withForeignSource(value));
                apply(parameters, NodeParamLabels.NODE_FOREIGNID, value -> nodeBuilder.withForeignId(value));
                apply(parameters, NodeParamLabels.NODE_NODESYSNAME, value -> nodeBuilder.withSyslocation(value));
                apply(parameters, NodeParamLabels.NODE_OPERATINGSYSTEM, value -> nodeBuilder.withOperatingSystem(value));
                apply(parameters, NodeParamLabels.NODE_CATEGORIES, value -> nodeBuilder.withCategories(value));
                apply(parameters, NodeParamLabels.PARENT_NODELABEL, value -> nodeBuilder.withParentLabel(value));
                apply(parameters, NodeParamLabels.PARENT_NODEID, value -> nodeBuilder.withParentId(value));
                apply(parameters, NodeParamLabels.PARENT_FOREIGNSOURCE, value -> nodeBuilder.withParentForeignSource(value));
                apply(parameters, NodeParamLabels.PARENT_FOREIGNID, value -> nodeBuilder.withParentForeignId(value));

                // Apply Asset parameters
                AssetBuilder assetBuilder = nodeBuilder.withAssets();
                apply(parameters, NodeParamLabels.ASSET_COUNTRY, value -> assetBuilder.withCountry(value));
                apply(parameters, NodeParamLabels.ASSET_ADDRESS1, value -> assetBuilder.withAddress1(value));
                apply(parameters, NodeParamLabels.ASSET_ADDRESS2, value -> assetBuilder.withAddress2(value));
                apply(parameters, NodeParamLabels.ASSET_CITY, value -> assetBuilder.withCity(value));
                apply(parameters, NodeParamLabels.ASSET_ZIP, value -> assetBuilder.withZip(value));
                apply(parameters, NodeParamLabels.ASSET_STATE, value -> assetBuilder.withState(value));
                apply(parameters, NodeParamLabels.ASSET_LATITUDE, value -> assetBuilder.withLatitude(value));
                apply(parameters, NodeParamLabels.ASSET_LONGITUDE, value -> assetBuilder.withLongitude(value));
                apply(parameters, NodeParamLabels.ASSET_REGION, value -> assetBuilder.withRegion(value));
                apply(parameters, NodeParamLabels.ASSET_DIVISION, value -> assetBuilder.withDivision(value));
                apply(parameters, NodeParamLabels.ASSET_DEPARTMENT, value -> assetBuilder.withDepartment(value));
                apply(parameters, NodeParamLabels.ASSET_BUILDING, value -> assetBuilder.withBuilding(value));
                apply(parameters, NodeParamLabels.ASSET_FLOOR, value -> assetBuilder.withFloor(value));
                apply(parameters, NodeParamLabels.ASSET_ROOM, value -> assetBuilder.withRoom(value));
                apply(parameters, NodeParamLabels.ASSET_RACK, value -> assetBuilder.withRack(value));
                apply(parameters, NodeParamLabels.ASSET_SLOT, value -> assetBuilder.withSlot(value));
                apply(parameters, NodeParamLabels.ASSET_PORT, value -> assetBuilder.withPort(value));
                apply(parameters, NodeParamLabels.ASSET_CIRCUITID, value -> assetBuilder.withCircuitId(value));
                apply(parameters, NodeParamLabels.ASSET_CATEGORY, value -> assetBuilder.withCategory(value));
                apply(parameters, NodeParamLabels.ASSET_DISPLAYCATEGORY, value -> assetBuilder.withDisplayCategory(value));
                apply(parameters, NodeParamLabels.ASSET_NOTIFYCATEGORY, value -> assetBuilder.withNotifyCategory(value));
                apply(parameters, NodeParamLabels.ASSET_POLLERCATEGORY, value -> assetBuilder.withPollerCategory(value));
                apply(parameters, NodeParamLabels.ASSET_THRESHOLDCATEGORY, value -> assetBuilder.withThresholdCategory(value));
                apply(parameters, NodeParamLabels.ASSET_MANAGEDOBJECTTYPE, value -> assetBuilder.withManagedObjectType(value));
                apply(parameters, NodeParamLabels.ASSET_MANAGEDOBJECTINSTANCE, value -> assetBuilder.withManagedObjectInstance(value));
                apply(parameters, NodeParamLabels.ASSET_MANUFACTURER, value -> assetBuilder.withManufacturer(value));
                apply(parameters, NodeParamLabels.ASSET_VENDOR, value -> assetBuilder.withVendor(value));
                apply(parameters, NodeParamLabels.ASSET_MODELNUMBER, value -> assetBuilder.withModelNumber(value));
                apply(parameters, NodeParamLabels.ASSET_DESCRIPTION, value -> assetBuilder.withDescription(value));
                apply(parameters, NodeParamLabels.ASSET_OPERATINGSYSTEM, value -> assetBuilder.withOperatingSystem(value));

                OnmsNode node = nodeBuilder.getNode();
                return node;
            }).collect(Collectors.toList());
            return nodes;
    }


    private static void apply(Map<String, String> parameters, String parameterKey, Consumer<String> consumer) {
        if (parameters.get(parameterKey) != null) {
            consumer.accept(parameters.get(parameterKey));
        }
    }

}
