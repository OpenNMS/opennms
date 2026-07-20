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
package org.opennms.netmgt.surveillance.views;

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
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsOutage;
import org.opennms.netmgt.surveillance.views.SurveillanceViewDataService.NodeRtc;
import org.opennms.netmgt.surveillance.views.impl.DefaultSurveillanceViewDataService;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import com.google.common.collect.Sets;

/**
 * Ports the Vaadin service's {@code NMS14048_IT} onto
 * {@link DefaultSurveillanceViewDataService}: an outage window overlapping the
 * 24h RTC period boundary must not push availability past 100%.
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-mockConfigManager.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-mockSnmpPeerFactory.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(reuseDatabase = false, tempDbClass = MockDatabase.class)
@Transactional
public class DefaultSurveillanceViewDataServiceRtcIT implements InitializingBean {

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
    public void testAvailabilityDoesNotExceedOneHundredPercent() {
        final OutageDao outageDao = databasePopulator.getOutageDao();

        long now = new Date().getTime();
        outageDao.save(createOutage(databasePopulator.getNode1(), new Date(now - 60 * 60 * 1000), new Date(now - -60 * 60 * 1000 + 300_000)));
        outageDao.save(createOutage(databasePopulator.getNode1(), new Date(now - 24 * 60 * 60 * 1000 - 60 * 60 * 1000), new Date(now - 24 * 60 * 60 * 1000 - 60 * 60 * 1000 + 300_000)));

        final DefaultSurveillanceViewDataService service = new DefaultSurveillanceViewDataService();
        service.setTransactionOperations(transactionOperations);
        service.setMonitoredServiceDao(databasePopulator.getMonitoredServiceDao());
        service.setOutageDao(databasePopulator.getOutageDao());

        final List<NodeRtc> nodeRtcList = service.getNodeRtcsForCategories(Sets.newHashSet("Routers"), Sets.newHashSet("DEV_AC"));

        Assert.assertNotNull(nodeRtcList);
        Assert.assertFalse(nodeRtcList.isEmpty());
        Assert.assertNotNull(nodeRtcList.get(0).getNodeLabel());
        Assert.assertTrue("Availability must not exceed 100%.", nodeRtcList.get(0).getAvailability() <= 1.0);
    }
}
