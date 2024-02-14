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
package org.opennms.netmgt.threshd;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.opennms.core.mate.api.EntityScopeProvider;
import org.opennms.netmgt.config.dao.outages.api.ReadablePollOutagesDao;
import org.opennms.netmgt.config.dao.thresholding.api.ReadableThreshdDao;
import org.opennms.netmgt.config.dao.thresholding.api.ReadableThresholdingDao;
import org.opennms.netmgt.dao.api.IfLabel;
import org.opennms.netmgt.threshd.api.ThresholdInitializationException;
import org.opennms.netmgt.threshd.api.ThresholdingEventProxy;
import org.opennms.netmgt.threshd.api.ThresholdingSession;
import org.opennms.netmgt.threshd.api.ThresholdingSessionKey;
import org.opennms.netmgt.threshd.api.ThresholdingSet;
import org.opennms.netmgt.threshd.api.ThresholdingSetPersister;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * HashMap implementation of a {@link ThresholdingSetPersister}.
 */
public class DefaultThresholdingSetPersister implements ThresholdingSetPersister
{

    private ConcurrentMap<ThresholdingSessionKey, ThresholdingSet> thresholdingSets = new ConcurrentHashMap<>();
    
    @Autowired
    private ReadableThreshdDao threshdDao;
    
    @Autowired
    private ReadableThresholdingDao thresholdingDao;
    
    @Autowired
    private ReadablePollOutagesDao pollOutagesDao;
    
    @Autowired
    private IfLabel ifLabelDao;
    
    @Autowired
    private EntityScopeProvider entityScopeProvider;

    @Override
    public void persistSet(ThresholdingSession session, ThresholdingSet set) {
        thresholdingSets.put(session.getKey(), set);
    }

    @Override
    public ThresholdingSet getThresholdingSet(ThresholdingSession session, ThresholdingEventProxy eventProxy) throws ThresholdInitializationException {
        // Hacky way to use `computeIfAbsent` with something that throws a checked exception.
        // This is needed because `computeIfAbsent` is guaranteed to be atomic. In contrast to
        // an implementation using two calls like `get` and `put`.
        try {
            return thresholdingSets.computeIfAbsent(session.getKey(), (key) -> {
                try {
                    return new ThresholdingSetImpl(key.getNodeId(), key.getLocation(), key.getServiceName(),
                                                   ((ThresholdingSessionImpl) session).getServiceParameters(),
                                                   eventProxy, session, threshdDao,
                                                   thresholdingDao, pollOutagesDao, ifLabelDao, entityScopeProvider);
                } catch (final ThresholdInitializationException e) {
                    throw e.wrapUnchecked();
                }
            });
        } catch (final ThresholdInitializationException.Unchecked e) {
            throw e.getChecked();
        }
    }

    @Override
    public void reinitializeThresholdingSets() {
        thresholdingSets.values().forEach(ThresholdingSet::reinitialize);
    }

    @Override
    public void clear(ThresholdingSession session) {
        ThresholdingSessionKey key = session.getKey();
        thresholdingSets.remove(key);
    }

    public void setThreshdDao(ReadableThreshdDao threshdDao) {
        this.threshdDao = Objects.requireNonNull(threshdDao);
    }

    public void setThresholdingDao(ReadableThresholdingDao thresholdingDao) {
        this.thresholdingDao = Objects.requireNonNull(thresholdingDao);
    }

    public void setPollOutagesDao(ReadablePollOutagesDao pollOutagesDao) {
        this.pollOutagesDao = Objects.requireNonNull(pollOutagesDao);
    }

    public void setIfLabelDao(IfLabel ifLabelDao) {
        this.ifLabelDao = Objects.requireNonNull(ifLabelDao);
    }

    public void setEntityScopeProvider(EntityScopeProvider entityScopeProvider) {
        this.entityScopeProvider = Objects.requireNonNull(entityScopeProvider);
    }
}
