/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2008-2009 The OpenNMS Group, Inc.  All rights reserved.
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
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

package org.opennms.netmgt.provision.service.lifecycle;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opennms.netmgt.provision.service.tasks.DefaultTaskCoordinator;

public class DefaultLifeCycleRepository implements LifeCycleRepository {
    
    final private Map<String, LifeCycle> m_lifeCycles = new HashMap<String, LifeCycle>();
    
    final private DefaultTaskCoordinator m_coordinator;
    
    public DefaultLifeCycleRepository(DefaultTaskCoordinator coordinator) {
        m_coordinator = coordinator;
    }


    public LifeCycleInstance createNestedLifeCycleInstance(Phase containingPhase, String lifeCycleName, Object... providers) {
        LifeCycle lifeCycle = getLifeCycle(lifeCycleName);
        
        return new DefaultLifeCycleInstance(containingPhase, this, m_coordinator, lifeCycle.getLifeCycleName(), lifeCycle.getPhaseNames(), providers);
    }


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
    
    public void addLifeCycle(LifeCycle lifeCycle) {
        m_lifeCycles.put(lifeCycle.getLifeCycleName(), lifeCycle);
    }

    public void setLifeCycles(List<LifeCycle> lifecycles) {
        m_lifeCycles.clear();
        for (LifeCycle l : lifecycles) {
            addLifeCycle(l);
        }
    }
}