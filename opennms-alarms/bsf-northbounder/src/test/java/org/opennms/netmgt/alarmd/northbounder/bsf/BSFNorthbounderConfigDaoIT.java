/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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
package org.opennms.netmgt.alarmd.northbounder.bsf;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.springframework.core.io.FileSystemResource;

/**
 * The test Class for BSFNorthbounderConfigDao.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class BSFNorthbounderConfigDaoIT {

    /** The temporary folder. */
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    /** The configuration DAO. */
    private BSFNorthbounderConfigDao configDao;

    /**
     * Sets up the test..
     *
     * @throws Exception the exception
     */
    @Before
    public void setUp() throws Exception {
        FileUtils.copyDirectory(new File("src/test/resources/etc"), tempFolder.newFolder("etc"));
        System.setProperty("opennms.home", tempFolder.getRoot().getAbsolutePath());

        // Setup the configuration DAO
        FileSystemResource resource = new FileSystemResource(new File(tempFolder.getRoot(), "etc/bsf-northbounder-config.xml"));
        configDao = new BSFNorthbounderConfigDao();
        configDao.setConfigResource(resource);
        configDao.afterPropertiesSet();
    }

    /**
     * Test configuration.
     *
     * @throws Exception the exception
     */
    @Test
    public void testConfiguration() throws Exception {
        BSFNorthbounderConfig config = configDao.getConfig();
        Assert.assertNotNull(config);
        BSFEngineHandler engine = config.getEngine("JUnit");
        Assert.assertNotNull(engine);
    }

}
