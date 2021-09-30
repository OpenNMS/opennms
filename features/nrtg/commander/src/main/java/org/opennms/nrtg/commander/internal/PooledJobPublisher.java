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

package org.opennms.nrtg.commander.internal;

/**
 *
 * @author Markus Neumann
 */

import org.opennms.nrtg.api.model.CollectionJob;
import org.opennms.nrtg.api.model.CollectionTask;
import org.opennms.nrtg.api.model.DefaultCollectionJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;

import java.util.TreeSet;
import java.util.logging.Level;

/**
 * generates {@link CollectionJob}s and publish them via jms
 *
 * @author Markus Neumann
 */
public class PooledJobPublisher implements JobPublisher, Runnable {

    private static final Logger logger = LoggerFactory.getLogger(PooledJobPublisher.class);

    private final JmsTemplate jmsTemplate;

    private final CollectionTask task;
    private CollectionJob job = null;

    public PooledJobPublisher(CollectionTask task) {
        this.jmsTemplate = (JmsTemplate) CollectionCommanderStarter.getContext().getBean("JmsTemplate");
        this.task = task;
    }

    @Override
    public void publishJob(CollectionJob job, String site) {
        jmsTemplate.convertAndSend(site, job);
    }

    @Override
    public void run() {
        for (int i = 0; i < task.getCount(); i++) {
            CollectionJob job = this.createTestJob();
            logger.info("publishJob '{}' start", i + " " + Thread.currentThread());
            Long startTime = System.currentTimeMillis();
            publishJob(job, "NrtCollectMe");
//            publishJob(job, "NrtResults");

            try {
                Thread.sleep(25);
            } catch (InterruptedException ex) {
                java.util.logging.Logger.getLogger(PooledJobPublisher.class.getName()).log(Level.SEVERE, null, ex);
            }

            logger.info("publishJob '{}' done after'{}'", i + " " + Thread.currentThread(), System.currentTimeMillis() - startTime);
        }
    }

    private CollectionJob createTestJob() {
        if (this.job == null) {
            TreeSet<String> destinationSet = new TreeSet<>();
            destinationSet.add("NrtResults");

            CollectionJob snmpJob = new DefaultCollectionJob();

            snmpJob.addMetric(".1.3.6.1.4.1.2021.9.1.9.1", destinationSet, "DummyName");
//            snmpJob.addMetric(".1.3.6.1.2.1.2.2.1.10.1", destinationSet);
//            snmpJob.addMetric(".1.3.6.1.2.1.2.2.1.10.2", destinationSet);
//            snmpJob.addMetric(".1.3.6.1.2.1.2.2.1.10.4", destinationSet);
//            snmpJob.addMetric(".1.3.6.1.2.1.2.2.1.10.5", destinationSet);
//            snmpJob.addMetric(".1.3.6.1.2.1.2.2.1.10.6", destinationSet);
//            snmpJob.addMetric(".1.3.6.1.2.1.2.2.1.10.7", destinationSet);
//            snmpJob.addMetric(".1.3.6.1.2.1.2.2.1.10.8", destinationSet);
//            snmpJob.addMetric(".1.3.6.1.2.1.2.2.1.10.9", destinationSet);
//            snmpJob.addMetric(".1.3.6.1.2.1.2.2.1.10.10", destinationSet);
//            snmpJob.addMetric(".1.3.6.1.2.1.2.2.1.10.11", destinationSet);

//            destinationSet.add("NrtPersister");
//            snmpJob.addMetric(".1.3.6.1.2.1.2.2.1.10.11", destinationSet);
            snmpJob.setNetInterface("127.0.0.1");
            snmpJob.setService("SNMP");

            this.job = snmpJob;
        }
        return this.job;
    }
}
/*
snmpJob.addMetric(".1.3.6.1.2.1.25.1.5.0", destinationSet);
snmpJob.addMetric(".1.3.6.1.4.1.2021.4.11.0", destinationSet);
snmpJob.addMetric(".1.3.6.1.4.1.2021.4.1.0", destinationSet);
snmpJob.addMetric(".1.3.6.1.4.1.2021.4.2.0", destinationSet);
snmpJob.addMetric(".1.3.6.1.4.1.2021.4.3.0", destinationSet);
snmpJob.addMetric(".1.3.6.1.4.1.2021.4.4.0", destinationSet);
snmpJob.addMetric(".1.3.6.1.4.1.2021.4.5.0", destinationSet);
snmpJob.addMetric(".1.3.6.1.4.1.2021.4.6.0", destinationSet);
snmpJob.addMetric(".1.3.6.1.4.1.2021.4.7.0", destinationSet);
snmpJob.addMetric(".1.3.6.1.4.1.2021.4.9.0", destinationSet);
snmpJob.addMetric(".1.3.6.1.4.1.2021.4.11.0", destinationSet);
snmpJob.addMetric(".1.3.6.1.4.1.2021.4.12.0", destinationSet);
snmpJob.addMetric(".1.3.6.1.4.1.2021.4.13.0", destinationSet);
snmpJob.addMetric(".1.3.6.1.4.1.2021.4.14.0", destinationSet);
snmpJob.addMetric(".1.3.6.1.4.1.2021.4.15.0", destinationSet);
snmpJob.addMetric(".1.3.6.1.4.1.2021.4.16.0", destinationSet);
snmpJob.addMetric(".1.3.6.1.4.1.2021.4.17.0", destinationSet);
snmpJob.addMetric(".1.3.6.1.4.1.2021.4.100.0", destinationSet);
snmpJob.addMetric(".1.3.6.1.4.1.2021.4.101.0", destinationSet);
snmpJob.addMetric(".1.3.6.1.4.1.2021.11.1.0", destinationSet);
snmpJob.addMetric(".1.3.6.1.4.1.2021.11.2.0", destinationSet);
snmpJob.addMetric(".1.3.6.1.4.1.2021.11.3.0", destinationSet);
snmpJob.addMetric(".1.3.6.1.4.1.2021.11.4.0", destinationSet);
snmpJob.addMetric(".1.3.6.1.4.1.2021.11.50.0", destinationSet);
snmpJob.addMetric(".1.3.6.1.4.1.2021.11.51.0", destinationSet);
snmpJob.addMetric(".1.3.6.1.4.1.2021.11.52.0", destinationSet);
snmpJob.addMetric(".1.3.6.1.4.1.2021.11.53.0", destinationSet);
snmpJob.addMetric(".1.3.6.1.4.1.2021.11.54.0", destinationSet);
snmpJob.addMetric(".1.3.6.1.4.1.2021.11.55.0", destinationSet);
snmpJob.addMetric(".1.3.6.1.4.1.2021.11.56.0", destinationSet);
snmpJob.addMetric(".1.3.6.1.4.1.2021.11.57.0", destinationSet);
snmpJob.addMetric(".1.3.6.1.4.1.2021.11.58.0", destinationSet);
snmpJob.addMetric(".1.3.6.1.4.1.2021.11.59.0", destinationSet);
snmpJob.addMetric(".1.3.6.1.4.1.2021.11.60.0", destinationSet);
snmpJob.addMetric(".1.3.6.1.4.1.2021.11.61.0", destinationSet);
snmpJob.addMetric(".1.3.6.1.4.1.2021.11.62.0", destinationSet);
snmpJob.addMetric(".1.3.6.1.4.1.2021.11.63.0", destinationSet);
snmpJob.addMetric(".1.3.6.1.4.1.2021.16.1.0", destinationSet);
snmpJob.addMetric(".1.3.6.1.4.1.2021.100.1.0", destinationSet);
snmpJob.addMetric(".1.3.6.1.4.1.2021.100.2.0", destinationSet);
snmpJob.addMetric(".1.3.6.1.4.1.2021.100.3.0", destinationSet);
snmpJob.addMetric(".1.3.6.1.4.1.2021.100.4.0", destinationSet);
snmpJob.addMetric(".1.3.6.1.4.1.2021.100.5.0", destinationSet);
snmpJob.addMetric(".1.3.6.1.4.1.2021.100.6.0", destinationSet);
snmpJob.addMetric(".1.3.6.1.4.1.2021.100.10.0", destinationSet);
snmpJob.addMetric(".1.3.6.1.4.1.2021.100.11.0", destinationSet);
snmpJob.addMetric(".1.3.6.1.4.1.2021.100.12.0", destinationSet);
snmpJob.addMetric(".1.3.6.1.4.1.2021.100.13.0", destinationSet);
snmpJob.addMetric(".1.3.6.1.4.1.2021.100.20.0", destinationSet);
snmpJob.addMetric(".1.3.6.1.4.1.2021.101.1.0", destinationSet);
snmpJob.addMetric(".1.3.6.1.4.1.2021.101.2.0", destinationSet);
snmpJob.addMetric(".1.3.6.1.4.1.2021.101.100.0", destinationSet);
snmpJob.addMetric(".1.3.6.1.4.1.2021.101.101.0", destinationSet);
*/