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
package org.opennms.netmgt.alarmd.examples;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Optional;

import org.apache.commons.io.FileUtils;
import org.hibernate.SessionFactory;
import org.hibernate.Session;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.netmgt.alarmd.drools.DefaultAlarmService;
import org.opennms.netmgt.alarmd.drools.DroolsAlarmContext;
import org.opennms.netmgt.dao.api.AcknowledgmentDao;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.dao.mock.MockTransactionTemplate;
import org.opennms.netmgt.dao.support.AlarmEntityNotifierImpl;
import org.opennms.netmgt.events.api.EventForwarder;

public abstract class DroolsExampleIT {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    protected DroolsAlarmContext dac;
    protected AlarmDao alarmDao;
    protected EventForwarder eventForwarder;

    public abstract String getRulesFile();

    @Before
    public void setUp() throws IOException, InterruptedException {
        // Copy default set of rules to a new folder
        File rulesFolder = temporaryFolder.newFolder("rules");
        FileUtils.copyDirectory(DroolsAlarmContext.getDefaultRulesFolder(), rulesFolder);

        // Copy the rules from the example folder alongside the rules in our temporary folder
        FileUtils.copyFile(Paths.get(ConfigFileConstants.getHome(), "etc", "examples",
                "alarmd", "drools-rules.d", getRulesFile()).toFile(),
                new File(rulesFolder, getRulesFile()));

        // Wire up the engine with mocks
        dac = new DroolsAlarmContext(rulesFolder);
        dac.setUsePseudoClock(true);
        dac.setUseManualTick(true);

        MockTransactionTemplate transactionTemplate = new MockTransactionTemplate();
        transactionTemplate.afterPropertiesSet();
        dac.setTransactionTemplate(transactionTemplate);

        alarmDao = mock(AlarmDao.class);
        dac.setAlarmDao(alarmDao);

        DefaultAlarmService alarmService = new DefaultAlarmService();
        alarmService.setAlarmDao(alarmDao);
        eventForwarder = mock(EventForwarder.class);
        alarmService.setEventForwarder(eventForwarder);

        AcknowledgmentDao acknowledgmentDao = mock(AcknowledgmentDao.class);
        when(acknowledgmentDao.findLatestAckForRefId(any(Integer.class))).thenReturn(Optional.empty());
        alarmService.setAcknowledgmentDao(acknowledgmentDao);

        AlarmEntityNotifierImpl alarmEntityNotifier = mock(AlarmEntityNotifierImpl.class);
        alarmService.setAlarmEntityNotifier(alarmEntityNotifier);
        dac.setAlarmService(alarmService);
        dac.setAcknowledgmentDao(acknowledgmentDao);

        Session session = mock(Session.class);
        SessionFactory sessionFactory = mock(SessionFactory.class);
        when(sessionFactory.getCurrentSession()).thenReturn(session);
        dac.setSessionFactory(sessionFactory);

        dac.start();
        dac.waitForInitialSeedToBeSubmitted();
    }

    @After
    public void tearDown() {
        if (dac != null) {
            dac.stop();
        }
    }
}
