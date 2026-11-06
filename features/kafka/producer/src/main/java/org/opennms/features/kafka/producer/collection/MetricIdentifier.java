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

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Derives the public {@code metric_id} of an attribute.
 *
 * <p>Each collector and telemetry adapter declares an agent protocol, for example
 * {@code SNMP}. Collectors that identify their metrics prefix the internal identifier
 * with that protocol and {@code _}, for example {@code SNMP_.1.3.6.1.2.1.31.1.1.1.6.512}.
 * The {@code metric_id} is the internal identifier without that prefix. Internal
 * identifiers are not changed, because RRD {@code .meta} files and NRTG depend on them.</p>
 */
public final class MetricIdentifier {

    /** Resource attribute name for the agent protocol. */
    public static final String AGENT_PROTOCOL = "agent_protocol";

    /** Resource context attributes that every split Kafka message keeps. */
    public static final Set<String> RESOURCE_CONTEXT_ATTRIBUTES = Set.of(AGENT_PROTOCOL);

    private MetricIdentifier() {
    }

    /**
     * Returns the public metric id for an attribute.
     *
     * @param agentProtocol the protocol of the collector, may be {@code null}
     * @param internalIdentifier the attribute's internal identifier, may be {@code null}
     * @param attributeName the attribute name
     * @return the internal identifier without the {@code <agentProtocol>_} prefix, or empty
     *         if there is no protocol, the identifier lacks the prefix, nothing follows it,
     *         or the identifier is only the attribute name
     */
    public static Optional<String> metricId(String agentProtocol, String internalIdentifier, String attributeName) {
        if (agentProtocol == null || internalIdentifier == null
                || Objects.equals(internalIdentifier, attributeName)) {
            return Optional.empty();
        }
        final String prefix = agentProtocol + "_";
        if (internalIdentifier.startsWith(prefix) && internalIdentifier.length() > prefix.length()) {
            return Optional.of(internalIdentifier.substring(prefix.length()));
        }
        return Optional.empty();
    }
}
