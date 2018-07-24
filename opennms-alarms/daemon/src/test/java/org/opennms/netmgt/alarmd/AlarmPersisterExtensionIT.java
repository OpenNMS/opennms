/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
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

import static com.jayway.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;

import java.util.Collections;
import java.util.Date;
import java.util.concurrent.Callable;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.MockDatabase;
import org.opennms.core.test.db.TemporaryDatabaseAware;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.alarmd.api.AlarmPersisterExtension;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.dao.api.MonitoringLocationDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.mock.MockEventIpcManager;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.AlarmData;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

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
@JUnitTemporaryDatabase(dirtiesContext=false,tempDbClass=MockDatabase.class)
public class AlarmPersisterExtensionIT implements TemporaryDatabaseAware<MockDatabase>, AlarmPersisterExtension {

    @Autowired
    private Alarmd m_alarmd;

    @Autowired
    private MonitoringLocationDao m_locationDao;

    @Autowired
    private NodeDao m_nodeDao;

    @Autowired
    private AlarmDao m_alarmDao;

    @Autowired
    private MockEventIpcManager m_eventMgr;

    @Autowired
    private AlarmPersisterImpl m_alarmPersister;

    private MockDatabase m_database;

    @Before
    public void setUp() {
        // Async.
        m_eventMgr.setSynchronous(false);

        // Events need database IDs to make alarmd happy
        m_eventMgr.setEventWriter(m_database);

        // Events need to real nodes too
        final OnmsNode node = new OnmsNode(m_locationDao.getDefaultLocation(), "node1");
        node.setId(1);
        m_nodeDao.save(node);

        // Register!
        m_alarmPersister.onExtensionRegistered(this, Collections.emptyMap());

        // Fire it up
        m_alarmd.start();
    }

    @After
    public void tearDown() {
        m_alarmd.destroy();
    }

    @Test
    public void canIssueCreateAndUpdateCallbacks() throws Exception {
        // Send a nodeDown
        sendNodeDownEvent(1);

        // Wait until the alarm is created
        await().until(() -> getNodeDownAlarmWithDaoFor(1).call(), notNullValue());

        // Verify that the persisted alarm has some property we have set in the (create) callback
        await().until(() -> getNodeDownAlarmWithDaoFor(1).call().getManagedObjectType(), equalTo("create"));

        // Send another nodeDown
        sendNodeDownEvent(1);

        // Verify that the persisted alarm has some property we have set in the (update) callback
        await().until(() -> getNodeDownAlarmWithDaoFor(1).call().getManagedObjectType(), equalTo("update"));
    }

    @Override
    public void afterAlarmCreated(OnmsAlarm alarm, Event event, OnmsEvent dbEvent) {
        // Update *some* alarm property to help validate that the resulting object gets saved
        alarm.setManagedObjectInstance(alarm.getNodeLabel());
        alarm.setManagedObjectType("create");
    }

    @Override
    public void afterAlarmUpdated(OnmsAlarm alarm, Event event, OnmsEvent dbEvent) {
        // Update *some* alarm property to help validate that the resulting object gets saved
        alarm.setManagedObjectType("update");
    }

    private void sendNodeDownEvent(long nodeId) {
        EventBuilder builder = new EventBuilder(EventConstants.NODE_DOWN_EVENT_UEI, "test");
        Date currentTime = new Date();
        builder.setTime(currentTime);
        builder.setNodeid(nodeId);
        builder.setSeverity(OnmsSeverity.MAJOR.getLabel());

        AlarmData data = new AlarmData();
        data.setAlarmType(1);
        data.setReductionKey(reductionKeyForNodeDown(nodeId));
        builder.setAlarmData(data);

        builder.setLogDest("logndisplay");
        builder.setLogMessage("testing");

        m_eventMgr.sendNow(builder.getEvent());
    }

    private String reductionKeyForNodeDown(long nodeId) {
        return String.format("%s:%d", EventConstants.NODE_DOWN_EVENT_UEI, nodeId);
    }

    private Callable<OnmsAlarm> getNodeDownAlarmWithDaoFor(long nodeId) {
        return () -> m_alarmDao.findByReductionKey(reductionKeyForNodeDown(nodeId));
    }

    @Override
    public void setTemporaryDatabase(final MockDatabase database) {
        m_database = database;
    }
}
