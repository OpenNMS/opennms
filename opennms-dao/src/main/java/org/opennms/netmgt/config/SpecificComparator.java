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

package org.opennms.netmgt.config;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.Comparator;

import org.opennms.core.utils.ByteArrayComparator;
import org.opennms.core.utils.InetAddressUtils;

/**
 * This class is used to compare Specific object from the config SNMP package.
 *
 * @author <a href="mailto:david@openmms.org">David Hustace</a>
 */
public class SpecificComparator implements Comparator<String>, Serializable {
	private static final long serialVersionUID = 5791618124389187729L;

	/**
     * returns the difference of spec1 - spec2
     *
     * @param spec1 a {@link java.lang.String} object.
     * @param spec2 a {@link java.lang.String} object.
     * @return -1 for spec1 < spec2, 0 for spec1 == spec2, 1 for spec1 > spec2
     */
    public int compare(final String spec1, final String spec2) {
    	final InetAddress addr1 = InetAddressUtils.addr(spec1);
		final InetAddress addr2 = InetAddressUtils.addr(spec2);
		return new ByteArrayComparator().compare(addr1 == null? null : addr1.getAddress(), addr2 == null? null : addr2.getAddress());
    }
}

