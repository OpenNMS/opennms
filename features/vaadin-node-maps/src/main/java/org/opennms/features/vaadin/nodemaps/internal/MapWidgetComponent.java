/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2013 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.vaadin.nodemaps.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.features.geocoder.Coordinates;
import org.opennms.features.geocoder.GeocoderException;
import org.opennms.features.geocoder.GeocoderService;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.ui.VMapWidget;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.dao.api.AssetRecordDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsAssetRecord;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsGeolocation;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSeverity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionOperations;

import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.ui.ClientWidget;
import com.vaadin.ui.VerticalLayout;

@ClientWidget(value = VMapWidget.class)
public class MapWidgetComponent extends VerticalLayout {
    private static final String[] EMPTY_STRING_ARRAY = new String[]{};

    public static final class NodeEntry {

        private Float m_longitude;
        private Float m_latitude;
        private Integer m_nodeId;
        private String m_nodeLabel;
        private String m_foreignSource;
        private String m_foreignId;
        private String m_description;
        private String m_maintcontract;
        private String m_ipAddress;
        private OnmsSeverity m_severity = OnmsSeverity.NORMAL;
        private List<String> m_categories = new ArrayList<String>();
        private int m_unackedCount = 0;

        public NodeEntry(final OnmsNode node) {
            final OnmsAssetRecord assetRecord = node.getAssetRecord();
            if (assetRecord != null && assetRecord.getGeolocation() != null) {
                final OnmsGeolocation geolocation = assetRecord.getGeolocation();
                m_longitude = geolocation.getLongitude();
                m_latitude  = geolocation.getLatitude();
            }

            m_nodeId        = node.getId();
            m_nodeLabel     = node.getLabel();
            m_foreignSource = node.getForeignSource();
            m_foreignId     = node.getForeignId();
            
            if (assetRecord != null) {
                m_maintcontract = assetRecord.getMaintcontract();
                m_description   = assetRecord.getDescription();
            }

            if (node.getPrimaryInterface() != null) {
                m_ipAddress = InetAddressUtils.str(node.getPrimaryInterface().getIpAddress());
            }
            
            if (node.getCategories() != null && node.getCategories().size() > 0) {
                for (final OnmsCategory category : node.getCategories()) {
                    m_categories.add(category.getName());
                }
            }
        }

        public void setSeverity(final OnmsSeverity severity) {
            m_severity = severity;
        }

        public void visit(final PaintTarget target) throws PaintException {
            target.startTag("node");

            // longitude/latitude, as floats
            target.addAttribute("longitude", m_longitude);
            target.addAttribute("latitude", m_latitude);

            // everything else gets sent as basic string properties
            if (m_nodeId        != null) target.addAttribute("nodeId", m_nodeId.toString());
            if (m_nodeLabel     != null) target.addAttribute("nodeLabel", m_nodeLabel);
            if (m_foreignSource != null) target.addAttribute("foreignSource", m_foreignSource);
            if (m_foreignId     != null) target.addAttribute("foreignId", m_foreignId);
            if (m_ipAddress     != null) target.addAttribute("ipAddress", m_ipAddress);
            if (m_description   != null) target.addAttribute("description", m_description);
            if (m_maintcontract != null) target.addAttribute("maintcontract", m_maintcontract);

            // alarm data
            target.addAttribute("severityLabel", m_severity.getLabel());
            target.addAttribute("severity", m_severity.getId());
            target.addAttribute("unackedCount", String.valueOf(m_unackedCount));

            if (m_categories.size() > 0) {
                target.addAttribute("categories", m_categories.toArray(EMPTY_STRING_ARRAY));
            }

            target.endTag("node");
        }

        public void setUnackedCount(final int unackedCount) {
            m_unackedCount = unackedCount;
        }
    }

    private static final long serialVersionUID = 2L;

    private NodeDao m_nodeDao;
    private AssetRecordDao m_assetDao;
    private AlarmDao m_alarmDao;

    private GeocoderService m_geocoderService;

    private Logger m_log = LoggerFactory.getLogger(getClass());

    private TransactionOperations m_transactionOperations;

    private String m_searchString;

    public MapWidgetComponent() {
    }

    public MapWidgetComponent(final NodeDao nodeDao, final AssetRecordDao assetDao, final AlarmDao alarmDao, final GeocoderService geocoder) {
        m_nodeDao = nodeDao;
        m_assetDao = assetDao;
        m_alarmDao = alarmDao;
        m_geocoderService = geocoder;
    }

    @Override
    public void paintContent(final PaintTarget target) throws PaintException {
        super.paintContent(target);

        if (m_nodeDao == null) return;

        m_log.debug("getting nodes");
        final CriteriaBuilder cb = new CriteriaBuilder(OnmsNode.class);
        cb.alias("assetRecord", "asset");
        cb.orderBy("id").asc();

        final Map<Integer,NodeEntry> nodes = new HashMap<Integer,NodeEntry>();
        final List<OnmsAssetRecord> updatedAssets = new ArrayList<OnmsAssetRecord>();

        for (final OnmsNode node : m_nodeDao.findMatching(cb.toCriteria())) {
            m_log.trace("processing node {}", node.getId());

            final OnmsAssetRecord assets = node.getAssetRecord();
            if (assets != null && assets.getGeolocation() != null) {
                final OnmsGeolocation geolocation = assets.getGeolocation();
                final String addressString = geolocation.asAddressString();

                final Float longitude = geolocation.getLongitude();
                final Float latitude = geolocation.getLatitude();

                if (longitude != null && latitude != null) {
                    if (longitude == Float.NEGATIVE_INFINITY || latitude == Float.NEGATIVE_INFINITY) {
                        // we've already cached it as bad, skip it
                        continue;
                    } else {
                        // we've already got good coordinates, return the node
                        nodes.put(node.getId(), new NodeEntry(node));
                        continue;
                    }
                } else if (addressString == null || "".equals(addressString)) {
                    // no real address info, skip it
                    continue;
                } else {
                    m_log.debug("Node {} has an asset record with address \"{}\", but no coordinates.", new Object[] { node.getId(), addressString });
                    final Coordinates coordinates = getCoordinates(addressString);
                    geolocation.setLongitude(coordinates.getLongitude());
                    geolocation.setLatitude(coordinates.getLatitude());
                    updatedAssets.add(assets);

                    if (coordinates.getLongitude() == Float.NEGATIVE_INFINITY || coordinates.getLatitude() == Float.NEGATIVE_INFINITY) {
                        // we got bad coordinates
                        m_log.debug("Node {} has an asset record with address, but we were unable to find valid coordinates.", node.getId());
                        continue;
                    } else {
                        // valid coordinates, add to the list
                        nodes.put(node.getId(), new NodeEntry(node));
                    }
                }
            } else {
                // no asset information
            }
        }

        int lastId = -1;
        int unackedCount = 0;

        if (!nodes.isEmpty()) {
            m_log.debug("getting alarms for nodes");
            final CriteriaBuilder ab = new CriteriaBuilder(OnmsAlarm.class);
            ab.alias("node", "node");
            ab.ge("severity", OnmsSeverity.WARNING);
            ab.in("node.id", nodes.keySet());
            ab.orderBy("node.id").asc();
            ab.orderBy("severity").desc();
    
            for (final OnmsAlarm alarm : m_alarmDao.findMatching(ab.toCriteria())) {
                final int nodeId = alarm.getNodeId();
                m_log.debug("nodeId = {}, lastId = {}, unackedCount = {}", new Object[] { nodeId, lastId, unackedCount });
                if (nodeId != lastId) {
                    m_log.debug("  setting severity for node {} to {}", new Object[] { nodeId, alarm.getSeverity().getLabel() });
                    nodes.get(nodeId).setSeverity(alarm.getSeverity());
                    if (lastId != -1) {
                        nodes.get(nodeId).setUnackedCount(unackedCount);
                        unackedCount = 0;
                    }
                }
                if (alarm.getAckUser() == null) {
                    unackedCount++;
                }
    
                lastId = nodeId;
            }
        }

        if (lastId != -1) {
            nodes.get(lastId).setUnackedCount(unackedCount);
        }

        m_log.debug("pushing nodes to the UI");
        if (m_searchString != null) target.addAttribute("initialSearchString", m_searchString);

        target.startTag("nodes");
        for (final NodeEntry node : nodes.values()) {
            node.visit(target);
        }
        target.endTag("nodes");

        m_log.debug("saving {} updated asset records to the database", updatedAssets.size());
        m_transactionOperations.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(final TransactionStatus status) {
                for (final OnmsAssetRecord asset : updatedAssets) {
                    m_assetDao.saveOrUpdate(asset);
                }
            }
        });
    }

    /**
     * Given an address, return the coordinates for that address.
     * @param address the complete address, in a format a geolocator can understand
     * @return the coordinates for the given address
     */
    private Coordinates getCoordinates(final String address) {
        Coordinates coordinates = null;
        try {
            coordinates = m_geocoderService.getCoordinates(address);
        } catch (final GeocoderException e) {
            m_log.debug("Failed to find coordinates for address {}", address);
        }
        if (coordinates == null) {
            coordinates = new Coordinates(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY);
        }
        return coordinates;
    }

    public void setNodeDao(final NodeDao nodeDao) {
        m_nodeDao = nodeDao;
    }

    public void setAssetRecordDao(final AssetRecordDao assetDao) {
        m_assetDao = assetDao;
    }

    public void setAlarmDao(final AlarmDao alarmDao) {
        m_alarmDao = alarmDao;
    }

    public void setGeocoderService(final GeocoderService geocoderService) {
        m_geocoderService = geocoderService;
    }

    public void setTransactionOperation(final TransactionOperations tx) {
        m_transactionOperations = tx;
    }

    public void setSearchString(final String searchString) {
        m_searchString = searchString;
    }
}
