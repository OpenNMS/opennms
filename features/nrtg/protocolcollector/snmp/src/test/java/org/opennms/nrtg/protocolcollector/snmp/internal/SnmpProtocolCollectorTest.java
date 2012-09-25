/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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

import org.junit.Ignore;
import org.junit.Test;
import org.opennms.nrtg.api.model.CollectionJob;
import org.opennms.nrtg.api.model.DefaultCollectionJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.TreeSet;

/**
 * @author Markus Neumann
 */
public class SnmpProtocolCollectorTest {

    private static Logger logger = LoggerFactory.getLogger(SnmpProtocolCollectorTest.class);

    @Ignore
    @Test
    public void testCollect() {
        SnmpProtocolCollector snmpProtocolCollector = new SnmpProtocolCollector();
        CollectionJob collectionJob = new DefaultCollectionJob();

        TreeSet<String> destinationSet = new TreeSet<String>();
        destinationSet.add("result");
        collectionJob.addMetric(".1.3.6.1.2.1.25.1.5.0", destinationSet);
        collectionJob.setNetInterface("127.0.0.1");

        collectionJob.addMetric(".1.3.6.1.4.1.2021.4.1.0", destinationSet);
        collectionJob.addMetric(".1.3.6.1.4.1.2021.4.2.0", destinationSet);
        collectionJob.addMetric(".1.3.6.1.4.1.2021.4.3.0", destinationSet);
        collectionJob.addMetric(".1.3.6.1.4.1.2021.4.4.0", destinationSet);
        collectionJob.addMetric(".1.3.6.1.4.1.2021.4.5.0", destinationSet);
        collectionJob.addMetric(".1.3.6.1.4.1.2021.4.6.0", destinationSet);
        collectionJob.addMetric(".1.3.6.1.4.1.2021.4.7.0", destinationSet);
        collectionJob.addMetric(".1.3.6.1.4.1.2021.4.9.0", destinationSet);
        collectionJob.addMetric(".1.3.6.1.4.1.2021.4.11.0", destinationSet);
        collectionJob.addMetric(".1.3.6.1.4.1.2021.4.12.0", destinationSet);
        collectionJob.addMetric(".1.3.6.1.4.1.2021.4.13.0", destinationSet);
        collectionJob.addMetric(".1.3.6.1.4.1.2021.4.14.0", destinationSet);
        collectionJob.addMetric(".1.3.6.1.4.1.2021.4.15.0", destinationSet);
        collectionJob.addMetric(".1.3.6.1.4.1.2021.4.16.0", destinationSet);
        collectionJob.addMetric(".1.3.6.1.4.1.2021.4.17.0", destinationSet);
        collectionJob.addMetric(".1.3.6.1.4.1.2021.4.100.0", destinationSet);
        collectionJob.addMetric(".1.3.6.1.4.1.2021.4.101.0", destinationSet);
        collectionJob.addMetric(".1.3.6.1.4.1.2021.11.1.0", destinationSet);
        collectionJob.addMetric(".1.3.6.1.4.1.2021.11.2.0", destinationSet);
        collectionJob.addMetric(".1.3.6.1.4.1.2021.11.3.0", destinationSet);
        collectionJob.addMetric(".1.3.6.1.4.1.2021.11.4.0", destinationSet);
        collectionJob.addMetric(".1.3.6.1.4.1.2021.11.50.0", destinationSet);
        collectionJob.addMetric(".1.3.6.1.4.1.2021.11.51.0", destinationSet);
        collectionJob.addMetric(".1.3.6.1.4.1.2021.11.52.0", destinationSet);
        collectionJob.addMetric(".1.3.6.1.4.1.2021.11.53.0", destinationSet);
        collectionJob.addMetric(".1.3.6.1.4.1.2021.11.54.0", destinationSet);
        collectionJob.addMetric(".1.3.6.1.4.1.2021.11.55.0", destinationSet);
        collectionJob.addMetric(".1.3.6.1.4.1.2021.11.56.0", destinationSet);
        collectionJob.addMetric(".1.3.6.1.4.1.2021.11.57.0", destinationSet);
        collectionJob.addMetric(".1.3.6.1.4.1.2021.11.58.0", destinationSet);
        collectionJob.addMetric(".1.3.6.1.4.1.2021.11.59.0", destinationSet);
        collectionJob.addMetric(".1.3.6.1.4.1.2021.11.60.0", destinationSet);
        collectionJob.addMetric(".1.3.6.1.4.1.2021.11.61.0", destinationSet);
        collectionJob.addMetric(".1.3.6.1.4.1.2021.11.62.0", destinationSet);
        collectionJob.addMetric(".1.3.6.1.4.1.2021.11.63.0", destinationSet);
        collectionJob.addMetric(".1.3.6.1.4.1.2021.16.1.0", destinationSet);
        collectionJob.addMetric(".1.3.6.1.4.1.2021.100.1.0", destinationSet);
        collectionJob.addMetric(".1.3.6.1.4.1.2021.100.2.0", destinationSet);
        collectionJob.addMetric(".1.3.6.1.4.1.2021.100.3.0", destinationSet);
        collectionJob.addMetric(".1.3.6.1.4.1.2021.100.4.0", destinationSet);
        collectionJob.addMetric(".1.3.6.1.4.1.2021.100.5.0", destinationSet);
        collectionJob.addMetric(".1.3.6.1.4.1.2021.100.6.0", destinationSet);
        collectionJob.addMetric(".1.3.6.1.4.1.2021.100.10.0", destinationSet);
        collectionJob.addMetric(".1.3.6.1.4.1.2021.100.11.0", destinationSet);
        collectionJob.addMetric(".1.3.6.1.4.1.2021.100.12.0", destinationSet);
        collectionJob.addMetric(".1.3.6.1.4.1.2021.100.13.0", destinationSet);
        collectionJob.addMetric(".1.3.6.1.4.1.2021.100.20.0", destinationSet);
        collectionJob.addMetric(".1.3.6.1.4.1.2021.101.1.0", destinationSet);
        collectionJob.addMetric(".1.3.6.1.4.1.2021.101.2.0", destinationSet);
        collectionJob.addMetric(".1.3.6.1.4.1.2021.101.100.0", destinationSet);
        collectionJob.addMetric(".1.3.6.1.4.1.2021.101.101.0", destinationSet);

        collectionJob.setNetInterface("127.0.0.1");

        CollectionJob resultCollectionJob = snmpProtocolCollector.collect(collectionJob);
        for (String metric : resultCollectionJob.getAllMetrics()) {
            logger.trace("Metric: '{}' \t Result: '{}'", metric, resultCollectionJob.getMetricValue(metric));
        }
    }
}