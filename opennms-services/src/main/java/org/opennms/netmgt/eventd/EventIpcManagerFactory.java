/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
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

import org.opennms.netmgt.model.events.EventIpcManager;
import org.springframework.util.Assert;

/**
 * <p>EventIpcManagerFactory class.</p>
 *
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya Nataraj </A>
 * @author <A HREF="http://www.opennms.org">OpenNMS.org </A>
 */
public abstract class EventIpcManagerFactory {
	/**
     * The EventIpcManager instance.
     */
    private static EventIpcManager m_ipcManager;

    /**
     * Create the singleton instance of this factory
     */
    public static synchronized void init() {
    }

    /**
     * Returns an implementation of the default EventIpcManager class
     *
     * @return a {@link org.opennms.netmgt.model.events.EventIpcManager} object.
     */
    public static EventIpcManager getIpcManager() {
        Assert.state(m_ipcManager != null, "this factory has not been initialized");
        return m_ipcManager;
    }

    /**
     * <p>setIpcManager</p>
     *
     * @param ipcManager a {@link org.opennms.netmgt.model.events.EventIpcManager} object.
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
