//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2007 Apr 06: Add a reset method for unit tests and clean up a bit. - dj@opennms.org
// 2007 Mar 21: Added assertions. - dj@opennms.org
//
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.                                                            
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//       
// For more information contact: 
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
// Tab Size = 8
//

package org.opennms.netmgt.eventd;

import org.springframework.util.Assert;

/**
 * <p>EventIpcManagerFactory class.</p>
 *
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya Nataraj </A>
 * @author <A HREF="http://www.opennms.org">OpenNMS.org </A>
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya Nataraj </A>
 * @author <A HREF="http://www.opennms.org">OpenNMS.org </A>
 * @version $Id: $
 */
public class EventIpcManagerFactory {
	/**
     * The EventIpcManager instance.
     */
    private static EventIpcManager m_ipcManager;

    /**
     * This class only has static methods.
     */
    private EventIpcManagerFactory() {
    }

    /**
     * Create the singleton instance of this factory
     */
    public static synchronized void init() {
    }

    /**
     * Returns an implementation of the default EventIpcManager class
     *
     * @return a {@link org.opennms.netmgt.eventd.EventIpcManager} object.
     */
    public static EventIpcManager getIpcManager() {
        Assert.state(m_ipcManager != null, "this factory has not been initialized");
        return m_ipcManager;
    }

    /**
     * <p>setIpcManager</p>
     *
     * @param ipcManager a {@link org.opennms.netmgt.eventd.EventIpcManager} object.
     */
    public static void setIpcManager(EventIpcManager ipcManager) {
        Assert.notNull(ipcManager, "argument ipcManager must not be null");
        m_ipcManager = ipcManager;
    }
    
    /**
     * This is here for unit testing so we can reset this class before
     * every test.
     */
    protected static void reset() {
        m_ipcManager = null;
    }

}
