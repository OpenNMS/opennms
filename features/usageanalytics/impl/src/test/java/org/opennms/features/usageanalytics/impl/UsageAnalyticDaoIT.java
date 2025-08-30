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
