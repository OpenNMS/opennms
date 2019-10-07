/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.alarmd.examples;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Optional;

import org.apache.commons.io.FileUtils;
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
