/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.eventd;

import static org.easymock.EasyMock.createMock;

import org.opennms.netmgt.model.events.EventIpcManager;
import org.opennms.test.ThrowableAnticipator;

import junit.framework.TestCase;

/**
 * Test case for EventIpcManagerFactory.
 * 
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class EventIpcManagerFactoryTest extends TestCase {
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        EventIpcManagerFactory.reset();
    }
    
    public void testSetIpcManager() {
        EventIpcManager manager = createMock(EventIpcManager.class);
        EventIpcManagerFactory.setIpcManager(manager);
        assertNotNull("manager should not be null", EventIpcManagerFactory.getIpcManager());
        assertEquals("manager", manager, EventIpcManagerFactory.getIpcManager());
    }
    
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
