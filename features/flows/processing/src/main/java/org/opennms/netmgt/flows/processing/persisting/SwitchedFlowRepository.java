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
package org.opennms.netmgt.flows.processing.persisting;

import java.time.Duration;
import java.util.Collection;
import java.util.Objects;

import org.opennms.integration.api.v1.flows.Flow;
import org.opennms.integration.api.v1.flows.FlowException;
import org.opennms.integration.api.v1.flows.FlowRepository;
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
    public void persist(final Collection<? extends Flow> flows) throws FlowException {
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
