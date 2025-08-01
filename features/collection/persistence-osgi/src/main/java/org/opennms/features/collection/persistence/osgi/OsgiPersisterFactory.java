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
package org.opennms.features.collection.persistence.osgi;

import org.opennms.core.soa.lookup.ServiceLookup;
import org.opennms.core.soa.lookup.ServiceLookupBuilder;
import org.opennms.core.soa.lookup.ServiceRegistryLookup;
import org.opennms.core.soa.support.DefaultServiceRegistry;
import org.opennms.netmgt.collection.api.Persister;
import org.opennms.netmgt.collection.api.PersisterFactory;
import org.opennms.netmgt.collection.api.ServiceParameters;
import org.opennms.netmgt.rrd.RrdRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OsgiPersisterFactory implements PersisterFactory {

    private static final Logger LOG = LoggerFactory.getLogger(OsgiPersisterFactory.class);

    private final ServiceLookup<Class<?>, String> serviceLookup;

    public OsgiPersisterFactory() {
        this(false);
    }

    public OsgiPersisterFactory(boolean blocking) {
        if (blocking) {
            serviceLookup = new ServiceLookupBuilder(new ServiceRegistryLookup(DefaultServiceRegistry.INSTANCE))
                    .blocking()
                    .build();
        } else {
            serviceLookup = new ServiceLookupBuilder(new ServiceRegistryLookup(DefaultServiceRegistry.INSTANCE))
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
