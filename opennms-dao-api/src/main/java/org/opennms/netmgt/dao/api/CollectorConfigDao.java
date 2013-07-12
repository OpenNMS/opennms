/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao.api;

import java.util.Collection;

import org.opennms.netmgt.config.CollectdPackage;
import org.opennms.netmgt.config.collectd.Collector;

/**
 * <p>CollectorConfigDao interface.</p>
 */
public interface CollectorConfigDao {

    /**
     * <p>getSchedulerThreads</p>
     *
     * @return a int.
     */
    int getSchedulerThreads();

    /**
     * <p>getCollectors</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    Collection<Collector> getCollectors();

    /**
     * <p>rebuildPackageIpListMap</p>
     */
    void rebuildPackageIpListMap();
    
    /**
     * <p>getPackages</p>
     *
     * @return a {@link java.util.Collection} object.
     */
    Collection<CollectdPackage> getPackages();
    
    /**
     * <p>getPackage</p>
     *
     * @param name a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.config.CollectdPackage} object.
     */
    CollectdPackage getPackage(String name);

}
