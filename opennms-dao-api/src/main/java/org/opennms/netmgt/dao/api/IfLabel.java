/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao.api;

import java.net.InetAddress;
import java.sql.SQLException;
import java.util.Map;

public interface IfLabel {

    public static String NO_IFLABEL = "no_ifLabel";

    /**
     * Return a map of useful SNMP information for the interface specified by
     * the nodeId and ifLabel. Essentially a "decoding" algorithm for the
     * ifLabel.
     *
     * @param nodeId
     *            Node id
     * @param ifLabel
     *            Interface label of format: <description>- <macAddr>
     * @return Map of SNMP info keyed by 'snmpInterface' table column names for
     *         the interface specified by nodeId and ifLabel args.
     * @throws SQLException
     *             if error occurs accessing the database.
     */
    Map<String, String> getInterfaceInfoFromIfLabel(int nodeId, String ifLabel);

    /**
     * <p>getIfLabel</p>
     *
     * @param nodeId a int.
     * @param ipAddr a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    String getIfLabel(final int nodeId, final InetAddress ipAddr);

    /**
     * <p>getIfLabelfromIfIndex</p>
     *
     * @param nodeId a int.
     * @param ipAddr a {@link java.lang.String} object.
     * @param ifIndex a int.
     * @return a {@link java.lang.String} object.
     */
    String getIfLabelfromIfIndex(final int nodeId, final InetAddress ipAddr, final int ifIndex);

    /**
     * Return the ifLabel as a string for the given node and ifIndex. Intended for
     * use with non-ip interfaces.
     *
     * @return String
     * @param nodeId a int.
     * @param ifIndex a int.
     */
    String getIfLabelfromSnmpIfIndex(final int nodeId, final int ifIndex);

    /**
     * <p>getIfLabel</p>
     *
     * @param name a {@link java.lang.String} object.
     * @param descr a {@link java.lang.String} object.
     * @param physAddr a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    String getIfLabel(String name, String descr, String physAddr);
}
