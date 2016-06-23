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

package org.opennms.core.config.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.opennms.core.config.api.ConfigurationResource;
import org.opennms.core.config.api.ConfigurationResourceException;
import org.opennms.netmgt.config.collectd.CollectdConfiguration;
import org.springframework.core.io.FileSystemResource;

public class JaxbResourceConfigurationTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    protected File getConfigFile() throws IOException {
        final File configFile = new File("target/test-classes/collectd-configuration.xml");
        final File tempFile = File.createTempFile("collectd", ".xml", configFile.getParentFile());
        final FileReader reader = new FileReader(configFile);
        final FileWriter writer = new FileWriter(tempFile);
        tempFile.deleteOnExit();
        IOUtils.copy(reader, writer);
        writer.close();
        reader.close();
        return tempFile;
    }

    @Test
    public void testFileSystemResourceExists() throws ConfigurationResourceException, IOException {
        final File configFile = getConfigFile();
        final ConfigurationResource<CollectdConfiguration> collectd = new JaxbResourceConfiguration<CollectdConfiguration>(CollectdConfiguration.class, new FileSystemResource(configFile));
        assertNotNull(collectd);
        final CollectdConfiguration config = collectd.get();
        assertNotNull(config);
        assertEquals(5, config.getPackages().size());
        assertEquals("vmware3", config.getPackages().get(0).getName());
    }

    @Test(expected=org.opennms.core.config.api.ConfigurationResourceException.class)
    public void testFileSystemResourceDoesNotExist() throws ConfigurationResourceException {
        final File configFile = new File("target/test-classes/collectd-configuration.x");
        final ConfigurationResource<CollectdConfiguration> collectd = new JaxbResourceConfiguration<CollectdConfiguration>(CollectdConfiguration.class, new FileSystemResource(configFile));
        assertNotNull(collectd);
        collectd.get();
    }

    @Test
    public void testSave() throws ConfigurationResourceException, IOException {
        final File configFile = getConfigFile();
        final ConfigurationResource<CollectdConfiguration> collectd = new JaxbResourceConfiguration<CollectdConfiguration>(CollectdConfiguration.class, new FileSystemResource(configFile));
        assertNotNull(collectd);
        CollectdConfiguration config = collectd.get();
        assertNotNull(config);
        assertEquals(5, config.getPackages().size());
        config.removePackage(config.getPackages().get(0));
        assertEquals("vmware4", config.getPackages().get(0).getName());
        assertEquals(4, config.getPackages().size());
        collectd.save(config);

        config = collectd.get();
        assertNotNull(config);
        assertEquals(4, config.getPackages().size());
        assertEquals("vmware4", config.getPackages().get(0).getName());
    }
}
