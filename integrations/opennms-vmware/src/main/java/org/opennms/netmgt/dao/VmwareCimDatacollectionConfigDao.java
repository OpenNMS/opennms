/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao;

import org.opennms.netmgt.config.vmware.cim.VmwareCimCollection;
import org.opennms.netmgt.config.vmware.cim.VmwareCimDatacollectionConfig;
import org.opennms.netmgt.model.RrdRepository;

/**
 * The Interface VmwareCimDatacollectionConfigDao
 * <p/>
 * This class is used for defining the methods for accessing the configuration data for the Vmware Cim Data Collection
 *
 * @author Christian Pape <Christian.Pape@informatik.hs-fulda.de>
 */
public interface VmwareCimDatacollectionConfigDao {

    /**
     * Returns the loaded config object.
     *
     * @return the current config object
     */
    VmwareCimDatacollectionConfig getConfig();

    /**
     * This method returns a subset of the configuration data for a given collection name.
     *
     * @param collectionName the collection's name
     * @return the Cim collection object
     */
    VmwareCimCollection getVmwareCimCollection(String collectionName);

    /**
     * Returns the Rrd repository for a given collection name.
     *
     * @param collectionName the collection's name
     * @return the repository
     */
    public RrdRepository getRrdRepository(String collectionName);

    /**
     * Returns the base Rrd's path.
     *
     * @return the Rrd's path
     */
    public String getRrdPath();
}
