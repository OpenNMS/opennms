/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.notifd;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.config.NotificationManager;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.model.notifd.Argument;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath*:/META-INF/opennms/component-service.xml",
        "classpath:/META-INF/opennms/applicationContext-pinger.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        // Notifd
        "classpath:/META-INF/opennms/applicationContext-notifdTest.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class BSFNotificationStrategyIT {

    private BSFNotificationStrategy bsfNotificationStrategy = new BSFNotificationStrategy();

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Autowired
    private DatabasePopulator databasePopulator;

    @Before
    public void setUp() {
        MockLogAppender.setupLogging();

        // Add nodes to the database - we reference node with id 1
        databasePopulator.populateDatabase();
    }

    @After
    public void tearDown() {
        MockLogAppender.assertNoWarningsOrGreater();
    }

    /**
     * Verifies that we can invoke a BSH script and that
     * the an instance of the appropriate OnmsNode object is
     * passed to the script. 
     */
    @Test
    public void canUseNodeInScript() throws IOException {
        // Create a simple BSH script that verifies the node bean
        File notifyBsh = tempFolder.newFile("notify.bsh");
        FileUtils.write(notifyBsh, "results.put(\"status\", node.id == 1 ? \"OK\" : \"NOT_OK\");");

        List<Argument> arguments = new ArrayList<>();
        // Point to our script
        arguments.add(new Argument("file-name", null, notifyBsh.getAbsolutePath(), false));
        // Reference node 1
        arguments.add(new Argument(NotificationManager.PARAM_NODE, null, "1", false));

        // Should succeed
        assertEquals(0, bsfNotificationStrategy.send(arguments));
    }
}
