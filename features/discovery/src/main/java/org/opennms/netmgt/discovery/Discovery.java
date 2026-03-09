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
package org.opennms.netmgt.discovery;

import java.io.IOException;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import org.opennms.netmgt.config.DiscoveryConfigFactory;
import org.opennms.netmgt.daemon.AbstractServiceDaemon;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.model.events.EventBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * This class is the main interface to the OpenNMS discovery service. The service 
 * delays the reading of configuration information until the service is started.
 *
 * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
 * @author <a href="http://www.opennms.org/">OpenNMS.org </a>
 */
public class Discovery extends AbstractServiceDaemon {

    private static final Logger LOG = LoggerFactory.getLogger(Discovery.class);

    protected static final String DAEMON_NAME = "Discovery";

    protected static final String LOG4J_CATEGORY = "discovery";

    @Autowired
    private DiscoveryConfigFactory m_discoveryFactory;

    @Autowired
    private DiscoveryTaskExecutor m_discoveryTaskExecutor;

    @Autowired
    @Qualifier("eventIpcManager")
    private EventForwarder m_eventForwarder;

    private Timer discoveryTimer;

    /**
     * Constructs a new discovery instance.
     */
    public Discovery() {
        super(LOG4J_CATEGORY);
    }

    /**
     * <p>onInit</p>
     *
     * @throws java.lang.IllegalStateException if any.
     */
    @Override
    protected void onInit() throws IllegalStateException {
        Objects.requireNonNull(m_eventForwarder, "must set the eventForwarder property");
        Objects.requireNonNull(m_discoveryTaskExecutor, "must set the discoveryTaskExecutor property");
        Objects.requireNonNull(m_discoveryFactory, "must set the discoveryFactory property");

        try {
        	LOG.debug("Initializing configuration...");
        	m_discoveryFactory.reload();
        } catch (Throwable e) {
            LOG.debug("onInit: initialization failed", e);
            throw new IllegalStateException("Could not initialize discovery configuration.", e);
        }

    }

    /**
     * <p>onStart</p>
     */
    @Override
    protected synchronized void onStart() {
        if (discoveryTimer != null) {
            LOG.warn("Discovery is already started.");
            return;
        }

        discoveryTimer = new Timer(DAEMON_NAME);
        discoveryTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                LOG.info("Discovery triggered by timer.");
                try {
                    m_discoveryTaskExecutor.handleDiscoveryTask(m_discoveryFactory.getConfiguration()).whenComplete((result, ex) -> {
                        LOG.info("Discovery completed succesfully.");
                    }).join();
                } catch (Throwable t) {
                    LOG.error("Discovery failed. Will try again in {} ms", m_discoveryFactory.getRestartSleepTime(), t);
                }
            }
        }, m_discoveryFactory.getInitialSleepTime(), m_discoveryFactory.getRestartSleepTime());
    }

    /**
     * <p>onStop</p>
     */
    @Override
    protected synchronized void onStop() {
        if (discoveryTimer == null) {
            LOG.warn("Discovery is already stopped.");
            return;
        }

        discoveryTimer.cancel();
        discoveryTimer = null;
    }

    /**
     * <p>onPause</p>
     */
    @Override
    protected void onPause() {
        onStop();
    }

    /**
     * <p>onResume</p>
     */
    @Override
    protected void onResume() {
        onStart();
    }

    private void reloadAndReStart() {
        EventBuilder ebldr = null;
        try {
            m_discoveryFactory.reload();
            ebldr = new EventBuilder(EventConstants.RELOAD_DAEMON_CONFIG_SUCCESSFUL_UEI, DAEMON_NAME);
            ebldr.addParam(EventConstants.PARM_DAEMON_NAME, DAEMON_NAME);
            this.stop();
            this.start();
        } catch (IOException e) {
            LOG.error("Unable to initialize the discovery configuration factory", e);
            ebldr = new EventBuilder(EventConstants.RELOAD_DAEMON_CONFIG_FAILED_UEI, DAEMON_NAME);
            ebldr.addParam(EventConstants.PARM_DAEMON_NAME, DAEMON_NAME);
            ebldr.addParam(EventConstants.PARM_REASON, e.getLocalizedMessage().substring(0, 128));
        }
        // Outgoing success/failure events still go through EventForwarder for full
        // pipeline processing (TSID assignment, routing, local broadcast to non-migrated daemons)
        m_eventForwarder.sendNow(ebldr.getEvent());
    }

    public static String getLoggingCategory() {
        return LOG4J_CATEGORY;
    }

    public void setEventForwarder(EventForwarder eventForwarder) {
        m_eventForwarder = eventForwarder;
    }

    public void setDiscoveryFactory(DiscoveryConfigFactory discoveryFactory) {
        m_discoveryFactory = discoveryFactory;
    }

    public void setDiscoveryTaskExecutor(DiscoveryTaskExecutor discoveryTaskExecutor) {
        m_discoveryTaskExecutor = discoveryTaskExecutor;
    }
}
