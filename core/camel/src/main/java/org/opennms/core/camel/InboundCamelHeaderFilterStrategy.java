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
package org.opennms.core.camel;

import java.util.Locale;
import java.util.Objects;

import org.apache.camel.Exchange;
import org.apache.camel.spi.HeaderFilterStrategy;

/**
 * Drops inbound headers that name a Camel internal header, matching the name
 * case-insensitively, and otherwise defers to the wrapped strategy.
 *
 * The stock JMS strategies pass inbound Camel* headers through (or only match
 * them case-sensitively), so a JMS producer could set internal headers such
 * as CamelExecCommandExecutable on a consumer's exchange (CVE-2026-40453).
 */
public class InboundCamelHeaderFilterStrategy implements HeaderFilterStrategy {
    private final HeaderFilterStrategy delegate;

    public InboundCamelHeaderFilterStrategy(HeaderFilterStrategy delegate) {
        this.delegate = Objects.requireNonNull(delegate);
    }

    @Override
    public boolean applyFilterToCamelHeaders(String headerName, Object headerValue, Exchange exchange) {
        return delegate.applyFilterToCamelHeaders(headerName, headerValue, exchange);
    }

    @Override
    public boolean applyFilterToExternalHeaders(String headerName, Object headerValue, Exchange exchange) {
        return isCamelHeader(headerName) || delegate.applyFilterToExternalHeaders(headerName, headerValue, exchange);
    }

    static boolean isCamelHeader(String headerName) {
        if (headerName == null) {
            return false;
        }
        final String name = headerName.toLowerCase(Locale.ROOT);
        return name.startsWith("camel") || name.startsWith("org.apache.camel.");
    }
}
