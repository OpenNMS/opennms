/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.vaadin.nodemaps.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.features.geocoder.Coordinates;
import org.opennms.features.geocoder.GeocoderException;
import org.opennms.features.geocoder.GeocoderService;
import org.opennms.features.geocoder.TemporaryGeocoderException;
import org.opennms.features.topology.api.geo.GeoAssetProvider;
import org.opennms.features.topology.api.topo.AbstractVertex;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.dao.api.AssetRecordDao;
import org.opennms.netmgt.dao.api.NodeDao;
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

/**
 * @author Marcus Hellberg (marcus@vaadin.com)
 */
public class MapWidgetComponent extends NodeMapComponent implements GeoAssetProvider {
    private static final long serialVersionUID = -6364929103619363239L;
    private static final Logger LOG = LoggerFactory.getLogger(MapWidgetComponent.class);

    private final ScheduledExecutorService m_executor = Executors.newScheduledThreadPool(1, new ThreadFactory() {
        @Override public Thread newThread(final Runnable runnable) {
            return new Thread(runnable, "NodeMapUpdater-Thread");
        }
    });

    private NodeDao m_nodeDao;
    private AssetRecordDao m_assetDao;
    private AlarmDao m_alarmDao;
    private GeocoderService m_geocoderService;
    private TransactionOperations m_transaction;

    private Map<Integer,NodeEntry> m_activeNodes = new HashMap<Integer,NodeEntry>();

    public NodeDao getNodeDao() {
        return m_nodeDao;
    }

    public void setNodeDao(final NodeDao nodeDao) {
        m_nodeDao = nodeDao;
    }

    public AssetRecordDao getAssetRecordDao() {
        return m_assetDao;
    }

    public void setAssetRecordDao(final AssetRecordDao assetDao) {
        m_assetDao = assetDao;
    }

    public AlarmDao getAlarmDao() {
        return m_alarmDao;
    }

    public void setAlarmDao(final AlarmDao alarmDao) {
        m_alarmDao = alarmDao;
    }

    public GeocoderService getGeocoderService() {
        return m_geocoderService;
    }

    public void setGeocoderService(final GeocoderService geocoderService) {
        m_geocoderService = geocoderService;
    }

    public void setTransactionOperations(final TransactionOperations tx) {
        m_transaction = tx;
    }

    public void init() {
        m_executor.scheduleWithFixedDelay(new Runnable() {
            @Override public void run() {
                refreshNodeData();
            }
        }, 0, 5, TimeUnit.MINUTES);
    }

    public void refresh() {
        m_executor.schedule(new Runnable() {
            @Override public void run() {
                refreshNodeData();
            }
        }, 0, TimeUnit.MINUTES);
    }

    @Override
    public Collection<VertexRef> getNodesWithCoordinates() {
        final List<VertexRef> nodes = new ArrayList<VertexRef>();
        for (final Map.Entry<Integer,NodeEntry> entry : m_activeNodes.entrySet()) {
            nodes.add(new AbstractVertex("nodes", entry.getKey().toString(), entry.getValue().getNodeLabel()));
        }
        return nodes;
    }

    private void refreshNodeData() {
        if (getNodeDao() == null) {
            LOG.warn("No node DAO!  Can't refresh node data.");
            return;
        }

        LOG.debug("Refreshing node data.");

        final CriteriaBuilder cb = new CriteriaBuilder(OnmsNode.class);
        cb.alias("assetRecord", "asset");
        cb.orderBy("id").asc();

        final List<OnmsAssetRecord> updatedAssets = new ArrayList<OnmsAssetRecord>();
        final Map<Integer, NodeEntry> nodes = new HashMap<Integer, NodeEntry>();

        m_transaction.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(final TransactionStatus status) {
                for (final OnmsNode node : getNodeDao().findMatching(cb.toCriteria())) {
                    LOG.trace("processing node {}", node.getId());

                    // pass 1: get the nodes with asset data
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
                            LOG.debug("Node {} has an asset record with address \"{}\", but no coordinates.", new Object[]{node.getId(), addressString});
                            final Coordinates coordinates = getCoordinates(addressString);

                            if (coordinates == null) {
                                LOG.debug("Node {} has an asset record with address, but we were unable to find valid coordinates.", node.getId());
                                continue;
                            }

                            geolocation.setLongitude(coordinates.getLongitude());
                            geolocation.setLatitude(coordinates.getLatitude());
                            updatedAssets.add(assets);

                            if (coordinates.getLongitude() == Float.NEGATIVE_INFINITY || coordinates.getLatitude() == Float.NEGATIVE_INFINITY) {
                                // we got bad coordinates
                                LOG.debug("Node {} has an asset record with address, but we were unable to find valid coordinates.", node.getId());
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

                // pass 2: get alarm data for anything that's been grabbed from the DB
                if (!nodes.isEmpty()) {
                    LOG.debug("getting alarms for nodes");
                    final CriteriaBuilder ab = new CriteriaBuilder(OnmsAlarm.class);
                    ab.alias("node", "node");
                    ab.ge("severity", OnmsSeverity.WARNING);
                    ab.in("node.id", nodes.keySet());
                    ab.orderBy("node.id").asc();
                    ab.orderBy("severity").desc();

                    for (final OnmsAlarm alarm : getAlarmDao().findMatching(ab.toCriteria())) {
                        final int nodeId = alarm.getNodeId();
                        LOG.debug("nodeId = {}, lastId = {}, unackedCount = {}", new Object[]{nodeId, lastId, unackedCount});
                        if (nodeId != lastId) {
                            LOG.debug("  setting severity for node {} to {}", new Object[]{nodeId, alarm.getSeverity().getLabel()});
                            final NodeEntry nodeEntry = nodes.get(nodeId);
                            nodeEntry.setSeverity(alarm.getSeverity());
                            if (lastId != -1) {
                                nodeEntry.setUnackedCount(unackedCount);
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

                // pass 3: save any asset updates to the database
                LOG.debug("saving {} updated asset records to the database", updatedAssets.size());
                for (final OnmsAssetRecord asset : updatedAssets) {
                    getAssetRecordDao().saveOrUpdate(asset);
                }
            }
        });


        m_activeNodes = nodes;
        showNodes(nodes);
    }

    /**
     * Given an address, return the coordinates for that address.
     *
     * @param address the complete address, in a format a geolocator can understand
     * @return the coordinates for the given address
     */
    private Coordinates getCoordinates(final String address) {
        Coordinates coordinates = null;
        try {
            coordinates = getGeocoderService().getCoordinates(address);
            if (coordinates == null) {
                coordinates = new Coordinates(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY);
            }
        } catch (final TemporaryGeocoderException e) {
            LOG.debug("Failed to find coordinates for address '{}' due to a temporary failure.", address);
        } catch (final GeocoderException e) {
            LOG.debug("Failed to find coordinates for address '{}'.", address);
            coordinates = new Coordinates(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY);
        }
        return coordinates;
    }


    public void setSearchString(final String searchString) {
        getState().searchString = searchString;
    }
}
