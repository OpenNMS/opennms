/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.service.lifecycle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * LifeCycle
 *
 * @author brozow
 * @version $Id: $
 */
public class LifeCycle {
   
    private static final String[] OF_STRINGS = new String[0];
    
    private final String m_lifeCycleName;
    private final List<String> m_phases;
    
    /**
     * <p>Constructor for LifeCycle.</p>
     *
     * @param lifeCycleName a {@link java.lang.String} object.
     */
    public LifeCycle(String lifeCycleName) {
        this(lifeCycleName, new ArrayList<String>());
    }

    /**
     * <p>Constructor for LifeCycle.</p>
     *
     * @param lifeCycleName a {@link java.lang.String} object.
     * @param phaseNames a {@link java.util.List} object.
     */
    public LifeCycle(String lifeCycleName, List<String> phaseNames) {
        m_lifeCycleName = lifeCycleName;
        m_phases = phaseNames;
    }

    /**
     * <p>getLifeCycleName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getLifeCycleName() {
        return m_lifeCycleName;
    }

    /**
     * <p>addPhase</p>
     *
     * @param phaseName a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.provision.service.lifecycle.LifeCycle} object.
     */
    public LifeCycle addPhase(String phaseName) {
        m_phases.add(phaseName);
        return this;
    }

    /**
     * <p>addPhases</p>
     *
     * @param phases a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.provision.service.lifecycle.LifeCycle} object.
     */
    public LifeCycle addPhases(String... phases) {
        m_phases.addAll(Arrays.asList(phases));
        return this;
    }

    /**
     * <p>getPhaseNames</p>
     *
     * @return an array of {@link java.lang.String} objects.
     */
    public String[] getPhaseNames() {
        return m_phases.toArray(OF_STRINGS);
    }

}
