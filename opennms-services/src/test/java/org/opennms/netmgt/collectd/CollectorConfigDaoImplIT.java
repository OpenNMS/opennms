/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.collectd;

import java.io.IOException;
import java.io.InputStream;

import junit.framework.TestCase;

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

        InputStream stream = null;

        stream = getInputStreamForFile("/org/opennms/netmgt/config/test-database-schema.xml");
        DatabaseSchemaConfigFactory.setInstance(new DatabaseSchemaConfigFactory(stream));
        stream.close();

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
            new CollectdConfigFactory(stream, "localhost", false);
        } finally {
            stream.close();
        }
    }
}
