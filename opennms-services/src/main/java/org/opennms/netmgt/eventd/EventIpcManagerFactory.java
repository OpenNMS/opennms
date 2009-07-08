/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2002-2008 The OpenNMS Group, Inc.  All rights reserved.
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
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/


package org.opennms.netmgt.eventd;

import org.springframework.util.Assert;

/**
 * 
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya Nataraj </A>
 * @author <A HREF="http://www.opennms.org">OpenNMS.org </A>
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
     */
    public static EventIpcManager getIpcManager() {
        Assert.state(m_ipcManager != null, "this factory has not been initialized");
        return m_ipcManager;
    }

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
