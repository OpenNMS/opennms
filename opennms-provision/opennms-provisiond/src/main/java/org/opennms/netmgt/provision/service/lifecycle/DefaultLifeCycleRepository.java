/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
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
