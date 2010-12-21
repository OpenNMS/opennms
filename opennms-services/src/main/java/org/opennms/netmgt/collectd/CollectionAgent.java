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
 * Created June 29, 2007
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
package org.opennms.netmgt.collectd;

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
     * <p>getSysObjectId</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public abstract String getSysObjectId();

    /**
     * <p>validateAgent</p>
     */
    public abstract void validateAgent();

    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public abstract String toString();

    /**
     * <p>getAgentConfig</p>
     *
     * @return a {@link org.opennms.netmgt.snmp.SnmpAgentConfig} object.
     */
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
