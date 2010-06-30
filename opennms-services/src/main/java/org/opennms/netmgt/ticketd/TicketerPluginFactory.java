/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: May 22, 2007
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
package org.opennms.netmgt.ticketd;

import org.springframework.beans.factory.FactoryBean;
import org.opennms.api.integration.ticketing.*;

/**
 * <p>TicketerPluginFactory class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @version $Id: $
 */
public class TicketerPluginFactory implements FactoryBean {

    private Class m_pluginClass;
    private Plugin m_ticketerPlugin;

    /**
     * <p>setPluginClass</p>
     *
     * @param pluginClass a {@link java.lang.Class} object.
     */
    public void setPluginClass(Class pluginClass) {
        m_pluginClass = pluginClass;
    }
    
    /**
     * <p>getObject</p>
     *
     * @return a {@link java.lang.Object} object.
     * @throws java.lang.Exception if any.
     */
    public Object getObject() throws Exception {
        if (m_pluginClass == null) {
            throw new IllegalStateException("pluginClass must be set");
        }
        if (!Plugin.class.isAssignableFrom(m_pluginClass)) {
            throw new IllegalArgumentException("The class "+m_pluginClass+" is not assignable to TicketPlugin class");
        }
        if (m_ticketerPlugin == null) {
            m_ticketerPlugin = (Plugin)m_pluginClass.newInstance();
        }
        return m_ticketerPlugin;
        
    }

    /**
     * <p>getObjectType</p>
     *
     * @return a {@link java.lang.Class} object.
     */
    public Class getObjectType() {
        return (m_pluginClass == null ? Plugin.class : m_pluginClass);
    }

    /**
     * <p>isSingleton</p>
     *
     * @return a boolean.
     */
    public boolean isSingleton() {
        return true;
    }
    
}
