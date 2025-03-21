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
import java.util.Collections;

import org.apache.commons.io.FileUtils;
import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.service.Attribute;
import org.opennms.netmgt.config.service.Invoke;
import org.opennms.netmgt.config.service.InvokeAtType;
import org.opennms.netmgt.config.service.Service;
import org.opennms.netmgt.config.service.ServiceConfiguration;
import org.opennms.upgrade.api.AbstractOnmsUpgrade;
import org.opennms.upgrade.api.OnmsUpgradeException;

import com.google.common.collect.Lists;

public class RemotePollerServiceConfigMigratorOffline extends AbstractOnmsUpgrade {

    private File configFile;

    public static final String DEPRECATED_REMOTE_POLLER_SERVICENAME = "OpenNMS:Name=PollerBackEnd";
    public static final String PERSPECTIVE_POLLER_SERVICENAME = "OpenNMS:Name=PerspectivePoller";

    public RemotePollerServiceConfigMigratorOffline() throws OnmsUpgradeException {
        super();
        try {
            configFile = ConfigFileConstants.getFile(ConfigFileConstants.SERVICE_CONF_FILE_NAME);
        } catch (final IOException e) {
            throw new OnmsUpgradeException("Can't find Services Configuration file", e);
        }
    }

    @Override
    public int getOrder() {
        return 14;
    }

    @Override
    public String getDescription() {
        return "Remove deprecated RemotePoller service entry from service-configuration.xml, see NMS-12684";
    }

    @Override
    public void preExecute() throws OnmsUpgradeException {
        try {
            log("Creating backup of %s\n", configFile);
            zipFile(configFile);
        } catch (Exception e) {
            throw new OnmsUpgradeException("Can't backup service-configurations.xml because " + e.getMessage());
        }

    }

    @Override
    public void postExecute() throws OnmsUpgradeException {
        File zip = new File(configFile.getAbsolutePath() + ZIP_EXT);
        if (zip.exists()) {
            log("Removing backup %s\n", zip);
            FileUtils.deleteQuietly(zip);
        }
    }

    @Override
    public void rollback() throws OnmsUpgradeException {
        log("Restoring backup %s\n", configFile);
        File zip = new File(configFile.getAbsolutePath() + ZIP_EXT);
        FileUtils.deleteQuietly(configFile);
        unzipFile(zip, zip.getParentFile());
    }

    @Override
    public void execute() throws OnmsUpgradeException {
        try {
            final ServiceConfiguration currentCfg = JaxbUtils.unmarshal(ServiceConfiguration.class, configFile);
            boolean skipRemovePollerNgEntryCreation = false;
            boolean deprecatedServiceEnabled = true;

            log("Current configuration: " + currentCfg.getServices().size() + " services.\n");

            for (int i = currentCfg.getServices().size() - 1; i >= 0; i--) {
                final Service localSvc = currentCfg.getServices().get(i);
                final String name = localSvc.getName();

                if (DEPRECATED_REMOTE_POLLER_SERVICENAME.equals(name)) {
                    // Perhaps the administrator has intentionally disabled it, so PerspectivePoller should only be
                    // enabled if deprecated RemotePoller was enabled. If no entry was found this value defaults to true.
                    deprecatedServiceEnabled = localSvc.isEnabled();

                    // remove the entry from the configuration
                    currentCfg.getServices().remove(i);
                    log("Removing deprecated '%s' entry\n", DEPRECATED_REMOTE_POLLER_SERVICENAME);
                }


                if (PERSPECTIVE_POLLER_SERVICENAME.equals(name)) {
                    // if a existing PerspectivePoller entry exists, do not touch it's configuration
                    skipRemovePollerNgEntryCreation = true;
                }
            }

            if (skipRemovePollerNgEntryCreation) {
                log("A service entry named '%s' already exists.\n", PERSPECTIVE_POLLER_SERVICENAME);
            } else {
                final Service service = new Service();
                service.setEnabled(deprecatedServiceEnabled);
                service.setName(PERSPECTIVE_POLLER_SERVICENAME);
                service.setClassName("org.opennms.netmgt.daemon.SimpleSpringContextJmxServiceDaemon");
                service.getAttributes().add(new Attribute("LoggingPrefix", "java.lang.String", "perspectivepollerd"));
                service.getAttributes().add(new Attribute("SpringContext", "java.lang.String", "perspectivepollerdContext"));
                service.setInvokes(Lists.newArrayList(
                        new Invoke(InvokeAtType.START, 0, "init", Collections.emptyList()),
                        new Invoke(InvokeAtType.START, 1, "start", Collections.emptyList()),
                        new Invoke(InvokeAtType.STATUS, 0, "status", Collections.emptyList()),
                        new Invoke(InvokeAtType.STOP, 0, "stop", Collections.emptyList())
                ));

                log("Adding new 'OpenNMS:Name=PerspectivePoller' entry\n");
                currentCfg.addService(service);
            }

            log("Final configuration: " + currentCfg.getServices().size() + " services.\n");

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
            throw new OnmsUpgradeException("Can't migrate service-configuration.xml because " + e.getMessage(), e);
        }
    }

    @Override
    public boolean requiresOnmsRunning() {
        return false;
    }
}
