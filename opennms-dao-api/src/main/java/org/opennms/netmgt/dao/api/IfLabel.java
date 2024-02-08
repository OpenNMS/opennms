/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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

    void setSnmpInterfaceDao(SnmpInterfaceDao snmpInterfaceDao);
}
