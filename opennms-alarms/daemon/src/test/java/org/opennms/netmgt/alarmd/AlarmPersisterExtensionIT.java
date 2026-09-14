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
package org.opennms.netmgt.alarmd;

import static org.awaitility.Awaitility.await;
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
import org.opennms.netmgt.dao.api.DistPollerDao;
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
import org.springframework.transaction.support.TransactionTemplate;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-mockConfigManager.xml",
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

    @Autowired
    private TransactionTemplate transactionTemplate;

    private MockDatabase m_database;

    @Autowired
    private DistPollerDao m_distPollerDao;

    @Before
    public void setUp() {
        // Async.
        m_eventMgr.setSynchronous(false);

        transactionTemplate.execute(status -> {
            // Events need database IDs to make alarmd happy
            m_database.setDistPoller(m_distPollerDao.whoami().getId());
            m_eventMgr.setEventWriter(m_database);

            // Events need to real nodes too
            final OnmsNode node = new OnmsNode(m_locationDao.getDefaultLocation(), "node1");
            node.setId(1);
            m_nodeDao.save(node);

            // Register!
            m_alarmPersister.onExtensionRegistered(this, Collections.emptyMap());
            return null;
        });

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
