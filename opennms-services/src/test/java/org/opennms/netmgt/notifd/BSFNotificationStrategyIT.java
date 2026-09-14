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
import org.opennms.core.test.Level;
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
        "classpath:/META-INF/opennms/applicationContext-mockConfigManager.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath*:/META-INF/opennms/component-service.xml",
        "classpath:/META-INF/opennms/applicationContext-pinger.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/applicationContext-testPollerConfigDaos.xml",
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

        // Add nodes to the database; node ids are sequence-assigned, so tests
        // must reference databasePopulator.getNode1().getId() rather than 1
        databasePopulator.populateDatabase();
    }

    private String node1Id() {
        return String.valueOf(databasePopulator.getNode1().getId());
    }

    @After
    public void tearDown() {
        // the temporary database outlives individual test methods
        databasePopulator.resetDatabase();
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
        FileUtils.write(notifyBsh, "results.put(\"status\", node.id == " + node1Id() + " ? \"OK\" : \"NOT_OK\");");

        List<Argument> arguments = new ArrayList<>();
        // Point to our script
        arguments.add(new Argument("file-name", null, notifyBsh.getAbsolutePath(), false));
        // Reference node 1
        arguments.add(new Argument(NotificationManager.PARAM_NODE, null, node1Id(), false));

        // Should succeed
        assertEquals(0, bsfNotificationStrategy.send(arguments));
    }

    @Test
    public void canEvalBeanshellScript() throws IOException {
        // Under run-type=eval the script's last expression becomes the status
        File notifyBsh = tempFolder.newFile("notify-eval.bsh");
        FileUtils.write(notifyBsh, "node.id == " + node1Id() + " ? \"OK\" : \"NOT_OK\";");

        List<Argument> arguments = new ArrayList<>();
        arguments.add(new Argument("file-name", null, notifyBsh.getAbsolutePath(), false));
        arguments.add(new Argument("run-type", null, "eval", false));
        arguments.add(new Argument(NotificationManager.PARAM_NODE, null, node1Id(), false));

        assertEquals(0, bsfNotificationStrategy.send(arguments));
    }

    @Test
    public void canUseGroovyScript() throws IOException {
        File notifyGroovy = tempFolder.newFile("notify.groovy");
        FileUtils.write(notifyGroovy, "results.put(\"status\", node.getId() == " + node1Id() + " ? \"OK\" : \"NOT_OK\")");

        List<Argument> arguments = new ArrayList<>();
        arguments.add(new Argument("file-name", null, notifyGroovy.getAbsolutePath(), false));
        arguments.add(new Argument(NotificationManager.PARAM_NODE, null, node1Id(), false));

        assertEquals(0, bsfNotificationStrategy.send(arguments));
    }

    @Test
    public void canResolveGyExtensionAsGroovy() throws IOException {
        // BSF registered .gy for Groovy; the JSR-223 Groovy engine does not
        File notifyGy = tempFolder.newFile("notify.gy");
        FileUtils.write(notifyGy, "results.put(\"status\", node.getId() == " + node1Id() + " ? \"OK\" : \"NOT_OK\")");

        List<Argument> arguments = new ArrayList<>();
        arguments.add(new Argument("file-name", null, notifyGy.getAbsolutePath(), false));
        arguments.add(new Argument(NotificationManager.PARAM_NODE, null, node1Id(), false));

        assertEquals(0, bsfNotificationStrategy.send(arguments));
    }

    @Test
    public void canResolveEngineViaLangClass() throws IOException {
        // No usable extension; lang-class selects the engine by JSR-223 name
        File notifyTxt = tempFolder.newFile("notify.txt");
        FileUtils.write(notifyTxt, "results.put(\"status\", node.id == " + node1Id() + " ? \"OK\" : \"NOT_OK\");");

        List<Argument> arguments = new ArrayList<>();
        arguments.add(new Argument("file-name", null, notifyTxt.getAbsolutePath(), false));
        arguments.add(new Argument("lang-class", null, "beanshell", false));
        arguments.add(new Argument(NotificationManager.PARAM_NODE, null, node1Id(), false));

        assertEquals(0, bsfNotificationStrategy.send(arguments));
    }

    @Test
    public void nullVariablesStayDefinedInBeanshell() throws IOException {
        // no -nodeid argument: node, node_label, foreign_source etc. are null;
        // BSF defined them as null and scripts test them against null
        File notifyBsh = tempFolder.newFile("notify-null.bsh");
        FileUtils.write(notifyBsh, "results.put(\"status\", (node == null && foreign_source == null) ? \"OK\" : \"NOT_OK\");");

        List<Argument> arguments = new ArrayList<>();
        arguments.add(new Argument("file-name", null, notifyBsh.getAbsolutePath(), false));

        assertEquals(0, bsfNotificationStrategy.send(arguments));
    }

    @Test
    public void fileNameFromSubstitutionWorks() throws IOException {
        // notifd passes "" as the value for switches without a notification
        // parameter; the command's <substitution> must be honored then
        File notifyBsh = tempFolder.newFile("notify-subst.bsh");
        FileUtils.write(notifyBsh, "results.put(\"status\", node.id == " + node1Id() + " ? \"OK\" : \"NOT_OK\");");

        List<Argument> arguments = new ArrayList<>();
        arguments.add(new Argument("file-name", notifyBsh.getAbsolutePath(), "", false));
        arguments.add(new Argument(NotificationManager.PARAM_NODE, null, node1Id(), false));

        assertEquals(0, bsfNotificationStrategy.send(arguments));
    }

    @Test
    public void missingFileNameArgumentReturnsFailure() {
        List<Argument> arguments = new ArrayList<>();
        arguments.add(new Argument(NotificationManager.PARAM_NODE, null, node1Id(), false));

        assertEquals(-1, bsfNotificationStrategy.send(arguments));
        MockLogAppender.assertLogMatched(Level.WARN, "No 'file-name' argument supplied");
        MockLogAppender.resetState();
    }

    @Test
    public void editedGroovyScriptIsRecompiled() throws IOException {
        // exercises the compiled-script cache: reuse, then mtime invalidation
        File notifyGroovy = tempFolder.newFile("notify-cache.groovy");
        FileUtils.write(notifyGroovy, "results.put(\"status\", \"OK\")");

        List<Argument> arguments = new ArrayList<>();
        arguments.add(new Argument("file-name", null, notifyGroovy.getAbsolutePath(), false));

        assertEquals(0, bsfNotificationStrategy.send(arguments));
        // second run served from the cache
        assertEquals(0, bsfNotificationStrategy.send(arguments));

        FileUtils.write(notifyGroovy, "results.put(\"status\", \"NOT_OK\")");
        // mtime granularity can swallow quick successive writes; force it
        assertEquals(true, notifyGroovy.setLastModified(notifyGroovy.lastModified() + 2000));

        assertEquals(-1, bsfNotificationStrategy.send(arguments));
        MockLogAppender.assertLogMatched(Level.WARN, "did not indicate successful notification");
        MockLogAppender.resetState();
    }

    @Test
    public void missingScriptFileReturnsFailure() {
        List<Argument> arguments = new ArrayList<>();
        arguments.add(new Argument("file-name", null, new File(tempFolder.getRoot(), "no-such-script.bsh").getAbsolutePath(), false));

        assertEquals(-1, bsfNotificationStrategy.send(arguments));
        MockLogAppender.assertLogMatched(Level.WARN, "Cannot locate or read script file");
        MockLogAppender.resetState();
    }

    @Test
    public void nonOkStatusReturnsFailure() throws IOException {
        File notifyBsh = tempFolder.newFile("notify-nok.bsh");
        FileUtils.write(notifyBsh, "results.put(\"status\", \"NOT_OK\");");

        List<Argument> arguments = new ArrayList<>();
        arguments.add(new Argument("file-name", null, notifyBsh.getAbsolutePath(), false));

        assertEquals(-1, bsfNotificationStrategy.send(arguments));
        MockLogAppender.assertLogMatched(Level.WARN, "did not indicate successful notification");
        MockLogAppender.resetState();
    }

    @Test
    public void invalidRunTypeReturnsFailure() throws IOException {
        File notifyBsh = tempFolder.newFile("notify-bogus.bsh");
        FileUtils.write(notifyBsh, "results.put(\"status\", \"OK\");");

        List<Argument> arguments = new ArrayList<>();
        arguments.add(new Argument("file-name", null, notifyBsh.getAbsolutePath(), false));
        arguments.add(new Argument("run-type", null, "bogus", false));

        assertEquals(-1, bsfNotificationStrategy.send(arguments));
        MockLogAppender.assertLogMatched(Level.WARN, "Invalid run-type parameter value");
        MockLogAppender.resetState();
    }

    @Test
    public void deprecatedBsfEngineSwitchWarnsButStillWorks() throws IOException {
        File notifyBsh = tempFolder.newFile("notify-deprecated.bsh");
        FileUtils.write(notifyBsh, "results.put(\"status\", node.id == " + node1Id() + " ? \"OK\" : \"NOT_OK\");");

        List<Argument> arguments = new ArrayList<>();
        arguments.add(new Argument("file-name", null, notifyBsh.getAbsolutePath(), false));
        arguments.add(new Argument("lang-class", null, "beanshell", false));
        arguments.add(new Argument("bsf-engine", null, "bsh.util.BeanShellBSFEngine", false));
        arguments.add(new Argument("file-extensions", null, "bsh", false));
        arguments.add(new Argument(NotificationManager.PARAM_NODE, null, node1Id(), false));

        assertEquals(0, bsfNotificationStrategy.send(arguments));
        MockLogAppender.assertLogMatched(Level.WARN, "'bsf-engine' and 'file-extensions' switches are no longer supported");
        MockLogAppender.resetState();
    }
}
