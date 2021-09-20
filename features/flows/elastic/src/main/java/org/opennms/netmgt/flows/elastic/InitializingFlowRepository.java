/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.flows.elastic;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import org.opennms.features.jest.client.ConnectionPoolShutdownException;
import org.opennms.features.jest.client.template.DefaultTemplateInitializer;
import org.opennms.features.jest.client.template.IndexSettings;
import org.opennms.netmgt.flows.api.Flow;
import org.opennms.netmgt.flows.api.FlowException;
import org.opennms.netmgt.flows.api.FlowRepository;
import org.opennms.netmgt.flows.api.FlowSource;
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
    public void persist(Collection<Flow> flows, FlowSource source) throws FlowException {
        try {
            ensureInitialized();
            delegate.persist(flows, source);
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
