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

import java.util.List;
import java.util.Objects;

import org.opennms.netmgt.flows.api.FlowException;
import org.opennms.netmgt.flows.api.FlowRepository;
import org.opennms.netmgt.flows.api.NetflowDocument;
import org.opennms.netmgt.flows.config.IndexSettings;

import io.searchbox.client.JestClient;

public class InitializingFlowRepository implements FlowRepository {

    private final ElasticFlowRepositoryInitializer initializer;
    private final FlowRepository delegate;

    public InitializingFlowRepository(final FlowRepository delegate, final JestClient client, final IndexSettings indexSettings) {
        Objects.requireNonNull(client);
        Objects.requireNonNull(delegate);
        Objects.requireNonNull(indexSettings);

        this.initializer = new ElasticFlowRepositoryInitializer(client, indexSettings);
        this.delegate = delegate;
    }

    protected InitializingFlowRepository(final FlowRepository delegate, final JestClient client) {
        this(delegate, client, new IndexSettings());
    }

    @Override
    public void save(List<NetflowDocument> document) throws FlowException {
        ensureInitialized();
        delegate.save(document);
    }

    @Override
    public List<NetflowDocument> findAll(String query) throws FlowException {
        ensureInitialized();
        return delegate.findAll(query);
    }

    @Override
    public String rawQuery(String query) throws FlowException {
        ensureInitialized();
        return delegate.rawQuery(query);
    }

    private void ensureInitialized() {
        if (!initializer.isInitialized()) {
            initializer.initialize();
        }
    }
}
