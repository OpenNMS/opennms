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

import java.util.Map;
import java.util.concurrent.Callable;

import org.opennms.api.integration.ticketing.Plugin;
import org.opennms.api.integration.ticketing.PluginException;
import org.opennms.api.integration.ticketing.Ticket;
import org.opennms.core.logging.Logging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Ticketer plugin that reference an implementation pulled from the OSGi Service Registry.
 *
 * The first plugin to be registered is used until it is unregistered.
 *
 * @author jwhite
 */
public class OSGiBasedTicketerPlugin implements Plugin {
    private static final Logger LOG = LoggerFactory.getLogger(OSGiBasedTicketerPlugin.class);

    private Plugin m_ticketerPlugin = null;
    
    @Override
    public Ticket get(String ticketId) throws PluginException {
        return getTicketerPlugin().get(ticketId);
    }

    @Override
    public void saveOrUpdate(Ticket ticket) throws PluginException {
        getTicketerPlugin().saveOrUpdate(ticket);
    }

    /**
     * Registers the {@link Plugin} use. Only the first registered plugin will be used.
     *
     * This method is called by the OSGi Service Registry when a service implementing
     * the {@link Plugin} interface is registered.
     *
     * @param plugin the plugin
     * @param attributes service-level attributes for the plugin
     */
    public void registerTicketerPlugin(Plugin plugin, Map<String, String> attributes) {
        Logging.withPrefix(TroubleTicketer.getLoggingCategory(), new Runnable() {
            @Override
            public void run() {
                if (m_ticketerPlugin == null) {
                    LOG.info("Registering ticketer plugin {} with attributes {}", plugin, attributes);
                    m_ticketerPlugin = plugin;
                } else {
                    LOG.warn("Ticketer plugin {} will not be registered, since {} is already active.", plugin, m_ticketerPlugin);
                }
            }
        });
    }

    /**
     * Unregisters a previously registered {@link Plugin} use.
     *
     * This method is called by the OSGi Service Registry when a service implementing
     * the {@link Plugin} interface is unregistered.
     *
     * @param plugin the plugin
     * @param attributes service-level attributes for the plugin
     * @return true if the plugin was previously in use, false otherwise
     * @throws Exception 
     */
    public boolean unregisterTicketerPlugin(Plugin plugin, Map<String, String> attributes) throws Exception {
       return Logging.withPrefix(TroubleTicketer.getLoggingCategory(), new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                if (m_ticketerPlugin == plugin) {
                    LOG.info("Unregistering ticketer plugin {} with attributes {}", plugin, attributes);
                    m_ticketerPlugin = null;
                    return true;
                } else {
                    return false;
                }
            }
        });
    }

    /**
     * Retrieves the plugin that is currently registered.
     * 
     * If no plugin is currently registered, this full will throw a {@link PluginException}
     * instead of returning null.
     *
     * @return the currently registered plugin
     * @throws PluginException when no plugin is currently registered
     */
    public Plugin getTicketerPlugin() throws PluginException {
        if (m_ticketerPlugin == null) {
            throw new PluginException("No ticketing plugin is currently registered.");
        }
        return m_ticketerPlugin;
    }
}
