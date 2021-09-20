/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2018 The OpenNMS Group, Inc.
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
