/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.timeseries.integration.persistence;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;

import org.opennms.netmgt.collection.api.CollectionResource;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.SessionUtils;
import org.opennms.netmgt.model.OnmsAssetRecord;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.ResourceTypeUtils;
import org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation;
import org.opennms.netmgt.timeseries.integration.MetaTagConfiguration;
import org.opennms.netmgt.timeseries.integration.MetaTagConfiguration.AssetTagKey;
import org.opennms.netmgt.timeseries.integration.MetaTagConfiguration.MetaTagKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.common.cache.CacheLoader;
import com.google.common.collect.Maps;

public class MetaTagDataLoader extends CacheLoader<CollectionResource, Map<String, String>> {

    private static final Logger LOG = LoggerFactory.getLogger(MetaTagDataLoader.class);

    private final NodeDao nodeDao;
    private final SessionUtils sessionUtils;
    private MetaTagConfiguration config;

    @Inject
    public MetaTagDataLoader(final NodeDao nodeDao, final SessionUtils sessionUtils) {
        this.nodeDao = nodeDao;
        this.sessionUtils = sessionUtils;
        setConfig(new MetaTagConfiguration(Maps.fromProperties(System.getProperties())));
    }

    public void setConfig(final MetaTagConfiguration config) {
        this.config = config;
    }

    public Map<String, String> load(final CollectionResource resource) {
        return sessionUtils.withReadOnlyTransaction(() -> {
            final Map<String, String> tags = new HashMap<>();
            addTag(tags, MetaTagKey.resourceLabel, Optional.ofNullable(resource.getInterfaceLabel()).orElse(null));

            if (resource.getResourceTypeName().equals(CollectionResource.RESOURCE_TYPE_NODE)) {
                String nodeCriteria = getNodeCriteriaFromResource(resource);
                mapNode(tags, nodeCriteria);
            } else if (resource.getResourceTypeName().equals(CollectionResource.RESOURCE_TYPE_IF)) {
                String nodeCriteria = getNodeCriteriaFromResource(resource);
                if (!Strings.isNullOrEmpty(nodeCriteria)) {
                    mapNode(tags, nodeCriteria);
                }
                addTag(tags, MetaTagKey.ifDescr, resource.getInterfaceLabel()); // TODO: Patrick: is that correct?
                // addTag(tags, MetaTagKey.ifAlias, ); // TODO: Patrick: from where do we get that attribute?
            } else if (resource.getResourceTypeName().equals(CollectionResource.RESOURCE_TYPE_LATENCY)) {
                mapResponseTimeResource(resource, tags);
            } else {
                String nodeCriteria = getNodeCriteriaFromResource(resource);
                if (!Strings.isNullOrEmpty(nodeCriteria)) {
                    mapNode(tags, nodeCriteria);
                }
            }

            return tags;
        });
    }

    private void mapNode(final Map<String, String> tags, final String nodeCriteria) {
        Optional<OnmsNode> node = getNode(nodeCriteria);
        addTag(tags, MetaTagKey.nodeLabel, node.map(OnmsNode::getLabel).orElse(null));
        addTag(tags, MetaTagKey.location, node.map(OnmsNode::getLocation).map(OnmsMonitoringLocation::getLocationName).orElse(null));
        addTag(tags, MetaTagKey.sysObjectID, node.map(OnmsNode::getSysObjectId).orElse(null));
        addTag(tags, MetaTagKey.foreignSource, node.map(OnmsNode::getForeignSource).orElse(null));
        addTag(tags, MetaTagKey.foreignId, node.map(OnmsNode::getForeignId).orElse(null));
        addTag(tags, MetaTagKey.nodeCriteria, nodeCriteria);

        // categories
        if(node.isPresent()) {
            AtomicInteger n = new AtomicInteger(1);
            // Tags are only unique by key and value
            node.get().getCategories().stream()
                    .map(OnmsCategory::getName)
                    .filter(config::isCategoryEnabled)
                    // TODO: Patrick: is this acceptable? Or would we rather create Tags instead of a HashMap?
                    .forEach(catName -> tags.put("category_" + n.getAndIncrement(), catName));
        }

        // assets
        if(node.map(OnmsNode::getAssetRecord).isPresent()) {
            mapAssets(tags, node.get().getAssetRecord());
        }

        // Meta-Data DSL
        // TODO Patrick: how will that work?
    }

    private void mapAssets(final Map<String, String> tags, final OnmsAssetRecord assets) {
        Objects.requireNonNull(assets);

        addTag(tags, AssetTagKey.id, Optional.ofNullable(assets.getId()).map(Object::toString).orElse(null));
        addTag(tags, AssetTagKey.category, assets.getCategory());
        addTag(tags, AssetTagKey.manufacturer, assets.getManufacturer());
        addTag(tags, AssetTagKey.vendor, assets.getVendor());
        addTag(tags, AssetTagKey.modelNumber, assets.getModelNumber());
        addTag(tags, AssetTagKey.serialNumber, assets.getSerialNumber());
        addTag(tags, AssetTagKey.description, assets.getDescription());
        addTag(tags, AssetTagKey.circuitId, assets.getCircuitId());
        addTag(tags, AssetTagKey.assetNumber, assets.getAssetNumber());
        addTag(tags, AssetTagKey.operatingSystem, assets.getOperatingSystem());
        addTag(tags, AssetTagKey.rack, assets.getRack());
        addTag(tags, AssetTagKey.slot, assets.getSlot());
        addTag(tags, AssetTagKey.port, assets.getPort());
        addTag(tags, AssetTagKey.region, assets.getRegion());
        addTag(tags, AssetTagKey.division, assets.getDivision());
        addTag(tags, AssetTagKey.department, assets.getDepartment());
        addTag(tags, AssetTagKey.building, assets.getBuilding());
        addTag(tags, AssetTagKey.floor, assets.getFloor());
        addTag(tags, AssetTagKey.room, assets.getRoom());
        addTag(tags, AssetTagKey.vendorPhone, assets.getVendorPhone());
        addTag(tags, AssetTagKey.vendorFax, assets.getVendorFax());
        addTag(tags, AssetTagKey.vendorAssetNumber, assets.getVendorAssetNumber());
        addTag(tags, AssetTagKey.username, assets.getUsername());
        addTag(tags, AssetTagKey.password, assets.getPassword());
        addTag(tags, AssetTagKey.enable, assets.getEnable());
        addTag(tags, AssetTagKey.connection, assets.getConnection());
        addTag(tags, AssetTagKey.autoenable, assets.getAutoenable());
        addTag(tags, AssetTagKey.lastModifiedBy, assets.getLastModifiedBy());
        addTag(tags, AssetTagKey.lastModifiedDate, Optional.ofNullable(assets.getLastModifiedDate()).map(Date::toString).orElse(null));
        addTag(tags, AssetTagKey.dateInstalled, assets.getDateInstalled());
        addTag(tags, AssetTagKey.lease, assets.getLease());
        addTag(tags, AssetTagKey.leaseExpires, assets.getLeaseExpires());
        addTag(tags, AssetTagKey.supportPhone, assets.getSupportPhone());
        addTag(tags, AssetTagKey.maintcontract, assets.getMaintcontract());
        addTag(tags, AssetTagKey.maintContractExpiration, assets.getMaintContractExpiration());
        addTag(tags, AssetTagKey.displayCategory, assets.getDisplayCategory());
        addTag(tags, AssetTagKey.notifyCategory, assets.getNotifyCategory());
        addTag(tags, AssetTagKey.pollerCategory, assets.getPollerCategory());
        addTag(tags, AssetTagKey.thresholdCategory, assets.getThresholdCategory());
        addTag(tags, AssetTagKey.comment, assets.getComment());
        addTag(tags, AssetTagKey.cpu, assets.getCpu());
        addTag(tags, AssetTagKey.ram, assets.getRam());
        addTag(tags, AssetTagKey.storagectrl, assets.getStoragectrl());
        addTag(tags, AssetTagKey.hdd1, assets.getHdd1());
        addTag(tags, AssetTagKey.hdd2, assets.getHdd2());
        addTag(tags, AssetTagKey.hdd3, assets.getHdd3());
        addTag(tags, AssetTagKey.hdd4, assets.getHdd4());
        addTag(tags, AssetTagKey.hdd5, assets.getHdd5());
        addTag(tags, AssetTagKey.hdd6, assets.getHdd6());
        addTag(tags, AssetTagKey.numpowersupplies, assets.getNumpowersupplies());
        addTag(tags, AssetTagKey.inputpower, assets.getInputpower());
        addTag(tags, AssetTagKey.additionalhardware, assets.getAdditionalhardware());
        addTag(tags, AssetTagKey.admin, assets.getAdmin());
        addTag(tags, AssetTagKey.snmpcommunity, assets.getSnmpcommunity());
        addTag(tags, AssetTagKey.rackunitheight, assets.getRackunitheight());
        addTag(tags, AssetTagKey.managedObjectType, assets.getManagedObjectType());
        addTag(tags, AssetTagKey.managedObjectInstance, assets.getManagedObjectInstance());
        addTag(tags, AssetTagKey.geolocation, Optional.ofNullable(assets.getGeolocation()).map(Object::toString).orElse(null));
        addTag(tags, AssetTagKey.vmwareManagedObjectId, assets.getVmwareManagedObjectId());
        addTag(tags, AssetTagKey.vmwareManagedEntityType, assets.getVmwareManagedEntityType());
        addTag(tags, AssetTagKey.vmwareManagementServer, assets.getVmwareManagementServer());
        addTag(tags, AssetTagKey.vmwareTopologyInfo, assets.getVmwareTopologyInfo());
        addTag(tags, AssetTagKey.vmwareState, assets.getVmwareState());
    }

    private void addTag(final Map<String, String> tags, final MetaTagKey key, final String value) {
        Optional.ofNullable(value)
                .filter((v) -> this.config.isEnabled(key))
                .ifPresent((v) -> tags.put(key.name(), v));
    }

    private void addTag(final Map<String, String> tags, final AssetTagKey key, final String value){
        Optional.ofNullable(value)
                .filter((v) -> this.config.isEnabled(key))
                .ifPresent((v) -> tags.put(key.name(), v));
    }

    private Optional<OnmsNode> getNode(String nodeCriteria) {
            try {
                return Optional.ofNullable(nodeDao.get(nodeCriteria));
            } catch (Exception e) {
                LOG.error("error while trying to match node from {}", nodeCriteria);
            }
            return Optional.empty();
    }

    private String getNodeCriteriaFromResource(CollectionResource resource) {

        String nodeCriteria = null;
        if (resource.getParent() != null) {
            String[] resourcePathArray = resource.getParent().elements();
            if (ResourceTypeUtils.FOREIGN_SOURCE_DIRECTORY.equals(resourcePathArray[0])
                    && resourcePathArray.length == 3) {
                // parent denotes nodeCriteria, form fs:fid
                nodeCriteria = resourcePathArray[1] + ":" + resourcePathArray[2];
            } else if (checkNumeric(resourcePathArray[0])) {
                // parent denotes nodeId
                nodeCriteria = resourcePathArray[0];
            }
        }
        return nodeCriteria;
    }

    private boolean checkNumeric(String nodeCriteria) {
        try {
            Integer.parseInt(nodeCriteria);
            return true;
        } catch (NumberFormatException e) {
            // not a number
            return false;
        }
    }

    private void mapResponseTimeResource(final CollectionResource resource, final Map<String, String> tags) {
        boolean validIp = false;
        // Check if resource parent is an IpAddress.
        if (resource.getParent() != null && resource.getParent().elements().length == 1) {
            String[] resourcePathArray = resource.getParent().elements();
            validIp = checkForValidIpAddress(resourcePathArray[0]);
        }
        if (resource.getPath() != null && validIp) {
            // extract path which consists of location and IpAddress.
            String[] resourcePathArray = resource.getPath().elements();

            if (resourcePathArray.length == 2) {
                // first element is location, 2nd IpAddress.
                tags.put(MetaTagKey.location.name(), resourcePathArray[0]);
                tags.put(MetaTagKey.ipAddress.name(), resourcePathArray[1]);
            } else if (resourcePathArray.length == 1) {
                tags.put(MetaTagKey.ipAddress.name(), resourcePathArray[0]);
            }
        }

        // TODO: Patrick: from where do we get the service?
        // tags.put(MetaTagKey.service.name(), );
    }

    private boolean checkForValidIpAddress(String resourcePath) {
        try {
            InetAddress.getByName(resourcePath);
            return true;
        } catch (UnknownHostException e) {
            // not an ipaddress.
            return false;
        }
    }
}

