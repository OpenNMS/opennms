/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
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

package org.opennms.upgrade.implementations;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

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
public class ServiceConfig1701MigratorOffline extends AbstractOnmsUpgrade {

    /** 
     * The services configuration file.
     */
    private File configFile;

    private final List<String> oldServices = Arrays.asList(new String[] {
            ":Name=HttpAdaptor",
            ":Name=HttpAdaptorMgmt",
            ":Name=XSLTProcessor",
            "OpenNMS:Name=AccessPointMonitor",
            "OpenNMS:Name=Capsd",
            "OpenNMS:Name=Importer",
            "OpenNMS:Name=Linkd",
            "OpenNMS:Name=Threshd",
            "OpenNMS:Name=XmlrpcProvisioner",
            "OpenNMS:Name=Xmlrpcd"
    });

    /**
     * Instantiates a new Service Configuration migrator offline.
     *
     * @throws OnmsUpgradeException the OpenNMS upgrade exception
     */
    public ServiceConfig1701MigratorOffline() throws OnmsUpgradeException {
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
        return 7;
    }

    /* (non-Javadoc)
     * @see org.opennms.upgrade.api.OnmsUpgrade#getDescription()
     */
    @Override
    public String getDescription() {
        return "Fixes service-configuration.xml if necessary when upgrading from 1.12: NMS-6970";
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
        try {
            ServiceConfiguration currentCfg = JaxbUtils.unmarshal(ServiceConfiguration.class, configFile);

            log("Current configuration: " + currentCfg.getServices().size() + " services.\n");

            for (int i=currentCfg.getServices().size() - 1; i >= 0; i--) {
                final Service localSvc = (Service) currentCfg.getServices().get(i);
                final String name = localSvc.getName();
                if (oldServices.contains(name)) {
                    log("Removing old service %s\n", name);
                    currentCfg.getServices().remove(i);
                }
            }

            log("New configuration: " + currentCfg.getServices().size() + " services.\n");

            // now remove 
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
        } catch (Exception e) {
            throw new OnmsUpgradeException("Can't fix services configuration because " + e.getMessage(), e);
        }
    }

}
