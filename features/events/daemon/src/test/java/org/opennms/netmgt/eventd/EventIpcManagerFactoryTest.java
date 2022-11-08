/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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
