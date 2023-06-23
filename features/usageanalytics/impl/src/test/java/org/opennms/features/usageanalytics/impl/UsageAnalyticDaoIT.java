/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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


package org.opennms.features.usageanalytics.impl;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.features.usageanalytics.api.UsageAnalytic;
import org.opennms.features.usageanalytics.api.UsageAnalyticDao;
import org.opennms.features.usageanalytics.api.UsageAnalyticMetricName;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;


/**
 * Basic Unit test to check that the database and methods of the interface are working correctly.
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml"})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(reuseDatabase = false)
public class UsageAnalyticDaoIT {

    private static final Logger LOG = LoggerFactory.getLogger(UsageAnalyticDaoIT.class);

    @Autowired
    private UsageAnalyticDao usageAnalyticDao;

    @Test
    @Transactional
    public void persistenceOfUsageAnalyticTest() {
        LOG.info("Test started");
        UsageAnalytic ua = new UsageAnalytic();
        ua.setMetricName(UsageAnalyticMetricName.DCB_FAILED.toString());
        ua.setCounter(13l);
        usageAnalyticDao.saveOrUpdate(ua);

        ua = new UsageAnalytic();
        ua.setMetricName(UsageAnalyticMetricName.DCB_SUCCEED.toString());
        ua.setCounter(23l);
        usageAnalyticDao.saveOrUpdate(ua);

        Assert.assertEquals(13l, usageAnalyticDao.getValueByMetricName(UsageAnalyticMetricName.DCB_FAILED.toString()));
        Assert.assertEquals(23l, usageAnalyticDao.getValueByMetricName(UsageAnalyticMetricName.DCB_SUCCEED.toString()));

        usageAnalyticDao.incrementCounterByMetricName(UsageAnalyticMetricName.DCB_FAILED.toString());
        usageAnalyticDao.incrementCounterByMetricName(UsageAnalyticMetricName.DCB_SUCCEED.toString());

        Assert.assertEquals(14l, usageAnalyticDao.getValueByMetricName(UsageAnalyticMetricName.DCB_FAILED.toString()));
        Assert.assertEquals(24l, usageAnalyticDao.getValueByMetricName(UsageAnalyticMetricName.DCB_SUCCEED.toString()));

        LOG.info("Test finished");
    }
}
