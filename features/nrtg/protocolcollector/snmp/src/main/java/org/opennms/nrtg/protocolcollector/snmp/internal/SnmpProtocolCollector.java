/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
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

package org.opennms.nrtg.protocolcollector.snmp.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation;
import org.opennms.netmgt.snmp.AggregateTracker;
import org.opennms.netmgt.snmp.Collectable;
import org.opennms.netmgt.snmp.SingleInstanceTracker;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpResult;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.snmp.proxy.LocationAwareSnmpClient;
import org.opennms.nrtg.api.ProtocolCollector;
import org.opennms.nrtg.api.model.CollectionJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A ProtocolCollector to execute CollectionJobs for SNMP
 *
 * @author Markus Neumann
 */
public class SnmpProtocolCollector implements ProtocolCollector {
	
    private static Logger LOG = LoggerFactory.getLogger(SnmpProtocolCollector.class);

    private static final String PROTOCOL = "SNMP";

    private NodeDao m_nodeDao;

    private LocationAwareSnmpClient m_locationAwareSnmpClient;

    private String typeToString(int type) {
        switch (type) {
            case SnmpValue.SNMP_COUNTER32:
                return "counter32";
            case SnmpValue.SNMP_COUNTER64:
                return "counter64";
            case SnmpValue.SNMP_GAUGE32:
                return "gauge32";
            case SnmpValue.SNMP_INT32:
                return "int32";
            case SnmpValue.SNMP_IPADDRESS:
                return "ipAddress";
            case SnmpValue.SNMP_OCTET_STRING:
                return "octetString";
            case SnmpValue.SNMP_OPAQUE:
                return "opaque";
            case SnmpValue.SNMP_TIMETICKS:
                return "timeticks";
            case SnmpValue.SNMP_OBJECT_IDENTIFIER:
                return "objectIdentifier";
            case SnmpValue.SNMP_NULL:
                return "null";
            default:
                return "unknown";
        }
    }

    @Override
    public CollectionJob collect(final CollectionJob collectionJob) {
        LOG.info("SnmpProtocolCollector is collecting collectionJob '{}'", collectionJob.getId());

        SnmpAgentConfig snmpAgentConfig = SnmpAgentConfig.parseProtocolConfigurationString(collectionJob.getProtocolConfiguration());

        List<Collectable> trackers = new ArrayList<>();
        for (final String metricObjId : collectionJob.getAllMetrics()) {
        	
        	SnmpObjId requestOid = SnmpObjId.get(metricObjId);
        	SnmpObjId base = requestOid.getPrefix(requestOid.length()-1);
        	int lastId = requestOid.getLastSubId();
        	
        	SingleInstanceTracker instanceTracker = new SingleInstanceTracker(base, new SnmpInstId(lastId)) {

				@Override
				protected void storeResult(SnmpResult result) {
				    LOG.trace("Collected SnmpValue '{}'", result);
					SnmpValue value = result.getValue();
					String metricType = value == null ? "unknown" : typeToString(value.getType());
					collectionJob.setMetricValue(metricObjId, metricType, value == null ? null : value.toDisplayString());
				}

				@Override
				public void setFailed(boolean failed) {
					super.setFailed(failed);
					LOG.trace("Collection Failed for metricObjId '{}'", metricObjId);
					collectionJob.setMetricValue(metricObjId, "unknown", null);
				}

				@Override
				public void setTimedOut(boolean timedOut) {
					super.setTimedOut(timedOut);
					LOG.trace("Collection timedOut for metricObjId '{}'", metricObjId);
					collectionJob.setMetricValue(metricObjId, "unknown", null);
				}

        	};
			trackers.add(instanceTracker);
        	
        }

        // Attempt to determine the location name
        String locationName = null;
        OnmsNode node = m_nodeDao.get(collectionJob.getNodeId());
        if (node != null) {
            OnmsMonitoringLocation monitoringLocation = node.getLocation();
            if (monitoringLocation != null) {
                locationName = monitoringLocation.getLocationName();
            }
        }

        AggregateTracker tracker = new AggregateTracker(trackers);
        CompletableFuture<AggregateTracker> future = m_locationAwareSnmpClient.walk(snmpAgentConfig, tracker)
            .withDescription("NRTG")
            .withLocation(locationName)
            .execute();

        try {
            future.get();
		} catch (ExecutionException e) {
		    LOG.warn("Failed to collect SNMP metrics for {}.", snmpAgentConfig.getAddress(), e);
        } catch (InterruptedException e) {
            LOG.warn("Interupted while collectiong SNMP metrics for {}.", snmpAgentConfig.getAddress());
            Thread.interrupted();
        }
        return collectionJob;
    }

    /*
      * (non-Javadoc)
      *
      * @see org.opennms.nrtg.api.ProtocolCollector#getProtcol()
      */
    @Override
    public String getProtcol() {
        return PROTOCOL;
    }

    public void setLocationAwareSnmpClient(LocationAwareSnmpClient locationAwareSnmpClient) {
        m_locationAwareSnmpClient = locationAwareSnmpClient;
    }

    public void setNodeDao(NodeDao nodeDao) {
        m_nodeDao = nodeDao;
    }

}
