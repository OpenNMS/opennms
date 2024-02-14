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
package org.opennms.netmgt.provision.persist.policies;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.mockito.Mockito;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.mock.MockSessionUtils;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.PrimaryType;
import org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation;
import org.opennms.netmgt.provision.persist.JSR223ScriptCache;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ScriptPolicyTest {

    private final JSR223ScriptCache scriptCache = new JSR223ScriptCache();

    @BeforeClass
    public static void beforeClass() {
        System.setProperty("opennms.home", System.getProperty("user.dir"));
    }

    @Test(expected = IOException.class)
    public void testScriptOutsideScriptPath() throws Exception {
        final ScriptPolicy p = new ScriptPolicy(Paths.get("/", "opt", "opennms").toAbsolutePath());
        p.compileScript("../policy.groovy");
    }


    @Test
    public void testScriptPolicy() throws Exception {
        // create one node
        OnmsNode node1 = new OnmsNode();
        node1.setNodeId("1");
        node1.setLocation(new OnmsMonitoringLocation("Default", ""));

        final OnmsIpInterface iface1_1 = new OnmsIpInterface();
        iface1_1.setIpAddress(InetAddress.getByName("172.16.0.1"));
        iface1_1.setIsSnmpPrimary(PrimaryType.PRIMARY);

        final OnmsIpInterface iface1_2 = new OnmsIpInterface();
        iface1_2.setIpAddress(InetAddress.getByName("172.17.0.1"));
        iface1_2.setIsSnmpPrimary(PrimaryType.SECONDARY);

        final OnmsIpInterface iface1_3 = new OnmsIpInterface();
        iface1_3.setIpAddress(InetAddress.getByName("192.168.0.10"));
        iface1_3.setIsSnmpPrimary(PrimaryType.NOT_ELIGIBLE);

        node1.addIpInterface(iface1_1);
        node1.addIpInterface(iface1_2);
        node1.addIpInterface(iface1_3);

        node1.setForeignSource("nodes");
        node1.setForeignId("1");
        node1.setLabel("Node-1-Foo");

        // create another one
        OnmsNode node2 = new OnmsNode();
        node2.setNodeId("2");
        node2.setLocation(new OnmsMonitoringLocation("Default", ""));

        final OnmsIpInterface iface2_1 = new OnmsIpInterface();
        iface2_1.setIpAddress(InetAddress.getByName("172.16.0.1"));
        iface2_1.setIsSnmpPrimary(PrimaryType.PRIMARY);

        final OnmsIpInterface iface2_2 = new OnmsIpInterface();
        iface2_2.setIpAddress(InetAddress.getByName("172.17.0.1"));
        iface2_2.setIsSnmpPrimary(PrimaryType.SECONDARY);

        final OnmsIpInterface iface2_3 = new OnmsIpInterface();
        iface2_3.setIpAddress(InetAddress.getByName("192.168.0.20"));
        iface2_3.setIsSnmpPrimary(PrimaryType.NOT_ELIGIBLE);

        node2.addIpInterface(iface2_1);
        node2.addIpInterface(iface2_2);
        node2.addIpInterface(iface2_3);

        node2.setForeignSource("nodes");
        node2.setForeignId("2");
        node2.setLabel("Node-2-Bar");

        // create policy that matches only the first node
        final ScriptPolicy p = new ScriptPolicy(Paths.get("src", "test", "resources").toAbsolutePath());
        p.setLabel("~.*Foo$");
        p.setMatchBehavior("ALL_PARAMETERS");
        p.setScript("policy.groovy");
        NodeDao mockNodeDao = Mockito.mock(NodeDao.class);
        when(mockNodeDao.get(Mockito.eq(1))).thenReturn(node1);
        when(mockNodeDao.get(Mockito.eq(2))).thenReturn(node2);
        p.setNodeDao(mockNodeDao);
        p.setSessionUtils(new MockSessionUtils());
        p.setScriptCache(scriptCache);

        node1 = p.apply(node1, Collections.emptyMap());

        // check that the data of the first node is altered
        assertEquals(PrimaryType.NOT_ELIGIBLE, node1.getIpInterfaceByIpAddress("172.16.0.1").getIsSnmpPrimary());
        assertEquals(PrimaryType.NOT_ELIGIBLE, node1.getIpInterfaceByIpAddress("172.17.0.1").getIsSnmpPrimary());
        assertEquals(PrimaryType.PRIMARY, node1.getIpInterfaceByIpAddress("192.168.0.10").getIsSnmpPrimary());
        assertEquals("custom-location", node1.getLocation().getLocationName());

        node2 = p.apply(node2, Collections.emptyMap());

        // check that the second node is not modified
        assertEquals(PrimaryType.PRIMARY, node2.getIpInterfaceByIpAddress("172.16.0.1").getIsSnmpPrimary());
        assertEquals(PrimaryType.SECONDARY, node2.getIpInterfaceByIpAddress("172.17.0.1").getIsSnmpPrimary());
        assertEquals(PrimaryType.NOT_ELIGIBLE, node2.getIpInterfaceByIpAddress("192.168.0.20").getIsSnmpPrimary());
        assertEquals("Default", node2.getLocation().getLocationName());
    }

    @Test
    public void testScriptCompilation() throws Exception {
        // create one node
        OnmsNode node1 = new OnmsNode();
        node1.setNodeId("1");
        node1.setLocation(new OnmsMonitoringLocation("Default", ""));

        final OnmsIpInterface iface1_1 = new OnmsIpInterface();
        iface1_1.setIpAddress(InetAddress.getByName("172.16.0.1"));
        iface1_1.setIsSnmpPrimary(PrimaryType.PRIMARY);

        final OnmsIpInterface iface1_2 = new OnmsIpInterface();
        iface1_2.setIpAddress(InetAddress.getByName("172.17.0.1"));
        iface1_2.setIsSnmpPrimary(PrimaryType.SECONDARY);

        final OnmsIpInterface iface1_3 = new OnmsIpInterface();
        iface1_3.setIpAddress(InetAddress.getByName("192.168.0.10"));
        iface1_3.setIsSnmpPrimary(PrimaryType.NOT_ELIGIBLE);

        node1.addIpInterface(iface1_1);
        node1.addIpInterface(iface1_2);
        node1.addIpInterface(iface1_3);

        node1.setForeignSource("nodes");
        node1.setForeignId("1");
        node1.setLabel("Node-1-Foo");

        // create temporary file...
        final File scriptFile = File.createTempFile("foobar", ".groovy");

        // ...and attach it to the ScriptPolicy
        final ScriptPolicy p = new ScriptPolicy(scriptFile.toPath().getParent());
        p.setLabel("~.*");
        p.setMatchBehavior("ALL_PARAMETERS");
        p.setScript(scriptFile.getAbsolutePath());
        NodeDao mockNodeDao = Mockito.mock(NodeDao.class);
        when(mockNodeDao.get(Mockito.eq(1))).thenReturn(node1);
        p.setNodeDao(mockNodeDao);
        p.setSessionUtils(new MockSessionUtils());
        p.setScriptCache(new JSR223ScriptCache());

        // create script file's content and modify lastModified
        createScriptFile(scriptFile, 1, false);

        // first run, it should compile and results in a "Test #1" node label
        node1 = p.apply(node1, Collections.emptyMap());
        assertEquals("Test #1", node1.getLabel());
        Thread.sleep(500);

        // update script file's content and leave lastModified unchanged
        createScriptFile(scriptFile, 2, true);

        // second run with an unmodified lastModified, it should not compile and should still results in a "Test #1" node label
        node1 = p.apply(node1, Collections.emptyMap());
        assertEquals("Test #1", node1.getLabel());
        Thread.sleep(500);

        // update script file's content and modify lastModified
        createScriptFile(scriptFile, 3, false);

        // third run with a modified lastModified, it should compile and result in a "Test #3" node label
        node1 = p.apply(node1, Collections.emptyMap());
        assertEquals("Test #3", node1.getLabel());
    }

    private void createScriptFile(final File file, final int run, final boolean preserveLastModified) throws IOException {
        final long lastModified = file.lastModified();
        try (final PrintWriter printWriter = new PrintWriter(new FileWriter(file))) {
            printWriter.println("node.setLabel(\"Test #" + run + "\")");
            printWriter.println("return node");
        }
        if (preserveLastModified) {
            file.setLastModified(lastModified);
        }
    }

    /**
     * Used to reproduce NMS-15798.
     *
     * Java 11 running with w/ cmdline: -ea -Xms64m -Xmx64m -XX:MaxMetaspaceSize=32M -Dgroovy.use.classvalue=true
     * JVM will eventually OOM. Heap/metaspace is filled w/ Groovy related classloader objects.
     * @throws Exception
     */
    @Test
    @Ignore("local debugging only - don't run in CI")
    public void testGroovyMemoryLeak() throws Exception {
        while (true) {
            List<Thread> threads = new LinkedList<>();
            for (int i = 0; i < 10; i++) {
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try  {
                            testScriptPolicy();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                threads.add(t);
            }
            for (Thread t : threads) {
                t.run();
            }
            for (Thread t : threads) {
                t.join();
            }
            System.gc();
            System.out.println("Heap size: " + Runtime.getRuntime().totalMemory());
        }
    }
}
