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
