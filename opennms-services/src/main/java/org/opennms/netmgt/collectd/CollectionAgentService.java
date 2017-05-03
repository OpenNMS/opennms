/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
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

import org.opennms.netmgt.model.PrimaryType;
import org.opennms.netmgt.model.ResourcePath;
import org.opennms.netmgt.snmp.SnmpAgentConfig;

/**
 * <p>CollectionAgentService interface.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @version $Id: $
 */
public interface CollectionAgentService {

    /**
     * <p>getHostAddress</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public abstract String getHostAddress();
    
    /**
     * <p>isStoreByForeignSource</p>
     * 
     * @return a {@link java.lang.Boolean} object.
     */
    public abstract Boolean isStoreByForeignSource();
    
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
     * <p>getLocationName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public abstract String getLocationName();

    public abstract ResourcePath getStorageResourcePath();


    /**
     * <p>getNodeId</p>
     *
     * @return a int.
     */
    public abstract int getNodeId();

    /**
     * <p>getNodeLabel</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public abstract String getNodeLabel();

    /**
     * <p>getIfIndex</p>
     *
     * @return a int.
     */
    public abstract int getIfIndex();

    /**
     * <p>getSysObjectId</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public abstract String getSysObjectId();

    /**
     * <p>getIsSnmpPrimary</p>
     *
     * @return a {@link org.opennms.netmgt.model.PrimaryType} object.
     */
    public abstract PrimaryType getIsSnmpPrimary();
    
    /**
     * <p>getAgentConfig</p>
     *
     * @return a {@link org.opennms.netmgt.snmp.SnmpAgentConfig} object.
     */
    public abstract SnmpAgentConfig getAgentConfig();

    /**
     * <p>getSnmpInterfaceData</p>
     *
     * @return a {@link java.util.Set} object.
     */
    public abstract Set<SnmpIfData> getSnmpInterfaceData();
    
    /**
     * <p>getInetAddress</p>
     *
     * @return a {@link java.net.InetAddress} object.
     */
    public abstract InetAddress getInetAddress();

}
