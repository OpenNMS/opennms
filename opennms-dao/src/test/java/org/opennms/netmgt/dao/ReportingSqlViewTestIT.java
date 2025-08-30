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
package org.opennms.netmgt.dao;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-mockConfigManager.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml"})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class ReportingSqlViewTestIT {

    @Autowired
    private DatabasePopulator databasePopulator;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Before
    public void before() {
        databasePopulator.populateDatabase();
    }

    @After
    public void after() {
        databasePopulator.resetDatabase();
    }

    @Test
    public void verifyNodeOutagesViewExist() {
        Assert.assertNotNull(jdbcTemplate.queryForList("SELECT outageid,nodelabel FROM node_outages;"));
    }

    @Test
    public void verifyNodeCategoriesViewExist() {
        Assert.assertNotNull(jdbcTemplate.queryForList("SELECT nodelabel,categoryname FROM node_categories;"));
    }

    @Test
    public void verifyNodeAlarmsViewExist() {
        Assert.assertNotNull(jdbcTemplate.queryForList("SELECT alarmid,nodelabel FROM node_alarms;"));
    }

    @Test
    public void verifyNodeIpServicesViewExist() {
        Assert.assertNotNull(jdbcTemplate.queryForList("SELECT nodelabel,ipaddr,servicename FROM node_ip_services;"));
    }
}
