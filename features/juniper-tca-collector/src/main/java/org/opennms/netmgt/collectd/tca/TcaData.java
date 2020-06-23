/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.collectd.tca;

import java.net.InetAddress;

import org.opennms.netmgt.capsd.snmp.SnmpTable;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;

/**
 * The Class TcaData.
 * 
 * @author Alejandro Galue <agalue@opennms.org>
 */
public final class TcaData extends SnmpTable<TcaDataEntry> {
	
	/**
	 * Instantiates a new TCA data.
	 *
	 * @param address the address
	 */
	protected TcaData(InetAddress address) {
		super(address, "jnxTcaSlaTable", TcaDataEntry.tca_elemList);
	}

	/* (non-Javadoc)
	 * @see org.opennms.netmgt.capsd.snmp.SnmpTable#createTableEntry(org.opennms.netmgt.snmp.SnmpObjId, org.opennms.netmgt.snmp.SnmpInstId, java.lang.Object)
	 */
	@Override
	protected TcaDataEntry createTableEntry(SnmpObjId base, SnmpInstId inst, Object val) {
		return new TcaDataEntry(inst);
	}

}
