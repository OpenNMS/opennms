/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.collection.persistence.newts;

import org.opennms.core.soa.lookup.ServiceLookup;
import org.opennms.core.soa.lookup.ServiceLookupBuilder;
import org.opennms.core.soa.support.DefaultServiceRegistry;
import org.opennms.netmgt.collection.api.Persister;
import org.opennms.netmgt.collection.api.PersisterFactory;
import org.opennms.netmgt.collection.api.ServiceParameters;
import org.opennms.netmgt.newts.NewtsWriter;
import org.opennms.netmgt.rrd.RrdRepository;
import org.opennms.newts.api.Context;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Factory for {@link org.opennms.netmgt.collection.persistence.newts.NewtsPersister}.
 *
 * @author jwhite
 */
public class NewtsPersisterFactory implements PersisterFactory {

    @Autowired
    private NewtsWriter m_newtsWriter;

    @Autowired
    private Context m_context;

    @Override
    public Persister createPersister(ServiceParameters params, RrdRepository repository) {
        return createPersister(params, repository, false, false, false);
    }

    @Override
    public Persister createPersister(ServiceParameters params, RrdRepository repository, boolean dontPersistCounters,
            boolean forceStoreByGroup, boolean dontReorderAttributes) {
        // We ignore the forceStoreByGroup flag since we always store by group, and we ignore
        // the dontReorderAttributes flag since attribute order does not matter
        NewtsPersister persister =  new NewtsPersister(params, repository, m_newtsWriter, m_context);
        persister.setIgnorePersist(dontPersistCounters);
        // load kafka persister if it is enabled 
        Persister kafkaPersister = loadKafkaPersister(params, repository);
        persister.setKafkaPersister(kafkaPersister);
        return persister;
    }
    

    private Persister loadKafkaPersister(ServiceParameters params, RrdRepository repository) {
        Boolean enabled = new Boolean(System.getProperty("org.opennms.timeseries.kafka.persister", "false"));
        Persister persister = null;
        if (enabled) {
            final ServiceLookup serviceLookup = new ServiceLookupBuilder(DefaultServiceRegistry.INSTANCE).blocking()
                    .build();
            PersisterFactory persisterFactory = serviceLookup.lookup(PersisterFactory.class, "(strategy=kafka)");
            persister = persisterFactory.createPersister(params, repository);
        }
        return persister;
    }
}
