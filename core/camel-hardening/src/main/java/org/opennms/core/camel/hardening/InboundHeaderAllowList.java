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
package org.opennms.core.camel.hardening;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.apache.camel.Exchange;
import org.apache.camel.spi.HeaderFilterStrategy;

/**
 * Keeps only the inbound JMS properties OpenNMS reads and drops everything else,
 * so a producer on the broker cannot set Camel internals such as
 * CamelExecCommandExecutable on a consumer's exchange (CVE-2026-40453).
 *
 * Camel copies the standard JMS header fields (JMSCorrelationID, JMSReplyTo, ...)
 * without consulting the strategy, so request-reply is unaffected. Outbound
 * headers are left to the wrapped strategy.
 */
public class InboundHeaderAllowList implements HeaderFilterStrategy {
    private final HeaderFilterStrategy outbound;
    private final Set<String> allowed;

    public InboundHeaderAllowList(final HeaderFilterStrategy outbound, final Collection<String> allowed) {
        this.outbound = Objects.requireNonNull(outbound);
        this.allowed = Collections.unmodifiableSet(new HashSet<>(Objects.requireNonNull(allowed)));
    }

    @Override
    public boolean applyFilterToCamelHeaders(final String headerName, final Object headerValue, final Exchange exchange) {
        return outbound.applyFilterToCamelHeaders(headerName, headerValue, exchange);
    }

    @Override
    public boolean applyFilterToExternalHeaders(final String headerName, final Object headerValue, final Exchange exchange) {
        return headerName == null || !allowed.contains(headerName);
    }

    public Set<String> getAllowed() {
        return allowed;
    }
}
