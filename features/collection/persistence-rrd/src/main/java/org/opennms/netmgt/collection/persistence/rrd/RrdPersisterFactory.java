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
package org.opennms.netmgt.collection.persistence.rrd;

import org.opennms.netmgt.collection.api.Persister;
import org.opennms.netmgt.collection.api.PersisterFactory;
import org.opennms.netmgt.collection.api.ServiceParameters;
import org.opennms.netmgt.dao.api.ResourceStorageDao;
import org.opennms.netmgt.model.ResourceTypeUtils;
import org.opennms.netmgt.rrd.RrdRepository;
import org.opennms.netmgt.rrd.RrdStrategy;

public class RrdPersisterFactory implements PersisterFactory {

    private RrdStrategy<?, ?> m_rrdStrategy;

    private ResourceStorageDao m_resourceStorageDao;

    public Persister createPersister(ServiceParameters params, RrdRepository repository) {
        return createPersister(params, repository, false, false, false);
    }

    @Override
    public Persister createPersister(ServiceParameters params, RrdRepository repository,
            boolean dontPersistCounters, boolean forceStoreByGroup, boolean dontReorderAttributes) {
        if (ResourceTypeUtils.isStoreByGroup() || forceStoreByGroup) {
            return createGroupPersister(params, repository, dontPersistCounters, dontReorderAttributes);
        } else {
            return createOneToOnePersister(params, repository, dontPersistCounters, dontReorderAttributes);
        }
    }

    public Persister createGroupPersister(ServiceParameters params, RrdRepository repository,
            boolean dontPersistCounters, boolean dontReorderAttributes) {
        GroupPersister persister = new GroupPersister(params, repository, m_rrdStrategy, m_resourceStorageDao);
        persister.setIgnorePersist(dontPersistCounters);
        persister.setDontReorderAttributes(dontReorderAttributes);
        return persister;
    }

    public Persister createOneToOnePersister(ServiceParameters params, RrdRepository repository,
            boolean dontPersistCounters, boolean dontReorderAttributes) {
        OneToOnePersister persister = new OneToOnePersister(params, repository, m_rrdStrategy, m_resourceStorageDao);
        persister.setIgnorePersist(dontPersistCounters);
        persister.setDontReorderAttributes(dontReorderAttributes);
        return persister;
    }

    public RrdStrategy<?, ?> getRrdStrategy() {
        return m_rrdStrategy;
    }

    public void setRrdStrategy(RrdStrategy<?, ?> rrdStrategy) {
        m_rrdStrategy = rrdStrategy;
    }

    public ResourceStorageDao getResourceStorageDao() {
        return m_resourceStorageDao;
    }

    public void setResourceStorageDao(ResourceStorageDao resourceStorageDao) {
        m_resourceStorageDao = resourceStorageDao;
    }
}

