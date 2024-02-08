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
package org.opennms.netmgt.snmpinterfacepoller;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.snmp.proxy.LocationAwareSnmpClient;
import org.opennms.netmgt.snmpinterfacepoller.pollable.PollableSnmpInterface.SnmpMinimalPollInterface;
import org.opennms.netmgt.snmpinterfacepoller.pollable.SnmpInterfaceStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <P>
 * This class is designed to be used by the service poller framework to test the
 * availability of the SNMP service on remote interfaces. The class implements
 * the ServiceMonitor interface that allows it to be used along with other
 * plug-ins by the service poller framework.
 * </P>
 *
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 * @version $Id: $
 */
public class SnmpPollInterfaceMonitor {
    
    private static final Logger LOG = LoggerFactory.getLogger(SnmpPollInterfaceMonitor.class);

    /**
     * ifAdminStatus table from MIB-2.
     */
    private static final String IF_ADMIN_STATUS_OID = ".1.3.6.1.2.1.2.2.1.7.";
    
    /**
     * ifOperStatus table from MIB-2.
     */
    private static final String IF_OPER_STATUS_OID = ".1.3.6.1.2.1.2.2.1.8.";

    private final LocationAwareSnmpClient m_client;

    private String m_location;

    private long m_interval;

    public SnmpPollInterfaceMonitor(LocationAwareSnmpClient locationAwareSnmpClient) {
        this.m_client = locationAwareSnmpClient;
    }

    /**
     * <p>poll</p>
     *
     * @param agentConfig a {@link org.opennms.netmgt.snmp.SnmpAgentConfig} object.
     * @param mifaces a {@link java.util.List} object.
     * @return a {@link java.util.List} object.
     */
	public List<SnmpMinimalPollInterface> poll(SnmpAgentConfig agentConfig,
			List<SnmpMinimalPollInterface> mifaces) {

		if (mifaces == null) {
			LOG.error("Null Interfaces passed to Monitor, exiting");
			return null;
		}

		LOG.debug("Got {} interfaces to poll", mifaces.size());

		// Retrieve this interface's SNMP peer object
		if (agentConfig == null)
			throw new RuntimeException("SnmpAgentConfig object not available");

		SnmpObjId[] adminoids = new SnmpObjId[mifaces.size()];
		SnmpObjId[] operooids = new SnmpObjId[mifaces.size()];

		for (int i = 0; i < mifaces.size(); i++) {
			SnmpMinimalPollInterface miface = mifaces.get(i);
			miface.setStatus(PollStatus.unavailable());
			adminoids[i] = SnmpObjId.get(IF_ADMIN_STATUS_OID
					+ miface.getIfindex());
			operooids[i] = SnmpObjId.get(IF_OPER_STATUS_OID
					+ miface.getIfindex());
			LOG.debug("Adding Admin/Oper oids: {}/{}", adminoids[i], operooids[i]);
		}

        String ipAddress = agentConfig.getAddress().getCanonicalHostName();
        CompletableFuture<List<SnmpValue>> adminValuesFuture = m_client.get(agentConfig, adminoids)
                .withLocation(m_location).withDescription("SnmpInterfacePoller Admin Status for " + ipAddress)
                .withTimeToLive(m_interval).execute();
        CompletableFuture<List<SnmpValue>> operationalValesFuture = m_client.get(agentConfig, operooids)
                .withLocation(m_location).withDescription("SnmpInterfacePoller Operational Status for " + ipAddress)
                .withTimeToLive(m_interval).execute();
        List<SnmpValue> adminSnmpValues = new ArrayList<>();
        List<SnmpValue> operationalSnmpValues = new ArrayList<>();
        try {
            adminSnmpValues = adminValuesFuture.get();
            operationalSnmpValues = operationalValesFuture.get();
            if (adminSnmpValues.size() != mifaces.size()) {
                LOG.warn("Snmp Interface Admin statuses collection failed for interfaces in '{}' at location ", ipAddress, m_location);
                return mifaces;
            }
            if (operationalSnmpValues.size() != mifaces.size()) {
                LOG.warn("Snmp Interface Operational statuses collection failed for interfaces in '{}' at location {}", ipAddress, m_location);
                return mifaces;
            }
            LOG.debug("Received admin/operational statuses for interfaces in '{}' at location {}", ipAddress, m_location);
        } catch (InterruptedException | ExecutionException e) {
            LOG.error("Exception while retrieving admin/operational statuses for interfaces in '{}' at location {}", ipAddress, m_location, e);
            return null;
        }

        for (int i = 0; i < mifaces.size(); i++) {
            SnmpMinimalPollInterface miface = mifaces.get(i);
            SnmpValue adminSnmpValue = adminSnmpValues.get(i);
            SnmpValue operationalSnmpValue = operationalSnmpValues.get(i);

            if (adminSnmpValue != null && operationalSnmpValue != null) {
                try {
                    miface.setAdminstatus(SnmpInterfaceStatus.statusFromMibValue(adminSnmpValue.toInt()));
                    miface.setOperstatus(SnmpInterfaceStatus.statusFromMibValue(operationalSnmpValue.toInt()));
                    miface.setStatus(PollStatus.up());
                    LOG.debug("SNMP Value is {} for oid: {}", adminSnmpValue.toInt(), adminoids[i]);
                    LOG.debug("SNMP Value is {} for oid: {}", operationalSnmpValue.toInt(), operooids[i]);
                } catch (Exception e) {
                    LOG.warn("SNMP Value is {} for oid: {}", adminSnmpValue.toDisplayString(), adminoids[i]);
                    LOG.warn("SNMP Value is {} for oid: {}", operationalSnmpValue.toDisplayString(), operooids[i]);
                }
            } else {
                LOG.info("SNMP Value is null for oid: {}/{}", adminoids[i], operooids[i]);
            }
        }

        return mifaces;
	}

    public String getLocation() {
        return m_location;
    }

    public void setLocation(String location) {
        this.m_location = location;
    }

    public long getInterval() {
        return m_interval;
    }

    public void setInterval(long interval) {
        this.m_interval = interval;
    }

}
