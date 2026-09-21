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
package org.opennms.container.daemon;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.List;

import org.apache.karaf.main.Main;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.soa.support.OnmsOSGiBridgeActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.launch.Framework;

public class KarafDaemonTest {
    private static final List<String> KARAF_PROPERTIES = List.of(
            "karaf.home", "karaf.base", "karaf.data", "karaf.log", "karaf.etc", "karaf.history",
            "karaf.instances", "karaf.startLocalConsole", "karaf.startRemoteShell", "karaf.lock");

    private final Main m_main = mock(Main.class);
    private final Framework m_framework = mock(Framework.class);
    private final BundleContext m_bundleContext = mock(BundleContext.class);
    private final OnmsOSGiBridgeActivator m_bridge = mock(OnmsOSGiBridgeActivator.class);
    private KarafDaemon m_daemon;

    @Before
    public void setUp() {
        System.setProperty("opennms.home", "/tmp/opennms-karaf-daemon-test");
        when(m_main.getFramework()).thenReturn(m_framework);
        when(m_framework.getBundleContext()).thenReturn(m_bundleContext);
        m_daemon = new KarafDaemon() {
            @Override
            protected Main createMain() {
                return m_main;
            }

            @Override
            protected OnmsOSGiBridgeActivator createBridge() {
                return m_bridge;
            }
        };
    }

    @After
    public void tearDown() {
        KarafContext.clear(m_bundleContext);
        System.clearProperty("opennms.home");
        KARAF_PROPERTIES.forEach(System::clearProperty);
    }

    @Test
    public void initializesKarafPropertiesFromOpenNmsHome() {
        m_daemon.init();

        final String root = System.getProperty("opennms.home");
        assertEquals(root, System.getProperty("karaf.home"));
        assertEquals(root, System.getProperty("karaf.base"));
        assertEquals(root + File.separator + "data", System.getProperty("karaf.data"));
        assertEquals(root + File.separator + "logs", System.getProperty("karaf.log"));
        assertEquals(root + File.separator + "etc", System.getProperty("karaf.etc"));
        assertEquals("false", System.getProperty("karaf.startLocalConsole"));
        assertEquals("true", System.getProperty("karaf.startRemoteShell"));
        assertEquals("false", System.getProperty("karaf.lock"));
    }

    @Test
    public void publishesContextAndKeepsLifecycleOutsideConsumers() throws Exception {
        m_daemon.init();
        m_daemon.start();

        verify(m_main).launch();
        verify(m_bridge).start(m_bundleContext);
        assertSame(m_bundleContext, KarafContext.getBundleContext());

        m_daemon.stop();

        verify(m_bridge).stop(m_bundleContext);
        verify(m_main).destroy();
        assertThrows(IllegalStateException.class, KarafContext::getBundleContext);
    }

    @Test
    public void rollsBackWhenBridgeStartupFails() throws Exception {
        org.mockito.Mockito.doThrow(new IllegalStateException("bridge failed"))
                .when(m_bridge).start(m_bundleContext);

        m_daemon.init();
        assertThrows(IllegalStateException.class, m_daemon::start);

        verify(m_bridge).stop(m_bundleContext);
        verify(m_main).destroy();
        assertThrows(IllegalStateException.class, KarafContext::getBundleContext);
    }
}
