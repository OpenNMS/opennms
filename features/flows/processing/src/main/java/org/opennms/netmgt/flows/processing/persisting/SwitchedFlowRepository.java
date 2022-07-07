/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.flows.processing.persisting;

import java.time.Duration;
import java.util.Collection;
import java.util.Objects;

import org.opennms.netmgt.flows.api.FlowException;
import org.opennms.netmgt.flows.processing.enrichment.EnrichedFlow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.swrve.ratelimitedlogger.RateLimitedLog;

/**
 * Eventually forwards flows to delegate flow repository.
 *
 * Whether the flows are forwarded can be controlled by a property.
 */
public class SwitchedFlowRepository implements FlowRepository {

    private static final Logger LOG = LoggerFactory.getLogger(SwitchedFlowRepository.class);

    private final RateLimitedLog RATE_LIMITED_LOGGER = RateLimitedLog
            .withRateLimit(LOG)
            .maxRate(5).every(Duration.ofSeconds(30))
            .build();

    private final FlowRepository delegate;

    private boolean enabled = true;

    public SwitchedFlowRepository(final FlowRepository delegate) {
        this.delegate = Objects.requireNonNull(delegate);
    }

    @Override
    public void persist(final Collection<EnrichedFlow> flows) throws FlowException {
        if (!this.enabled) {
            RATE_LIMITED_LOGGER.info("Flow persistence disabled for {}. Dropping {} flow documents.", this.delegate, flows.size());
            return;
        }

        this.delegate.persist(flows);
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isDisabled() { return !this.enabled; }

    public void setDisabled(final boolean disabled) {
        this.enabled = !disabled;
    }
}
