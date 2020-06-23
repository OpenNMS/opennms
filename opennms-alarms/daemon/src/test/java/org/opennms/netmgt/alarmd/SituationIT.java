/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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
package org.opennms.netmgt.alarmd;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.api.AlarmAssociationDao;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.dao.api.DistPollerDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.AlarmAssociation;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/applicationContext-alarmd.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(reuseDatabase=false)
public class SituationIT {

    @Autowired
    private AlarmDao m_alarmDao;

    @Autowired
    private DistPollerDao m_distPollerDao;

    @Autowired
    private NodeDao m_nodeDao;
    
    @Autowired
    private AlarmAssociationDao alarmAssociationDao;

    private OnmsAlarm linkDownAlarmOnR1;
    private OnmsAlarm linkDownAlarmOnR2;
    private OnmsAlarm linkDownAlarmOnR3;

    private OnmsNode testNode;

    @Before
    public void setup() {
        // Create a Test Node
        testNode = new OnmsNode();
        testNode.setLabel("TEST NODE");
        testNode.setCreateTime(new Date());
        testNode.setLocation(new OnmsMonitoringLocation("Default", "Default"));
        m_nodeDao.saveOrUpdate(testNode);

        // Create first alarm
        linkDownAlarmOnR1 = new OnmsAlarm();
        linkDownAlarmOnR1.setDistPoller(m_distPollerDao.whoami());
        linkDownAlarmOnR1.setCounter(1);
        linkDownAlarmOnR1.setUei("linkDown");
        linkDownAlarmOnR1.setNode(testNode);

        // Create second alarm
        linkDownAlarmOnR2 = new OnmsAlarm();
        linkDownAlarmOnR2.setDistPoller(m_distPollerDao.whoami());
        linkDownAlarmOnR2.setCounter(1);
        linkDownAlarmOnR2.setUei("linkDown");
        linkDownAlarmOnR2.setNode(testNode);


        // Create third alarm
        linkDownAlarmOnR3 = new OnmsAlarm();
        linkDownAlarmOnR3.setDistPoller(m_distPollerDao.whoami());
        linkDownAlarmOnR3.setCounter(1);
        linkDownAlarmOnR3.setUei("linkDown");
        linkDownAlarmOnR3.setNode(testNode);

        m_alarmDao.save(linkDownAlarmOnR3);
        m_alarmDao.save(linkDownAlarmOnR2);
        m_alarmDao.save(linkDownAlarmOnR1);

    }

    @Test
    @Transactional
    public void testCreate() {
        // create a situation relating multiple alarms
        OnmsAlarm situation = new OnmsAlarm();
        situation.setDistPoller(m_distPollerDao.whoami());
        situation.setCounter(1);
        situation.setUei("cardDown");
        situation.setRelatedAlarms(new HashSet<>(Arrays.asList(linkDownAlarmOnR1, linkDownAlarmOnR2)));
        situation.setReductionKey("situation/reduction/key");

        m_alarmDao.saveOrUpdate(situation);
        OnmsAlarm retrieved = m_alarmDao.findByReductionKey("situation/reduction/key");
        assertThat(retrieved.getRelatedAlarms().size(), is(2));
        assertThat(retrieved.getRelatedAlarms(), containsInAnyOrder(linkDownAlarmOnR1, linkDownAlarmOnR2));
    }

    @Test
    @Transactional
    public void testUpdateAddAlarm() {
        OnmsAlarm situation = new OnmsAlarm();
        situation.setDistPoller(m_distPollerDao.whoami());
        situation.setCounter(1);
        situation.setUei("cardDown");
        situation.setRelatedAlarms(new HashSet<>(Arrays.asList(linkDownAlarmOnR1, linkDownAlarmOnR2)));
        situation.setReductionKey("situation/reduction/key");

        m_alarmDao.saveOrUpdate(situation);

        OnmsAlarm retrieved = m_alarmDao.findByReductionKey("situation/reduction/key");
        assertThat(retrieved.getRelatedAlarms().size(), is(2));

        // update the situation by adding an alarm
        OnmsAlarm alarm3 = new OnmsAlarm();
        alarm3.setDistPoller(m_distPollerDao.whoami());
        alarm3.setCounter(1);
        alarm3.setUei("linkDown");
        // need to save alarms as related alarms are always fetched from db
        m_alarmDao.save(alarm3);
        retrieved.addRelatedAlarm(alarm3);
        assertThat(retrieved.getRelatedAlarms().size(), is(3));

        m_alarmDao.saveOrUpdate(retrieved);

        OnmsAlarm retrieved2 = m_alarmDao.findByReductionKey("situation/reduction/key");
        assertThat(retrieved2.getRelatedAlarms().size(), is(3));
        assertThat(retrieved2.getAffectedNodeCount(), is(1));
   }

    @Test
    @Transactional
    public void testUpdateRemoveAlarm() {
        OnmsAlarm situation = new OnmsAlarm();
        situation.setDistPoller(m_distPollerDao.whoami());
        situation.setCounter(1);
        situation.setUei("cardDown");
        situation.setRelatedAlarms(new HashSet<>(Arrays.asList(linkDownAlarmOnR1, linkDownAlarmOnR2)));
        situation.setReductionKey("situation/reduction/key");

        m_alarmDao.saveOrUpdate(situation);

        OnmsAlarm retrieved = m_alarmDao.findByReductionKey("situation/reduction/key");
        assertThat(retrieved.getRelatedAlarms().size(), is(2));

        // remove one of the alarms
        retrieved.setRelatedAlarms(new HashSet<>(Arrays.asList(linkDownAlarmOnR1)));
        assertThat(retrieved.getRelatedAlarms().size(), is(1));

        m_alarmDao.saveOrUpdate(retrieved);

        OnmsAlarm retrieved2 = m_alarmDao.findByReductionKey("situation/reduction/key");
        assertThat(retrieved2.getRelatedAlarms().size(), is(1));
        assertThat(retrieved2.getRelatedAlarms().stream().findFirst(), is(Optional.of(linkDownAlarmOnR1)));
    }

    @Test
    @Transactional
    public void testDelete() {
        long startTime = new Date().getTime();
        OnmsAlarm situation = new OnmsAlarm();
        situation.setDistPoller(m_distPollerDao.whoami());
        situation.setCounter(1);
        situation.setUei("cardDown");
        situation.setRelatedAlarms(new HashSet<>(Arrays.asList(linkDownAlarmOnR1, linkDownAlarmOnR2)));
        situation.setReductionKey("situation/reduction/key");

        m_alarmDao.saveOrUpdate(situation);

        OnmsAlarm retrieved = m_alarmDao.findByReductionKey("situation/reduction/key");
        assertThat(retrieved.getRelatedAlarms().size(), is(2));
        List<AlarmAssociation> associatedAlarms = alarmAssociationDao.findAll();
        assertThat(associatedAlarms.size(), is(2));
        AlarmAssociation alarmAssociation = associatedAlarms.get(0);
        assertThat(alarmAssociation.getMappedTime().getTime(), is(greaterThanOrEqualTo(startTime)));
        // delete the situation
        m_alarmDao.delete(retrieved);

        OnmsAlarm retrieved2 = m_alarmDao.findByReductionKey("situation/reduction/key");
        assertThat(retrieved2, is(nullValue()));
        associatedAlarms = alarmAssociationDao.findAll();
        assertThat(associatedAlarms.size(), is(0));
    }

    @Test
    @Transactional
    public void testMultipleSavesForSituation() {
        OnmsAlarm situation = new OnmsAlarm();
        situation.setDistPoller(m_distPollerDao.whoami());
        situation.setCounter(1);
        situation.setUei("cardDown");
        situation.setRelatedAlarms(new HashSet<>(Arrays.asList(linkDownAlarmOnR1, linkDownAlarmOnR3)));
        situation.setReductionKey("situation/reduction/key");

        m_alarmDao.saveOrUpdate(situation);
        OnmsAlarm retrieved = m_alarmDao.findByReductionKey("situation/reduction/key");
        assertThat(retrieved.getRelatedAlarms().size(), is(2));
        situation.setRelatedAlarms(new HashSet<>(Arrays.asList(linkDownAlarmOnR1, linkDownAlarmOnR2)));
        m_alarmDao.saveOrUpdate(situation);
        OnmsAlarm retrieved2 = m_alarmDao.findByReductionKey("situation/reduction/key");
        assertThat(retrieved2.getRelatedAlarms().size(), is(2));

        situation.addRelatedAlarm(linkDownAlarmOnR1);
        m_alarmDao.saveOrUpdate(situation);
        OnmsAlarm retrieved3 = m_alarmDao.findByReductionKey("situation/reduction/key");
        assertThat(retrieved3.getRelatedAlarms().size(), is(2));

    }
}
