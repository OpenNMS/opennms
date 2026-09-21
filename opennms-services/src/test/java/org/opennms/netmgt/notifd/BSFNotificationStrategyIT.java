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
     * Verifies that we can invoke a Groovy script and that
     * the an instance of the appropriate OnmsNode object is
     * passed to the script.
     */
    @Test
    public void canUseNodeInScript() throws IOException {
        // Create a simple Groovy script that verifies the node bean
        File notifyScript = tempFolder.newFile("notify.groovy");
        FileUtils.write(notifyScript, "results.put(\"status\", node.id == " + node1Id() + " ? \"OK\" : \"NOT_OK\")");

        List<Argument> arguments = new ArrayList<>();
        // Point to our script
        arguments.add(new Argument("file-name", null, notifyScript.getAbsolutePath(), false));
        // Reference node 1
        arguments.add(new Argument(NotificationManager.PARAM_NODE, null, node1Id(), false));

        // Should succeed
        assertEquals(0, bsfNotificationStrategy.send(arguments));
    }

    @Test
    public void canEvalScript() throws IOException {
        // Under run-type=eval the script's last expression becomes the status
        File notifyScript = tempFolder.newFile("notify-eval.groovy");
        FileUtils.write(notifyScript, "node.id == " + node1Id() + " ? \"OK\" : \"NOT_OK\"");

        List<Argument> arguments = new ArrayList<>();
        arguments.add(new Argument("file-name", null, notifyScript.getAbsolutePath(), false));
        arguments.add(new Argument("run-type", null, "eval", false));
        arguments.add(new Argument(NotificationManager.PARAM_NODE, null, node1Id(), false));

        assertEquals(0, bsfNotificationStrategy.send(arguments));
    }

    @Test
    public void beanshellScriptIsNoLongerSupported() throws IOException {
        // BeanShell was removed along with BSF; a .bsh script must now fail
        // cleanly with a diagnosable message rather than silently doing nothing
        File notifyBsh = tempFolder.newFile("notify-legacy.bsh");
        FileUtils.write(notifyBsh, "results.put(\"status\", \"OK\");");

        List<Argument> arguments = new ArrayList<>();
        arguments.add(new Argument("file-name", null, notifyBsh.getAbsolutePath(), false));

        assertEquals(-1, bsfNotificationStrategy.send(arguments));
        MockLogAppender.assertLogMatched(Level.ERROR, "No JSR-223 script engine found");
        MockLogAppender.resetState();
    }

    @Test
    public void beanshellLangClassIsNoLongerSupported() throws IOException {
        // likewise for an explicit lang-class left over from a BSF-era config
        File notifyScript = tempFolder.newFile("notify-legacy-lang.groovy");
        FileUtils.write(notifyScript, "results.put(\"status\", \"OK\")");

        List<Argument> arguments = new ArrayList<>();
        arguments.add(new Argument("file-name", null, notifyScript.getAbsolutePath(), false));
        arguments.add(new Argument("lang-class", null, "beanshell", false));

        assertEquals(-1, bsfNotificationStrategy.send(arguments));
        MockLogAppender.assertLogMatched(Level.ERROR, "No JSR-223 script engine found");
        MockLogAppender.resetState();
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
        FileUtils.write(notifyTxt, "results.put(\"status\", node.id == " + node1Id() + " ? \"OK\" : \"NOT_OK\")");

        List<Argument> arguments = new ArrayList<>();
        arguments.add(new Argument("file-name", null, notifyTxt.getAbsolutePath(), false));
        arguments.add(new Argument("lang-class", null, "groovy", false));
        arguments.add(new Argument(NotificationManager.PARAM_NODE, null, node1Id(), false));

        assertEquals(0, bsfNotificationStrategy.send(arguments));
    }

    @Test
    public void nullVariablesStayDefined() throws IOException {
        // no -nodeid argument: node, node_label, foreign_source etc. are null.
        // They must still be *defined* so scripts can test them against null,
        // rather than failing with a missing-property error.
        File notifyScript = tempFolder.newFile("notify-null.groovy");
        FileUtils.write(notifyScript, "results.put(\"status\", (node == null && foreign_source == null) ? \"OK\" : \"NOT_OK\")");

        List<Argument> arguments = new ArrayList<>();
        arguments.add(new Argument("file-name", null, notifyScript.getAbsolutePath(), false));

        assertEquals(0, bsfNotificationStrategy.send(arguments));
    }

    @Test
    public void fileNameFromSubstitutionWorks() throws IOException {
        // notifd passes "" as the value for switches without a notification
        // parameter; the command's <substitution> must be honored then
        File notifyScript = tempFolder.newFile("notify-subst.groovy");
        FileUtils.write(notifyScript, "results.put(\"status\", node.id == " + node1Id() + " ? \"OK\" : \"NOT_OK\")");

        List<Argument> arguments = new ArrayList<>();
        arguments.add(new Argument("file-name", notifyScript.getAbsolutePath(), "", false));
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
        arguments.add(new Argument("file-name", null, new File(tempFolder.getRoot(), "no-such-script.groovy").getAbsolutePath(), false));

        assertEquals(-1, bsfNotificationStrategy.send(arguments));
        MockLogAppender.assertLogMatched(Level.WARN, "Cannot locate or read script file");
        MockLogAppender.resetState();
    }

    @Test
    public void nonOkStatusReturnsFailure() throws IOException {
        File notifyScript = tempFolder.newFile("notify-nok.groovy");
        FileUtils.write(notifyScript, "results.put(\"status\", \"NOT_OK\")");

        List<Argument> arguments = new ArrayList<>();
        arguments.add(new Argument("file-name", null, notifyScript.getAbsolutePath(), false));

        assertEquals(-1, bsfNotificationStrategy.send(arguments));
        MockLogAppender.assertLogMatched(Level.WARN, "did not indicate successful notification");
        MockLogAppender.resetState();
    }

    @Test
    public void invalidRunTypeReturnsFailure() throws IOException {
        File notifyScript = tempFolder.newFile("notify-bogus.groovy");
        FileUtils.write(notifyScript, "results.put(\"status\", \"OK\")");

        List<Argument> arguments = new ArrayList<>();
        arguments.add(new Argument("file-name", null, notifyScript.getAbsolutePath(), false));
        arguments.add(new Argument("run-type", null, "bogus", false));

        assertEquals(-1, bsfNotificationStrategy.send(arguments));
        MockLogAppender.assertLogMatched(Level.WARN, "Invalid run-type parameter value");
        MockLogAppender.resetState();
    }

    @Test
    public void deprecatedBsfEngineSwitchWarnsButStillWorks() throws IOException {
        File notifyScript = tempFolder.newFile("notify-deprecated.groovy");
        FileUtils.write(notifyScript, "results.put(\"status\", node.id == " + node1Id() + " ? \"OK\" : \"NOT_OK\")");

        List<Argument> arguments = new ArrayList<>();
        arguments.add(new Argument("file-name", null, notifyScript.getAbsolutePath(), false));
        arguments.add(new Argument("lang-class", null, "groovy", false));
        // a genuine BSF-era value: it must be ignored, not acted on
        arguments.add(new Argument("bsf-engine", null, "bsh.util.BeanShellBSFEngine", false));
        arguments.add(new Argument("file-extensions", null, "bsh", false));
        arguments.add(new Argument(NotificationManager.PARAM_NODE, null, node1Id(), false));

        assertEquals(0, bsfNotificationStrategy.send(arguments));
        MockLogAppender.assertLogMatched(Level.WARN, "'bsf-engine' and 'file-extensions' switches are no longer supported");
        MockLogAppender.resetState();
    }
}
