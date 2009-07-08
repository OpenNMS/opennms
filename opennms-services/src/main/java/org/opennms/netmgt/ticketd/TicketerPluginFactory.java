/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * Copyright (C) 2007-2008 The OpenNMS Group, Inc.  All rights reserved.
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

package org.opennms.netmgt.ticketd;

import org.springframework.beans.factory.FactoryBean;
import org.opennms.api.integration.ticketing.*;

/**
 * 
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 */
public class TicketerPluginFactory implements FactoryBean {

    private Class m_pluginClass;
    private Plugin m_ticketerPlugin;

    public void setPluginClass(Class pluginClass) {
        m_pluginClass = pluginClass;
    }
    
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

    public Class getObjectType() {
        return (m_pluginClass == null ? Plugin.class : m_pluginClass);
    }

    public boolean isSingleton() {
        return true;
    }
    
}
