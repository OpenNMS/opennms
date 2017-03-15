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

package org.opennms.features.topology.plugins.topo.asset.layers.definition;

import java.util.stream.Collectors;

import org.opennms.features.graphml.model.GraphMLNode;
import org.opennms.features.topology.plugins.topo.asset.layers.ItemProvider;
import org.opennms.features.topology.plugins.topo.asset.layers.LayerDefinition;
import org.opennms.features.topology.plugins.topo.asset.layers.NodeDecorator;
import org.opennms.features.topology.plugins.topo.graphml.GraphMLProperties;
import org.opennms.netmgt.model.OnmsNode;

public enum Layers {
    @Key("asset-country")
    @Restriction("assetRecord.geolocation.country is not null")
    ASSET_COUNTRY(new AssetLayerDefinition() {

        @Override
        public String getId() {
            return "asset-country";
        }

        @Override
        public String getLabel() {
            return "Country";
        }

        @Override
        public ItemProvider<String> getItemProvider() {
            return node -> node.getAssetRecord().getGeolocation().getCountry();
        }
    }),

    @Key("asset-address1")
    @Restriction("assetRecord.geolocation.address1 is not null")
    ASSET_ADDRESS1(new AssetLayerDefinition() {

        @Override
        public String getId() {
            return "asset-address1";
        }

        @Override
        public String getLabel() {
            return "Address 1";
        }

        @Override
        public ItemProvider<String> getItemProvider() {
            return node -> node.getAssetRecord().getGeolocation().getAddress1();
        }
    }),

    @Key("asset-address2")
    @Restriction("assetRecord.geolocation.address2 is not null")
    ASSET_ADDRESS2(new AssetLayerDefinition() {

        @Override
        public String getId() {
            return "asset-address2";
        }

        @Override
        public String getLabel() {
            return "Address 2";
        }

        @Override
        public ItemProvider<String> getItemProvider() {
            return node -> node.getAssetRecord().getGeolocation().getAddress2();
        }
    }),

    @Key("asset-city")
    @Restriction("assetRecord.geolocation.city is not null")
    ASSET_CITY(new AssetLayerDefinition() {

        @Override
        public String getId() {
            return "asset-city";
        }

        @Override
        public String getLabel() {
            return "City";
        }

        @Override
        public ItemProvider<String> getItemProvider() {
            return node -> node.getAssetRecord().getGeolocation().getCity();
        }
    }),

    @Key("asset-zip")
    @Restriction("assetRecord.geolocation.zip is not null")
    ASSET_ZIP(new AssetLayerDefinition() {

        @Override
        public String getId() {
            return "asset-zip";
        }

        @Override
        public String getLabel() {
            return "Zip";
        }

        @Override
        public ItemProvider<String> getItemProvider() {
            return node -> node.getAssetRecord().getGeolocation().getZip();
        }
    }),

    @Key("asset-state")
    @Restriction("assetRecord.geolocation.state is not null")
    ASSET_STATE(new AssetLayerDefinition() {

        @Override
        public String getId() {
            return "asset-state";
        }

        @Override
        public String getLabel() {
            return "State";
        }

        @Override
        public ItemProvider<String> getItemProvider() {
            return node -> node.getAssetRecord().getGeolocation().getState();
        }
    }),

    @Key("asset-latitude")
    @Restriction("assetRecord.geolocation.latitude is not null")
    ASSET_LATITUDE(new AssetLayerDefinition() {

        @Override
        public String getId() {
            return "asset-latitude";
        }

        @Override
        public String getLabel() {
            return "Latitude";
        }

        @Override
        public ItemProvider<String> getItemProvider() {
            return node -> {
                Float latitude = node.getAssetRecord().getGeolocation().getLatitude();
                return latitude == null ? null : latitude.toString();
            };
        }
    }),

    @Key("asset-longitude")
    @Restriction("assetRecord.geolocation.longitude is not null")
    ASSET_LONGITUDE(new AssetLayerDefinition() {

        @Override
        public String getId() {
            return "asset-longitude";
        }

        @Override
        public String getLabel() {
            return "Longitude";
        }

        @Override
        public ItemProvider<String> getItemProvider() {
            return node -> {
                Float longitude = node.getAssetRecord().getGeolocation().getLongitude();
                return longitude == null ? null : longitude.toString();
            };
        }
    }),

    @Key("asset-building")
    @Restriction("assetRecord.building is not null")
    ASSET_BUILDING(new AssetLayerDefinition() {

        @Override
        public String getId() {
            return "asset-building";
        }

        @Override
        public String getLabel() {
            return "Building";
        }

        @Override
        public ItemProvider<String> getItemProvider() {
            return node -> node.getAssetRecord().getBuilding();
        }
    }),

    @Key("asset-region")
    @Restriction("assetRecord.region is not null")
    ASSET_REGION(new AssetLayerDefinition() {

        @Override
        public String getId() {
            return "asset-region";
        }

        @Override
        public String getLabel() {
            return "Region";
        }

        @Override
        public ItemProvider<String> getItemProvider() {
            return node -> node.getAssetRecord().getRegion();
        }
    }),

    @Key("asset-division")
    @Restriction("assetRecord.division is not null")
    ASSET_DIVISION(new AssetLayerDefinition() {

        @Override
        public String getId() {
            return "asset-division";
        }

        @Override
        public String getLabel() {
            return "Division";
        }

        @Override
        public ItemProvider<String> getItemProvider() {
            return node -> node.getAssetRecord().getDivision();
        }
    }),

    @Key("asset-department")
    @Restriction("assetRecord.department is not null")
    ASSET_DEPARTMENT(new AssetLayerDefinition() {

        @Override
        public String getId() {
            return "asset-department";
        }

        @Override
        public String getLabel() {
            return "Department";
        }

        @Override
        public ItemProvider<String> getItemProvider() {
            return node -> node.getAssetRecord().getDepartment();
        }
    }),

    @Key("asset-floor")
    @Restriction("assetRecord.floor is not null")
    ASSET_FLOOR(new AssetLayerDefinition() {

        @Override
        public String getId() {
            return "asset-floor";
        }

        @Override
        public String getLabel() {
            return "Floor";
        }

        @Override
        public ItemProvider<String> getItemProvider() {
            return node -> node.getAssetRecord().getFloor();
        }
    }),

    @Key("asset-room")
    @Restriction("assetRecord.room is not null")
    ASSET_ROOM(new AssetLayerDefinition() {

        @Override
        public String getId() {
            return "asset-room";
        }

        @Override
        public String getLabel() {
            return "Room";
        }

        @Override
        public ItemProvider<String> getItemProvider() {
            return node -> node.getAssetRecord().getRoom();
        }
    }),

    @Key("asset-rack")
    @Restriction("assetRecord.rack is not null")
    ASSET_RACK(new AssetLayerDefinition() {
        @Override
        public String getId() {
            return "asset-rack";
        }

        @Override
        public String getLabel() {
            return "Rack";
        }

        @Override
        public ItemProvider<String> getItemProvider() {
            return node -> node.getAssetRecord().getRack();
        }
    }),

    @Key("asset-slot")
    @Restriction("assetRecord.slot is not null")
    ASSET_SLOT(new AssetLayerDefinition() {
        @Override
        public String getId() {
            return "asset-slot";
        }

        @Override
        public String getLabel() {
            return "Slot";
        }

        @Override
        public ItemProvider<String> getItemProvider() {
            return node -> node.getAssetRecord().getSlot();
        }
    }),

    @Key("asset-port")
    @Restriction("assetRecord.port is not null")
    ASSET_PORT(new AssetLayerDefinition() {
        @Override
        public String getId() {
            return "asset-port";
        }

        @Override
        public String getLabel() {
            return "Port";
        }

        @Override
        public ItemProvider<String> getItemProvider() {
            return node -> node.getAssetRecord().getPort();
        }
    }),

    @Key("asset-circuitid")
    @Restriction("assetRecord.circuitId is not null")
    ASSET_CIRCUIT_ID(new AssetLayerDefinition() {
        @Override
        public String getId() {
            return "asset-circuitid";
        }

        @Override
        public String getLabel() {
            return "Circuit ID";
        }

        @Override
        public ItemProvider<String> getItemProvider() {
            return node -> node.getAssetRecord().getCircuitId();
        }
    }),

    @Key("asset-category")
    @Restriction("assetRecord.category is not null")
    ASSET_CATEGORY(new AssetLayerDefinition() {
        @Override
        public String getId() {
            return "asset-category";
        }

        @Override
        public String getLabel() {
            return "Category";
        }

        @Override
        public ItemProvider<String> getItemProvider() {
            return node -> node.getAssetRecord().getCategory();
        }
    }),

    @Key("asset-displaycategory")
    @Restriction("assetRecord.displayCategory is not null")
    ASSET_DISPLAY_CATEGORY(new AssetLayerDefinition() {
        @Override
        public String getId() {
            return "asset-displaycategory";
        }

        @Override
        public String getLabel() {
            return "Display Category";
        }

        @Override
        public ItemProvider<String> getItemProvider() {
            return node -> node.getAssetRecord().getDisplayCategory();
        }
    }),

    @Key("asset-notifycategory")
    @Restriction("assetRecord.notifyCategory is not null")
    ASSET_NOTIFY_CATEGORY(new AssetLayerDefinition() {
        @Override
        public String getId() {
            return "asset-notifycategory";
        }

        @Override
        public String getLabel() {
            return "Notify Category";
        }

        @Override
        public ItemProvider<String> getItemProvider() {
            return node -> node.getAssetRecord().getNotifyCategory();
        }
    }),

    @Key("asset-pollercategory")
    @Restriction("assetRecord.pollerCategory is not null")
    ASSET_POLLER_CATEGORY(new AssetLayerDefinition() {
        @Override
        public String getId() {
            return "asset-pollercategory";
        }

        @Override
        public String getLabel() {
            return "Poller Category";
        }

        @Override
        public ItemProvider<String> getItemProvider() {
            return node -> node.getAssetRecord().getPollerCategory();
        }
    }),

    @Key("asset-thresholdcategory")
    @Restriction("assetRecord.thresholdCategory is not null")
    ASSET_THRESHOLD_CATEGORY(new AssetLayerDefinition() {
        @Override
        public String getId() {
            return "asset-thresholdcategory";
        }

        @Override
        public String getLabel() {
            return "Threshold Category";
        }

        @Override
        public ItemProvider<String> getItemProvider() {
            return node -> node.getAssetRecord().getThresholdCategory();
        }
    }),

    @Key("asset-managedobjecttype")
    @Restriction("assetRecord.managedObjectType is not null")
    ASSET_MANAGED_OBJECT_TYPE(new AssetLayerDefinition() {
        @Override
        public String getId() {
            return "asset-managedobjecttype";
        }

        @Override
        public String getLabel() {
            return "Managed Object Type";
        }

        @Override
        public ItemProvider<String> getItemProvider() {
            return node -> node.getAssetRecord().getManagedObjectType();
        }
    }),

    @Key("asset-managedobjectinstance")
    @Restriction("assetRecord.managedObjectInstance is not null")
    ASSET_MANAGED_OBJECT_INSTANCE(new AssetLayerDefinition() {
        @Override
        public String getId() {
            return "asset-managedobjectinstance";
        }

        @Override
        public String getLabel() {
            return "Managed Object Instance";
        }

        @Override
        public ItemProvider<String> getItemProvider() {
            return node -> node.getAssetRecord().getManagedObjectInstance();
        }
    }),

    @Key("asset-manufacturer")
    @Restriction("assetRecord.manufacturer is not null")
    ASSET_MANUFACTURER(new AssetLayerDefinition() {
        @Override
        public String getId() {
            return "asset-manufacturer";
        }

        @Override
        public String getLabel() {
            return "Manufacturer";
        }

        @Override
        public ItemProvider<String> getItemProvider() {
            return node -> node.getAssetRecord().getManufacturer();
        }
    }),

    @Key("asset-vendor")
    @Restriction("assetRecord.vendor is not null")
    ASSET_VENDOR(new AssetLayerDefinition() {
        @Override
        public String getId() {
            return "asset-vendor";
        }

        @Override
        public String getLabel() {
            return "Vendor";
        }

        @Override
        public ItemProvider<String> getItemProvider() {
            return node -> node.getAssetRecord().getVendor();
        }
    }),

    @Key("asset-modelnumber")
    @Restriction("assetRecord.modelNumber is not null")
    ASSET_MODEL_NUMBER(new AssetLayerDefinition() {
        @Override
        public String getId() {
            return "asset-modelnumber";
        }

        @Override
        public String getLabel() {
            return "Model Number";
        }

        @Override
        public ItemProvider<String> getItemProvider() {
            return node -> node.getAssetRecord().getModelNumber();
        }
    }),

    @Key("asset-description")
    @Restriction("assetRecord.description is not null")
    ASSET_DESCRIPTION(new AssetLayerDefinition() {
        @Override
        public String getId() {
            return "asset-description";
        }

        @Override
        public String getLabel() {
            return "Description";
        }

        @Override
        public ItemProvider<String> getItemProvider() {
            return node -> node.getAssetRecord().getDescription();
        }
    }),

    @Key("asset-operatingsystem")
    @Restriction("assetRecord.operatingSystem is not null")
    ASSET_OPERATING_SYSTEM(new AssetLayerDefinition() {
        @Override
        public String getId() {
            return "asset-operatingsystem";
        }

        @Override
        public String getLabel() {
            return "Operating System";
        }

        @Override
        public ItemProvider<String> getItemProvider() {
            return node -> node.getAssetRecord().getOperatingSystem();
        }
    }),

    @Key("node-nodelabel")
    @Restriction("label is not null")
    NODE_LABEL(new AssetLayerDefinition() {
        @Override
        public String getId() {
            return "node-nodelabel";
        }

        @Override
        public String getLabel() {
            return "Node Label";
        }

        @Override
        public ItemProvider<String> getItemProvider() {
            return node -> node.getLabel();
        }
    }),

    @Key("node-nodeid")
    @Restriction("id is not null")
    NODE_ID(new AssetLayerDefinition() {
        @Override
        public String getId() {
            return "node-id";
        }

        @Override
        public String getLabel() {
            return "Node ID";
        }

        @Override
        public ItemProvider<String> getItemProvider() {
            return node -> node.getNodeId();
        }
    }),

    @Key("node-foreignsource")
    @Restriction("foreignSource is not null")
    NODE_FOREIGN_SOURCE(new AssetLayerDefinition() {
        @Override
        public String getId() {
            return "node-foreignsource";
        }

        @Override
        public String getLabel() {
            return "Node Foreign Source";
        }

        @Override
        public ItemProvider<String> getItemProvider() {
            return node -> node.getForeignSource();
        }
    }),

    @Key("node-foreignid")
    @Restriction("foreignId is not null")
    NODE_FOREIGN_ID(new AssetLayerDefinition() {
        @Override
        public String getId() {
            return "node-foreignid";
        }

        @Override
        public String getLabel() {
            return "Node Foreign ID";
        }

        @Override
        public ItemProvider<String> getItemProvider() {
            return node -> node.getForeignId();
        }
    }),

    @Key("node-nodesysname")
    @Restriction("sysName is not null")
    NODE_SYSTEM_NAME(new AssetLayerDefinition() {
        @Override
        public String getId() {
            return "node-nodesysname";
        }

        @Override
        public String getLabel() {
            return "Node System Name";
        }

        @Override
        public ItemProvider<String> getItemProvider() {
            return node -> node.getSysName();
        }
    }),

    @Key("node-nodesyslocation")
    @Restriction("sysLocation is not null")
    NODE_SYSTEM_LOCATION(new AssetLayerDefinition() {
        @Override
        public String getId() {
            return "node-nodesyslocation";
        }

        @Override
        public String getLabel() {
            return "Node Syslocation";
        }

        @Override
        public ItemProvider<String> getItemProvider() {
            return node -> node.getSysLocation();
        }
    }),

    @Key("node-operatingsystem")
    @Restriction("operatingSystem is not null")
    NODE_OPERATING_SYSTEM(new AssetLayerDefinition() {
        @Override
        public String getId() {
            return "node-operatingsystem";
        }

        @Override
        public String getLabel() {
            return "Node Operating System";
        }

        @Override
        public ItemProvider<String> getItemProvider() {
            return node -> node.getOperatingSystem();
        }
    }),

    @Key("parent-nodelabel")
    @Restriction("parent.label is not null")
    PARENT_NODE_LABEL(new AssetLayerDefinition() {
        @Override
        public String getId() {
            return "parent-nodelabel";
        }

        @Override
        public String getLabel() {
            return "Parent Node Label";
        }

        @Override
        public ItemProvider<String> getItemProvider() {
            return node -> node.getParent().getLabel();
        }
    }),

    @Key("parent-nodeid")
    @Restriction("parent.id is not null")
    PARENT_NODE_ID(new AssetLayerDefinition() {
        @Override
        public String getId() {
            return "parent-nodeid";
        }

        @Override
        public String getLabel() {
            return "Parent Node ID";
        }

        @Override
        public ItemProvider<String> getItemProvider() {
            return node -> node.getParent().getNodeId();
        }
    }),

    @Key("parent-foreignsource")
    @Restriction("parent.foreignSource is not null")
    PARENT_FOREIGN_SOURCE(new AssetLayerDefinition() {
        @Override
        public String getId() {
            return "parent-foreignsource";
        }

        @Override
        public String getLabel() {
            return "Parent Foreign Source";
        }

        @Override
        public ItemProvider<String> getItemProvider() {
            return node -> node.getParent().getForeignSource();
        }
    }),

    @Key("parent-foreignid")
    @Restriction("parent.foreignId is not null")
    PARENT_FOREIGN_ID(new AssetLayerDefinition() {
        @Override
        public String getId() {
            return "parent-foreignid";
        }

        @Override
        public String getLabel() {
            return "Parent Foreign ID";
        }

        @Override
        public ItemProvider<String> getItemProvider() {
            return node -> node.getParent().getForeignId();
        }
    }),

    @Key("node-categories")
    @Restriction("categories is not empty")
    NODE_CATEGORIES(new AssetLayerDefinition() {
        @Override
        public String getId() {
            return "node-categories";
        }

        @Override
        public String getLabel() {
            return "Node Categories";
        }

        @Override
        public NodeDecorator<String> getNodeDecorator() {
            return new NodeDecorator<String>() {
                @Override
                public void decorate(GraphMLNode graphMLNode, String value) {
                    graphMLNode.setProperty(GraphMLProperties.LABEL, value);
                }

                @Override
                public String getId(String value) {
                    return value;
                }
            };
        }

        @Override
        public ItemProvider<String> getItemProvider() {
            return new ItemProvider<String>() {

                @Override
                public String getItem(OnmsNode node) {
                    if (node.getCategories().size() == 1) {
                        return node.getCategories().iterator().next().getName();
                    }
                    if (node.getCategories().size() > 1) {
                        return node.getCategories().stream().map(c -> c.getName()).collect(Collectors.joining(","));
                    }
                    return null;
                }
            };
        }
    });

    private final LayerDefinition layerDefinition;

    Layers(LayerDefinition layerDefinition) {
        this.layerDefinition = layerDefinition;
    }

    public LayerDefinition getLayerDefinition() {
        return layerDefinition;
    }
}
