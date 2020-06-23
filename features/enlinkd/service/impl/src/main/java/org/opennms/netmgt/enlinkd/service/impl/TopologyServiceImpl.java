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

package org.opennms.netmgt.enlinkd.service.impl;

import java.util.concurrent.atomic.AtomicBoolean;

import org.opennms.netmgt.enlinkd.persistence.api.TopologyEntityCache;
import org.opennms.netmgt.enlinkd.service.api.TopologyService;

public class TopologyServiceImpl implements TopologyService {

    private TopologyEntityCache m_topologyEntityCache;
    private AtomicBoolean m_updates = new AtomicBoolean(false);

    @Override
    public  boolean parseUpdates() {
        if (m_updates.get()) {
            m_updates.set(false);
            return true;
        }
        return false;            
    }

    @Override
    public void updatesAvailable() {
            m_updates.set(true);
    }

    @Override
    public boolean hasUpdates() {
            return m_updates.get();
    }

    @Override
    public void refresh() {
        m_topologyEntityCache.refresh();
    }

    public TopologyEntityCache getTopologyEntityCache() {
        return m_topologyEntityCache;
    }

    public void setTopologyEntityCache(TopologyEntityCache topologyEntityCache) {
        m_topologyEntityCache = topologyEntityCache;
    }


}
