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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.opennms.core.tasks.BatchTask;
import org.opennms.core.tasks.DefaultTaskCoordinator;
import org.opennms.core.tasks.SequenceTask;

class DefaultLifeCycleInstance extends SequenceTask implements LifeCycleInstance {
    
    /*
     * Complications... 
     * separation between 'phase list... lifecycle definition', 'provider set', 'phase running'
     * 
     * also
     * confusion.. about definitions and factories
     * 
     * other notes:
     * phase runners can and should be built at lifecycle creation time rather than 'on the fly'
     * only strange case is 'fan-out' of lifecycles
     * 
     * 
     *  
     */
    
    final BatchTask m_containingPhase;
    final LifeCycleRepository m_repository;
    final DefaultTaskCoordinator m_coordinator;
    final String m_name;
    final Phase[] m_phases;
    final Object[] m_providers;
    final Map<String, Object> m_attributes = new HashMap<String, Object>();
    
    public DefaultLifeCycleInstance(BatchTask containingPhase, LifeCycleRepository repository,
            DefaultTaskCoordinator coordinator, String lifeCycleName, String[] phaseNames, Object[] providers) {

        super(coordinator, containingPhase);
        m_containingPhase = containingPhase;
        m_repository = repository;
        m_coordinator = coordinator;
        m_name = lifeCycleName;
        m_providers = providers;
        
        if (log().isDebugEnabled()) {
            log().debug("Set up default lifecycle instance: " + this);
        }

        m_phases = new Phase[phaseNames.length];
        for(int i = 0; i < phaseNames.length; i++) {
            m_phases[i] = new Phase(this, this, phaseNames[i], m_providers);
            if (log().isDebugEnabled()) {
                log().debug(String.format("Adding phase %s to lifecycle", m_phases[i].getName()));
            }
            add(m_phases[i]);
        }
        
        setAttribute("lifeCycleInstance", this);
    }


    public DefaultLifeCycleInstance(LifeCycleRepository repository, DefaultTaskCoordinator coordinator, String lifeCycleName, String[] phaseNames, Object[] providers) {
        this(null, repository, coordinator, lifeCycleName, phaseNames, providers);
    }

    public List<String> getPhaseNames() {
        List<String> phaseNames = new ArrayList<String>(m_phases.length);
        for(Phase phase : m_phases) {
            phaseNames.add(phase.getName());
        }
        return phaseNames;
    }

    public String getName() {
        return m_name;
    }

    public Object getAttribute(String key) {
        return m_attributes.get(key);
    }
    
    public <T> T getAttribute(String key, T defaultValue) {
        if (getAttribute(key) == null) {
            return defaultValue;
        } else {
            return getAttribute(key, getClass(defaultValue));
        }
        
    }
    
    @SuppressWarnings("unchecked")
    public <T> Class<T> getClass(T t) {
        return (Class<T>) t.getClass();
    }

    public <T> T getAttribute(String key, Class<T> type) {
        return type.cast(getAttribute(key));
    }

    public LifeCycleInstance setAttribute(String key, Object value) {
        m_attributes.put(key, value);
        return this;
    }
    
    public <T> T findAttributeByType(Class<T> clazz) {
        T result = null;
        for(Entry<String, Object> entry : m_attributes.entrySet()) {
            if (clazz.isInstance(entry.getValue())) {
                if (result != null) {
                    throw new IllegalStateException("More than one attribute of type "+clazz+" in lifecycle "+this);
                } else {
                    result = clazz.cast(entry.getValue());
                }
            }
        }
        return result;
    }

    
    public LifeCycleInstance createNestedLifeCycle(BatchTask containingPhase, String lifeCycleName) {
        return m_repository.createNestedLifeCycleInstance(containingPhase, lifeCycleName, m_providers);
    }

    public void trigger() {
        if (m_containingPhase != null) {
            m_containingPhase.add(this);
        } else {
            this.schedule();
        }
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("name", m_name)
            .append("containing phase", m_containingPhase)
            .append("repository", m_repository)
            .append("coordinator", m_coordinator)
            .toString();
   }
}