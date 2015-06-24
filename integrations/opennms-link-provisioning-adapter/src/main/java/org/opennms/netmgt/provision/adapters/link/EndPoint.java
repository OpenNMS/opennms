/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
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

/**
 * <p>EndPoint interface.</p>
 *
 * @author ranger
 * @version $Id: $
 */
package org.opennms.netmgt.provision.adapters.link;

import java.net.InetAddress;

import org.opennms.netmgt.snmp.SnmpValue;
public interface EndPoint {
    /**
     * <p>get</p>
     *
     * @param oid a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.snmp.SnmpValue} object.
     */
    public SnmpValue get(String oid);
    /**
     * <p>getSysOid</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getSysOid();
    /**
     * <p>getAddress</p>
     *
     * @return a {@link java.net.InetAddress} object.
     */
    public InetAddress getAddress();
    /**
     * <p>ping</p>
     *
     * @return a boolean.
     * @throws org.opennms.netmgt.provision.adapters.link.EndPointStatusException if any.
     */
    public boolean ping() throws EndPointStatusException;
}
