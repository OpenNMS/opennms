/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config.collector;

import java.io.Serializable;
import java.util.Comparator;

/**
 * <p>ByNameComparator class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public final class ByNameComparator implements Comparator<AttributeDefinition>, Serializable {

    private static final long serialVersionUID = -2596801053643459622L;

    /**
     * <p>compare</p>
     *
     * @param type0 a {@link org.opennms.netmgt.config.collector.AttributeDefinition} object.
     * @param type1 a {@link org.opennms.netmgt.config.collector.AttributeDefinition} object.
     * @return a int.
     */
    public int compare(final AttributeDefinition type0, final AttributeDefinition type1) {
        return type0.getName().compareTo(type1.getName());
    }
    
    /** {@inheritDoc} */
    public boolean equals(final Object o) {
        return o instanceof ByNameComparator;
    }
    
    /**
     * <p>hashCode</p>
     *
     * @return a int.
     */
    public int hashCode() {
        return 0;
    }
}
