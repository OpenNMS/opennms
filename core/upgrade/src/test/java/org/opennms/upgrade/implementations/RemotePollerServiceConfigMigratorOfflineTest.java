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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.service.Service;
import org.opennms.netmgt.config.service.ServiceConfiguration;

public class RemotePollerServiceConfigMigratorOfflineTest {

    @Test
    public void testDeprecatedEntry() throws Exception {
        // file contains an disabled deprecated remote poller entry
        copyFile("src/test/resources/NMS-12684/service-configuration.xml-1");

        final File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.SERVICE_CONF_FILE_NAME);
        final ServiceConfiguration cfg = JaxbUtils.unmarshal(ServiceConfiguration.class, cfgFile);

        assertEquals(32, cfg.getServices().size());

        final RemotePollerServiceConfigMigratorOffline remotePollerServiceConfigMigratorOffline = new RemotePollerServiceConfigMigratorOffline();
        remotePollerServiceConfigMigratorOffline.execute();

        final ServiceConfiguration newCfg = JaxbUtils.unmarshal(ServiceConfiguration.class, cfgFile);

        Assert.assertEquals(32, newCfg.getServices().size());

        final Set<String> services = newCfg.getServices().stream().map(s -> s.getName()).collect(Collectors.toSet());

        Assert.assertEquals(false, services.contains(RemotePollerServiceConfigMigratorOffline.DEPRECATED_REMOTE_POLLER_SERVICENAME));
        Assert.assertEquals(true, services.contains(RemotePollerServiceConfigMigratorOffline.PERSPECTIVE_POLLER_SERVICENAME));

        final List<Service> matchingEntries = newCfg.getServices().stream()
                .filter(s -> RemotePollerServiceConfigMigratorOffline.PERSPECTIVE_POLLER_SERVICENAME.equals(s.getName()))
                .collect(Collectors.toList());

        Assert.assertEquals(1, matchingEntries.size());
        Assert.assertThat(matchingEntries.get(0).isEnabled(), is(false));
    }

    @Test
    public void testBasicServiceConfiguration() throws Exception {
        // file does not include any remote poller entry
        copyFile("src/test/resources/NMS-12684/service-configuration.xml-2");

        final File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.SERVICE_CONF_FILE_NAME);
        final ServiceConfiguration cfg = JaxbUtils.unmarshal(ServiceConfiguration.class, cfgFile);

        assertEquals(31, cfg.getServices().size());

        final RemotePollerServiceConfigMigratorOffline remotePollerServiceConfigMigratorOffline = new RemotePollerServiceConfigMigratorOffline();
        remotePollerServiceConfigMigratorOffline.execute();

        final ServiceConfiguration newCfg = JaxbUtils.unmarshal(ServiceConfiguration.class, cfgFile);

        Assert.assertEquals(32, newCfg.getServices().size());

        final Set<String> services = newCfg.getServices().stream().map(s -> s.getName()).collect(Collectors.toSet());

        Assert.assertEquals(false, services.contains(RemotePollerServiceConfigMigratorOffline.DEPRECATED_REMOTE_POLLER_SERVICENAME));
        Assert.assertEquals(true, services.contains(RemotePollerServiceConfigMigratorOffline.PERSPECTIVE_POLLER_SERVICENAME));

        final List<Service> matchingEntries = newCfg.getServices().stream()
                .filter(s -> RemotePollerServiceConfigMigratorOffline.PERSPECTIVE_POLLER_SERVICENAME.equals(s.getName()))
                .collect(Collectors.toList());

        Assert.assertEquals(1, matchingEntries.size());
        Assert.assertThat(matchingEntries.get(0).isEnabled(), is(true));
    }

    @Test
    public void testRemotePollerNgEntry() throws Exception {
        // file contains the remote poller ng entry
        copyFile("src/test/resources/NMS-12684/service-configuration.xml-3");

        final File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.SERVICE_CONF_FILE_NAME);
        final ServiceConfiguration cfg = JaxbUtils.unmarshal(ServiceConfiguration.class, cfgFile);

        assertEquals(32, cfg.getServices().size());

        final RemotePollerServiceConfigMigratorOffline remotePollerServiceConfigMigratorOffline = new RemotePollerServiceConfigMigratorOffline();
        remotePollerServiceConfigMigratorOffline.execute();

        final ServiceConfiguration newCfg = JaxbUtils.unmarshal(ServiceConfiguration.class, cfgFile);

        Assert.assertEquals(32, newCfg.getServices().size());

        final Set<String> services = newCfg.getServices().stream().map(s -> s.getName()).collect(Collectors.toSet());

        Assert.assertEquals(false, services.contains(RemotePollerServiceConfigMigratorOffline.DEPRECATED_REMOTE_POLLER_SERVICENAME));
        Assert.assertEquals(true, services.contains(RemotePollerServiceConfigMigratorOffline.PERSPECTIVE_POLLER_SERVICENAME));

        final List<Service> matchingEntries = newCfg.getServices().stream()
                .filter(s -> RemotePollerServiceConfigMigratorOffline.PERSPECTIVE_POLLER_SERVICENAME.equals(s.getName()))
                .collect(Collectors.toList());

        Assert.assertEquals(1, matchingEntries.size());
        Assert.assertThat(matchingEntries.get(0).isEnabled(), is(true));

        //verify that the entry was not touched by the migrator
        Assert.assertThat(
                matchingEntries
                        .get(0)
                        .getAttributes()
                        .stream()
                        .map(a -> a.getValue().getContent())
                        .collect(Collectors.toSet()),
                Matchers.containsInAnyOrder("remotepollerd-foo", "remotepollerdContext-bar")
        );
    }

    private void copyFile(final String testFile) throws Exception {
        FileUtils.copyDirectory(new File("src/test/resources/etc"), new File("target/home/etc"));
        System.setProperty("opennms.home", "target/home");
        FileUtils.copyFile(new File(testFile), new File("target/home/etc/service-configuration.xml"));
    }
}
