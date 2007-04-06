/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * 2007 Apr 06: Reset the factory before each test and add a test for the
 *              non-error case. - dj@opennms.org
 * 
 * Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
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
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.eventd;

import static org.easymock.EasyMock.createMock;

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
