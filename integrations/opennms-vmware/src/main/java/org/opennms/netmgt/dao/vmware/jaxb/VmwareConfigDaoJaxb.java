/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao.vmware.jaxb;

import org.opennms.features.config.service.impl.AbstractCmJaxbConfigDao;
import org.opennms.netmgt.config.vmware.VmwareConfig;
import org.opennms.netmgt.config.vmware.VmwareServer;
import org.opennms.netmgt.dao.vmware.VmwareConfigDao;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * The Class VmwareConfigDaoJaxb
 * <p/>
 * This class is used for accessing the Vmware Account configuration file
 *
 * @author Christian Pape <Christian.Pape@informatik.hs-fulda.de>
 */

public class VmwareConfigDaoJaxb extends AbstractCmJaxbConfigDao<VmwareConfig> implements VmwareConfigDao {
    protected static VmwareConfig m_config;
    private static final String CONFIG_NAME = "vmware";
    private static final String DEFAULT_CONFIG_ID = "default";
    /**
     * Default constructor
     */

    public VmwareConfigDaoJaxb() {
        super(VmwareConfig.class, "Vmware Configuration");
    }


    @PostConstruct
    public void postConstruct() throws IOException {
        this.m_config = this.loadConfig(this.getDefaultConfigId());
    }

    /**
     * Returns the loaded config object.
     *
     * @return the current config object
     */
    @Override
    public VmwareConfig getConfig() {
        return m_config;
    }

    /**
     * Returns the map of server entries from the configuration object.
     *
     * @return the map of server entries
     */
    @Override
    public Map<String, VmwareServer> getServerMap() {
        HashMap<String, VmwareServer> vmwareServerMap = new HashMap<String, VmwareServer>();

        for (VmwareServer vmwareServer : getConfig().getVmwareServer()) {
            vmwareServerMap.put(vmwareServer.getHostname(), vmwareServer);
        }
        return vmwareServerMap;
    }

    @Override
    protected String getConfigName() {
        return CONFIG_NAME;
    }

    @Override
    protected String getDefaultConfigId() {
        return DEFAULT_CONFIG_ID;
    }


}
