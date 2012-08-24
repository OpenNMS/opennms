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
import org.opennms.netmgt.snmp.SnmpStrategy;
import org.opennms.netmgt.snmp.SnmpValue;
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
    private static Logger logger = LoggerFactory.getLogger(SnmpProtocolCollector.class);

    private static final String PROTOCOL = "SNMP";

    private SnmpStrategy m_snmpStrategy;

    public SnmpStrategy getSnmpStrategy() {
        return m_snmpStrategy;
    }

    public void setSnmpStrategy(SnmpStrategy snmpStrategy) {
        m_snmpStrategy = snmpStrategy;
    }

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
    public CollectionJob collect(CollectionJob collectionJob) {
        logger.info("SnmpProtocolCollector is collecting collectionJob '{}'", collectionJob.getId());

        SnmpAgentConfig snmpAgentConfig = SnmpAgentConfig.parseProtocolConfigurationString(collectionJob.getProtocolConfiguration());

        for (String metric : collectionJob.getAllMetrics()) {
            SnmpValue snmpValue = m_snmpStrategy.get(snmpAgentConfig, SnmpObjId.get(metric));
            String metricType = typeToString(snmpValue.getType());

            logger.trace("Collected SnmpValue '{}'", snmpValue);
            if (snmpValue == null) {
                collectionJob.setMetricValue(metric, metricType, null);
            } else {
                collectionJob.setMetricValue(metric, metricType, snmpValue.toDisplayString());
            }
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

}
