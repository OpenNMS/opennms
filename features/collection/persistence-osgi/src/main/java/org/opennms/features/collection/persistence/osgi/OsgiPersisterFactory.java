/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.features.collection.persistence.osgi;

import org.opennms.core.soa.lookup.ServiceLookup;
import org.opennms.core.soa.lookup.ServiceLookupBuilder;
import org.opennms.core.soa.support.DefaultServiceRegistry;
import org.opennms.netmgt.collection.api.Persister;
import org.opennms.netmgt.collection.api.PersisterFactory;
import org.opennms.netmgt.collection.api.ServiceParameters;
import org.opennms.netmgt.rrd.RrdRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OsgiPersisterFactory implements PersisterFactory {

    private static final Logger LOG = LoggerFactory.getLogger(OsgiPersisterFactory.class);

    private final ServiceLookup serviceLookup;

    public OsgiPersisterFactory() {
        this(false);
    }

    public OsgiPersisterFactory(boolean blocking) {
        if (blocking) {
            serviceLookup = new ServiceLookupBuilder(DefaultServiceRegistry.INSTANCE)
                    .blocking()
                    .build();
        } else {
            serviceLookup = new ServiceLookupBuilder(DefaultServiceRegistry.INSTANCE)
                    .build();
        }
    }

    @Override
    public Persister createPersister(ServiceParameters params, RrdRepository repository) {
        final PersisterFactory delegate = getDelegate();
        // atleast one of the persister factory should be on osgi registry, else it will use Null Persister.
        if (delegate == null) {
            LOG.info("Unable to find any persister factory from osgi registry, use NullPersister");
            return new NullPersister();
        }
        return delegate.createPersister(params, repository);
    }

    @Override
    public Persister createPersister(ServiceParameters params, RrdRepository repository, boolean dontPersistCounters,
            boolean forceStoreByGroup, boolean dontReorderAttributes) {
        final PersisterFactory delegate = getDelegate();
        // atleast one of the persister factory should be on osgi registry, else it will use Null Persister.
        if (delegate == null) {
            LOG.info("Unable to find any persister factory from osgi registry, use NullPersister");
            return new NullPersister();
        }
        return delegate.createPersister(params, repository, dontPersistCounters, forceStoreByGroup, dontReorderAttributes);
    }

    private PersisterFactory getDelegate() {
        // Find the first concrete persister factory implementation
        // Exclude the delegate strategy to avoid circular calls since it may also be exposed in the service registry
        return serviceLookup.lookup(PersisterFactory.class, "(!(strategy=delegate))");
    }

}
