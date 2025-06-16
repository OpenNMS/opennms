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
