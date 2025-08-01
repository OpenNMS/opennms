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
package org.opennms.core.config.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.opennms.core.config.api.ConfigurationResource;
import org.opennms.core.config.api.ConfigurationResourceException;
import org.opennms.netmgt.config.collectd.CollectdConfiguration;
import org.springframework.core.io.FileSystemResource;

public class JaxbResourceConfigurationTest {

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
        assertEquals(2, config.getPackages().size());
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
        assertEquals(2, config.getPackages().size());
        config.removePackage(config.getPackages().get(0));
        assertEquals(1, config.getPackages().size());
        collectd.save(config);

        config = collectd.get();
        assertNotNull(config);
        assertEquals(1, config.getPackages().size());
    }
}
