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

import java.io.File;
import java.util.Objects;

import org.apache.karaf.main.Main;
import org.opennms.core.soa.support.OnmsOSGiBridgeActivator;
import org.opennms.netmgt.daemon.AbstractServiceDaemon;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Owns the lifecycle of the embedded Karaf framework. */
public class KarafDaemon extends AbstractServiceDaemon {
    private static final Logger LOG = LoggerFactory.getLogger(KarafDaemon.class);
    private static final String LOGGING_CATEGORY = "karaf";

    private Main m_main;
    private BundleContext m_bundleContext;
    private OnmsOSGiBridgeActivator m_bridge;

    public KarafDaemon() {
        super(LOGGING_CATEGORY);
    }

    @Override
    protected void onInit() {
        final String root = Objects.requireNonNull(System.getProperty("opennms.home"),
                "The opennms.home system property must be set before starting Karaf");

        System.setProperty("karaf.home", root);
        System.setProperty("karaf.base", root);
        System.setProperty("karaf.data", root + File.separator + "data");
        System.setProperty("karaf.log", root + File.separator + "logs");
        System.setProperty("karaf.etc", root + File.separator + "etc");
        System.setProperty("karaf.history", root + File.separator + "data" + File.separator + "history.txt");
        System.setProperty("karaf.instances", root + File.separator + "instances");
        System.setProperty("karaf.startLocalConsole", "false");
        System.setProperty("karaf.startRemoteShell", "true");
        System.setProperty("karaf.lock", "false");
    }

    @Override
    protected synchronized void onStart() {
        if (m_main != null) {
            throw new IllegalStateException("Karaf is already running");
        }

        try {
            m_main = createMain();
            m_main.launch();
            m_bundleContext = Objects.requireNonNull(m_main.getFramework().getBundleContext(),
                    "Karaf did not provide a bundle context");
            KarafContext.publish(m_bundleContext);

            m_bridge = createBridge();
            m_bridge.start(m_bundleContext);
        } catch (final Throwable t) {
            stopContainer();
            throw new IllegalStateException("Unable to start the embedded Karaf container", t);
        }
    }

    @Override
    protected synchronized void onStop() {
        stopContainer();
    }

    protected Main createMain() {
        return new Main(new String[0]);
    }

    protected OnmsOSGiBridgeActivator createBridge() {
        return new OnmsOSGiBridgeActivator();
    }

    private void stopContainer() {
        if (m_bridge != null && m_bundleContext != null) {
            try {
                m_bridge.stop(m_bundleContext);
            } catch (final Throwable t) {
                LOG.warn("An error occurred while stopping the OpenNMS OSGi service bridge", t);
            } finally {
                m_bridge = null;
            }
        }

        if (m_bundleContext != null) {
            KarafContext.clear(m_bundleContext);
            m_bundleContext = null;
        }

        if (m_main != null) {
            try {
                m_main.destroy();
            } catch (final Throwable t) {
                LOG.warn("An error occurred while stopping the embedded Karaf container", t);
            } finally {
                m_main = null;
            }
        }
    }
}
