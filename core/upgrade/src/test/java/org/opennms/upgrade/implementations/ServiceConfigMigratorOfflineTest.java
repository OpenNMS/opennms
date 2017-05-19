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
