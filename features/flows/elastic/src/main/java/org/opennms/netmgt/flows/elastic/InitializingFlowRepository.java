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
package org.opennms.netmgt.flows.elastic;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import org.opennms.features.jest.client.ConnectionPoolShutdownException;
import org.opennms.features.jest.client.template.DefaultTemplateInitializer;
import org.opennms.features.jest.client.template.IndexSettings;
import org.opennms.integration.api.v1.flows.Flow;
import org.opennms.integration.api.v1.flows.FlowException;
import org.opennms.integration.api.v1.flows.FlowRepository;
import org.opennms.netmgt.flows.api.UnrecoverableFlowException;
import org.osgi.framework.BundleContext;

import io.searchbox.client.JestClient;

/**
 * This {@link FlowRepository} wrapper will ensure that the repository has
 * been initialized before any *write* calls are made to the given delegate.
 */
public class InitializingFlowRepository implements FlowRepository {

    private final List<DefaultTemplateInitializer> initializers;
    private final FlowRepository delegate;
    private final AtomicBoolean initialized = new AtomicBoolean(false);

    public InitializingFlowRepository(final BundleContext bundleContext, final FlowRepository delegate, final JestClient client,
                                      final IndexSettings rawIndexSettings,
                                      final IndexSettings aggIndexSettings) {
        this(delegate, new RawIndexInitializer(bundleContext, client, rawIndexSettings), new AggregateIndexInitializer(bundleContext, client, aggIndexSettings));
    }

    protected InitializingFlowRepository(final FlowRepository delegate, final JestClient client) {
        this(delegate, new RawIndexInitializer(client), new AggregateIndexInitializer(client));
    }

    private InitializingFlowRepository(final FlowRepository delegate, final DefaultTemplateInitializer... initializers) {
        this.delegate = Objects.requireNonNull(delegate);
        this.initializers = Arrays.asList(initializers);
    }

    @Override
    public void persist(final Collection<? extends Flow> flows) throws FlowException {
        try {
            ensureInitialized();
            delegate.persist(flows);
        } catch (ConnectionPoolShutdownException ex) {
            throw new UnrecoverableFlowException(ex.getMessage(), ex);
        }
    }

    private void ensureInitialized() {
        if (initialized.get()) {
            return;
        }
        for (DefaultTemplateInitializer initializer : initializers) {
            if (!initializer.isInitialized()) {
                initializer.initialize();
            }
        }
        initialized.set(true);
    }

}
