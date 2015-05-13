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
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        // Notifd
        "classpath:/META-INF/opennms/applicationContext-notifdTest.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class BSFNotificationStrategyTest {

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

        List<Argument> arguments = new ArrayList<Argument>();
        // Point to our script
        arguments.add(new Argument("file-name", null, notifyBsh.getAbsolutePath(), false));
        // Reference node 1
        arguments.add(new Argument(NotificationManager.PARAM_NODE, null, "1", false));

        // Should succeed
        assertEquals(0, bsfNotificationStrategy.send(arguments));
    }
}
