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
package org.opennms.features.vaadin.surveillanceviews.service;

import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.MockDatabase;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.api.OutageDao;
import org.opennms.netmgt.model.OnmsCategory;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsOutage;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import com.google.common.collect.Sets;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-mockConfigManager.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(reuseDatabase = false, tempDbClass = MockDatabase.class)
@Transactional
public class NMS14048_IT implements InitializingBean {

    @Autowired
    DatabasePopulator databasePopulator;

    @Autowired
    TransactionTemplate transactionOperations;

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    @Before
    public void setUp() throws Exception {
        databasePopulator.populateDatabase();
    }

    private OnmsOutage createOutage(final OnmsNode node, final Date lost, final Date regained) {
        final OnmsOutage onmsOutage = new OnmsOutage();
        onmsOutage.setNode(node);
        onmsOutage.setIfLostService(lost);
        onmsOutage.setIfRegainedService(regained);
        onmsOutage.setMonitoredService(databasePopulator.getMonitoredServiceDao().findByNode(node.getId()).get(0));
        return onmsOutage;
    }

    @Test
    public void testNMS14048() {
        final OutageDao outageDao = databasePopulator.getOutageDao();

        long now = new Date().getTime();
        outageDao.save(createOutage(databasePopulator.getNode1(), new Date(now - 60 * 60 * 1000), new Date(now - -60 * 60 * 1000 + 300_000)));
        outageDao.save(createOutage(databasePopulator.getNode1(), new Date(now - 24 * 60 * 60 * 1000 - 60 * 60 * 1000), new Date(now - 24 * 60 * 60 * 1000 - 60 * 60 * 1000 + 300_000)));

        final DefaultSurveillanceViewService surveillanceViewService = new DefaultSurveillanceViewService();
        surveillanceViewService.setTransactionOperations(transactionOperations);
        surveillanceViewService.setMonitoredServiceDao(databasePopulator.getMonitoredServiceDao());
        surveillanceViewService.setOutageDao(databasePopulator.getOutageDao());
        final OnmsCategory cat1 = databasePopulator.getCategoryDao().findByName("Routers");
        final OnmsCategory cat2 = databasePopulator.getCategoryDao().findByName("DEV_AC");
        final List<SurveillanceViewService.NodeRtc> nodeRtcList = surveillanceViewService.getNodeRtcsForCategories(Sets.newHashSet(cat1), Sets.newHashSet(cat2));

        Assert.assertNotNull(nodeRtcList);
        Assert.assertTrue("Availability must not exceed 100%.", nodeRtcList.get(0).getAvailability() <= 100.0);
    }
}
