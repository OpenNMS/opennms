/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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

package org.opennms.nrtg.protocolcollector.snmp.internal;

import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.snmp.snmp4j.Snmp4JStrategy;
import org.opennms.nrtg.api.ProtocolCollector;
import org.opennms.nrtg.api.model.CollectionJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Collects all {@link SnmpValue} for the {@link CollectionJob} and puts them
 * into the metrics list.
 * <p>
 * For the SnmpCall {@link SnmpUtils} from the OpenNMS SnmpApi is used. A
 * SnmpUtils.get(AgentConfig, SnmpOid[]) is used. SnmpTable iterations are NOT
 * working. Requesting specific values from a table is working.
 * </p>
 *
 * @author Markus Neumann
 */
// FIXME configurable
public class SnmpChunkProtocolCollector implements ProtocolCollector {

    private static Logger logger = LoggerFactory.getLogger(SnmpChunkProtocolCollector.class);

    @Override
    public CollectionJob collect(CollectionJob collectionJob) {
        logger.trace("SnmpChunkProtocolCollector is collecting collectionJob '{}' from '{}'",
                collectionJob.getId(), collectionJob.getNetInterface());
        
        SnmpAgentConfig snmpAgentConfig = SnmpAgentConfig.parseProtocolConfigurationString(collectionJob.getProtocolConfiguration());

        SnmpObjId[] oids = new SnmpObjId[collectionJob.getAllMetrics().size()];
        int j = 0;
        for (String metric : collectionJob.getAllMetrics()) {
            oids[j] = SnmpObjId.get(metric);
            j++;
        }

        // FIXME SnmpUtils can't be used until NMS-5461 is resolved
        // SnmpUtils.get(snmpAgentConfig, oids);
        SnmpValue[] snmpValues = new Snmp4JStrategy().get(snmpAgentConfig, oids);

        int i = 0;

        for (String metric : collectionJob.getAllMetrics()) {
            if (snmpValues.length > i && snmpValues[i] != null) {
                logger.trace("Chunked results: '{}'", snmpValues[i].toDisplayString());
                collectionJob.setMetricValue(metric, snmpValues[i].toDisplayString());
            } else {
                collectionJob.setMetricValue(metric, null);
            }
            i++;
        }
        return collectionJob;
    }

    @Override
    public String getProtcol() {
        return "SNMP";
    }
}
