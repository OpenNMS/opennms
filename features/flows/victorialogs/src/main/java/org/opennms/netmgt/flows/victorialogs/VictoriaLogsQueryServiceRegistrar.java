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

package org.opennms.netmgt.flows.victorialogs;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Objects;

import org.opennms.netmgt.flows.api.FlowQueryService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Puts the query service in the OSGi registry, but only when it has been asked for.
 *
 * <h2>Why this is not a {@code <service>} element</h2>
 *
 * Because a blueprint {@code <service>} is unconditional, and for this interface that is unsafe.
 * {@code FlowQueryService} is consumed through a singleton {@code <reference>} in
 * {@code features/flows/rest/impl} and another in {@code features/datachoices} — one implementation
 * is bound, and everything the flow UI shows comes from it. An unconditional registration therefore
 * competes for that binding whether or not anyone enabled this backend.
 *
 * <p>Service ranking does not solve it, which is the trap worth writing down. Aries binds a plain
 * {@code <reference>} <em>reluctantly</em>: {@code ReferenceRecipe.track} re-binds only for a greedy
 * reference, while {@code untrack} re-binds to the best of whatever is left. So the sequence that
 * matters is not a contest of rankings but a departure:
 *
 * <ol>
 *   <li>An operator edits {@code org.opennms.features.flows.persistence.elastic.cfg} — any property.
 *   <li>That placeholder is {@code update-strategy="reload"}, so the Elasticsearch blueprint
 *       container is destroyed and its {@code FlowQueryService} unregisters.
 *   <li>The REST layer's reference untracks and re-binds to the only candidate left — this one, at
 *       any ranking, because ranking cannot break a field of one.
 *   <li>Elasticsearch re-registers moments later. Nothing re-binds, because the reference is
 *       reluctant. The flow UI is served by this backend until the container restarts.
 * </ol>
 *
 * A cold boot has the same shape for a different reason: the Elasticsearch blueprint waits on eight
 * mandatory references before it publishes anything and this module waits on none, so it would
 * ordinarily win the race outright.
 *
 * <p>Not registering at all is the only state that cannot be bound by accident. Hence a flag, and
 * hence this class.
 *
 * <h2>The residual limitation, stated rather than hidden</h2>
 *
 * When the flag <em>is</em> set, this registers at a high ranking so that a reference binding after
 * that point prefers it. A reference that has already bound Elasticsearch will not move, for exactly
 * the reluctant-damping reason above — so enabling the query path on a running system may need the
 * consuming bundles restarted, or simply a restart. That is a prototype limitation, not a design.
 */
public class VictoriaLogsQueryServiceRegistrar {

    private static final Logger LOG = LoggerFactory.getLogger(VictoriaLogsQueryServiceRegistrar.class);

    /**
     * High enough to be preferred over the Elasticsearch registration, which sets none and so ranks
     * zero. Only consulted when a reference binds while both are present; see the class javadoc for
     * why that is a weaker guarantee than it looks.
     */
    private static final int RANKING_WHEN_ENABLED = 1000;

    private final BundleContext bundleContext;
    private final FlowQueryService queryService;
    private final VictoriaLogsClient client;

    /** Off unless configuration says otherwise, like every other part of this backend. */
    private volatile boolean disabled = true;

    private ServiceRegistration<FlowQueryService> registration;

    public VictoriaLogsQueryServiceRegistrar(final BundleContext bundleContext,
                                             final FlowQueryService queryService,
                                             final VictoriaLogsClient client) {
        this.bundleContext = Objects.requireNonNull(bundleContext);
        this.queryService = Objects.requireNonNull(queryService);
        this.client = Objects.requireNonNull(client);
    }

    public void setDisabled(final boolean disabled) {
        this.disabled = disabled;
    }

    public synchronized void start() {
        if (disabled) {
            LOG.debug("VictoriaLogs flow querying is disabled; the query service is not registered.");
            return;
        }
        if (registration != null) {
            return;
        }
        // Enabled is not sufficient; the connection has to be usable. Since a misconfiguration no
        // longer fails the container, registering on the flag alone would hand the flow UI to a
        // backend that logged at startup that it will not be used -- every query answered with an
        // exception, where before the same typo left Elasticsearch serving untouched. Refusing to
        // register is what keeps a bad url a VictoriaLogs problem instead of everyone's.
        final String misconfigured = client.getConfigurationError();
        if (misconfigured != null) {
            LOG.error("VictoriaLogs flow querying is enabled but the client is misconfigured, so "
                    + "the query service is NOT registered and flow queries continue to be served "
                    + "by whatever else is present: {}", misconfigured);
            return;
        }
        final Dictionary<String, Object> properties = new Hashtable<>();
        properties.put(Constants.SERVICE_RANKING, RANKING_WHEN_ENABLED);
        registration = bundleContext.registerService(FlowQueryService.class, queryService, properties);
        LOG.warn("VictoriaLogs is now registered to answer flow queries, at service ranking {}. "
                + "Consumers that have already bound another backend keep it until they rebind.",
                RANKING_WHEN_ENABLED);
    }

    public synchronized void stop() {
        if (registration == null) {
            return;
        }
        try {
            registration.unregister();
        } catch (final IllegalStateException alreadyGone) {
            // The framework unregisters everything this bundle owns when it stops, so losing the
            // race with it is normal and not worth a stack trace.
            LOG.debug("The VictoriaLogs query service was already unregistered.", alreadyGone);
        } finally {
            registration = null;
        }
    }
}
