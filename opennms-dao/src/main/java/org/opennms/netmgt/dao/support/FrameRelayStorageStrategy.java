/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
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
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

package org.opennms.netmgt.dao.support;

import java.util.StringTokenizer;

/**
 * This class use the new implementation of SnmpStorageStrategy extending the new
 * IndexStorageStrategy from opennms-services
 */
public class FrameRelayStorageStrategy extends IndexStorageStrategy {

    @Override
    public String getResourceNameFromIndex(String resourceParent, String resourceIndex) {
        StringTokenizer indexes = new StringTokenizer(resourceIndex, ".");
        String ifIndex = indexes.nextToken();
        String ifName = getInterfaceName(resourceParent, ifIndex);
        String dlci = indexes.nextToken();
        return ifName + "." + dlci;
    }
       
    public String getInterfaceName(String nodeId, String ifIndex) {
       String label = m_storageStrategyService.getSnmpInterfaceLabel(new Integer(ifIndex));
       return label != null ? label : ifIndex;
    }

}
