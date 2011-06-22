/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 2 of the License,
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

package org.opennms.netmgt.dao.support;

import java.util.StringTokenizer;

/**
 * This class use the new implementation of SnmpStorageStrategy extending the new
 * IndexStorageStrategy from opennms-services
 *
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class FrameRelayStorageStrategy extends IndexStorageStrategy {

    /** {@inheritDoc} */
    @Override
    public String getResourceNameFromIndex(String resourceParent, String resourceIndex) {
        StringTokenizer indexes = new StringTokenizer(resourceIndex, ".");
        String ifIndex = indexes.nextToken();
        String ifName = getInterfaceName(resourceParent, ifIndex);
        String dlci = indexes.nextToken();
        return ifName + "." + dlci;
    }
       
    /**
     * <p>getInterfaceName</p>
     *
     * @param nodeId a {@link java.lang.String} object.
     * @param ifIndex a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    public String getInterfaceName(String nodeId, String ifIndex) {
       String label = m_storageStrategyService.getSnmpInterfaceLabel(new Integer(ifIndex));
       return label != null ? label : ifIndex;
    }

}
