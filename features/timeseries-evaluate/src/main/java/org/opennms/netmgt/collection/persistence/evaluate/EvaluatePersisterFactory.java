/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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
package org.opennms.netmgt.collection.persistence.evaluate;

import org.opennms.netmgt.collection.api.Persister;
import org.opennms.netmgt.collection.api.PersisterFactory;
import org.opennms.netmgt.collection.api.ServiceParameters;
import org.opennms.netmgt.model.ResourceTypeUtils;
import org.opennms.netmgt.rrd.RrdRepository;

import org.springframework.util.Assert;

/**
 * A factory for creating EvaluatePersister objects.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class EvaluatePersisterFactory implements PersisterFactory {

    /** The evaluation statistics. */
    private EvaluateStats stats;

    /**
     * Instantiates a new evaluate persister factory.
     *
     * @param stats the evaluation statistics object
     */
    public EvaluatePersisterFactory(EvaluateStats stats) {
        Assert.notNull(stats, "EvaluateStats is required");
        this.stats = stats;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collection.api.PersisterFactory#createPersister(org.opennms.netmgt.collection.api.ServiceParameters, org.opennms.netmgt.rrd.RrdRepository)
     */
    @Override
    public Persister createPersister(ServiceParameters params, RrdRepository repository) {
        return createPersister(params, repository, false, false, false);
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.collection.api.PersisterFactory#createPersister(org.opennms.netmgt.collection.api.ServiceParameters, org.opennms.netmgt.rrd.RrdRepository, boolean, boolean, boolean)
     */
    @Override
    public Persister createPersister(ServiceParameters params,
            RrdRepository repository, boolean dontPersistCounters,
            boolean forceStoreByGroup, boolean dontReorderAttributes) {
        if (ResourceTypeUtils.isStoreByGroup() || forceStoreByGroup) {
            return createGroupPersister(params, repository, dontPersistCounters);
        } else {
            return createOneToOnePersister(params, repository, dontPersistCounters);
        }
    }

    /**
     * Creates a new EvaluatePersister object when storeByGroup is enabled.
     *
     * @param params the service parameters
     * @param repository the repository
     * @param dontPersistCounters the don't persist counters
     * @return the persister
     */
    public Persister createGroupPersister(ServiceParameters params, RrdRepository repository, boolean dontPersistCounters) {
        EvaluateGroupPersister persister = new EvaluateGroupPersister(stats, params, repository);
        persister.setIgnorePersist(dontPersistCounters);
        return persister;
    }

    /**
     * Creates a new EvaluatePersister object when storeByGroup is disabled.
     *
     * @param params the service parameters
     * @param repository the repository
     * @param dontPersistCounters the don't persist counters
     * @return the persister
     */
    public Persister createOneToOnePersister(ServiceParameters params, RrdRepository repository, boolean dontPersistCounters) {
        EvaluateSinglePersister persister = new EvaluateSinglePersister(stats, params, repository);
        persister.setIgnorePersist(dontPersistCounters);
        return persister;
    }

}
