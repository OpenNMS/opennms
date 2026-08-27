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

import java.util.Dictionary;
import java.util.Hashtable;

import org.opennms.core.mate.api.ContextKey;
import org.opennms.core.mate.api.EntityScopeProvider;
import org.opennms.netmgt.collection.api.PersisterFactory;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.ResourceDao;
import org.opennms.netmgt.dao.api.SessionUtils;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.MetricSet;
import com.codahale.metrics.jmx.JmxReporter;

public class KafkaPersisterActivator implements BundleActivator {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaPersisterActivator.class);
    public static final String FORWARD_METRICS = "forward.metrics";
    public static final String PRODUCER_CONFIG = "org.opennms.features.kafka.producer";
    private static final String METRIC_TOPIC = "metricTopic";

    private static final String DISABLE_METRIC_SPLITTING = "disable.metrics.splitting";
    private static final String METRIC_FILTER = "metricFilter";

    private static final String METRIC_ROUTING_ENABLED = "metricRouting.enabled";
    private static final String METRIC_ROUTING_CONTEXT_KEY = "metricRouting.contextKey";
    private static final String METRIC_ROUTING_CACHE_TIMEOUT_MS = "metricRouting.cacheTimeoutMs";

    /** Must match the default in {@link KafkaPersister}. */
    private static final String DEFAULT_METRIC_TOPIC = "metrics";

    static final String DEFAULT_METRIC_ROUTING_CONTEXT_KEY = "requisition:metricRouting.package";
    static final long DEFAULT_METRIC_ROUTING_CACHE_TIMEOUT_MS = 300000L;

    private MetricRegistry metricRegistry;
    private JmxReporter jmxReporter;
    private ServiceRegistration<MetricSet> metricSetRegistration;
    private ServiceRegistration<MetricTopicRouter> metricTopicRouterRegistration;
    private KafkaPersisterFactory kafkaPersisterFactory;

    @Override
    public void start(BundleContext context) throws Exception {
        ConfigurationAdmin configAdmin = null;
        Boolean forwardMetrics = false;
        String metricTopic = null;
        boolean disableMetricsSplitting = false;
        String metricFilter = null;
        boolean metricRoutingEnabled = false;
        String metricRoutingContextKey = DEFAULT_METRIC_ROUTING_CONTEXT_KEY;
        long metricRoutingCacheTimeoutMs = DEFAULT_METRIC_ROUTING_CACHE_TIMEOUT_MS;
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
                    if (properties.get(METRIC_FILTER) instanceof String) {
                        metricFilter = (String) properties.get(METRIC_FILTER);
                    }
                    if (properties.get(METRIC_ROUTING_ENABLED) instanceof String) {
                        metricRoutingEnabled = Boolean.parseBoolean((String) properties.get(METRIC_ROUTING_ENABLED));
                    }
                    if (properties.get(METRIC_ROUTING_CONTEXT_KEY) instanceof String) {
                        final String configuredContextKey = (String) properties.get(METRIC_ROUTING_CONTEXT_KEY);
                        if (!configuredContextKey.trim().isEmpty()) {
                            metricRoutingContextKey = configuredContextKey.trim();
                        }
                    }
                    if (properties.get(METRIC_ROUTING_CACHE_TIMEOUT_MS) instanceof String) {
                        metricRoutingCacheTimeoutMs = parseLong((String) properties.get(METRIC_ROUTING_CACHE_TIMEOUT_MS),
                                DEFAULT_METRIC_ROUTING_CACHE_TIMEOUT_MS, METRIC_ROUTING_CACHE_TIMEOUT_MS);
                    }
                }
            }
        } catch (Exception e) {
            LOG.error(" Exception while loading configuration", e);
        }

        final String effectiveMetricTopic = (metricTopic != null && !metricTopic.isEmpty())
                ? metricTopic : DEFAULT_METRIC_TOPIC;

        // The persister factory is created here rather than in blueprint, so the metric registry
        // has to be created here too - a bundle activator cannot reach a blueprint bean.
        metricRegistry = new MetricRegistry();
        try {
            jmxReporter = JmxReporter.forRegistry(metricRegistry).inDomain(PRODUCER_CONFIG).build();
            jmxReporter.start();
        } catch (Exception e) {
            LOG.error("Failed to start the JMX reporter for the Kafka producer metrics.", e);
        }
        final Dictionary<String, String> metricSetProps = new Hashtable<>();
        metricSetProps.put("name", PRODUCER_CONFIG);
        metricSetProps.put("description", "OpenNMS Kafka Producer");
        metricSetRegistration = context.registerService(MetricSet.class, metricRegistry, metricSetProps);

        // Metric routing is set up separately, and defensively: a bad metricRouting.contextKey or
        // a missing EntityScopeProvider must never stop metrics from being forwarded at all.
        MetricTopicRouter metricTopicRouter;
        try {
            final ContextKey contextKey = new ContextKey(metricRoutingContextKey);
            final EntityScopeProvider entityScopeProvider = getService(context, EntityScopeProvider.class);
            final SessionUtils sessionUtils = getService(context, SessionUtils.class);
            if (metricRoutingEnabled && (entityScopeProvider == null || sessionUtils == null)) {
                LOG.error("Metric routing is enabled but the required services are unavailable "
                        + "(EntityScopeProvider={}, SessionUtils={}). All metrics will be forwarded to '{}'.",
                        entityScopeProvider, sessionUtils, effectiveMetricTopic);
            }
            metricTopicRouter = new MetricTopicRouter(entityScopeProvider, sessionUtils, metricRegistry,
                    effectiveMetricTopic, contextKey, metricRoutingEnabled, metricRoutingCacheTimeoutMs);
            if (metricTopicRouter.isEnabled()) {
                LOG.info("Metric routing is enabled on meta-data key '{}', default topic '{}'.",
                        metricTopicRouter.describeContextKey(), effectiveMetricTopic);
            }
        } catch (Exception e) {
            LOG.error("Failed to initialize metric routing ({}='{}'). All metrics will be forwarded to '{}'.",
                    METRIC_ROUTING_CONTEXT_KEY, metricRoutingContextKey, effectiveMetricTopic, e);
            metricTopicRouter = new MetricTopicRouter(null, null, metricRegistry, effectiveMetricTopic,
                    null, false, DEFAULT_METRIC_ROUTING_CACHE_TIMEOUT_MS);
        }
        // Registered even when disabled, so that opennms:kafka-metric-routing-test can be used to
        // preview a resolution before routing is switched on.
        metricTopicRouterRegistration = context.registerService(MetricTopicRouter.class, metricTopicRouter, null);

        if (forwardMetrics) {
            try {
                NodeDao nodeDao = context.getService(context.getServiceReference(NodeDao.class));
                SessionUtils sessionUtils = context
                        .getService(context.getServiceReference(SessionUtils.class));
                ResourceDao resourceDao = context.getService(context.getServiceReference(ResourceDao.class));

                CollectionSetMapper collectionSetMapper = new CollectionSetMapper(nodeDao, sessionUtils, resourceDao);
                kafkaPersisterFactory = new KafkaPersisterFactory();
                kafkaPersisterFactory.setCollectionSetMapper(collectionSetMapper);
                kafkaPersisterFactory.setConfigAdmin(configAdmin);
                kafkaPersisterFactory.init();
                kafkaPersisterFactory.setTopicName(metricTopic);
                kafkaPersisterFactory.setDisableMetricsSplitting(disableMetricsSplitting);
                kafkaPersisterFactory.setMetricFilter(metricFilter);
                kafkaPersisterFactory.setMetricTopicRouter(metricTopicRouter);
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
        // Service registrations are dropped by the framework, but the JMX reporter owns an MBean
        // domain that would otherwise survive a bundle refresh.
        if (jmxReporter != null) {
            try {
                jmxReporter.stop();
            } catch (Exception e) {
                LOG.warn("Failed to stop the JMX reporter.", e);
            }
            jmxReporter = null;
        }
        unregister(metricSetRegistration);
        metricSetRegistration = null;
        unregister(metricTopicRouterRegistration);
        metricTopicRouterRegistration = null;
        if (kafkaPersisterFactory != null) {
            kafkaPersisterFactory.destroy();
            kafkaPersisterFactory = null;
        }
        metricRegistry = null;
    }

    private static void unregister(final ServiceRegistration<?> registration) {
        if (registration == null) {
            return;
        }
        try {
            registration.unregister();
        } catch (IllegalStateException e) {
            // already unregistered by the framework
        }
    }

    private static <T> T getService(final BundleContext context, final Class<T> clazz) {
        final ServiceReference<T> reference = context.getServiceReference(clazz);
        return reference != null ? context.getService(reference) : null;
    }

    private static long parseLong(final String value, final long fallback, final String propertyName) {
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            LOG.warn("'{}' is not a valid value for {}, using {}.", value, propertyName, fallback);
            return fallback;
        }
    }
}
