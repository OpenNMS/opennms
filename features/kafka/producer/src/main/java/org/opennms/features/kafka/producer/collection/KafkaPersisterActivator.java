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
package org.opennms.features.kafka.producer.collection;

import org.opennms.netmgt.collection.api.PersisterFactory;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.ResourceDao;
import org.opennms.netmgt.dao.api.SessionUtils;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Dictionary;
import java.util.Hashtable;

public class KafkaPersisterActivator implements BundleActivator {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaPersisterActivator.class);
    public static final String FORWARD_METRICS = "forward.metrics";
    public static final String PRODUCER_CONFIG = "org.opennms.features.kafka.producer";
    private static final String METRIC_TOPIC = "metricTopic";

    private static final String DISABLE_METRIC_SPLITTING = "disable.metrics.splitting";

    @Override
    public void start(BundleContext context) throws Exception {
        ConfigurationAdmin configAdmin = null;
        Boolean forwardMetrics = false;
        String metricTopic = null;
        boolean disableMetricsSplitting = false;
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
                    if (properties.get(DISABLE_METRIC_SPLITTING) instanceof String) {
                        disableMetricsSplitting = Boolean.parseBoolean((String) properties.get(DISABLE_METRIC_SPLITTING));
                    }
                }
            }
        } catch (Exception e) {
            LOG.error(" Exception while loading configuration", e);
        }

        if (forwardMetrics) {
            try {
                NodeDao nodeDao = context.getService(context.getServiceReference(NodeDao.class));
                SessionUtils sessionUtils = context
                        .getService(context.getServiceReference(SessionUtils.class));
                ResourceDao resourceDao = context.getService(context.getServiceReference(ResourceDao.class));
                // Wait  for kafka manager blueprint to initialize weather to use global or metric specific producer
                Thread.sleep(5000);
                CollectionSetMapper collectionSetMapper = new CollectionSetMapper(nodeDao, sessionUtils, resourceDao);
                KafkaPersisterFactory kafkaPersisterFactory = new KafkaPersisterFactory();
                kafkaPersisterFactory.setCollectionSetMapper(collectionSetMapper);
                kafkaPersisterFactory.setConfigAdmin(configAdmin);
                kafkaPersisterFactory.init();
                kafkaPersisterFactory.setTopicName(metricTopic);
                kafkaPersisterFactory.setDisableMetricsSplitting(disableMetricsSplitting);
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
