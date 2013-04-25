/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
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

package org.opennms.netmgt.snmp;

public class SnmpResult implements Comparable<SnmpResult> {
    private final SnmpObjId m_base;
    private final SnmpInstId m_instance;
    private final SnmpValue m_value;
    
    public SnmpResult(SnmpObjId base, SnmpInstId instance, SnmpValue value) {
        m_base = base;
        m_instance = instance;
        m_value = value;
    }

    public SnmpObjId getBase() {
        return m_base;
    }

    public SnmpInstId getInstance() {
        return m_instance;
    }

    public SnmpValue getValue() {
        return m_value;
    }

    public SnmpObjId getAbsoluteInstance() {
        return getBase().append(getInstance());
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("base", getBase())
            .append("instance", getInstance())
            .append("value", getValue())
            .toString();
    }

	@Override
	public int compareTo(SnmpResult other) {
		return getAbsoluteInstance().compareTo(other.getAbsoluteInstance());
	}
    
    
    

}
