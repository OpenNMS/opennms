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
package org.opennms.netmgt.collectd;

import java.io.IOException;
import java.io.InputStream;

import org.opennms.core.db.DataSourceFactory;
import org.opennms.core.test.ConfigurationTestUtils;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.db.MockDatabase;
import org.opennms.netmgt.config.CollectdConfigFactory;
import org.opennms.netmgt.config.DataCollectionConfigFactory;
import org.opennms.netmgt.config.DatabaseSchemaConfigFactory;
import org.opennms.netmgt.config.DefaultDataCollectionConfigDao;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.test.mock.MockUtil;
import org.springframework.core.io.InputStreamResource;

import junit.framework.TestCase;

public class CollectorConfigDaoImplIT extends TestCase {
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockUtil.println("------------ Begin Test "+getName()+" --------------------------");
        MockLogAppender.setupLogging();

        MockDatabase m_db = new MockDatabase();
        //        m_db.populate(m_network);

        DataSourceFactory.setInstance(m_db);

    }

    @Override
    public void runTest() throws Throwable {
        super.runTest();
        MockLogAppender.assertNoWarningsOrGreater();
    }

    @Override
    protected void tearDown() throws Exception {
        MockUtil.println("------------ End Test "+getName()+" --------------------------");
        super.tearDown();
    }

    private InputStream getInputStreamForFile(String fileName) {
        return getClass().getResourceAsStream(fileName);
    }

    public void testInstantiate() throws IOException, Exception {
        initialize();
    }

    private void initialize() throws IOException, Exception {
        System.setProperty("opennms.home", ConfigurationTestUtils.getDaemonEtcDirectory().getParentFile().getAbsolutePath());

        DatabaseSchemaConfigFactory.init();

        InputStream stream = null;
        stream = getInputStreamForFile("/org/opennms/netmgt/config/snmp-config.xml");
        SnmpPeerFactory.setInstance(new SnmpPeerFactory(new InputStreamResource(stream)));
        stream.close();

        stream = getInputStreamForFile("/org/opennms/netmgt/config/datacollection-config.xml");
        DefaultDataCollectionConfigDao dataCollectionDao = new DefaultDataCollectionConfigDao();
        dataCollectionDao.setConfigResource(new InputStreamResource(stream));
        dataCollectionDao.afterPropertiesSet();
        DataCollectionConfigFactory.setInstance(dataCollectionDao);
        stream.close();

        stream = getInputStreamForFile("/org/opennms/netmgt/config/collectd-testdata.xml");
        try {
            new CollectdConfigFactory(stream);
        } finally {
            stream.close();
        }
    }
}
