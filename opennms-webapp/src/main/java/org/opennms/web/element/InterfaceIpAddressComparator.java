/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2012 The OpenNMS Group, Inc.
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

package org.opennms.web.element;

import java.util.Comparator;

/**
 * <p>InterfaceIpAddressComparator class.</p>
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 * @since 1.8.1
 */
public class InterfaceIpAddressComparator implements Comparator<Interface> {
    /**
     * <p>compare</p>
     *
     * @param i1 a {@link org.opennms.web.element.Interface} object.
     * @param i2 a {@link org.opennms.web.element.Interface} object.
     * @return a int.
     */
    @Override
    public int compare(Interface i1, Interface i2) {
        return i1.getIpAddress().compareTo(i2.getIpAddress());
    }

//  public boolean equals(Interface i1, Interface i2) {
//      return i1.getIpAddress().equals(i2.getIpAddress());
//  }
}
