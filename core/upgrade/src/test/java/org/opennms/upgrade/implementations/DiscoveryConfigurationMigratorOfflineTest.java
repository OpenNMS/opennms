/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2015 The OpenNMS Group, Inc.
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

package org.opennms.upgrade.implementations;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.discovery.DiscoveryConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DiscoveryConfigurationMigratorOfflineTest {
    private static final Logger LOG = LoggerFactory.getLogger(DiscoveryConfigurationMigratorOfflineTest.class);

    @Rule
    public TemporaryFolder m_tempFolder = new TemporaryFolder();

    @Before
    public void setUp() throws Exception {
        FileUtils.copyDirectory(new File("src/test/resources/etc"), m_tempFolder.newFolder("etc"));
        System.setProperty("opennms.home", m_tempFolder.getRoot().getAbsolutePath());
        final List<File> files = new ArrayList<>(FileUtils.listFilesAndDirs(new File(m_tempFolder.getRoot(), "etc"), TrueFileFilter.TRUE, TrueFileFilter.INSTANCE));
        Collections.sort(files);
    }

    @Test
    public void testRemoveAttribute() throws Exception {
        final DiscoveryConfigurationMigratorOffline task = new DiscoveryConfigurationMigratorOffline();
        task.preExecute();
        task.execute();
        task.postExecute();

        final File configFile = new File(m_tempFolder.getRoot(), "etc/discovery-configuration.xml");
        final DiscoveryConfiguration config = JaxbUtils.unmarshal(DiscoveryConfiguration.class, configFile);
        Assert.assertNotNull(config);
        Assert.assertEquals(1, config.getIncludeRanges().size());
    }
}
