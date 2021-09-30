/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
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
