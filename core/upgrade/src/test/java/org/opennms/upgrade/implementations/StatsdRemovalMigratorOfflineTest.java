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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.service.ServiceConfiguration;

public class StatsdRemovalMigratorOfflineTest {

    @Before
    public void setUp() throws Exception {
        FileUtils.copyDirectory(new File("src/test/resources/etc"), new File("target/home/etc"));
        System.setProperty("opennms.home", "target/home");
        // a 16.0.4-era configuration still carries the Statsd entry
        FileUtils.copyFile(new File("target/home/etc/service-configuration-16.0.4.xml"), new File("target/home/etc/service-configuration.xml"));
        FileUtils.writeStringToFile(new File("target/home/etc/statsd-configuration.xml"), "<statistics-daemon-configuration/>", StandardCharsets.UTF_8);
    }

    @After
    public void tearDown() throws Exception {
        FileUtils.deleteDirectory(new File("target/home"));
    }

    @Test
    public void testRemovesStatsdAndArchivesItsFiles() throws Exception {
        final File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.SERVICE_CONF_FILE_NAME);
        ServiceConfiguration cfg = JaxbUtils.unmarshal(ServiceConfiguration.class, cfgFile);
        final int before = cfg.getServices().size();
        assertTrue(cfg.getServices().stream().anyMatch(s -> StatsdRemovalMigratorOffline.STATSD_SERVICE.equals(s.getName())));

        final StatsdRemovalMigratorOffline migrator = new StatsdRemovalMigratorOffline();
        migrator.preExecute();
        migrator.execute();
        migrator.postExecute();

        cfg = JaxbUtils.unmarshal(ServiceConfiguration.class, cfgFile);
        assertEquals(before - 1, cfg.getServices().size());
        assertFalse(cfg.getServices().stream().anyMatch(s -> StatsdRemovalMigratorOffline.STATSD_SERVICE.equals(s.getName())));
        // the other entries, and their enabled flags, survive the round trip
        assertTrue(cfg.getServices().stream().anyMatch(s -> "OpenNMS:Name=Eventd".equals(s.getName())));

        for (final String name : StatsdRemovalMigratorOffline.RETIRED_FILES) {
            assertFalse(name + " should have left etc", new File("target/home/etc/" + name).exists());
            assertTrue(name + " should be in etc_archive", new File("target/home/etc_archive/" + name).exists());
        }
        assertFalse("the backup is cleaned up", new File(cfgFile.getAbsolutePath() + ".zip").exists());
    }

    @Test
    public void testIsIdempotentWhenNothingIsLeft() throws Exception {
        final StatsdRemovalMigratorOffline migrator = new StatsdRemovalMigratorOffline();
        migrator.execute();
        final File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.SERVICE_CONF_FILE_NAME);
        final int after = JaxbUtils.unmarshal(ServiceConfiguration.class, cfgFile).getServices().size();

        migrator.execute();
        assertEquals(after, JaxbUtils.unmarshal(ServiceConfiguration.class, cfgFile).getServices().size());
    }
}
