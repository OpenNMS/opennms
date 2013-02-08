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

package org.opennms.features.vaadin.nodemaps.ui;

import java.util.List;

import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.features.geocoder.Coordinates;
import org.opennms.features.geocoder.GeocoderException;
import org.opennms.features.geocoder.GeocoderService;
import org.opennms.features.vaadin.nodemaps.gwt.client.VOpenlayersWidget;
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

@ClientWidget(value = VOpenlayersWidget.class)
public class OpenlayersWidgetComponent extends VerticalLayout {
    private static final long serialVersionUID = 1L;

    private NodeDao m_nodeDao;
    private AssetRecordDao m_assetDao;
    private AlarmDao m_alarmDao;

    private GeocoderService m_geocoderService;

    private boolean m_enableGeocoding = true;

    private Logger m_log = LoggerFactory.getLogger(getClass());

    private TransactionOperations m_transactionOperations;

    public OpenlayersWidgetComponent() {
    }

    public OpenlayersWidgetComponent(final NodeDao nodeDao, final AssetRecordDao assetDao, final AlarmDao alarmDao, final GeocoderService geocoder) {
        m_nodeDao = nodeDao;
        m_assetDao = assetDao;
        m_alarmDao = alarmDao;
        m_geocoderService = geocoder;
    }

    @Override
    public void paintContent(final PaintTarget target) throws PaintException {
        super.paintContent(target);

        if (m_nodeDao == null) return;

        final CriteriaBuilder cb = new CriteriaBuilder(OnmsNode.class);
        cb.alias("assetRecord", "asset");
        cb.orderBy("id");

        target.startTag("nodes");

        for (final OnmsNode node : m_nodeDao.findMatching(cb.toCriteria())) {
            paintNode(target, node);
        }
        target.endTag("nodes");
    }

    void paintNode(final PaintTarget target, final OnmsNode node) throws PaintException {
        final OnmsAssetRecord assets = node.getAssetRecord();
        if (assets != null && assets.getGeolocation() != null) {
            final OnmsGeolocation geolocation = assets.getGeolocation();

            final String addressString = geolocation.asAddressString();
            String coordinateString = geolocation.getCoordinates();

            if (m_enableGeocoding && (coordinateString == null || coordinateString == "" || !coordinateString.contains(",")) && addressString != "") {
                m_log.debug("No coordinates for node {}, getting geolocation for street address: {}", new Object[] { node.getId(), addressString });
                Coordinates coordinates = null;
                try {
                    coordinates = m_geocoderService.getCoordinates(addressString);
                    if (coordinates == null) {
                        geolocation.setCoordinates("-1,-1");
                        m_log.debug("Failed to look up coordinates for street address: {}", addressString);
                    } else {
                        coordinateString = coordinates.getLatitude() + "," + coordinates.getLongitude();
                        geolocation.setCoordinates(coordinateString);
                    }
                    m_transactionOperations.execute(new TransactionCallbackWithoutResult() {
                        @Override
                        protected void doInTransactionWithoutResult(final TransactionStatus status) {
                            m_assetDao.saveOrUpdate(assets);
                        }
                    });
                } catch (final GeocoderException e) {
                    m_log.debug("Failed to retrieve coordinates", e);
                }
            } else {
                m_log.debug("Found coordinates for node {}, geolocation for street address: {} = {}", new Object[] { node.getId(), addressString, coordinateString });
            }

            if (coordinateString != null && coordinateString != "") {
                final String[] coordinates = coordinateString.split(",");
                if (coordinates[0] != "-1" && coordinates[1] != "-1") {
                    target.startTag(node.getId().toString());

                    CriteriaBuilder builder = new CriteriaBuilder(OnmsAlarm.class);
                    builder.alias("node", "node");
                    builder.eq("node.id", node.getId());
                    builder.ge("severity", OnmsSeverity.WARNING);
                    builder.orderBy("severity").desc();
                    builder.limit(1);

                    // first, get the highest severity alarm
                    final List<OnmsAlarm> alarms = m_alarmDao.findMatching(builder.toCriteria());
                    if (alarms.size() == 1) {
                        final OnmsAlarm alarm = alarms.get(0);
                        final OnmsSeverity severity = alarm.getSeverity();
                        target.addAttribute("severityLabel", severity.getLabel());
                        target.addAttribute("severity", severity.getId());
                    } else {
                        // assumes everything is OK
                        target.addAttribute("severityLabel", OnmsSeverity.NORMAL.getLabel());
                        target.addAttribute("severity", OnmsSeverity.NORMAL.getId());
                    }

                    builder = new CriteriaBuilder(OnmsAlarm.class);
                    builder.alias("node", "node");
                    builder.eq("node.id", node.getId());
                    builder.ge("severity", OnmsSeverity.WARNING);
                    builder.isNull("alarmAckTime");
                    final int unackedCount = m_alarmDao.countMatching(builder.toCriteria());

                    // latitude/longitude, as floats
                    target.addAttribute("latitude", Float.valueOf(coordinates[0]));
                    target.addAttribute("longitude", Float.valueOf(coordinates[1]));

                    // everything else gets sent as basic string properties
                    target.addAttribute("nodeId", node.getId().toString());
                    target.addAttribute("nodeLabel", node.getLabel());
                    target.addAttribute("foreignSource", node.getForeignSource());
                    target.addAttribute("foreignId", node.getForeignId());
                    target.addAttribute("ipAddress", InetAddressUtils.str(node.getPrimaryInterface().getIpAddress()));
                    target.addAttribute("unackedCount", String.valueOf(unackedCount));

                    target.endTag(node.getId().toString());
                }
            }
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
}
