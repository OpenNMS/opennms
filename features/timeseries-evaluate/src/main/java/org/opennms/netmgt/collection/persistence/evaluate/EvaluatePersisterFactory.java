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
