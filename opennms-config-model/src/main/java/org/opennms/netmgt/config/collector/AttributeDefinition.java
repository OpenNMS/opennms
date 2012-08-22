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

/**
 * <p>AttributeDefinition interface.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public interface AttributeDefinition {

    /**
     * <p>getType</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public abstract String getType();

    /**
     * <p>getName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public abstract String getName();
    
    /**
     * <p>equals</p>
     *
     * @param o a {@link java.lang.Object} object.
     * @return a boolean.
     */
    public abstract boolean equals(Object o);
    
    /**
     * <p>hashCode</p>
     *
     * @return a int.
     */
    public abstract int hashCode();

}
