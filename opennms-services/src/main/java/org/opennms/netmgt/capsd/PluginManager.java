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

package org.opennms.netmgt.capsd;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.capsd.CapsdProtocolInfo.Action;
import org.opennms.netmgt.config.CapsdConfig;
import org.opennms.netmgt.config.capsd.Property;
import org.opennms.netmgt.config.capsd.ProtocolConfiguration;
import org.opennms.netmgt.config.capsd.ProtocolPlugin;
import org.opennms.netmgt.config.capsd.Range;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * <p>PluginManager class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class PluginManager implements InitializingBean {
    private CapsdConfig m_capsdConfig;

    /**
     * The map of capsd plugins indexed by protocol.
     */
    private Map<String, Plugin> m_pluginsByProtocol = new TreeMap<String, Plugin>();
    /**
     * The map of capsd plugins indexed by implementing class.
     */
    private Map<String, Plugin> m_pluginsByClass = new TreeMap<String, Plugin>();

    /**
     * <p>Constructor for PluginManager.</p>
     */
    public PluginManager() {
        super();
    }

    /**
     * Now load the plugins!
     */
    private void instantiatePlugins() throws ValidationException {
        List<ProtocolPlugin> plugins = getCapsdConfig().getProtocolPlugins();
        for (ProtocolPlugin plugin : plugins) {
            try {
                if (m_pluginsByClass.containsKey(plugin.getClassName())) {
                    Plugin oplugin = m_pluginsByClass.get(plugin.getClassName());
                    m_pluginsByProtocol.put(plugin.getProtocol(), oplugin);
                } else {
                    Class<?> cplugin = Class.forName(plugin.getClassName());
                    Object oplugin = cplugin.newInstance();
                    if (!(oplugin instanceof Plugin)) {
                        throw new ValidationException("CapsdConfigFactory: successfully loaded plugin class for protocol " + plugin.getProtocol() + ", class-name = " + plugin.getClassName() + ", however the class is not an instance of " + Plugin.class.getName());
                    }
                    
                    Plugin p = (Plugin) oplugin;
    
                    // map them
                    m_pluginsByClass.put(plugin.getClassName(), p);
                    m_pluginsByProtocol.put(plugin.getProtocol(), p);
                }
            } catch (Throwable t) {
                String message = "CapsdConfigFactory: failed to load plugin for protocol " + plugin.getProtocol() + ", class-name = " + plugin.getClassName() + ", exception = " + t; 
                log().error(message, t);
                throw new ValidationException(message, t);
            }
        }
    }

    private ThreadCategory log() {
        return ThreadCategory.getInstance(getClass());
    }

    /**
     * Returns the list of protocol plugins and the associated actions for the
     * named address. The currently loaded configuration is used to find, build,
     * and return the protocol information. The returns information has all the
     * necessary element to check the address for capabilities.
     *
     * @param address
     *            The address to get protocol information for.
     * @return The array of protocol information instances for the address.
     */
    public CapsdProtocolInfo[] getProtocolSpecification(InetAddress address) {
        /*
         * The list of protocols that will be turned into
         * and array and returned to the caller. These are
         * of type ProtocolInfo
         */
        List<CapsdProtocolInfo> lprotos = new ArrayList<CapsdProtocolInfo>(getCapsdConfig().getConfiguration().getProtocolPluginCount());
    
        // go through all the defined plugins
        List<ProtocolPlugin> plugins = getCapsdConfig().getProtocolPlugins();
        Iterator<ProtocolPlugin> pluginIter = plugins.iterator();
        PLUGINLOOP: while (pluginIter.hasNext()) {
            ProtocolPlugin plugin = pluginIter.next();
            boolean found = false;
    
            /*
             * Loop through the specific and ranges to find out
             * if there is a particular protocol specification
             */
            for (ProtocolConfiguration pluginConf : getCapsdConfig().getProtocolConfigurations(plugin)) {
                // Check specifics first
                List<String> saddrs = getCapsdConfig().getSpecifics(pluginConf);
                Iterator<String> saddrIter = saddrs.iterator();
                while (saddrIter.hasNext() && !found) {
                    String saddr = saddrIter.next();
                    InetAddress taddr = InetAddressUtils.addr(saddr);
                    if (taddr != null && taddr.equals(address)) {
                        found = true;
                    }
                }
    
                // check the ranges
                List<Range> ranges = getCapsdConfig().getRanges(pluginConf);
                Iterator<Range> rangeIter = ranges.iterator();
                while (rangeIter.hasNext() && !found) {
                    Range rng = rangeIter.next();
    
                    InetAddress start = null;
                    start = InetAddressUtils.addr(rng.getBegin());
                    if (start == null) {
                        log().warn("CapsdConfigFactory: failed to convert address " + rng.getBegin() + " to InetAddress");
                        continue;
                    }
    
                    InetAddress stop = null;
                    stop = InetAddressUtils.addr(rng.getEnd());
                    if (stop == null) {
                        log().warn("CapsdConfigFactory: failed to convert address " + rng.getEnd() + " to InetAddress");
                        continue;
                    }
    
                    if (InetAddressUtils.isInetAddressInRange(address.getAddress(), start.getAddress(), stop.getAddress())) {
                        found = true;
                    }
                }
    
                /*
                 * if it has not be found yet then it's not
                 * in this particular plugin conf, check the
                 * next
                 */
                if (!found) {
                    continue;
                }
    
                /* 
                 * if found then build protocol
                 * specification if on, else next protocol.
                 */
                String scan = null;
                if ((scan = pluginConf.getScan()) != null) {
                    if (scan.equals("enable")) {
                        lprotos.add(new CapsdProtocolInfo(plugin.getProtocol(), m_pluginsByProtocol.get(plugin.getProtocol()), null, Action.AUTO_SET));
                        continue PLUGINLOOP;
                    } else if (scan.equals("off")) {
                        continue PLUGINLOOP;
                    }
                } else if ((scan = plugin.getScan()) != null) {
                    if (scan.equals("off")) {
                        continue PLUGINLOOP;
                    }
                }
    
                // it's either on specifically, or by default
                // so map it parameters
                Map<String, Object> params = new TreeMap<String, Object>();
    
                // add the plugin defaults first, then specifics
                addProperties(getCapsdConfig().getPluginProperties(plugin), params);
                addProperties(getCapsdConfig().getProtocolConfigurationProperties(pluginConf), params);
    
                lprotos.add(new CapsdProtocolInfo(plugin.getProtocol(), m_pluginsByProtocol.get(plugin.getProtocol()), params, Action.SCAN));
            } // end ProtocolConfiguration loop
    
            // use default config if not found
            if (!found) {
                // if found then build protocol
                // specification if on, else next protocol.
                if ("off".equals(plugin.getScan())) {
                    continue PLUGINLOOP;
                }
    
                // it's either on specifically, or by default
                // so map it parameters
                Map<String, Object> params = new TreeMap<String, Object>();
                addProperties(getCapsdConfig().getPluginProperties(plugin), params);
    
                lprotos.add(new CapsdProtocolInfo(plugin.getProtocol(), m_pluginsByProtocol.get(plugin.getProtocol()), params, Action.SCAN));
            }
    
        } // end ProtocolPlugin
    
        /*
         * copy the protocol information to
         * the approriate array and return that
         * result
         */
        CapsdProtocolInfo[] result = new CapsdProtocolInfo[lprotos.size()];
    
        return lprotos.toArray(result);
    }

    private static void addProperties(List<Property> properties, Map<String, Object> params) {
        for (Property property : properties) {
            params.put(property.getKey(), property.getValue());
        }
    }

    /**
     * <p>getCapsdConfig</p>
     *
     * @return a {@link org.opennms.netmgt.config.CapsdConfig} object.
     */
    public CapsdConfig getCapsdConfig() {
        return m_capsdConfig;
    }

    /**
     * <p>setCapsdConfig</p>
     *
     * @param capsdConfig a {@link org.opennms.netmgt.config.CapsdConfig} object.
     */
    public void setCapsdConfig(CapsdConfig capsdConfig) {
        m_capsdConfig = capsdConfig;
    }
    
    /**
     * <p>afterPropertiesSet</p>
     *
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    @Override
    public void afterPropertiesSet() throws ValidationException {
        Assert.state(m_capsdConfig != null, "property capsdConfig must be set to a non-null value");
        
        instantiatePlugins();
    }

}
