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

package org.opennms.netmgt.config;

import java.io.IOException;
import java.net.InetAddress;
import java.util.List;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.config.capsd.CapsdConfiguration;
import org.opennms.netmgt.config.capsd.Property;
import org.opennms.netmgt.config.capsd.ProtocolConfiguration;
import org.opennms.netmgt.config.capsd.ProtocolPlugin;
import org.opennms.netmgt.config.capsd.Range;
import org.opennms.netmgt.config.capsd.SmbAuth;

/**
 * <p>CapsdConfig interface.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public interface CapsdConfig {
    /**
     * This integer value is used to represent the primary SNMP interface
     * ifindex in the ipinterface table for SNMP hosts that don't support
     * the MIB2 ipAddrTable
     */
    static final int LAME_SNMP_HOST_IFINDEX = -100;

    /**
     * Saves the current in-memory configuration to disk and reloads
     *
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    void save() throws MarshalException, IOException, ValidationException;

    /**
     * Return the Capsd configuration object.
     *
     * @return a {@link org.opennms.netmgt.config.capsd.CapsdConfiguration} object.
     */
    CapsdConfiguration getConfiguration();

    /**
     * Finds the SMB authentication object using the netbios name.
     *
     * The target of the search.
     *
     * @param target a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.config.capsd.SmbAuth} object.
     */
    SmbAuth getSmbAuth(String target);

    /**
     * Checks the configuration to determine if the target is managed or
     * unmanaged.
     *
     * @param target
     *            The target to check against.
     * @return a boolean.
     */
    boolean isAddressUnmanaged(InetAddress target);

    /**
     * <p>getRescanFrequency</p>
     *
     * @return a long.
     */
    long getRescanFrequency();

    /**
     * <p>getInitialSleepTime</p>
     *
     * @return a long.
     */
    long getInitialSleepTime();

    /**
     * <p>getMaxSuspectThreadPoolSize</p>
     *
     * @return a int.
     */
    int getMaxSuspectThreadPoolSize();

    /**
     * <p>getMaxRescanThreadPoolSize</p>
     *
     * @return a int.
     */
    int getMaxRescanThreadPoolSize();

    /**
     * Defines Capsd's behavior when, during a protocol scan, it gets a
     * java.net.NoRouteToHostException exception. If abort rescan property is
     * set to "true" then Capsd will not perform any additional protocol scans.
     *
     * @return a boolean.
     */
    boolean getAbortProtocolScansFlag();

    /**
     * <p>getDeletePropagationEnabled</p>
     *
     * @return a boolean.
     */
    boolean getDeletePropagationEnabled();

    /**
     * Return the boolean xmlrpc as string to indicate if notification to
     * external xmlrpc server is needed.
     *
     * @return boolean flag as a string value
     */
    String getXmlrpc();
    
    /**
     * <p>isXmlRpcEnabled</p>
     *
     * @return a boolean.
     */
    boolean isXmlRpcEnabled();
    
    /**
     * <p>getProtocolPlugin</p>
     *
     * @param svcName a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.config.capsd.ProtocolPlugin} object.
     */
    ProtocolPlugin getProtocolPlugin(String svcName);

    /**
     * <p>addProtocolPlugin</p>
     *
     * @param plugin a {@link org.opennms.netmgt.config.capsd.ProtocolPlugin} object.
     */
    void addProtocolPlugin(ProtocolPlugin plugin);
    
    /**
     * <p>determinePrimarySnmpInterface</p>
     *
     * @param addressList a {@link java.util.List} object.
     * @param strict a boolean.
     * @return a {@link java.net.InetAddress} object.
     */
    InetAddress determinePrimarySnmpInterface(List<InetAddress> addressList, boolean strict);

    /**
     * <p>getConfiguredProtocols</p>
     *
     * @return a {@link java.util.List} object.
     */
    List<String> getConfiguredProtocols();

    /**
     * <p>getProtocolPlugins</p>
     *
     * @return a {@link java.util.List} object.
     */
    List<ProtocolPlugin> getProtocolPlugins();

    /**
     * <p>getProtocolConfigurations</p>
     *
     * @param plugin a {@link org.opennms.netmgt.config.capsd.ProtocolPlugin} object.
     * @return a {@link java.util.List} object.
     */
    List<ProtocolConfiguration> getProtocolConfigurations(ProtocolPlugin plugin);

    /**
     * <p>getSpecifics</p>
     *
     * @param pluginConf a {@link org.opennms.netmgt.config.capsd.ProtocolConfiguration} object.
     * @return a {@link java.util.List} object.
     */
    List<String> getSpecifics(ProtocolConfiguration pluginConf);

    /**
     * <p>getRanges</p>
     *
     * @param pluginConf a {@link org.opennms.netmgt.config.capsd.ProtocolConfiguration} object.
     * @return a {@link java.util.List} object.
     */
    List<Range> getRanges(ProtocolConfiguration pluginConf);

    /**
     * <p>getPluginProperties</p>
     *
     * @param plugin a {@link org.opennms.netmgt.config.capsd.ProtocolPlugin} object.
     * @return a {@link java.util.List} object.
     */
    List<Property> getPluginProperties(ProtocolPlugin plugin);

    /**
     * <p>getProtocolConfigurationProperties</p>
     *
     * @param pluginConf a {@link org.opennms.netmgt.config.capsd.ProtocolConfiguration} object.
     * @return a {@link java.util.List} object.
     */
    List<Property> getProtocolConfigurationProperties(ProtocolConfiguration pluginConf);
}
