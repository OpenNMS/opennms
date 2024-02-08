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

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.ServiceConfigFactory;
import org.opennms.netmgt.config.service.ServiceConfiguration;

/**
 * The Test Class for ServiceConfigMigratorOffline.
 * 
 * @author Alejandro Galue <agalue@opennms.org>
 */
public class ServiceConfigMigratorOfflineTest {

    /**
     * Sets up the test.
     *
     * @throws Exception the exception
     */
    @Before
    public void setUp() throws Exception {
        FileUtils.copyDirectory(new File("src/test/resources/etc"), new File("target/home/etc"));
        System.setProperty("opennms.home", "target/home");
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

    /**
     * Test fixing the configuration file.
     *
     * @throws Exception the exception
     */
    @Test
    public void testFixingConfig() throws Exception {
        ServiceConfigMigratorOffline migrator = new ServiceConfigMigratorOffline();
        migrator.execute();

        // Checking parsing the fixed file (it should contain all the services)
        File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.SERVICE_CONF_FILE_NAME);
        ServiceConfiguration cfg = JaxbUtils.unmarshal(ServiceConfiguration.class, cfgFile);

        // Do not change this value: the config file behind this test should always contain
        // the content for OpenNMS 14.0.0, regardless of whether or not services are added,
        // changed, or removed in the latest version.
        Assert.assertEquals(38, cfg.getServices().size());

        // Checking Service Factory (it should return only the enabled services)
        ServiceConfigFactory factory = new ServiceConfigFactory();
        Assert.assertEquals(27, factory.getServices().length);
    }

}
