/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.snmp.proxy.common;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.opennms.netmgt.model.OnmsDistPoller;
import org.opennms.netmgt.snmp.CollectionTracker;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpResult;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.snmp.proxy.LocationAwareSnmpClient;
import org.opennms.netmgt.snmp.proxy.SNMPRequestBuilder;
import org.opennms.netmgt.snmp.proxy.common.utils.LocationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * Location-aware SNMP client that builds a {@link SnmpRequestDTO} and delegates
 * the request to either a local, or a remote @{link SnmpRequestExecutor}.
 *
 * @author jwhite
 */
public class DelegatingLocationAwareSnmpClientImpl implements LocationAwareSnmpClient {

    private static final Logger LOG = LoggerFactory.getLogger(DelegatingLocationAwareSnmpClientImpl.class);

    @Autowired
    @Qualifier("localSnmpRequestExecutor")
    private SnmpRequestExecutor localSnmpRequestExecutor;

    @Autowired
    @Qualifier("remoteSnmpRequestExecutor")
    private SnmpRequestExecutor remoteSnmpRequestExecutor;

    @Autowired(required=false)
    private OnmsDistPoller identity;

    @Override
    public SNMPRequestBuilder<List<SnmpResult>> walk(SnmpAgentConfig agent, String... oids) {
        final List<SnmpObjId> snmpObjIds = Arrays.stream(oids)
                .map(oid -> SnmpObjId.get(oid))
                .collect(Collectors.toList());
        return walk(agent, snmpObjIds);
    }

    @Override
    public SNMPRequestBuilder<List<SnmpResult>> walk(SnmpAgentConfig agent, SnmpObjId... oids) {
        return walk(agent, Arrays.asList(oids));
    }

    @Override
    public SNMPRequestBuilder<List<SnmpResult>> walk(SnmpAgentConfig agent, List<SnmpObjId> oids) {
        return new SNMPWalkBuilder(this, agent, oids);
    }

    @Override
    public SNMPRequestBuilder<CollectionTracker> walk(SnmpAgentConfig agent, CollectionTracker tracker) {
        return new SNMPWalkWithTrackerBuilder(this, agent, tracker);
    }

    @Override
    public SNMPRequestBuilder<SnmpValue> get(SnmpAgentConfig agent, String oid) {
        return get(agent, SnmpObjId.get(oid));
    }

    @Override
    public SNMPRequestBuilder<SnmpValue> get(SnmpAgentConfig agent, SnmpObjId oid) {
        return new SNMPSingleGetBuilder(this, agent, oid);
    }

    @Override
    public SNMPRequestBuilder<List<SnmpValue>> get(SnmpAgentConfig agent, String... oids) {
        final List<SnmpObjId> snmpObjIds = Arrays.stream(oids)
                .map(oid -> SnmpObjId.get(oid))
                .collect(Collectors.toList());
        return get(agent, snmpObjIds);
    }

    @Override
    public SNMPRequestBuilder<List<SnmpValue>> get(SnmpAgentConfig agent, SnmpObjId... oids) {
        return get(agent, Arrays.asList(oids));
    }

    @Override
    public SNMPRequestBuilder<List<SnmpValue>> get(SnmpAgentConfig agent, List<SnmpObjId> oids) {
        return new SNMPMultiGetBuilder(this, agent, oids);
    }

    protected SnmpRequestExecutor getSnmpRequestExecutor(String location) {
        if (LocationUtils.isLocationOverrideEnabled()) {
            // Always use the remote executor when location override is enabled.
            return remoteSnmpRequestExecutor;
        }

        if (location == null || (identity != null && identity.getLocation().equals(location))) {
            return localSnmpRequestExecutor;
        } else {
            // Use the remote executor iff. the request is targeted for another location
            LOG.debug("Using remote SNMP request executor for location: {}", location);
            return remoteSnmpRequestExecutor;
        }
    }

    public void setIdentity(OnmsDistPoller identity) {
        this.identity = identity;
    }

    public void setLocalSnmpRequestExecutor(SnmpRequestExecutor localSnmpRequestExecutor) {
        this.localSnmpRequestExecutor = localSnmpRequestExecutor;
    }

    public void setRemoteSnmpRequestExecutor(SnmpRequestExecutor remoteSnmpRequestExecutor) {
        this.remoteSnmpRequestExecutor = remoteSnmpRequestExecutor;
    }
}
