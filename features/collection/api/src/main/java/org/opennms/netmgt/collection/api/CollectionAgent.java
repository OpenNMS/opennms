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

package org.opennms.netmgt.collection.api;

import java.io.File;
import java.net.InetAddress;

import org.opennms.netmgt.model.ResourcePath;
import org.opennms.netmgt.poller.NetworkInterface;

/**
 * <p>CollectionAgent interface.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @version $Id: $
 */
public interface CollectionAgent extends NetworkInterface<InetAddress> {

    /**
     * <p>isStoreByForeignSource</p>
     * 
     * @return a {@link java.lang.Boolean} object.
     */
    Boolean isStoreByForeignSource();
    
    /**
     * <p>getHostAddress</p>
     *
     * @return a {@link java.lang.String} object.
     */
    String getHostAddress();

    /**
     * <p>setSavedIfCount</p>
     *
     * @param ifCount a int.
     */
    void setSavedIfCount(int ifCount);

    /**
     * <p>getNodeId</p>
     *
     * @return a int.
     */
    int getNodeId();

    /**
     * <p>getNodeLabel</p>
     *
     * @return a {@link java.lang.String} object.
     */
    String getNodeLabel();

    /**
     * <p>getForeignSource</p>
     * 
     * @return a {@link java.lang.String} object.
     */
    String getForeignSource();
    
    /**
     * <p>getForeignId</p>
     * 
     * @return a {@link java.lang.String} object.
     */
    String getForeignId();

    /**
     * <p>getLocationName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    String getLocationName();

    ResourcePath getStorageResourcePath();
    
    /**
     * <p>getSysObjectId</p>
     *
     * @return a {@link java.lang.String} object.
     */
    String getSysObjectId();

    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    String toString();

    /**
     * <p>getSavedSysUpTime</p>
     *
     * @return a long.
     */
    long getSavedSysUpTime();

    /**
     * <p>setSavedSysUpTime</p>
     *
     * @param sysUpTime a long.
     */
    void setSavedSysUpTime(long sysUpTime);

}
