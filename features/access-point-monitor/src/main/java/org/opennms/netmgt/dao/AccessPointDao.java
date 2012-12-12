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

package org.opennms.netmgt.dao;

import java.util.List;

import org.opennms.netmgt.model.OnmsAccessPoint;
import org.opennms.netmgt.model.OnmsAccessPointCollection;

/**
 * <p>
 * AccessPointDao interface.
 * </p>
 * 
 * @author <a href="mailto:jwhite@datavalet.com">Jesse White</a>
 */
public interface AccessPointDao extends OnmsDao<OnmsAccessPoint, Integer> {

    /**
     * <p>
     * findByPhysAddr
     * </p>
     * 
     * @param physaddr
     *            a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.OnmsAccessPoint} object.
     */
    public OnmsAccessPoint findByPhysAddr(final String physaddr);

    /**
     * <p>
     * findByPackage
     * </p>
     * 
     * @param pkg
     *            a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.OnmsAccessPointCollection}
     *         object.
     */
    public OnmsAccessPointCollection findByPackage(final String pkg);

    /**
     * <p>
     * findDistinctPackagesLike
     * </p>
     * 
     * @param pkg
     *            a {@link java.lang.String} object.
     * @return a {@link java.util.List} object.
     */
    public List<String> findDistinctPackagesLike(final String pkg);

}
