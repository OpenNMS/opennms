/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.snmp;


class ToStringBuilder {
	
	final StringBuilder buf;
	boolean first = true;
	boolean finished = false;

	public ToStringBuilder(Object o) {
		buf = new StringBuilder(512);
		
		buf.append(o.getClass().getSimpleName());
		buf.append('@');
		buf.append(String.format("%x", System.identityHashCode(o)));
		buf.append('[');
		
	}
	
	public ToStringBuilder append(String label, String value) {
		assertNotFinished();
		if (!first) {
			buf.append(", ");
		} else {
			first = false;
		}
		buf.append(label).append('=').append(value);
		return this;
	}
	
	public ToStringBuilder append(String label, Object value) {
		return append(label, value == null ? null : value.toString());
	}
	
        @Override
	public String toString() {
		if (!finished) {
			buf.append(']');
			finished = true;
		}
		
		return buf.toString();
	}
	
	private void assertNotFinished() {
		if (finished) {
			throw new IllegalStateException("This builder has already been completed by calling toString");
		}
	}
	
}
