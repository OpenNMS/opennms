/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config.api;

import org.opennms.netmgt.config.threshd.Package;
import org.opennms.netmgt.config.threshd.ThreshdConfiguration;

public interface ThreshdConfig {
    
    /**
     * Return the threshd configuration object.
     *
     * @return a {@link org.opennms.netmgt.config.threshd.ThreshdConfiguration} object.
     */
    ThreshdConfiguration getConfiguration();

    /**
     * This method is used to determine if the named interface is included in the passed package definition. If the interface belongs to the package then a value of true is
     * returned. If the interface does not belong to the package a false value is returned. <strong>Note: </strong>Evaluation of the interface against a package filter will only
     * work if the IP is already in the database.
     *
     * @param iface
     *            The interface to test against the package.
     * @param pkg
     *            The package to check for the inclusion of the interface.
     * @return True if the interface is included in the package, false otherwise.
     */
    boolean interfaceInPackage(String iface, Package pkg);

    /**
     * Returns true if the service is part of the package and the status of the service is set to "on". Returns false if the service is not in the package or it is but the status
     * of the service is set to "off".
     *
     * @param svcName
     *            The service name to lookup.
     * @param pkg
     *            The package to lookup up service.
     * @return a boolean.
     */
    boolean serviceInPackageAndEnabled(String svcName, Package pkg);

    /**
     * Instruct the configuration to reload from its datasource.
     */
    void reload();

    void rebuildPackageIpListMap();

}
