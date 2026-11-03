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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import org.opennms.features.kafka.producer.model.CollectionSetProtos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import com.google.common.base.Strings;

/**
 * The SpEL based metric filter used by the Kafka persister.
 *
 * The filter is evaluated once per {@link CollectionSetProtos.CollectionSetResource}: a resource either
 * keeps all of its attributes or loses all of them. The expression is evaluated against a
 * {@link ResourceFilterContext}, which exposes the node and resource properties of the protobuf resource.
 *
 * A resource whose evaluation throws is forwarded, so that a broken expression does not silently
 * drop data. {@link #evaluate(CollectionSetProtos.CollectionSetResource)} reports that case as
 * {@link Outcome#ERROR} so that tooling can show it distinctly, while
 * {@link #shouldForward(CollectionSetProtos.CollectionSetResource)} applies the persister's semantics.
 */
public class MetricFilter {

    private static final Logger LOG = LoggerFactory.getLogger(MetricFilter.class);

    private static final ExpressionParser SPEL_PARSER = new SpelExpressionParser();

    public enum Outcome {
        /** The expression evaluated to true: the resource is forwarded. */
        FORWARD,
        /** The expression evaluated to false or null: the resource is dropped. */
        DROP,
        /** The expression failed to evaluate: the resource is forwarded anyway. */
        ERROR
    }

    public static final class Result {
        private final Outcome outcome;
        private final Throwable cause;

        private Result(Outcome outcome, Throwable cause) {
            this.outcome = Objects.requireNonNull(outcome);
            this.cause = cause;
        }

        public Outcome getOutcome() {
            return outcome;
        }

        /** The exception raised during evaluation, only set when the outcome is {@link Outcome#ERROR}. */
        public Throwable getCause() {
            return cause;
        }

        /** Whether the persister would forward the resource: true for FORWARD and ERROR. */
        public boolean isForwarded() {
            return outcome != Outcome.DROP;
        }
    }

    private final Expression expression;

    private MetricFilter(Expression expression) {
        this.expression = Objects.requireNonNull(expression);
    }

    /**
     * Parses the given SpEL expression.
     *
     * @return the filter, or null when the expression is null or empty (meaning: forward everything)
     * @throws org.springframework.expression.ParseException when the expression is not valid SpEL
     */
    public static MetricFilter parse(final String expression) {
        if (Strings.isNullOrEmpty(expression)) {
            return null;
        }
        return new MetricFilter(SPEL_PARSER.parseExpression(expression));
    }

    public String getExpressionString() {
        return expression.getExpressionString();
    }

    /**
     * Evaluates the expression against a single resource. Never throws.
     */
    public Result evaluate(final CollectionSetProtos.CollectionSetResource resource) {
        try {
            final Boolean result = expression.getValue(new ResourceFilterContext(resource), Boolean.class);
            return new Result(result != null && result ? Outcome.FORWARD : Outcome.DROP, null);
        } catch (Exception e) {
            return new Result(Outcome.ERROR, e);
        }
    }

    /**
     * Evaluate whether a resource should be forwarded. Resources that fail to evaluate are forwarded
     * to avoid data loss, and the failure is logged.
     */
    public boolean shouldForward(final CollectionSetProtos.CollectionSetResource resource) {
        final Result result = evaluate(resource);
        if (result.getOutcome() == Outcome.ERROR) {
            LOG.error("Metric filter '{}' failed to evaluate for resource. The resource will be forwarded anyways.",
                    getExpressionString(), result.getCause());
        }
        return result.isForwarded();
    }

    /**
     * Returns a copy of the collection set holding only the resources that should be forwarded.
     * The timestamp is preserved. The result may hold no resources at all.
     */
    public CollectionSetProtos.CollectionSet apply(final CollectionSetProtos.CollectionSet collectionSet) {
        final CollectionSetProtos.CollectionSet.Builder filteredBuilder = CollectionSetProtos.CollectionSet.newBuilder()
                .setTimestamp(collectionSet.getTimestamp());
        for (CollectionSetProtos.CollectionSetResource resource : collectionSet.getResourceList()) {
            if (shouldForward(resource)) {
                filteredBuilder.addResource(resource);
            }
        }
        return filteredBuilder.build();
    }

    /**
     * Context object that exposes resource properties for SpEL filtering.
     * Provides access to node properties (nodeId, nodeLabel, foreignSource, foreignId, location)
     * and resource properties (resourceTypeName, resourceName, resourceId, ifIndex, instance).
     */
    public static class ResourceFilterContext {
        private final CollectionSetProtos.CollectionSetResource resource;

        public ResourceFilterContext(CollectionSetProtos.CollectionSetResource resource) {
            this.resource = Objects.requireNonNull(resource);
        }

        private CollectionSetProtos.NodeLevelResource node() {
            if (resource.hasNode()) {
                return resource.getNode();
            } else if (resource.hasInterface() && resource.getInterface().hasNode()) {
                return resource.getInterface().getNode();
            } else if (resource.hasGeneric() && resource.getGeneric().hasNode()) {
                return resource.getGeneric().getNode();
            } else if (resource.hasResponse() && resource.getResponse().hasNode()) {
                return resource.getResponse().getNode();
            }
            return null;
        }

        private static String emptyToNull(String value) {
            return Strings.isNullOrEmpty(value) ? null : value;
        }

        public Long getNodeId() {
            final CollectionSetProtos.NodeLevelResource node = node();
            return node != null ? node.getNodeId() : null;
        }

        public String getNodeLabel() {
            final CollectionSetProtos.NodeLevelResource node = node();
            return node != null ? emptyToNull(node.getNodeLabel()) : null;
        }

        public String getForeignSource() {
            final CollectionSetProtos.NodeLevelResource node = node();
            return node != null ? emptyToNull(node.getForeignSource()) : null;
        }

        public String getForeignId() {
            final CollectionSetProtos.NodeLevelResource node = node();
            return node != null ? emptyToNull(node.getForeignId()) : null;
        }

        public String getLocation() {
            if (resource.hasResponse()) {
                // Mirrors the historical behaviour: a response time resource only reports its
                // own location when it also carries node information.
                return resource.getResponse().hasNode() ? emptyToNull(resource.getResponse().getLocation()) : null;
            }
            final CollectionSetProtos.NodeLevelResource node = node();
            return node != null ? emptyToNull(node.getLocation()) : null;
        }

        public String getResourceTypeName() {
            if (resource.hasResponse()) {
                return "responseTime";
            } else if (resource.hasGeneric()) {
                return resource.getGeneric().getType();
            } else {
                return resource.getResourceTypeName();
            }
        }

        public String getResourceName() {
            return emptyToNull(resource.getResourceName());
        }

        public Integer getIfIndex() {
            if (resource.hasInterface()) {
                return resource.getInterface().getIfIndex();
            }
            return null;
        }

        public String getResourceId() {
            return resource.getResourceId();
        }

        public String getInstance() {
            if (resource.hasGeneric()) {
                return resource.getGeneric().getInstance();
            } else if (resource.hasInterface()) {
                return resource.getInterface().getInstance();
            } else if (resource.hasResponse()) {
                return resource.getResponse().getInstance();
            }
            return null;
        }

        /**
         * All properties visible to the expression, in a stable order, for display purposes.
         */
        public Map<String, Object> toMap() {
            final Map<String, Object> map = new LinkedHashMap<>();
            map.put("nodeId", getNodeId());
            map.put("nodeLabel", getNodeLabel());
            map.put("foreignSource", getForeignSource());
            map.put("foreignId", getForeignId());
            map.put("location", getLocation());
            map.put("resourceTypeName", getResourceTypeName());
            map.put("resourceName", getResourceName());
            map.put("resourceId", getResourceId());
            map.put("ifIndex", getIfIndex());
            map.put("instance", getInstance());
            return map;
        }
    }
}
