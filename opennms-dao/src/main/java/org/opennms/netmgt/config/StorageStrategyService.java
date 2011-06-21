/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 *     along with OpenNMS(R).  If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information contact: 
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/
package org.opennms.netmgt.config;

import org.opennms.netmgt.snmp.SnmpAgentConfig;

/**
 * <p>StorageStrategyService interface.</p>
 */
public interface StorageStrategyService {

    /**
     * <p>getAgentConfig</p>
     *
     * @return a {@link org.opennms.netmgt.snmp.SnmpAgentConfig} object.
     */
    public SnmpAgentConfig getAgentConfig();

    /*
     * This method is used on StorageStrategy implementation when the resource index is associated to a
     * physical interface like frame relay resources. OpenNMS always track changes on ifTable so, make SNMP
     * queries on this table is redundant, and implementations of CollectionAgent know ifTable content always.
     * This method give interface information from a specific ifIndex.
     */
    /**
     * <p>getSnmpInterfaceLabel</p>
     *
     * @param ifIndex a int.
     * @return a {@link java.lang.String} object.
     */
    public String getSnmpInterfaceLabel(int ifIndex);
}
