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
package org.opennms.netmgt.collection.persistence.tcp;

import org.opennms.netmgt.collection.api.Persister;
import org.opennms.netmgt.collection.api.PersisterFactory;
import org.opennms.netmgt.collection.api.ServiceParameters;
import org.opennms.netmgt.model.ResourceTypeUtils;
import org.opennms.netmgt.rrd.RrdRepository;

public class TcpPersisterFactory implements PersisterFactory {

    private TcpOutputStrategy m_tcpStrategy;

    @Override
    public Persister createPersister(ServiceParameters params, RrdRepository repository) {
        return createPersister(params, repository, false, false, false);
    }

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
     * Creates a new TcpPersister object when storeByGroup is enabled.
     *
     * @param params the service parameters
     * @param repository the repository
     * @param dontPersistCounters the don't persist counters
     * @return the persister
     */
    public Persister createGroupPersister(ServiceParameters params, RrdRepository repository, boolean dontPersistCounters) {
        TcpGroupPersister persister = new TcpGroupPersister(params, repository, m_tcpStrategy);
        persister.setIgnorePersist(dontPersistCounters);
        return persister;
    }

    /**
     * Creates a new TcpPersister object when storeByGroup is disabled.
     *
     * @param params the service parameters
     * @param repository the repository
     * @param dontPersistCounters the don't persist counters
     * @return the persister
     */
    public Persister createOneToOnePersister(ServiceParameters params, RrdRepository repository, boolean dontPersistCounters) {
        TcpSinglePersister persister = new TcpSinglePersister(params, repository, m_tcpStrategy);
        persister.setIgnorePersist(dontPersistCounters);
        return persister;
    }

    public TcpOutputStrategy getTcpStrategy() {
        return m_tcpStrategy;
    }

    public void setTcpStrategy(TcpOutputStrategy tcpStrategy) {
        m_tcpStrategy = tcpStrategy;
    }
}
