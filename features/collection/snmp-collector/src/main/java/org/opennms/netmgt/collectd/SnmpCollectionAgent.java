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

import java.util.Set;

import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.CollectionInitializationException;
import org.opennms.netmgt.collection.api.StorageStrategyService;

/**
 * <p>CollectionAgent interface.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @version $Id: $
 */
public interface SnmpCollectionAgent extends CollectionAgent, StorageStrategyService {

    /**
     * <p>setSavedIfCount</p>
     *
     * @param ifCount a int.
     */
    void setSavedIfCount(int ifCount);

    /**
     * <p>getSavedIfCount</p>
     *
     * @return a int.
     */
    int getSavedIfCount();

    /**
     * <p>getSysObjectId</p>
     *
     * @return a {@link java.lang.String} object.
     */
    String getSysObjectId();

    /**
     * <p>validateAgent</p>
     * @throws CollectionInitializationException 
     */
    void validateAgent() throws CollectionInitializationException;

    /**
     * <p>getSnmpInterfaceInfo</p>
     *
     * @param type a {@link org.opennms.netmgt.collectd.IfResourceType} object.
     * @return a {@link java.util.Set} object.
     */
    Set<IfInfo> getSnmpInterfaceInfo(IfResourceType type);

}
