/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018-2018 The OpenNMS Group, Inc.
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
package org.opennms.features.situationfeedback.rest;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.features.situationfeedback.api.AlarmFeedback;
import org.opennms.features.situationfeedback.api.AlarmFeedback.FeedbackType;
import org.opennms.features.situationfeedback.api.FeedbackRepository;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.dao.api.DistPollerDao;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionOperations;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml", "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(reuseDatabase = false)
public class SituationFeedbackrestServiceIT {

    @Autowired
    private AlarmDao alarmDao;

    @Autowired
    private DistPollerDao distPollerDao;

    @Autowired
    private TransactionOperations transactionTemplate;

    private FeedbackRepository mockFeebackRepository = mock(FeedbackRepository.class);

    private OnmsAlarm linkDownAlarmOnR1;

    private OnmsAlarm linkDownAlarmOnR2;

    private OnmsAlarm situation;

    @Before
    public void setup() {
        // Create first alarm
        linkDownAlarmOnR1 = new OnmsAlarm();
        linkDownAlarmOnR1.setDistPoller(distPollerDao.whoami());
        linkDownAlarmOnR1.setCounter(1);
        linkDownAlarmOnR1.setUei("linkDown");
        linkDownAlarmOnR1.setReductionKey("linkDownAlarmOnR1");

        // Create second alarm
        linkDownAlarmOnR2 = new OnmsAlarm();
        linkDownAlarmOnR2.setDistPoller(distPollerDao.whoami());
        linkDownAlarmOnR2.setCounter(1);
        linkDownAlarmOnR2.setUei("linkDown");
        linkDownAlarmOnR2.setReductionKey("linkDownAlarmOnR2");

        alarmDao.save(linkDownAlarmOnR1);
        alarmDao.save(linkDownAlarmOnR2);

        // create a situation relating multiple alarms
        situation = new OnmsAlarm();
        situation.setDistPoller(distPollerDao.whoami());
        situation.setCounter(1);
        situation.setUei("cardDown");
        situation.setRelatedAlarms(new HashSet<>(Arrays.asList(linkDownAlarmOnR1, linkDownAlarmOnR2)));
        situation.setReductionKey("situation/reduction/key");

        alarmDao.saveOrUpdate(situation);
    }

    @Test
    @Transactional
    public void testRemoveAlarmWithFeedback() {
        SituationFeedbackRestServiceImpl sut = new SituationFeedbackRestServiceImpl(alarmDao, mockFeebackRepository, transactionTemplate);
        AlarmFeedback falsePositive = new AlarmFeedback(situation.getReductionKey(), "fingerprint", linkDownAlarmOnR1.getReductionKey(),
                                                        FeedbackType.FALSE_POSITVE, "not related", "user", System.currentTimeMillis());
        List<AlarmFeedback> feedback = Collections.singletonList(falsePositive);

        OnmsAlarm prior = alarmDao.findByReductionKey(situation.getReductionKey());
        assertThat(prior.getRelatedAlarms().size(), is(2));

        sut.setFeedback(feedback);

        OnmsAlarm restrieved = alarmDao.findByReductionKey(situation.getReductionKey());
        assertThat(restrieved.getRelatedAlarms().size(), is(1));
        assertThat(restrieved.getRelatedAlarms().stream().findFirst(), is(Optional.of(linkDownAlarmOnR2)));
    }

}
