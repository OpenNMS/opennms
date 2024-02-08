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
