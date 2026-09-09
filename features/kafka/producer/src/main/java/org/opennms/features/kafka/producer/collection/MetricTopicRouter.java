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

import java.net.InetAddress;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.opennms.core.cache.Cache;
import org.opennms.core.cache.CacheConfig;
import org.opennms.core.cache.CacheConfigBuilder;
import org.opennms.core.mate.api.ContextKey;
import org.opennms.core.mate.api.EntityScopeProvider;
import org.opennms.core.mate.api.FallbackScope;
import org.opennms.core.mate.api.Scope;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.ParameterMap;
import org.opennms.netmgt.collection.api.CollectionResource;
import org.opennms.netmgt.collection.api.LatencyCollectionResource;
import org.opennms.netmgt.collection.api.ServiceParameters;
import org.opennms.netmgt.dao.api.SessionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Strings;
import com.google.common.cache.CacheLoader;
import com.swrve.ratelimitedlogger.RateLimitedLog;

/**
 * Resolves the Kafka topic a collected resource should be published to, based on meta-data
 * defined on the node, interface and service whose collection produced it.
 *
 * The meta-data key is configurable ({@code metricRouting.contextKey}, by default
 * {@code requisition:metricRouting.package}) and its value is used verbatim as the topic name.
 * A resource without that meta-data - or with a value that is not a legal topic name - is
 * routed to the default topic, so routing never drops data.
 *
 * Following {@code MetaTagDataLoader}, the established way of resolving meta-data from a
 * {@link CollectionResource}, the whole resolution runs in a single read-only transaction.
 *
 * Collection is always triggered by a monitored service on an interface of a node, and routing
 * follows that collection rather than the individual metric. Scopes are consulted most specific
 * first:
 *
 * <ol>
 *   <li>the collecting service</li>
 *   <li>the interface the collecting service is assigned to</li>
 *   <li>the node the collecting service belongs to</li>
 * </ol>
 *
 * and the default topic when none of them carries the key.
 *
 * This class is shared by every {@code KafkaPersister} instance and therefore accessed
 * concurrently from all collectd threads. It holds no mutable per-resolution state.
 */
public class MetricTopicRouter {

    private static final Logger LOG = LoggerFactory.getLogger(MetricTopicRouter.class);

    private static final RateLimitedLog RATE_LIMITED_LOG = RateLimitedLog
            .withRateLimit(LOG)
            .maxRate(5).every(Duration.ofSeconds(30))
            .build();

    /**
     * Kafka's own limit is 249 characters, not 255: the broker reserves the remainder for the
     * {@code .ckpt} suffix it appends to log directory names.
     */
    static final int MAX_TOPIC_LENGTH = 249;

    private static final Pattern LEGAL_TOPIC = Pattern.compile("^[a-zA-Z0-9._-]+$");

    /** {@code address[service]}, with the optional {@code @location} the perspective poller adds. */
    private static final Pattern RESPONSE_TIME_INSTANCE = Pattern.compile("^([^\\[\\]]+)\\[([^\\[\\]]+)\\](?:@.*)?$");

    private static final String CACHE_NAME = "kafkaMetricRouting";

    private static final long MAX_CACHE_SIZE = 10000L;

    static final String METRIC_ROUTED_PREFIX = "metricRouting.routed";
    static final String METRIC_UNMAPPED = "metricRouting.unmapped";
    static final String METRIC_SANITIZE_REJECTED = "metricRouting.sanitizeRejected";
    static final String METRIC_UNRESOLVED_NODE = "metricRouting.unresolvedNode";

    private final EntityScopeProvider entityScopeProvider;

    private final SessionUtils sessionUtils;

    private final MetricRegistry metricRegistry;

    private final String defaultTopic;

    /** Never null when {@link #enabled} is true. */
    private final ContextKey contextKey;

    private final boolean enabled;

    private final Cache<RoutingKey, Resolution> cache;

    /**
     * @param contextKey the meta-data key to route on, or {@code null} to disable routing
     */
    public MetricTopicRouter(final EntityScopeProvider entityScopeProvider,
                             final SessionUtils sessionUtils,
                             final MetricRegistry metricRegistry,
                             final String defaultTopic,
                             final ContextKey contextKey,
                             final boolean enabled,
                             final long cacheTimeoutMs) {
        this.entityScopeProvider = entityScopeProvider;
        this.sessionUtils = sessionUtils;
        this.metricRegistry = Objects.requireNonNull(metricRegistry);
        this.defaultTopic = Objects.requireNonNull(defaultTopic);
        this.contextKey = contextKey;
        // Routing needs all of its collaborators; without them it stays off rather than failing
        // metric forwarding as a whole.
        this.enabled = enabled && contextKey != null && entityScopeProvider != null && sessionUtils != null;

        if (this.enabled) {
            final CacheConfig cacheConfig = new CacheConfigBuilder()
                    .withName(CACHE_NAME)
                    // CacheConfig expresses the expiry in seconds
                    .withExpireAfterWrite(Math.max(1L, cacheTimeoutMs / 1000L))
                    .withMaximumSize(MAX_CACHE_SIZE)
                    .build();
            cacheConfig.setMetricRegistry(metricRegistry);
            cacheConfig.setRecordStats(true);
            this.cache = new Cache<>(cacheConfig, CacheLoader.from(this::computeResolution));
        } else {
            this.cache = null;
        }

        if (this.enabled && !isLegalTopicName(defaultTopic)) {
            // Not fatal: the topic has presumably been working for this installation. Routed
            // topics are held to the rule, the pre-existing default is only reported.
            LOG.warn("The configured metricTopic '{}' is not a legal Kafka topic name.", defaultTopic);
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    /** Renders the context key the way the meta-data DSL writes it; ContextKey has no toString(). */
    public String describeContextKey() {
        return contextKey == null ? "<none>" : contextKey.getContext() + ":" + contextKey.getKey();
    }

    public String getDefaultTopic() {
        return defaultTopic;
    }

    public ContextKey getContextKey() {
        return contextKey;
    }

    public MetricRegistry getMetricRegistry() {
        return metricRegistry;
    }

    /**
     * Resolves the topic for the given resource. Returns the default topic when routing is
     * disabled, when the node could not be resolved, or when no usable meta-data was found.
     */
    public String resolveTopic(final CollectionResource resource, final int nodeId,
                              final ServiceParameters params) {
        if (!enabled) {
            return defaultTopic;
        }
        try {
            return resolveTopic(routingKeyFor(resource, nodeId, params));
        } catch (Exception e) {
            RATE_LIMITED_LOG.error("Failed to resolve the metric topic for {}, using '{}'.", resource, defaultTopic, e);
            return defaultTopic;
        }
    }

    /**
     * The counted resolution path: the lookup itself is cached, but the counters are applied on
     * every call, so they reflect resources rather than cache misses.
     */
    String resolveTopic(final RoutingKey key) {
        if (!enabled) {
            return defaultTopic;
        }
        Resolution resolution;
        try {
            resolution = cache.get(key);
        } catch (Exception e) {
            RATE_LIMITED_LOG.error("Failed to resolve the metric topic for {}, using '{}'.", key, defaultTopic, e);
            return defaultTopic;
        }
        countOutcome(resolution);
        return resolution.topic;
    }

    private void countOutcome(final Resolution resolution) {
        switch (resolution.status) {
            case UNMAPPED:
                metricRegistry.counter(METRIC_UNMAPPED).inc();
                break;
            case SANITIZE_REJECTED:
                metricRegistry.counter(METRIC_SANITIZE_REJECTED).inc();
                break;
            case UNRESOLVED_NODE:
                metricRegistry.counter(METRIC_UNRESOLVED_NODE).inc();
                break;
            default:
                // ROUTED is counted per published resource by the persister, once the metric
                // filter has had its say; DISABLED is not worth counting.
                break;
        }
    }

    /** Drops all cached routing decisions, so the next resolution hits the database again. */
    public void invalidateCache() {
        if (cache != null) {
            cache.invalidateAll();
        }
    }

    /**
     * Derives the meta-data lookup inputs from a resource and the parameters of the collection
     * that produced it.
     *
     * Collection in OpenNMS is triggered by a monitored service on an interface of a node, and
     * routing is a property of that collection rather than of the individual metric:
     *
     * <ul>
     *   <li>the collecting interface is {@code getOwnerName()} - for a collectd collection that is
     *       the agent's address, which is the address of the very {@code OnmsIpInterface} the
     *       service is assigned to, because collectd only schedules a service on an interface that
     *       has it. Response time resources report their own address here.</li>
     *   <li>the collecting service is the {@code SERVICE} parameter collectd puts into the
     *       {@link ServiceParameters}. Response time resources carry it themselves instead,
     *       because the poller passes empty parameters.</li>
     * </ul>
     *
     * Which interface a metric is <em>about</em> deliberately plays no part: meta-data on the SNMP
     * interfaces a collection describes is not consulted, only meta-data on the interface the
     * collecting service is assigned to.
     */
    RoutingKey routingKeyFor(final CollectionResource resource, final int nodeId,
                             final ServiceParameters params) {
        String serviceName = null;

        // The address of the interface the collection ran against.
        String ipAddress = Strings.emptyToNull(resource.getOwnerName());

        // Response time resources know their own address and service. They encode them as
        // 'address[service]', optionally followed by '@location' for the perspective poller.
        if (resource instanceof LatencyCollectionResource) {
            final LatencyCollectionResource latencyResource = (LatencyCollectionResource) resource;
            ipAddress = firstNonEmpty(latencyResource.getIpAddress(), ipAddress);
            serviceName = latencyResource.getServiceName();
        } else {
            final String[] addressAndService = parseResponseTimeInstance(resource.getInstance());
            if (addressAndService != null) {
                ipAddress = firstNonEmpty(addressAndService[0], ipAddress);
                serviceName = addressAndService[1];
            }
        }

        if (serviceName == null && params != null) {
            // ServiceParameters has no accessor for the service name, so read the key collectd
            // writes in CollectionSpecification.initializeParameters().
            serviceName = Strings.emptyToNull(ParameterMap.getKeyedString(params.getParameters(),
                    ServiceParameters.ParameterName.SERVICE.toString(), null));
        }

        return new RoutingKey(nodeId, ipAddress, serviceName);
    }

    private static String firstNonEmpty(final String preferred, final String fallback) {
        return Strings.isNullOrEmpty(preferred) ? fallback : preferred;
    }

    /**
     * Resolves a routing key, reporting where the answer came from. Side-effect free - it does
     * not touch the counters - so the shell command can preview a resolution without disturbing
     * what it is reporting on.
     */
    public Resolution resolve(final RoutingKey key) {
        return computeResolution(key);
    }

    private Resolution computeResolution(final RoutingKey key) {
        if (!enabled) {
            return new Resolution(defaultTopic, null, null, Status.DISABLED);
        }
        if (key.nodeId <= 0) {
            return new Resolution(defaultTopic, null, null, Status.UNRESOLVED_NODE);
        }

        // One transaction for the whole resolution. EntityScopeProviderImpl hands back
        // ObjectScopes over detached entities whose accessors run lazily, so consulting a scope
        // outside the transaction that built it would throw for any virtual-context key
        // (for example node:location).
        final Optional<Scope.ScopeValue> scopeValue = sessionUtils.withReadOnlyTransaction(() -> {
            final List<Scope> scopes = new ArrayList<>();
            scopes.add(entityScopeProvider.getScopeForNode(key.nodeId));

            // The interface the collecting service is assigned to.
            if (key.ipAddress != null) {
                scopes.add(entityScopeProvider.getScopeForInterface(key.nodeId, key.ipAddress));
            }

            if (key.ipAddress != null && key.serviceName != null) {
                final InetAddress address = InetAddressUtils.getInetAddress(key.ipAddress);
                if (address != null) {
                    scopes.add(entityScopeProvider.getScopeForService(key.nodeId, address, key.serviceName));
                }
            }

            // FallbackScope reverses the list, so the scopes added last win:
            // service > interface > node.
            return new FallbackScope(scopes).get(contextKey);
        });

        if (scopeValue.isEmpty() || Strings.isNullOrEmpty(scopeValue.get().value.trim())) {
            return new Resolution(defaultTopic, scopeValue.map(v -> v.value).orElse(null),
                    scopeValue.map(v -> v.scopeName).orElse(null), Status.UNMAPPED);
        }

        final String rawValue = scopeValue.get().value;
        final String topic = rawValue.trim();
        if (!isLegalTopicName(topic)) {
            RATE_LIMITED_LOG.warn("Meta-data {} resolved to '{}', which is not a legal Kafka topic name. "
                    + "Using the default topic '{}' instead.", describeContextKey(), rawValue, defaultTopic);
            return new Resolution(defaultTopic, rawValue, scopeValue.get().scopeName, Status.SANITIZE_REJECTED);
        }

        return new Resolution(topic, rawValue, scopeValue.get().scopeName, Status.ROUTED);
    }

    /**
     * Applies Kafka's topic naming rules. Values that fail are rejected rather than rewritten:
     * replacing illegal characters would invent topic names nobody configured, and because Kafka's
     * metrics layer treats '.' and '_' as equivalent, a rewrite could also collide with an
     * existing topic.
     */
    static boolean isLegalTopicName(final String topic) {
        if (Strings.isNullOrEmpty(topic) || topic.length() > MAX_TOPIC_LENGTH) {
            return false;
        }
        if (".".equals(topic) || "..".equals(topic)) {
            return false;
        }
        return LEGAL_TOPIC.matcher(topic).matches();
    }

    /**
     * Splits the instance string response time resources use to encode the polled address and
     * service: {@code address[service]}, or {@code address[service]@location} for the perspective
     * poller. Returns {@code null} for anything else, which is how a plain interface resource is
     * told apart from a response time one.
     *
     * @see org.opennms.netmgt.collection.api.LatencyCollectionResource#getInstance()
     * @see org.opennms.netmgt.collection.support.builder.LatencyTypeResource#getInstance()
     * @see org.opennms.netmgt.collection.support.builder.PerspectiveResponseTimeResource#getInstance()
     */
    static String[] parseResponseTimeInstance(final String instance) {
        if (instance == null) {
            return null;
        }
        final Matcher matcher = RESPONSE_TIME_INSTANCE.matcher(instance);
        if (!matcher.matches()) {
            return null;
        }
        return new String[]{matcher.group(1), matcher.group(2)};
    }

    private static boolean isNumeric(final String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public enum Status {
        /** Routing is not enabled. */
        DISABLED,
        /** Routed to a topic named by meta-data. */
        ROUTED,
        /** No node could be resolved for the resource. */
        UNRESOLVED_NODE,
        /** The meta-data key was not found, or resolved to a blank value. */
        UNMAPPED,
        /** The meta-data value is not a legal topic name. */
        SANITIZE_REJECTED
    }

    /** The outcome of a resolution, including where the value came from. */
    public static class Resolution {
        public final String topic;
        public final String value;
        public final Scope.ScopeName scopeName;
        public final Status status;

        Resolution(final String topic, final String value, final Scope.ScopeName scopeName, final Status status) {
            this.topic = topic;
            this.value = value;
            this.scopeName = scopeName;
            this.status = status;
        }

        @Override
        public String toString() {
            return "Resolution{" +
                    "topic='" + topic + '\'' +
                    ", value='" + value + '\'' +
                    ", scopeName=" + scopeName +
                    ", status=" + status +
                    '}';
        }
    }

    /**
     * Identifies the collection a metric came from, which is the complete set of inputs a
     * routing decision depends on and therefore also the cache key. These three fields are the
     * natural key of {@code ifservices}.
     *
     * Because they are constant across every resource of one CollectionSet, all of its resources
     * share a single cache entry.
     */
    public static class RoutingKey {
        public final int nodeId;
        public final String ipAddress;
        public final String serviceName;

        public RoutingKey(final int nodeId, final String ipAddress, final String serviceName) {
            this.nodeId = nodeId;
            this.ipAddress = ipAddress;
            this.serviceName = serviceName;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof RoutingKey)) {
                return false;
            }
            final RoutingKey that = (RoutingKey) o;
            return this.nodeId == that.nodeId
                    && Objects.equals(this.ipAddress, that.ipAddress)
                    && Objects.equals(this.serviceName, that.serviceName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(nodeId, ipAddress, serviceName);
        }

        @Override
        public String toString() {
            return String.format("RoutingKey[nodeId=%d, ipAddress=%s, serviceName=%s]",
                    nodeId, ipAddress, serviceName);
        }
    }
}
