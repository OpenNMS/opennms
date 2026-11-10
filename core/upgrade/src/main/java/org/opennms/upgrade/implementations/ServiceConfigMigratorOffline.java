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

import org.apache.commons.io.FileUtils;
import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.service.Attribute;
import org.opennms.netmgt.config.service.Service;
import org.opennms.netmgt.config.service.ServiceConfiguration;
import org.opennms.upgrade.api.AbstractOnmsUpgrade;
import org.opennms.upgrade.api.OnmsUpgradeException;
import org.xml.sax.InputSource;

/**
 * The Class Service Configuration Migrator.
 * 
 * <p>Issues fixed:</p>
 * <ul>
 * <li>NMS-6970</li>
 * </ul>
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
public class ServiceConfigMigratorOffline extends AbstractOnmsUpgrade {

    /**
     * The base configuration object (or configuration reference).
     */
    private ServiceConfiguration baseConfig;

    /** 
     * The services configuration file.
     */
    private File configFile;

    /**
     * Flag to skip the upgrade if the installed version is 14.0.0 or higher.
     */
    private boolean skipMe = false;

    /**
     * Instantiates a new Service Configuration migrator offline.
     *
     * @throws OnmsUpgradeException the OpenNMS upgrade exception
     */
    public ServiceConfigMigratorOffline() throws OnmsUpgradeException {
        super();
        try {
            configFile = ConfigFileConstants.getFile(ConfigFileConstants.SERVICE_CONF_FILE_NAME);
            InputSource src = new InputSource(getClass().getResourceAsStream("/default/service-configuration-14.0.0.xml"));
            baseConfig = JaxbUtils.unmarshal(ServiceConfiguration.class, src);
        } catch (IOException e) {
            throw new OnmsUpgradeException("Can't find Services Configuration file", e);
        }
    }

    /* (non-Javadoc)
     * @see org.opennms.upgrade.api.OnmsUpgrade#getOrder()
     */
    @Override
    public int getOrder() {
        return 6;
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
        if (isInstalledVersionGreaterOrEqual(14, 0, 0)) {
            log("This upgrade procedure should only run against systems older than 14.0.0; the current version is " + getOpennmsVersion() + "\n");
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
            int index = 0;
            for (Service baseSvc : baseConfig.getServices()) {
                Service localSvc = getService(currentCfg, baseSvc.getName());
                if (localSvc == null) {
                    if (baseSvc.isEnabled()) {
                        log("Adding new service %s\n", baseSvc.getName());
                    } else {
                        log("Marking service %s as disabled\n", baseSvc.getName());
                    }
                    currentCfg.getServices().add(index, baseSvc);
                    continue;
                }
                if (!baseSvc.isEnabled()) {
                    log("Disabling service %s because it is not on the default list of enabled services\n", localSvc.getName());
                    localSvc.setEnabled(false);
                }
                if (localSvc.getClassName().equals("org.opennms.netmgt.poller.jmx.RemotePollerBackEnd")) {
                    log("Fixing the class path for RemotePollerBackEnd.\n");
                    localSvc.setClassName("org.opennms.netmgt.poller.remote.jmx.RemotePollerBackEnd");
                }
                if (localSvc.getName().equals("OpenNMS:Name=Linkd")) {
                    log("Disabling Linkd (to promote EnhancedLinkd)\n");
                    localSvc.setEnabled(false);
                }
                Attribute a = getLoggingPrefix(localSvc);
                if (a != null) {
                    String prefix = a.getValue().getContent().toLowerCase();
                    // If the logging prefix isn't already lower case...
                    if (!a.getValue().getContent().equals(prefix)) {
                        // then set it to the lower case value
                        log("Fixing logging prefix for service %s\n", localSvc.getName());
                        a.getValue().setContent(prefix);
                    }
                }
                index++;
            }
            StringWriter sw = new StringWriter();
            sw.write("<?xml version=\"1.0\"?>\n");
            sw.write("<!-- NOTE!!!!!!!!!!!!!!!!!!!\n");
            sw.write("The order in which these services are specified is important - for example, Eventd\n");
            sw.write("will need to come up last so that none of the event topic subcribers loose any event.\n");
            sw.write("\nWhen splitting services to run on mutiple VMs, the order of the services should be\n");
            sw.write("maintained\n");
            sw.write("-->\n");
            JaxbUtils.marshal(currentCfg, sw);
            FileWriter fw = new FileWriter(configFile);
            fw.write(sw.toString());
            fw.close();
        } catch (Exception e) {
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

    /**
     * Gets the logging prefix.
     *
     * @param svc the OpenNMS service
     * @return the logging prefix attribute
     */
    private static Attribute getLoggingPrefix(Service svc) {
        for (Attribute a : svc.getAttributes()) {
            if (a.getName().equals("LoggingPrefix")) {
                return a;
            }
        }
        return null;
    }

}
