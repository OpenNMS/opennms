/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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

    /**
     * Flag to skip the upgrade if the installed version is 1.13 or higher.
     */
    private boolean skipMe = false;

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
        if (isInstalledVersionGreaterOrEqual(1, 13, 0)) {
            log("This upgrade procedure should only run against systems older than 1.13.0; the current version is " + getOpennmsVersion() + "\n");
            skipMe = true;
            return;
        }
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
        if (skipMe) return;
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
        if (skipMe) return;
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
        if (skipMe) return;
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
