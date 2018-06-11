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
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.dao.api.DistPollerDao;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.Situation;
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

    private OnmsAlarm linkDownAlarmOnR1;
    private OnmsAlarm linkDownAlarmOnR2;
    
    
    @Before
    public void setup() {
        // Create first alarm
        linkDownAlarmOnR1 = new OnmsAlarm();
        linkDownAlarmOnR1.setDistPoller(m_distPollerDao.whoami());
        linkDownAlarmOnR1.setCounter(1);
        linkDownAlarmOnR1.setUei("linkDown");

        // Create second alarm
        linkDownAlarmOnR2 = new OnmsAlarm();
        linkDownAlarmOnR2.setDistPoller(m_distPollerDao.whoami());
        linkDownAlarmOnR2.setCounter(1);
        linkDownAlarmOnR2.setUei("linkDown");

        m_alarmDao.save(linkDownAlarmOnR2);
        m_alarmDao.save(linkDownAlarmOnR1);

    }
    
    @Test
    @Transactional
    public void testCreate() {
        // create a situation relating multiple alarms
        Situation situation = new Situation();
        situation.setDistPoller(m_distPollerDao.whoami());
        situation.setCounter(1);
        situation.setUei("cardDown");
        situation.setAlarms(new HashSet<>(Arrays.asList(linkDownAlarmOnR1, linkDownAlarmOnR2)));
        situation.setReductionKey("situation/reduction/key");
        
        m_alarmDao.saveOrUpdate(situation);
        
        Situation retrieved = (Situation) m_alarmDao.findByReductionKey("situation/reduction/key");
        assertThat(retrieved.getAlarms().size(), is(2));
        assertThat(retrieved.getAlarms(), containsInAnyOrder(linkDownAlarmOnR1, linkDownAlarmOnR2));
    }

    @Test
    @Transactional
    public void testUpdateAddAlarm() {

        Situation situation = new Situation();
        situation.setDistPoller(m_distPollerDao.whoami());
        situation.setCounter(1);
        situation.setUei("cardDown");
        situation.setAlarms(new HashSet<>(Arrays.asList(linkDownAlarmOnR1, linkDownAlarmOnR2)));
        situation.setReductionKey("situation/reduction/key");
        
        m_alarmDao.saveOrUpdate(situation);

        Situation retrieved = (Situation) m_alarmDao.findByReductionKey("situation/reduction/key");
        assertThat(retrieved.getAlarms().size(), is(2));

        // update the situation by adding an alarm
        OnmsAlarm alarm3 = new OnmsAlarm();
        linkDownAlarmOnR2.setDistPoller(m_distPollerDao.whoami());
        linkDownAlarmOnR2.setCounter(1);
        linkDownAlarmOnR2.setUei("linkDown");
        retrieved.addAlarm(alarm3);
        assertThat(retrieved.getAlarms().size(), is(3));

        m_alarmDao.saveOrUpdate(retrieved);

        Situation retrieved2 = (Situation) m_alarmDao.findByReductionKey("situation/reduction/key");
        assertThat(retrieved2.getAlarms().size(), is(3));
   }

    @Test
    @Transactional
    public void testUpdateRemoveAlarm() {
        Situation situation = new Situation();
        situation.setDistPoller(m_distPollerDao.whoami());
        situation.setCounter(1);
        situation.setUei("cardDown");
        situation.setAlarms(new HashSet<>(Arrays.asList(linkDownAlarmOnR1, linkDownAlarmOnR2)));
        situation.setReductionKey("situation/reduction/key");

        m_alarmDao.saveOrUpdate(situation);

        Situation retrieved = (Situation) m_alarmDao.findByReductionKey("situation/reduction/key");
        assertThat(retrieved.getAlarms().size(), is(2));

        // remove one of the alarms
        retrieved.setAlarms(new HashSet<>(Arrays.asList(linkDownAlarmOnR1)));
        assertThat(retrieved.getAlarms().size(), is(1));
        
        m_alarmDao.saveOrUpdate(retrieved);

        Situation retrieved2 = (Situation) m_alarmDao.findByReductionKey("situation/reduction/key");
        assertThat(retrieved2.getAlarms().size(), is(1));
        assertThat(retrieved2.getAlarms().stream().findFirst(), is(Optional.of(linkDownAlarmOnR1)));
    }

    @Test
    @Transactional
    public void testDelete() {
        Situation situation = new Situation();
        situation.setDistPoller(m_distPollerDao.whoami());
        situation.setCounter(1);
        situation.setUei("cardDown");
        situation.setAlarms(new HashSet<>(Arrays.asList(linkDownAlarmOnR1, linkDownAlarmOnR2)));
        situation.setReductionKey("situation/reduction/key");
        
        m_alarmDao.saveOrUpdate(situation);

        Situation retrieved = (Situation) m_alarmDao.findByReductionKey("situation/reduction/key");
        assertThat(retrieved.getAlarms().size(), is(2));
        
        // delete the situation
        m_alarmDao.delete(retrieved);

        Situation retrieved2 = (Situation) m_alarmDao.findByReductionKey("situation/reduction/key");
        assertThat(retrieved2, is(nullValue()));
    }
}
