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

package org.opennms.nrtg.protocolcollector.tca.internal;

import java.util.ArrayList;
import java.util.List;

import org.opennms.netmgt.snmp.AggregateTracker;
import org.opennms.netmgt.snmp.Collectable;
import org.opennms.netmgt.snmp.CollectionTracker;
import org.opennms.netmgt.snmp.SingleInstanceTracker;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpResult;
import org.opennms.netmgt.snmp.SnmpStrategy;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.snmp.SnmpWalker;
import org.opennms.nrtg.api.ProtocolCollector;
import org.opennms.nrtg.api.model.CollectionJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A ProtocolCollector to execute CollectionJobs for TCA
 * TCA provides multiple values behind special SnmpOids.
 * This Collector is providing directly addressed values from the multiple value SnmpOids.
 *
 * @author Markus Neumann
 */
public class TcaProtocolCollector implements ProtocolCollector {

    private static Logger logger = LoggerFactory.getLogger(TcaProtocolCollector.class);
    private static final String PROTOCOL = "TCA";
    private SnmpStrategy m_snmpStrategy;
    private final List<String> keywords = new ArrayList<>();
    {
        keywords.add("inboundDelay");
        keywords.add("inboundJitter");
        keywords.add("outboundDelay");
        keywords.add("outboundJitter");
        keywords.add("timesyncStatus");
        
    }    
    public SnmpStrategy getSnmpStrategy() {
        return m_snmpStrategy;
    }

    public void setSnmpStrategy(SnmpStrategy snmpStrategy) {
        m_snmpStrategy = snmpStrategy;
    }

    protected String getCompositeValue(String keyword, String snmpResult) {
        //String sampleSnmpResult = "|25|1327451762,42,23,11,0,1|1327451763,11,0,11,0,1|1327451764,11,0,11,0,1|1327451765,11,0,11,0,1|1327451766,11,0,11,0,1|1327451767,11,0,11,0,1|1327451768,11,0,11,0,1|1327451769,11,0,11,0,1|1327451770,11,0,11,0,1|1327451771,11,0,11,0,1|1327451772,11,0,11,0,1|1327451773,11,0,11,0,1|1327451774,11,0,11,0,1|1327451775,11,0,11,0,1|1327451776,11,0,11,0,1|1327451777,11,0,11,0,1|1327451778,11,0,11,0,1|1327451779,11,0,11,0,1|1327451780,11,0,11,0,1|1327451781,11,0,11,0,1|1327451782,11,0,11,0,1|1327451783,11,0,11,0,1|1327451784,11,0,11,0,1|1327451785,11,0,11,0,1|1327451786,12,0,11,0,423|";
        String result = null;
        if (snmpResult != null && keyword != null && keywords.contains(keyword)) {
            String[] snmpResultSets = snmpResult.split("\\|");
            Integer amount = Integer.parseInt(snmpResultSets[1]);
            String snmpResultSubSet = snmpResultSets[amount +1];
            String[] results = snmpResultSubSet.split(",");
            result = results[keywords.indexOf(keyword) +1];
        }
        
        return result;
    }
    
    @Override
    public CollectionJob collect(final CollectionJob collectionJob) {
        logger.info("TcaProtocolCollector is collecting collectionJob '{}'", collectionJob);

        SnmpAgentConfig snmpAgentConfig = SnmpAgentConfig.parseProtocolConfigurationString(collectionJob.getProtocolConfiguration());

        List<Collectable> trackers = new ArrayList<>();
        for (final String metricObjId : collectionJob.getAllMetrics()) {
            
            final String keyword = metricObjId.substring(metricObjId.lastIndexOf("_") + 1);
            final SnmpObjId requestOid = SnmpObjId.get(metricObjId.substring(0, metricObjId.lastIndexOf("_")));
            
            SnmpObjId base = requestOid.getPrefix(requestOid.length() - 1);
            int lastId = requestOid.getLastSubId();

            SingleInstanceTracker instanceTracker = new SingleInstanceTracker(base, new SnmpInstId(lastId)) {
                @Override
                protected void storeResult(SnmpResult result) {
                    logger.trace("Collected SnmpValue '{}'", result);
                    SnmpValue value = result.getValue();
                    String compositeResult = getCompositeValue(keyword, value.toDisplayString());
                    collectionJob.setMetricValue(metricObjId, "int32", compositeResult);
                }

                @Override
                public void setFailed(boolean failed) {
                    super.setFailed(failed);
                    logger.trace("Collection Failed for metricObjId '{}'", metricObjId);
                    collectionJob.setMetricValue(metricObjId, "unknown", null);
                }

                @Override
                public void setTimedOut(boolean timedOut) {
                    super.setTimedOut(timedOut);
                    logger.trace("Collection timedOut for metricObjId '{}'", metricObjId);
                    collectionJob.setMetricValue(metricObjId, "unknown", null);
                }
            };
            trackers.add(instanceTracker);
        }

        CollectionTracker tracker = new AggregateTracker(trackers);

        try(SnmpWalker walker = m_snmpStrategy.createWalker(snmpAgentConfig, "SnmpProtocolCollector for " + snmpAgentConfig.getAddress(), tracker)) {
            walker.start();
            try {
                walker.waitFor();
            } catch (InterruptedException e) {
                logger.error("Interuppted while waiting for collector. Results may be incomplete.", e);
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
