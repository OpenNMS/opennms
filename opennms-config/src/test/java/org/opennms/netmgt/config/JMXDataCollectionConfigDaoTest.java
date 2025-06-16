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
