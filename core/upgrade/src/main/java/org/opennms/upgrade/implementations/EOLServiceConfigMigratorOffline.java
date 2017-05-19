/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

package org.opennms.upgrade.implementations;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;

import org.apache.commons.io.FileUtils;
import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.service.Service;
import org.opennms.netmgt.config.service.ServiceConfiguration;
import org.opennms.upgrade.api.AbstractOnmsUpgrade;
import org.opennms.upgrade.api.OnmsUpgradeException;

/**
 * The Class Service Configuration Migrator.
 * 
 * <p>Issues fixed:</p>
 * <ul>
 * <li>HZN-545</li>
 * </ul>
 * 
 * @author <a href="mailto:ranger@opennms.org">Benjamin Reed</a>
 */
public class EOLServiceConfigMigratorOffline extends AbstractOnmsUpgrade {
    /** 
     * The services configuration file.
     */
    private File configFile;

    /**
     * Instantiates a new Service Configuration migrator offline.
     *
     * @throws OnmsUpgradeException the OpenNMS upgrade exception
     */
    public EOLServiceConfigMigratorOffline() throws OnmsUpgradeException {
        super();
        try {
            configFile = ConfigFileConstants.getFile(ConfigFileConstants.SERVICE_CONF_FILE_NAME);
        } catch (final IOException e) {
            throw new OnmsUpgradeException("Can't find Services Configuration file", e);
        }
    }

    /* (non-Javadoc)
     * @see org.opennms.upgrade.api.OnmsUpgrade#getOrder()
     */
    @Override
    public int getOrder() {
        return 10;
    }

    /* (non-Javadoc)
     * @see org.opennms.upgrade.api.OnmsUpgrade#getDescription()
     */
    @Override
    public String getDescription() {
        return "Fixes service-configuration.xml if necessary when upgrading to 17.0.0: HZN-545";
    }

    /* (non-Javadoc)
     * @see org.opennms.upgrade.api.OnmsUpgrade#requiresOnmsRunning()
     */
    @Override
    public boolean requiresOnmsRunning() {
        return false;
    }

    /* (non-Javadoc)
     * @see org.opennms.upgrade.api.OnmsUpgrade#preExecute()
     */
    @Override
    public void preExecute() throws OnmsUpgradeException {
        try {
            log("Backing up %s\n", configFile);
            zipFile(configFile);
        } catch (Exception e) {
            throw new OnmsUpgradeException("Can't backup service-configurations.xml because " + e.getMessage());
        }
    }

    /* (non-Javadoc)
     * @see org.opennms.upgrade.api.OnmsUpgrade#postExecute()
     */
    @Override
    public void postExecute() throws OnmsUpgradeException {
        File zip = new File(configFile.getAbsolutePath() + ZIP_EXT);
        if (zip.exists()) {
            log("Removing backup %s\n", zip);
            FileUtils.deleteQuietly(zip);
        }
    }

    /* (non-Javadoc)
     * @see org.opennms.upgrade.api.OnmsUpgrade#rollback()
     */
    @Override
    public void rollback() throws OnmsUpgradeException {
        log("Restoring backup %s\n", configFile);
        File zip = new File(configFile.getAbsolutePath() + ZIP_EXT);
        FileUtils.deleteQuietly(configFile);
        unzipFile(zip, zip.getParentFile());
    }

    /* (non-Javadoc)
     * @see org.opennms.upgrade.api.OnmsUpgrade#execute()
     */
    @Override
    public void execute() throws OnmsUpgradeException {
        final String[] eol = {
                "OpenNMS:Name=Linkd",
                "OpenNMS:Name=Xmlrpcd",
                "OpenNMS:Name=XmlrpcProvisioner",
                "OpenNMS:Name=AccessPointMonitor"
        };

        try {
            final ServiceConfiguration currentCfg = JaxbUtils.unmarshal(ServiceConfiguration.class, configFile);

            // Remove any end-of-life'd daemons from service configuration
            for (final String serviceName : eol) {
                final Service eolService = getService(currentCfg, serviceName);
                if (eolService == null) {
                    continue;
                }
                final String eolServiceName = eolService.getName();
                if (eolServiceName.equals(serviceName)) {
                    final String displayName = serviceName.replace("OpenNMS:Name=", "");
                    log("Disabling EOL service: " + displayName + "\n");
                    eolService.setEnabled(false);
                }
            }

            final StringWriter sw = new StringWriter();
            sw.write("<?xml version=\"1.0\"?>\n");
            sw.write("<!-- NOTE!!!!!!!!!!!!!!!!!!!\n");
            sw.write("The order in which these services are specified is important - for example, Eventd\n");
            sw.write("will need to come up last so that none of the event topic subcribers loose any event.\n");
            sw.write("\nWhen splitting services to run on mutiple VMs, the order of the services should be\n");
            sw.write("maintained\n");
            sw.write("-->\n");
            JaxbUtils.marshal(currentCfg, sw);
            final FileWriter fw = new FileWriter(configFile);
            fw.write(sw.toString());
            fw.close();
        } catch (final Exception e) {
            throw new OnmsUpgradeException("Can't fix services configuration because " + e.getMessage(), e);
        }
    }

    /**
     * Gets the service.
     *
     * @param svcConfig the service configuration object
     * @param serviceName the service name
     * @return the service
     */
    private static Service getService(ServiceConfiguration svcConfig, String serviceName) {
        for(Service s : svcConfig.getServices()) {
            if (s.getName().equals(serviceName)) {
                return s;
            }
        }
        return null;
    }
}
