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
import org.opennms.core.utils.LogUtils;
import org.opennms.features.geocoder.Coordinates;
import org.opennms.features.geocoder.GeocoderException;
import org.opennms.features.geocoder.GeocoderService;
import org.opennms.features.vaadin.nodemaps.internal.gwt.client.VMapWidget;
import org.opennms.netmgt.dao.AlarmDao;
import org.opennms.netmgt.dao.AssetRecordDao;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsAssetRecord;
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
    public static final class NodeEntry {

        private Float m_longitude;
        private Float m_latitude;
        private Integer m_nodeId;
        private String m_nodeLabel;
        private String m_foreignSource;
        private String m_foreignId;
        private String m_ipAddress;
        private OnmsSeverity m_severity = OnmsSeverity.NORMAL;
        private int m_unackedCount = 0;

        public NodeEntry(final OnmsNode node) {
            final OnmsAssetRecord assetRecord = node.getAssetRecord();
            if (assetRecord != null && assetRecord.getGeolocation() != null) {
                final String coordinateString = assetRecord.getGeolocation().getCoordinates();
                final Coordinates coordinates;
                try {
                    coordinates = new Coordinates(coordinateString);
                    m_longitude = coordinates.getLongitude();
                    m_latitude  = coordinates.getLatitude();
                } catch (final GeocoderException e) {
                    LogUtils.debugf(this, "failed to parse coordinates: %s", coordinateString);
                }
            }

            m_nodeId        = node.getId();
            m_nodeLabel     = node.getLabel();
            m_foreignSource = node.getForeignSource();
            m_foreignId     = node.getForeignId();

            if (node.getPrimaryInterface() != null) {
                m_ipAddress = InetAddressUtils.str(node.getPrimaryInterface().getIpAddress());
            }
        }

        public void setSeverity(final OnmsSeverity severity) {
            m_severity = severity;
        }

        public void visit(final PaintTarget target) throws PaintException {
            target.startTag("node-" + m_nodeId.toString());

            // longitude/latitude, as floats
            target.addAttribute("longitude", m_longitude);
            target.addAttribute("latitude", m_latitude);

            // everything else gets sent as basic string properties
            target.addAttribute("nodeId", m_nodeId.toString());
            target.addAttribute("nodeLabel", m_nodeLabel);
            target.addAttribute("foreignSource", m_foreignSource);
            target.addAttribute("foreignId", m_foreignId);
            target.addAttribute("ipAddress", m_ipAddress);

            // alarm data
            target.addAttribute("severityLabel", m_severity.getLabel());
            target.addAttribute("severity", m_severity.getId());
            target.addAttribute("unackedCount", String.valueOf(m_unackedCount));

            target.endTag("node-" + m_nodeId.toString());
        }

        public void setUnackedCount(final int unackedCount) {
            m_unackedCount = unackedCount;
        }
    }

    private static final String BAD_COORDINATES = Integer.MIN_VALUE + "," + Integer.MIN_VALUE;

    private static final long serialVersionUID = 1L;

    private NodeDao m_nodeDao;
    private AssetRecordDao m_assetDao;
    private AlarmDao m_alarmDao;

    private GeocoderService m_geocoderService;

    private Logger m_log = LoggerFactory.getLogger(getClass());

    private TransactionOperations m_transactionOperations;

    private int singleNodeId = 0;

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

        if (singleNodeId > 0)
            cb.eq("id", singleNodeId);

        final Map<Integer,NodeEntry> nodes = new HashMap<Integer,NodeEntry>();
        final List<OnmsAssetRecord> updatedAssets = new ArrayList<OnmsAssetRecord>();

        for (final OnmsNode node : m_nodeDao.findMatching(cb.toCriteria())) {
            m_log.trace("processing node {}", node.getId());

            final OnmsAssetRecord assets = node.getAssetRecord();
            if (assets != null && assets.getGeolocation() != null) {
                final OnmsGeolocation geolocation = assets.getGeolocation();

                final String addressString = geolocation.asAddressString();
                if (addressString != null && !"".equals(addressString)) {
                    final String coordinateString = geolocation.getCoordinates();
                    if (coordinateString == null || "".equals(coordinateString)) {
                        m_log.debug("Node {} has an asset record with address \"{}\", but no coordinates.", new Object[] { node.getId(), addressString });
                        final String coordinates = getCoordinates(addressString);
                        geolocation.setCoordinates(coordinates);
                        updatedAssets.add(assets);
                    }
                    if (BAD_COORDINATES.equals(geolocation.getCoordinates())) {
                        m_log.debug("Node {} has an asset record with address, but we were unable to find valid coordinates.", node.getId());
                        continue;
                    }

                    nodes.put(node.getId(), new NodeEntry(node));
                }
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
     * @return the coordinates for the given address, in "longitude,latitude" format
     */
    private String getCoordinates(final String address) {
        try {
            final Coordinates coordinates = m_geocoderService.getCoordinates(address);
            return coordinates.getLongitude() + "," + coordinates.getLatitude();
        } catch (final GeocoderException e) {
            m_log.debug("Failed to find coordinates for address {}, returning {}", address, BAD_COORDINATES);
            return BAD_COORDINATES;
        }
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

    public void setSingleNodeId(int nodeId) {
        this.singleNodeId = nodeId;
    }
}
