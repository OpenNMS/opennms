/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2014 The OpenNMS Group, Inc.
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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.opennms.core.tasks.DefaultTaskCoordinator;
import org.opennms.core.tasks.TaskCoordinator;
import org.opennms.core.test.MockLogAppender;


/**
 * LifeCycleDefinitionTest
 *
 * @author brozow
 */
public class LifeCycleTest {
    
    private final String[] m_expectedPhases = new String[] {"phase1", "phase2", "phase3"};
    private DefaultLifeCycleRepository m_lifeCycleRepository;
    
    @Before
    public void setUp() {
        MockLogAppender.setupLogging();
        TaskCoordinator coordinator = new DefaultTaskCoordinator("LifeCycleTest");
        m_lifeCycleRepository = new DefaultLifeCycleRepository(coordinator);
    }


    @Test
    public void testBuildLifeCycle() {
        
        LifeCycle bldr = new LifeCycle("sample");
        for(String phase : m_expectedPhases) {
            bldr.addPhase(phase);
        }
        
        m_lifeCycleRepository.addLifeCycle(bldr);
        
        LifeCycleInstance lifecycle = m_lifeCycleRepository.createLifeCycleInstance("sample");

        
        assertNotNull(lifecycle);
        assertEquals("sample", lifecycle.getName());
        List<String> phases = lifecycle.getPhaseNames();
        assertArrayEquals(m_expectedPhases, phases.toArray(new String[0]));
    }

    @Test
    public void testBuildLifeCycleFromArray() {

        LifeCycle r = new LifeCycle("sample").addPhases(m_expectedPhases);
        
        m_lifeCycleRepository.addLifeCycle(r);
        
        LifeCycleInstance lifecycle = m_lifeCycleRepository.createLifeCycleInstance("sample");

        assertNotNull(lifecycle);
        assertEquals("sample", lifecycle.getName());
        List<String> phases = lifecycle.getPhaseNames();
        assertArrayEquals(m_expectedPhases, phases.toArray(new String[0]));
    }

}
