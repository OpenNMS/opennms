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
import java.util.Arrays;
import java.util.List;


/**
 * LifeCycleBuilder
 *
 * @author brozow
 */
public class LifeCycleDefinition {
    
    private String m_lifeCycleName;
    private List<String> m_phases = new ArrayList<String>();
    private List<Object> m_providers = new ArrayList<Object>();
    
    public LifeCycleDefinition(String lifeCycleName) {
        m_lifeCycleName = lifeCycleName;
    }

    public LifeCycleDefinition addPhase(String phaseName) {
        m_phases.add(phaseName);
        return this;
    }

    public LifeCycleDefinition addPhases(String... phases) {
        m_phases.addAll(Arrays.asList(phases));
        return this;
    }

    public LifeCycleDefinition addProviders(Object... providers) {
        m_providers.addAll(Arrays.asList(providers));
        return this;
    }

    public LifeCycle build() {
        return new DefaultLifeCycle(m_lifeCycleName, m_phases.toArray(new String[0]), m_providers.toArray());
    }


}
