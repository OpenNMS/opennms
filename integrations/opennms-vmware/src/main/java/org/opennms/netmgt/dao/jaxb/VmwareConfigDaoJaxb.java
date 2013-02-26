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

package org.opennms.netmgt.dao.jaxb;

import org.opennms.core.xml.AbstractJaxbConfigDao;
import org.opennms.netmgt.config.vmware.VmwareConfig;
import org.opennms.netmgt.config.vmware.VmwareServer;
import org.opennms.netmgt.dao.VmwareConfigDao;

import java.util.HashMap;
import java.util.Map;

/**
 * The Class VmwareConfigDaoJaxb
 * <p/>
 * This class is used for accessing the Vmware Account configuration file
 *
 * @author Christian Pape <Christian.Pape@informatik.hs-fulda.de>
 */
public class VmwareConfigDaoJaxb extends AbstractJaxbConfigDao<VmwareConfig, VmwareConfig> implements VmwareConfigDao {
    /**
     * Default constructor
     */
    public VmwareConfigDaoJaxb() {
        super(VmwareConfig.class, "Vmware Configuration");
    }

    /**
     * Returns the loaded config object.
     *
     * @return the current config object
     */
    public VmwareConfig getConfig() {
        return getContainer().getObject();
    }

    /**
     * Used to transform the config object to a custom representation. This method is not modified in this class, it just
     * returns the config object itself.
     *
     * @param jaxbConfig a config object.
     * @return a custom object
     */
    public VmwareConfig translateConfig(VmwareConfig jaxbConfig) {
        return jaxbConfig;
    }

    /**
     * Returns the map of server entries from the configuration object.
     *
     * @return the map of server entries
     */
    public Map<String, VmwareServer> getServerMap() {
        HashMap<String, VmwareServer> vmwareServerMap = new HashMap<String, VmwareServer>();

        for (VmwareServer vmwareServer : getConfig().getVmwareServer()) {
            vmwareServerMap.put(vmwareServer.getHostname(), vmwareServer);
        }
        return vmwareServerMap;
    }

}
