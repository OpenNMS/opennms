/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.collectd;

import java.io.File;
import java.net.InetAddress;
import java.util.Set;

import org.opennms.netmgt.config.StorageStrategyService;
import org.opennms.netmgt.poller.NetworkInterface;
import org.opennms.netmgt.snmp.SnmpAgentConfig;

/**
 * <p>CollectionAgent interface.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @version $Id: $
 */
public interface CollectionAgent extends NetworkInterface<InetAddress>,StorageStrategyService {

    /**
     * <p>isStoreByForeignSource</p>
     * 
     * @return a {@link java.lang.Boolean} object.
     */
    public abstract Boolean isStoreByForeignSource();
    
    /**
     * <p>getHostAddress</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public abstract String getHostAddress();

    /**
     * <p>setSavedIfCount</p>
     *
     * @param ifCount a int.
     */
    public abstract void setSavedIfCount(int ifCount);

    /**
     * <p>getSavedIfCount</p>
     *
     * @return a int.
     */
    public abstract int getSavedIfCount();

    /**
     * <p>getNodeId</p>
     *
     * @return a int.
     */
    public abstract int getNodeId();

    /**
     * <p>getForeignSource</p>
     * 
     * @return a {@link java.lang.String} object.
     */
    public abstract String getForeignSource();
    
    /**
     * <p>getForeignId</p>
     * 
     * @return a {@link java.lang.String} object.
     */
    public abstract String getForeignId();
    
    /**
     * <p>getStorageDir</p>
     * 
     * @return a {@link java.io.File} object.
     */
    public abstract File getStorageDir();
    
    /**
     * <p>getSysObjectId</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public abstract String getSysObjectId();

    /**
     * <p>validateAgent</p>
     * @throws CollectionInitializationException 
     */
    public abstract void validateAgent() throws CollectionInitializationException;

    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public abstract String toString();

    /**
     * <p>getAgentConfig</p>
     *
     * @return a {@link org.opennms.netmgt.snmp.SnmpAgentConfig} object.
     */
    @Override
    public abstract SnmpAgentConfig getAgentConfig();

    /**
     * <p>getSnmpInterfaceInfo</p>
     *
     * @param type a {@link org.opennms.netmgt.collectd.IfResourceType} object.
     * @return a {@link java.util.Set} object.
     */
    public abstract Set<IfInfo> getSnmpInterfaceInfo(IfResourceType type);

    /**
     * <p>getInetAddress</p>
     *
     * @return a {@link java.net.InetAddress} object.
     */
    public abstract InetAddress getInetAddress();

    /**
     * <p>getSavedSysUpTime</p>
     *
     * @return a long.
     */
    public abstract long getSavedSysUpTime();

    /**
     * <p>setSavedSysUpTime</p>
     *
     * @param sysUpTime a long.
     */
    public abstract void setSavedSysUpTime(long sysUpTime);

}
