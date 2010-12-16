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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;
import java.util.concurrent.Executors;

import org.junit.Before;
import org.junit.Test;
import org.opennms.core.tasks.DefaultTaskCoordinator;
import org.opennms.test.mock.MockLogAppender;


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
        DefaultTaskCoordinator coordinator = new DefaultTaskCoordinator("LifeCycleTest", Executors.newSingleThreadExecutor());
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
