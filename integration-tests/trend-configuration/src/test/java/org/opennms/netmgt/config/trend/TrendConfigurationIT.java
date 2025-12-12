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
package org.opennms.netmgt.config.trend;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.db.DataSourceFactory;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.ConfigurationTestUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-mockSnmpPeerFactory.xml",
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-mockConfigManager.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(reuseDatabase = false)
@RunWith(OpenNMSJUnit4ClassRunner.class)
public class TrendConfigurationIT {
    private static final Logger LOG = LoggerFactory.getLogger(TrendConfigurationIT.class);

    @Autowired
    private DatabasePopulator m_databasePopulator;

    @Before
    public void setUp() throws Exception {
        BeanUtils.assertAutowiring(this);
        m_databasePopulator.populateDatabase();
    }

    @Test
    public void verifySqlStatements() throws Exception {
        final TrendConfiguration trendConfiguration = JaxbUtils.unmarshal(TrendConfiguration.class, ConfigurationTestUtils.getFileForConfigFile("trend-configuration.xml"));

        for (final TrendDefinition trendDefinition : trendConfiguration.getTrendDefinitions()) {
            LOG.info("validating query for definition '{}'...", trendDefinition.getName());
            checkQuery(trendDefinition.getQuery());
        }
    }

    public void checkQuery(final String query) throws Exception {
        final Connection connection = DataSourceFactory.getInstance().getConnection();
        final Statement statement = connection.createStatement();
        final ResultSet resultSet = statement.executeQuery(query);
        resultSet.close();
        statement.close();
        connection.close();
    }
}
