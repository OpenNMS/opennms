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
package org.opennms.netmgt.enlinkd.service.impl;

import java.util.concurrent.atomic.AtomicBoolean;

import org.opennms.netmgt.enlinkd.persistence.api.TopologyEntityCache;
import org.opennms.netmgt.enlinkd.service.api.TopologyService;

public class TopologyServiceImpl implements TopologyService {

    private TopologyEntityCache m_topologyEntityCache;
    private final AtomicBoolean m_updates = new AtomicBoolean(false);

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
