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

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.ServiceConfigFactory;
import org.opennms.netmgt.config.service.Service;
import org.opennms.netmgt.config.service.ServiceConfiguration;

/**
 * The Test Class for EOLServiceConfigMigratorOffline.
 * 
 * @author <a href="mailto:ranger@opennms.org">Benjamin Reed</a>
 */
@RunWith(Parameterized.class)
public class EOLServiceConfigMigratorOfflineTest {
    private final String m_testFile;
    private final int m_totalBefore;
    private final int m_totalAfter;
    private final int m_enabledAfter;

    private final List<String> m_disabled = Arrays.asList(
            "OpenNMS:Name=Linkd",
            "OpenNMS:Name=Xmlrpcd",
            "OpenNMS:Name=XmlrpcProvisioner",
            "OpenNMS:Name=AccessPointMonitor",
            "OpenNMS:Name=PollerBackEnd"
    );

    public EOLServiceConfigMigratorOfflineTest(final String testFile, final int totalBefore, final int totalAfter, final int enabledAfter) {
        m_testFile = testFile;
        m_totalBefore = totalBefore;
        m_totalAfter = totalAfter;
        m_enabledAfter = enabledAfter;
    }

    /**
     * Sets up the test.
     *
     * @throws Exception the exception
     */
    @Before
    public void setUp() throws Exception {
        FileUtils.copyDirectory(new File("src/test/resources/etc"), new File("target/home/etc"));
        System.setProperty("opennms.home", "target/home");
        FileUtils.copyFile(new File(m_testFile), new File("target/home/etc/service-configuration.xml"));
    }

    /**
     * Tear down the test.
     *
     * @throws Exception the exception
     */
    @After
    public void tearDown() throws Exception {
        FileUtils.deleteDirectory(new File("target/home"));
    }

    @Parameters
    public static Collection<Object[]> params() {
        return Arrays.asList(new Object[][] {
            // service config, total, enabled
            { "target/home/etc/service-configuration-1.8.17.xml",  38, 38, 34 },
            { "target/home/etc/service-configuration-1.10.14.xml", 38, 38, 34 },
            { "target/home/etc/service-configuration-1.12.9.xml",  39, 39, 34 },
            { "target/home/etc/service-configuration-14.0.3.xml",  38, 38, 26 },
            { "target/home/etc/service-configuration-15.0.2.xml",  38, 38, 26 },
            { "target/home/etc/service-configuration-16.0.4.xml",  37, 37, 26 }
        });
    }

    /**
     * Test fixing the configuration file.
     *
     * @throws Exception the exception
     */
    @Test
    public void testUpgradeConfig() throws Exception {
        File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.SERVICE_CONF_FILE_NAME);
        ServiceConfiguration cfg = JaxbUtils.unmarshal(ServiceConfiguration.class, cfgFile);

        // check the total and active services before doing the upgrade
        assertEquals(m_totalBefore, cfg.getServices().size());

        // perform the upgrade
        final EOLServiceConfigMigratorOffline migrator = new EOLServiceConfigMigratorOffline();
        migrator.execute();

        // confirm the total and active services after the upgrade
        cfg = JaxbUtils.unmarshal(ServiceConfiguration.class, cfgFile);
        final ServiceConfigFactory factory = new ServiceConfigFactory();
        Assert.assertEquals(m_totalAfter, cfg.getServices().size());
        Assert.assertEquals(m_enabledAfter, factory.getServices().length);

        for (final Service svc : cfg.getServices()) {
            final String serviceName = svc.getName();
            if (m_disabled.contains(serviceName)) {
                assertFalse("Service " + serviceName + " should be disabled.", svc.isEnabled());
            }
        }
        for (final Service svc : factory.getServices()) {
            final String serviceName = svc.getName();
            assertFalse("Service " + serviceName + " should not be in the active service list.", m_disabled.contains(svc.getName()));
        }
    }

}
