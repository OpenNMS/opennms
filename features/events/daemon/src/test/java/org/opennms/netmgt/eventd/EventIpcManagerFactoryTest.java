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
package org.opennms.netmgt.eventd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.events.api.EventIpcManager;
import org.opennms.netmgt.events.api.EventIpcManagerFactory;
import org.opennms.test.ThrowableAnticipator;

/**
 * Test case for EventIpcManagerFactory.
 * 
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class EventIpcManagerFactoryTest {
    private EventIpcManager m_manager;

    @Before
    @SuppressWarnings("deprecation")
    public void setUp() throws Exception {
        EventIpcManagerFactory.reset();
    }

    @Test
    public void testSetIpcManager() {
        m_manager = mock(EventIpcManager.class);
        EventIpcManagerFactory.setIpcManager(m_manager);
        assertNotNull("manager should not be null", EventIpcManagerFactory.getIpcManager());
        assertEquals("manager", m_manager, EventIpcManagerFactory.getIpcManager());
        verifyNoMoreInteractions(m_manager);
    }

    @Test
    public void testSetIpcManagerNull() {
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalArgumentException("argument ipcManager must not be null"));
        
        try {
            EventIpcManagerFactory.setIpcManager(null);
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        
        ta.verifyAnticipated();
    }

    @Test
    public void testGetIpcManagerNotInitialized() {
        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new IllegalStateException("this factory has not been initialized"));
        
        try {
            EventIpcManagerFactory.getIpcManager();
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        
        ta.verifyAnticipated();
    }

}
