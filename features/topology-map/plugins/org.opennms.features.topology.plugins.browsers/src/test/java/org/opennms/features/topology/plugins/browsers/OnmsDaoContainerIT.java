/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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
 * OpenNMS(R) Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.topology.plugins.browsers;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.features.topology.api.browsers.ContentType;
import org.opennms.features.topology.api.browsers.OnmsVaadinContainer;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.dao.api.DistPollerDao;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionOperations;

@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath*:/META-INF/opennms/component-service.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(reuseDatabase = false)
@RunWith(OpenNMSJUnit4ClassRunner.class)
@Transactional
public class OnmsDaoContainerIT {

    @Autowired
    private DatabasePopulator m_databasePopulator;

    @Autowired
    private DistPollerDao m_distPollerDao;

    @Autowired
    private AlarmDao m_alarmDao;

    @Autowired
    private TransactionOperations m_transactionTemplate;

    @Before
    public void setUp() throws Exception {
        BeanUtils.assertAutowiring(this);
        m_databasePopulator.populateDatabase();
    }

    @Test
    public void verifyCacheIsReloadedCorrectly() {
        OnmsVaadinContainer<OnmsAlarm, Integer> container = new OnmsVaadinContainer<OnmsAlarm, Integer>(OnmsAlarm.class, new OnmsDaoContainerDatasource<>(m_alarmDao, m_transactionTemplate)) {
            @Override
            protected Integer getId(OnmsAlarm bean) {
                return bean == null ? null : bean.getId();
            }

            @Override
            protected ContentType getContentType() {
                return ContentType.Alarm;
            }
        };

        // verify that we have all item ids we need
        final int size = m_alarmDao.countAll();
        Collection<Integer> itemIds = (Collection<Integer>) container.getItemIds();
        Assert.assertEquals(size, itemIds.size());
        Assert.assertEquals(getAlarmIds(), sort(itemIds));

        // create new alarm and verify again
        createAlarm();
        itemIds = (Collection<Integer>) container.getItemIds();
        Assert.assertEquals(size + 1, itemIds.size());
        Assert.assertEquals(getAlarmIds(), sort(itemIds));
    }

    private <T> List<T> sort(Collection<T> input) {
        return input.stream().sorted().collect(Collectors.toList());
    }

    private List<Integer> getAlarmIds() {
        return m_alarmDao.findAll().stream().map(eachAlarm -> eachAlarm.getId()).sorted().collect(Collectors.toList());
    }

    private OnmsAlarm createAlarm() {
        final OnmsAlarm alarm = new OnmsAlarm();
        alarm.setDistPoller(m_distPollerDao.whoami());
        alarm.setUei(EventConstants.NODE_LOST_SERVICE_EVENT_UEI);
        alarm.setAlarmType(OnmsAlarm.PROBLEM_TYPE);
        alarm.setNode(m_databasePopulator.getNode1());
        alarm.setDescription("This is a test alarm");
        alarm.setLogMsg("this is a test alarm log message");
        alarm.setCounter(1);
        alarm.setIpAddr(InetAddressUtils.getInetAddress("192.168.1.1"));
        alarm.setSeverity(OnmsSeverity.NORMAL);

        m_alarmDao.save(alarm);
        m_alarmDao.flush();
        return alarm;
    }
}


