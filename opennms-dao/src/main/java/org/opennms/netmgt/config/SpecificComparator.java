//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.                                                            
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//       
// For more information contact: 
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//

package org.opennms.netmgt.config;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Comparator;

import org.apache.log4j.Category;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.ThreadCategory;

/**
 * This class is used to compare Specific object from the config SNMP package.
 * 
 * @author <a href="mailto:david@openmms.org">David Hustace</a>
 *
 */
public class SpecificComparator implements Comparator<String> {
    ThreadCategory log = ThreadCategory.getInstance(getClass());

    /**
     * returns the difference of spec1 - spec2
     */
    public int compare(String spec1, String spec2) {
        long compared = 0;
        try {
            final long specific1 = InetAddressUtils.toIpAddrLong(InetAddress.getByName(spec1));
            final long specific2 = InetAddressUtils.toIpAddrLong(InetAddress.getByName(spec2));
            compared = specific1 - specific2;
        } catch (UnknownHostException e) {
            log.error("compare: Exception sorting ranges.", e);
            throw new IllegalArgumentException(e.getLocalizedMessage());
        }
        return (int)compared;
    }
}

