/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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
