/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.opennms.core.xml.MarshallingResourceFailureException;
import org.opennms.netmgt.config.collectd.jmx.JmxDatacollectionConfig;

public class JMXDataCollectionConfigDaoTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test(expected=MarshallingResourceFailureException.class)
    public void failsWhenNoConfigIsPresent() {
        JMXDataCollectionConfigDao dao = new JMXDataCollectionConfigDao();
        dao.setOpennmsHome(tempFolder.getRoot().toPath());
        dao.getConfig();
    }

    @Test
    public void loadsConfigFiles() throws IOException {
        JMXDataCollectionConfigDao dao = new JMXDataCollectionConfigDao();
        dao.setOpennmsHome(Paths.get(new File( "." ).getCanonicalPath(), "src/test/resources"));
        dao.getConfig();

        JmxDatacollectionConfig config = dao.getConfig();
        Assert.assertNotNull("JMX data collection should not be null", config);
        // These are declared in the top-level jmx-datacollection-config.xml file
        Assert.assertEquals(4, config.getJmxCollection("jboss").getMbeanCount());
        Assert.assertEquals(15, config.getJmxCollection("jsr160").getMbeanCount());
        // These are automatically included from the jmx-datacollection-config.d/ folder
        Assert.assertEquals(8, config.getJmxCollection("jboss-included").getMbeanCount());
        Assert.assertEquals(4, config.getJmxCollection("jsr160-included").getMbeanCount());
        // These beans should be imported for other files
        Assert.assertEquals(16, config.getJmxCollection("ActiveMQ").getMbeanCount());
    }
}
