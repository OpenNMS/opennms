/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2018 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.threshd;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.opennms.core.rpc.utils.mate.EntityScopeProvider;
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

    private Map<ThresholdingSessionKey, ThresholdingSet> thresholdingSets = new HashMap<>();
    
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
        ThresholdingSessionKey key = session.getKey();
        ThresholdingSet tSet = thresholdingSets.get(key);
        if (tSet == null) {
            tSet = new ThresholdingSetImpl(key.getNodeId(), key.getLocation(), key.getServiceName(),
                    ((ThresholdingSessionImpl) session).getRrdRepository(),
                    ((ThresholdingSessionImpl) session).getServiceParameters(),
                    ((ThresholdingSessionImpl) session).getResourceDao(), eventProxy, session, threshdDao,
                    thresholdingDao, pollOutagesDao, ifLabelDao, entityScopeProvider);
            thresholdingSets.put(key, tSet);
        }
        return tSet;
    }

    @Override
    public void reinitializeThresholdingSets() {
        thresholdingSets.values().forEach(set -> set.reinitialize());
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
