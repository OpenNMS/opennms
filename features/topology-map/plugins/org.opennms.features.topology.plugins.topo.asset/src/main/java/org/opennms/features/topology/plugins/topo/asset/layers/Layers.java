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

package org.opennms.features.topology.plugins.topo.asset.layers;

import java.util.stream.Collectors;

import org.opennms.features.graphml.model.GraphMLNode;
import org.opennms.features.topology.plugins.topo.graphml.GraphMLProperties;
import org.opennms.netmgt.model.OnmsNode;

public enum Layers {
    @Key(NodeParamLabels.ASSET_COUNTRY)
    @Restriction(hql = "assetRecord.geolocation.country is not null")
    ASSET_COUNTRY(new AssetLayer() {

        @Override
        public String getId() {
            return "asset-country";
        }

        @Override
        public String getLabel() {
            return "Country";
        }

        @Override
        public String getDescription() {
            return "Displays all country fields of the topology";
        }

        @Override
        public ItemProvider<String> getItemProvider() {
            return node -> node.getAssetRecord().getGeolocation().getCountry();
        }
    }),

    @Key(NodeParamLabels.ASSET_ADDRESS1)
    @Restriction(hql = "assetRecord.geolocation.address1 is not null")
    ASSET_ADDRESS1(new AssetLayer() {

        @Override
        public String getId() {
            return "asset-address1";
        }

        @Override
        public String getLabel() {
            return "Address 1";
        }

        public String getDescription() {
            return "Displays all address 1 fields of the topology";
        }

        @Override
        public ItemProvider<String> getItemProvider() {
            return node -> node.getAssetRecord().getGeolocation().getAddress1();
        }
    }),

    @Key(NodeParamLabels.ASSET_ADDRESS2)
    @Restriction(hql = "assetRecord.geolocation.address2 is not null")
    ASSET_ADDRESS2(new AssetLayer() {

        @Override
        public String getId() {
            return "asset-address2";
        }

        @Override
        public String getLabel() {
            return "Address 2";
        }

        public String getDescription() {
            return "Displays all address 2 fields of the topology";
        }

        @Override
        public ItemProvider<String> getItemProvider() {
            return node -> node.getAssetRecord().getGeolocation().getAddress2();
        }
    }),

    @Key(NodeParamLabels.ASSET_CITY)
    @Restriction(hql = "assetRecord.geolocation.city is not null")
    ASSET_CITY(new AssetLayer() {

        @Override
        public String getId() {
            return "asset-city";
        }

        @Override
        public String getLabel() {
            return "City";
        }

        public String getDescription() {
            return "Displays all city fields of the topology";
        }

        @Override
        public ItemProvider<String> getItemProvider() {
            return node -> node.getAssetRecord().getGeolocation().getCity();
        }
    }),

    @Key(NodeParamLabels.ASSET_ZIP)
    @Restriction(hql = "assetRecord.geolocation.zip is not null")
    ASSET_ZIP(new AssetLayer() {

        @Override
        public String getId() {
            return "asset-zip";
        }

        @Override
        public String getLabel() {
            return "Zip";
        }

        public String getDescription() {
            return "Displays all zips of the topology";
        }

        @Override
        public ItemProvider<String> getItemProvider() {
            return node -> node.getAssetRecord().getGeolocation().getZip();
        }
    }),

    @Key(NodeParamLabels.ASSET_STATE)
    @Restriction(hql = "assetRecord.geolocation.state is not null")
    ASSET_STATE(new AssetLayer() {

        @Override
        public String getId() {
            return "asset-state";
        }

        @Override
        public String getLabel() {
            return "State";
        }

        public String getDescription() {
            return "Displays all state fields of the topology";
        }

        @Override
        public ItemProvider<String> getItemProvider() {
            return node -> node.getAssetRecord().getGeolocation().getState();
        }
    }),

    @Key(NodeParamLabels.ASSET_LATITUDE)
    @Restriction(hql = "assetRecord.geolocation.latitude is not null")
    ASSET_LATITUDE(new AssetLayer() {

        @Override
        public String getId() {
            return "asset-latitude";
        }

        @Override
        public String getLabel() {
            return "Latitude";
        }

        public String getDescription() {
            return "Displays all latitude fields of the topology";
        }

        @Override
        public ItemProvider<String> getItemProvider() {
            return node -> {
                Double latitude = node.getAssetRecord().getGeolocation().getLatitude();
                return latitude == null ? null : latitude.toString();
            };
        }
    }),

    @Key(NodeParamLabels.ASSET_LONGITUDE)
    @Restriction(hql = "assetRecord.geolocation.longitude is not null")
    ASSET_LONGITUDE(new AssetLayer() {

        @Override
        public String getId() {
            return "asset-longitude";
        }

        @Override
        public String getLabel() {
            return "Longitude";
        }

        public String getDescription() {
            return "Displays all longitude fields of the topology";
        }

        @Override
        public ItemProvider<String> getItemProvider() {
            return node -> {
                Double longitude = node.getAssetRecord().getGeolocation().getLongitude();
                return longitude == null ? null : longitude.toString();
            };
        }
    }),

    @Key(NodeParamLabels.ASSET_BUILDING)
    @Restriction(hql = "assetRecord.building is not null")
    ASSET_BUILDING(new AssetLayer() {

        @Override
        public String getId() {
            return "asset-building";
        }

        @Override
        public String getLabel() {
            return "Building";
        }

        public String getDescription() {
            return "Displays all buildings of the topology";
        }

        @Override
        public ItemProvider<String> getItemProvider() {
            return node -> node.getAssetRecord().getBuilding();
        }
    }),

    @Key(NodeParamLabels.ASSET_REGION)
    @Restriction(hql = "assetRecord.region is not null")
    ASSET_REGION(new AssetLayer() {

        @Override
        public String getId() {
            return "asset-region";
        }

        @Override
        public String getLabel() {
            return "Region";
        }

        public String getDescription() {
            return "Displays all regions of the topology";
        }

        @Override
        public ItemProvider<String> getItemProvider() {
            return node -> node.getAssetRecord().getRegion();
        }
    }),

    @Key(NodeParamLabels.ASSET_DIVISION)
    @Restriction(hql = "assetRecord.division is not null")
    ASSET_DIVISION(new AssetLayer() {

        @Override
        public String getId() {
            return "asset-division";
        }

        @Override
        public String getLabel() {
            return "Division";
        }

        public String getDescription() {
            return "Displays all divisions of the topology";
        }

        @Override
        public ItemProvider<String> getItemProvider() {
            return node -> node.getAssetRecord().getDivision();
        }
    }),

    @Key(NodeParamLabels.ASSET_DEPARTMENT)
    @Restriction(hql = "assetRecord.department is not null")
    ASSET_DEPARTMENT(new AssetLayer() {

        @Override
        public String getId() {
            return "asset-department";
        }

        @Override
        public String getLabel() {
            return "Department";
        }

        public String getDescription() {
            return "Displays all departments of the topology";
        }

        @Override
        public ItemProvider<String> getItemProvider() {
            return node -> node.getAssetRecord().getDepartment();
        }
    }),

    @Key(NodeParamLabels.ASSET_FLOOR)
    @Restriction(hql = "assetRecord.floor is not null")
    ASSET_FLOOR(new AssetLayer() {

        @Override
        public String getId() {
            return "asset-floor";
        }

        @Override
        public String getLabel() {
            return "Floor";
        }

        public String getDescription() {
            return "Displays all floors of the topology";
        }

        @Override
        public ItemProvider<String> getItemProvider() {
            return node -> node.getAssetRecord().getFloor();
        }
    }),

    @Key(NodeParamLabels.ASSET_ROOM)
    @Restriction(hql = "assetRecord.room is not null")
    ASSET_ROOM(new AssetLayer() {

        @Override
        public String getId() {
            return "asset-room";
        }

        @Override
        public String getLabel() {
            return "Room";
        }

        public String getDescription() {
            return "Displays all rooms of the topology";
        }

        @Override
        public ItemProvider<String> getItemProvider() {
            return node -> node.getAssetRecord().getRoom();
        }
    }),

    @Key(NodeParamLabels.ASSET_RACK)
    @Restriction(hql = "assetRecord.rack is not null")
    ASSET_RACK(new AssetLayer() {
        @Override
        public String getId() {
            return "asset-rack";
        }

        @Override
        public String getLabel() {
            return "Rack";
        }

        public String getDescription() {
            return "Displays all racks of the topology";
        }

        @Override
        public ItemProvider<String> getItemProvider() {
            return node -> node.getAssetRecord().getRack();
        }
    }),

    @Key(NodeParamLabels.ASSET_SLOT)
    @Restriction(hql = "assetRecord.slot is not null")
    ASSET_SLOT(new AssetLayer() {
        @Override
        public String getId() {
            return "asset-slot";
        }

        @Override
        public String getLabel() {
            return "Slot";
        }

        public String getDescription() {
            return "Displays all slots of the topology";
        }

        @Override
        public ItemProvider<String> getItemProvider() {
            return node -> node.getAssetRecord().getSlot();
        }
    }),

    @Key(NodeParamLabels.ASSET_PORT)
    @Restriction(hql = "assetRecord.port is not null")
    ASSET_PORT(new AssetLayer() {
        @Override
        public String getId() {
            return "asset-port";
        }

        @Override
        public String getLabel() {
            return "Port";
        }

        public String getDescription() {
            return "Displays all ports of the topology";
        }

        @Override
        public ItemProvider<String> getItemProvider() {
            return node -> node.getAssetRecord().getPort();
        }
    }),

    @Key(NodeParamLabels.ASSET_CIRCUITID)
    @Restriction(hql = "assetRecord.circuitId is not null")
    ASSET_CIRCUIT_ID(new AssetLayer() {
        @Override
        public String getId() {
            return "asset-circuitid";
        }

        @Override
        public String getLabel() {
            return "Circuit ID";
        }

        public String getDescription() {
            return "Displays all circuit ids of the topology";
        }

        @Override
        public ItemProvider<String> getItemProvider() {
            return node -> node.getAssetRecord().getCircuitId();
        }
    }),

    @Key(NodeParamLabels.ASSET_CATEGORY)
    @Restriction(hql = "assetRecord.category is not null")
    ASSET_CATEGORY(new AssetLayer() {
        @Override
        public String getId() {
            return "asset-category";
        }

        @Override
        public String getLabel() {
            return "Category";
        }

        public String getDescription() {
            return "Displays all categories of the topology";
        }

        @Override
        public ItemProvider<String> getItemProvider() {
            return node -> node.getAssetRecord().getCategory();
        }
    }),

    @Key(NodeParamLabels.ASSET_DISPLAYCATEGORY)
    @Restriction(hql = "assetRecord.displayCategory is not null")
    ASSET_DISPLAY_CATEGORY(new AssetLayer() {
        @Override
        public String getId() {
            return "asset-displaycategory";
        }

        @Override
        public String getLabel() {
            return "Display Category";
        }

        public String getDescription() {
            return "Displays all display categories of the topology";
        }

        @Override
        public ItemProvider<String> getItemProvider() {
            return node -> node.getAssetRecord().getDisplayCategory();
        }
    }),

    @Key(NodeParamLabels.ASSET_NOTIFYCATEGORY)
    @Restriction(hql = "assetRecord.notifyCategory is not null")
    ASSET_NOTIFY_CATEGORY(new AssetLayer() {
        @Override
        public String getId() {
            return "asset-notifycategory";
        }

        @Override
        public String getLabel() {
            return "Notify Category";
        }

        public String getDescription() {
            return "Displays all notify categories of the topology";
        }

        @Override
        public ItemProvider<String> getItemProvider() {
            return node -> node.getAssetRecord().getNotifyCategory();
        }
    }),

    @Key(NodeParamLabels.ASSET_POLLERCATEGORY)
    @Restriction(hql = "assetRecord.pollerCategory is not null")
    ASSET_POLLER_CATEGORY(new AssetLayer() {
        @Override
        public String getId() {
            return "asset-pollercategory";
        }

        @Override
        public String getLabel() {
            return "Poller Category";
        }

        public String getDescription() {
            return "Displays all poller categories of the topology";
        }

        @Override
        public ItemProvider<String> getItemProvider() {
            return node -> node.getAssetRecord().getPollerCategory();
        }
    }),

    @Key(NodeParamLabels.ASSET_THRESHOLDCATEGORY)
    @Restriction(hql = "assetRecord.thresholdCategory is not null")
    ASSET_THRESHOLD_CATEGORY(new AssetLayer() {
        @Override
        public String getId() {
            return "asset-thresholdcategory";
        }

        @Override
        public String getLabel() {
            return "Threshold Category";
        }

        public String getDescription() {
            return "Displays all threshold categories of the topology";
        }

        @Override
        public ItemProvider<String> getItemProvider() {
            return node -> node.getAssetRecord().getThresholdCategory();
        }
    }),

    @Key(NodeParamLabels.ASSET_MANAGEDOBJECTTYPE)
    @Restriction(hql = "assetRecord.managedObjectType is not null")
    ASSET_MANAGED_OBJECT_TYPE(new AssetLayer() {
        @Override
        public String getId() {
            return "asset-managedobjecttype";
        }

        @Override
        public String getLabel() {
            return "Managed Object Type";
        }

        public String getDescription() {
            return "Displays all managed object types of the topology";
        }

        @Override
        public ItemProvider<String> getItemProvider() {
            return node -> node.getAssetRecord().getManagedObjectType();
        }
    }),

    @Key(NodeParamLabels.ASSET_MANAGEDOBJECTINSTANCE)
    @Restriction(hql = "assetRecord.managedObjectInstance is not null")
    ASSET_MANAGED_OBJECT_INSTANCE(new AssetLayer() {
        @Override
        public String getId() {
            return "asset-managedobjectinstance";
        }

        @Override
        public String getLabel() {
            return "Managed Object Instance";
        }

        public String getDescription() {
            return "Displays all managed object instances of the topology";
        }

        @Override
        public ItemProvider<String> getItemProvider() {
            return node -> node.getAssetRecord().getManagedObjectInstance();
        }
    }),

    @Key(NodeParamLabels.ASSET_MANUFACTURER)
    @Restriction(hql = "assetRecord.manufacturer is not null")
    ASSET_MANUFACTURER(new AssetLayer() {
        @Override
        public String getId() {
            return "asset-manufacturer";
        }

        @Override
        public String getLabel() {
            return "Manufacturer";
        }

        public String getDescription() {
            return "Displays all manufacturers of the topology";
        }

        @Override
        public ItemProvider<String> getItemProvider() {
            return node -> node.getAssetRecord().getManufacturer();
        }
    }),

    @Key(NodeParamLabels.ASSET_VENDOR)
    @Restriction(hql = "assetRecord.vendor is not null")
    ASSET_VENDOR(new AssetLayer() {
        @Override
        public String getId() {
            return "asset-vendor";
        }

        @Override
        public String getLabel() {
            return "Vendor";
        }

        public String getDescription() {
            return "Displays all vendors of the topology";
        }

        @Override
        public ItemProvider<String> getItemProvider() {
            return node -> node.getAssetRecord().getVendor();
        }
    }),

    @Key(NodeParamLabels.ASSET_MODELNUMBER)
    @Restriction(hql = "assetRecord.modelNumber is not null")
    ASSET_MODEL_NUMBER(new AssetLayer() {
        @Override
        public String getId() {
            return "asset-modelnumber";
        }

        @Override
        public String getLabel() {
            return "Model Number";
        }

        public String getDescription() {
            return "Displays all model numbers of the topology";
        }

        @Override
        public ItemProvider<String> getItemProvider() {
            return node -> node.getAssetRecord().getModelNumber();
        }
    }),

    @Key(NodeParamLabels.ASSET_DESCRIPTION)
    @Restriction(hql = "assetRecord.description is not null")
    ASSET_DESCRIPTION(new AssetLayer() {
        @Override
        public String getId() {
            return "asset-description";
        }

        @Override
        public String getLabel() {
            return "Description";
        }

        public String getDescription() {
            return "Displays all descriptions of the topology";
        }

        @Override
        public ItemProvider<String> getItemProvider() {
            return node -> node.getAssetRecord().getDescription();
        }
    }),

    @Key(NodeParamLabels.ASSET_OPERATINGSYSTEM)
    @Restriction(hql = "assetRecord.operatingSystem is not null")
    ASSET_OPERATING_SYSTEM(new AssetLayer() {
        @Override
        public String getId() {
            return "asset-operatingsystem";
        }

        @Override
        public String getLabel() {
            return "Operating System";
        }

        public String getDescription() {
            return "Displays all operating systems of the topology";
        }

        @Override
        public ItemProvider<String> getItemProvider() {
            return node -> node.getAssetRecord().getOperatingSystem();
        }
    }),

    @Key(NodeParamLabels.NODE_NODELABEL)
    @Restriction(hql = "label is not null")
    NODE_LABEL(new AssetLayer() {
        @Override
        public String getId() {
            return "node-nodelabel";
        }

        @Override
        public String getLabel() {
            return "Node Label";
        }

        public String getDescription() {
            return "Displays all node labels of the topology";
        }

        @Override
        public ItemProvider<String> getItemProvider() {
            return OnmsNode::getLabel;
        }
    }),

    @Key(NodeParamLabels.NODE_NODEID)
    @Restriction(hql = "id is not null")
    NODE_ID(new AssetLayer() {
        @Override
        public String getId() {
            return "node-id";
        }

        @Override
        public String getLabel() {
            return "Node ID";
        }

        public String getDescription() {
            return "Displays all node ids of the topology";
        }

        @Override
        public ItemProvider<String> getItemProvider() {
            return OnmsNode::getNodeId;
        }
    }),

    @Key(NodeParamLabels.NODE_FOREIGNSOURCE)
    @Restriction(hql = "foreignSource is not null")
    NODE_FOREIGN_SOURCE(new AssetLayer() {
        @Override
        public String getId() {
            return "node-foreignsource";
        }

        @Override
        public String getLabel() {
            return "Node Foreign Source";
        }

        public String getDescription() {
            return "Displays all node foreign sources of the topology";
        }

        @Override
        public ItemProvider<String> getItemProvider() {
            return OnmsNode::getForeignSource;
        }
    }),

    @Key(NodeParamLabels.NODE_FOREIGNID)
    @Restriction(hql = "foreignId is not null")
    NODE_FOREIGN_ID(new AssetLayer() {
        @Override
        public String getId() {
            return "node-foreignid";
        }

        @Override
        public String getLabel() {
            return "Node Foreign ID";
        }

        public String getDescription() {
            return "Displays all node foreign id of the topology";
        }

        @Override
        public ItemProvider<String> getItemProvider() {
            return OnmsNode::getForeignId;
        }
    }),

    @Key(NodeParamLabels.NODE_NODESYSNAME)
    @Restriction(hql = "sysName is not null")
    NODE_SYSTEM_NAME(new AssetLayer() {
        @Override
        public String getId() {
            return "node-nodesysname";
        }

        @Override
        public String getLabel() {
            return "Node System Name";
        }

        public String getDescription() {
            return "Displays all node system names of the topology";
        }

        @Override
        public ItemProvider<String> getItemProvider() {
            return OnmsNode::getSysName;
        }
    }),

    @Key(NodeParamLabels.NODE_NODESYSLOCATION)
    @Restriction(hql = "sysLocation is not null")
    NODE_SYSTEM_LOCATION(new AssetLayer() {
        @Override
        public String getId() {
            return "node-nodesyslocation";
        }

        @Override
        public String getLabel() {
            return "Node System Location";
        }

        public String getDescription() {
            return "Displays all node system locations id of the topology";
        }

        @Override
        public ItemProvider<String> getItemProvider() {
            return OnmsNode::getSysLocation;
        }
    }),

    @Key(NodeParamLabels.NODE_OPERATINGSYSTEM)
    @Restriction(hql = "operatingSystem is not null")
    NODE_OPERATING_SYSTEM(new AssetLayer() {
        @Override
        public String getId() {
            return "node-operatingsystem";
        }

        @Override
        public String getLabel() {
            return "Node Operating System";
        }

        public String getDescription() {
            return "Displays all node operating systems of the topology";
        }

        @Override
        public ItemProvider<String> getItemProvider() {
            return OnmsNode::getOperatingSystem;
        }
    }),

    @Key(NodeParamLabels.PARENT_NODELABEL)
    @Restriction(hql = "parent.label is not null")
    PARENT_NODE_LABEL(new AssetLayer() {
        @Override
        public String getId() {
            return "parent-nodelabel";
        }

        @Override
        public String getLabel() {
            return "Parent Node Label";
        }

        public String getDescription() {
            return "Displays all parent node labels of the topology";
        }

        @Override
        public ItemProvider<String> getItemProvider() {
            return node -> node.getParent().getLabel();
        }
    }),

    @Key(NodeParamLabels.PARENT_NODEID)
    @Restriction(hql = "parent.id is not null")
    PARENT_NODE_ID(new AssetLayer() {
        @Override
        public String getId() {
            return "parent-nodeid";
        }

        @Override
        public String getLabel() {
            return "Parent Node ID";
        }

        public String getDescription() {
            return "Displays all parent node ids of the topology";
        }

        @Override
        public ItemProvider<String> getItemProvider() {
            return node -> node.getParent().getNodeId();
        }
    }),

    @Key(NodeParamLabels.PARENT_FOREIGNSOURCE)
    @Restriction(hql = "parent.foreignSource is not null")
    PARENT_FOREIGN_SOURCE(new AssetLayer() {
        @Override
        public String getId() {
            return "parent-foreignsource";
        }

        @Override
        public String getLabel() {
            return "Parent Foreign Source";
        }

        public String getDescription() {
            return "Displays all parent foreign sources of the topology";
        }

        @Override
        public ItemProvider<String> getItemProvider() {
            return node -> node.getParent().getForeignSource();
        }
    }),

    @Key(NodeParamLabels.PARENT_FOREIGNID)
    @Restriction(hql = "parent.foreignId is not null")
    PARENT_FOREIGN_ID(new AssetLayer() {
        @Override
        public String getId() {
            return "parent-foreignid";
        }

        @Override
        public String getLabel() {
            return "Parent Foreign ID";
        }

        public String getDescription() {
            return "Displays all parent foreign ids of the topology";
        }

        @Override
        public ItemProvider<String> getItemProvider() {
            return node -> node.getParent().getForeignId();
        }
    }),

    @Key(NodeParamLabels.NODE_CATEGORIES)
    @Restriction(hql = "categories is not empty")
    NODE_CATEGORIES(new AssetLayer() {
        @Override
        public String getId() {
            return "node-categories";
        }

        @Override
        public String getLabel() {
            return "Node Categories";
        }

        public String getDescription() {
            return "Displays all node categories of the topology";
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

    private final Layer layer;

    Layers(Layer layer) {
        this.layer = layer;
    }

    public Layer getLayer() {
        return layer;
    }
}
