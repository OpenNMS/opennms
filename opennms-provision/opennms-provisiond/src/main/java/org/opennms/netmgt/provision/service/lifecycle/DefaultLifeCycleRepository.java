/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.provision.service.lifecycle;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opennms.core.tasks.BatchTask;
import org.opennms.core.tasks.DefaultTaskCoordinator;

/**
 * <p>DefaultLifeCycleRepository class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class DefaultLifeCycleRepository implements LifeCycleRepository {
    
    final private Map<String, LifeCycle> m_lifeCycles = new HashMap<String, LifeCycle>();
    
    final private DefaultTaskCoordinator m_coordinator;
    
    /**
     * <p>Constructor for DefaultLifeCycleRepository.</p>
     *
     * @param coordinator a {@link org.opennms.core.tasks.DefaultTaskCoordinator} object.
     */
    public DefaultLifeCycleRepository(DefaultTaskCoordinator coordinator) {
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
