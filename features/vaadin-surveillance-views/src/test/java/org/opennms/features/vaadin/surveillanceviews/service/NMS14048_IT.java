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
