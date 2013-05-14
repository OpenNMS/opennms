/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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
public class TicketerPluginFactory implements FactoryBean<Plugin> {

    private Class<? extends Plugin> m_pluginClass;
    private Plugin m_ticketerPlugin;

    /**
     * <p>setPluginClass</p>
     *
     * @param pluginClass a {@link java.lang.Class} object.
     */
    public void setPluginClass(Class<? extends Plugin> pluginClass) {
        m_pluginClass = pluginClass;
    }
    
    /**
     * <p>getObject</p>
     *
     * @return a {@link org.opennms.api.integration.ticketing.Plugin} object.
     * @throws java.lang.Exception if any.
     */
    @Override
    public Plugin getObject() throws Exception {
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
    @Override
    public Class<? extends Plugin> getObjectType() {
        return (m_pluginClass == null ? Plugin.class : m_pluginClass);
    }

    /**
     * <p>isSingleton</p>
     *
     * @return a boolean.
     */
    @Override
    public boolean isSingleton() {
        return true;
    }
    
}
