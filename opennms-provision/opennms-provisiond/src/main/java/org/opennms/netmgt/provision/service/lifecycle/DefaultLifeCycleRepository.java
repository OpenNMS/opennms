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
package org.opennms.netmgt.provision.service.lifecycle;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opennms.core.tasks.BatchTask;
import org.opennms.core.tasks.TaskCoordinator;

/**
 * <p>DefaultLifeCycleRepository class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class DefaultLifeCycleRepository implements LifeCycleRepository {
    
    private final Map<String, LifeCycle> m_lifeCycles = new HashMap<String, LifeCycle>();
    
    private final TaskCoordinator m_coordinator;
    
    /**
     * <p>Constructor for DefaultLifeCycleRepository.</p>
     *
     * @param coordinator a {@link org.opennms.core.tasks.TaskCoordinator} object.
     */
    public DefaultLifeCycleRepository(TaskCoordinator coordinator) {
        m_coordinator = coordinator;
    }


    /**
     * <p>createNestedLifeCycleInstance</p>
     *
     * @param containingPhase a {@link org.opennms.core.tasks.BatchTask} object.
     * @param lifeCycleName a {@link java.lang.String} object.
     * @param providers a {@link java.lang.Object} object.
     * @return a {@link org.opennms.netmgt.provision.service.lifecycle.LifeCycleInstance} object.
     */
    @Override
    public LifeCycleInstance createNestedLifeCycleInstance(BatchTask containingPhase, String lifeCycleName, Object... providers) {
        LifeCycle lifeCycle = getLifeCycle(lifeCycleName);
        
        return new DefaultLifeCycleInstance(containingPhase, this, m_coordinator, lifeCycle.getLifeCycleName(), lifeCycle.getPhaseNames(), providers);
    }


    /**
     * <p>createLifeCycleInstance</p>
     *
     * @param lifeCycleName a {@link java.lang.String} object.
     * @param providers a {@link java.lang.Object} object.
     * @return a {@link org.opennms.netmgt.provision.service.lifecycle.LifeCycleInstance} object.
     */
    @Override
    public LifeCycleInstance createLifeCycleInstance(String lifeCycleName, Object... providers) {
        LifeCycle lifeCycle = getLifeCycle(lifeCycleName);
        
        return new DefaultLifeCycleInstance(this, m_coordinator, lifeCycle.getLifeCycleName(), lifeCycle.getPhaseNames(), providers);
    }

    private LifeCycle getLifeCycle(String lifeCycleName) {
        LifeCycle lifeCycle = m_lifeCycles.get(lifeCycleName);

        if (lifeCycle == null) {
            throw new IllegalArgumentException("Unable to find a definition for lifecycle "+lifeCycleName);
        }

        return lifeCycle;
    }
    
    /**
     * <p>addLifeCycle</p>
     *
     * @param lifeCycle a {@link org.opennms.netmgt.provision.service.lifecycle.LifeCycle} object.
     */
    public void addLifeCycle(LifeCycle lifeCycle) {
        m_lifeCycles.put(lifeCycle.getLifeCycleName(), lifeCycle);
    }

    /**
     * <p>setLifeCycles</p>
     *
     * @param lifecycles a {@link java.util.List} object.
     */
    public void setLifeCycles(List<LifeCycle> lifecycles) {
        m_lifeCycles.clear();
        for (LifeCycle l : lifecycles) {
            addLifeCycle(l);
        }
    }
}
