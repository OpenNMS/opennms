/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.features.kafka.producer.collection;

import java.util.Dictionary;
import java.util.Hashtable;

import org.opennms.netmgt.collection.api.PersisterFactory;
import org.opennms.netmgt.dao.api.NodeDao;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionOperations;

public class KafkaPersisterActivator implements BundleActivator {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaPersister.class);
    public static final String FORWARD_METRICS = "forward.metrics";
    public static final String PRODUCER_CONFIG = "org.opennms.features.kafka.producer";
    private static final String METRIC_TOPIC = "metricTopic";

    @Override
    public void start(BundleContext context) throws Exception {
        ConfigurationAdmin configAdmin = null;
        Boolean forwardMetrics = false;
        String metricTopic = null;
        try {
            configAdmin = context.getService(context.getServiceReference(ConfigurationAdmin.class));
            if (configAdmin != null) {
                Dictionary<String, Object> properties = configAdmin.getConfiguration(PRODUCER_CONFIG).getProperties();
                if (properties != null && properties.get(FORWARD_METRICS) != null) {
                    if (properties.get(FORWARD_METRICS) instanceof String) {
                        forwardMetrics = Boolean.parseBoolean((String) properties.get(FORWARD_METRICS));
                    }
                    if (properties.get(METRIC_TOPIC) instanceof String) {
                        metricTopic = (String) properties.get(METRIC_TOPIC);
                    }
                }
            }
        } catch (Exception e) {
            LOG.error(" Exception while loading configuration", e);
        }

        if (forwardMetrics) {
            try {
                NodeDao nodeDao = context.getService(context.getServiceReference(NodeDao.class));
                TransactionOperations transactionOperations = context
                        .getService(context.getServiceReference(TransactionOperations.class));

                CollectionSetMapper collectionSetMapper = new CollectionSetMapper(nodeDao, transactionOperations);
                KafkaPersisterFactory kafkaPersisterFactory = new KafkaPersisterFactory();
                kafkaPersisterFactory.setCollectionSetMapper(collectionSetMapper);
                kafkaPersisterFactory.setConfigAdmin(configAdmin);
                kafkaPersisterFactory.init();
                kafkaPersisterFactory.setTopicName(metricTopic);
                Dictionary<String, String> props = new Hashtable<String, String>();
                // needed to register to onms registry.
                props.put("strategy", "kafka");
                props.put("registration.export", "true");
                context.registerService(PersisterFactory.class, kafkaPersisterFactory, props);
                LOG.info("registered kafka persister factory to onms registry");
            } catch (Exception e) {
                LOG.error(" Exception while enabling kafka persister", e);
            }

        }
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        // no unregister service on bundle context, nothing to do
    }

}
