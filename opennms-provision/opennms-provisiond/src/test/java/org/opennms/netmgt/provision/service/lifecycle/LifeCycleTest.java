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
