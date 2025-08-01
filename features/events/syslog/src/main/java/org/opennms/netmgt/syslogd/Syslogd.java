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
package org.opennms.netmgt.syslogd;

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;

import org.opennms.netmgt.daemon.AbstractServiceDaemon;
import org.opennms.netmgt.daemon.DaemonTools;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.annotations.EventHandler;
import org.opennms.netmgt.events.api.annotations.EventListener;
import org.opennms.netmgt.events.api.model.IEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * <p>
 * The received messages are converted into XML and sent to eventd.
 * </p>
 * <p>
 * <strong>Note: </strong>Syslogd is a PausableFiber so as to receive control
 * events. However, a 'pause' on Syslogd has no impact on the receiving and
 * processing of syslog messages.
 * </p>
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @author <a href="mailto:joed@opennms.org">Johan Edstrom</a>
 * @author <a href="mailto:mhuot@opennms.org">Mike Huot</a>
 */
@EventListener(name=Syslogd.LOG4J_CATEGORY, logPrefix=Syslogd.LOG4J_CATEGORY)
public class Syslogd extends AbstractServiceDaemon {

    private static final Logger LOG = LoggerFactory.getLogger(Syslogd.class);

    /**
     * The name of the logging category for Syslogd.
     */
    public static final String LOG4J_CATEGORY = "syslogd";

    @Autowired
    private SyslogReceiver m_udpEventReceiver;

    /**
     * <p>Constructor for Syslogd.</p>
     */
    public Syslogd() {
        super(LOG4J_CATEGORY);
    }

    public SyslogReceiver getSyslogReceiver() {
        return m_udpEventReceiver;
    }

    public void setSyslogReceiver(SyslogReceiver receiver) {
        m_udpEventReceiver = receiver;
    }

    /**
     * <p>onInit</p>
     */
    @Override
    protected void onInit() {
        // Nothing to do
    }

    /**
     * <p>onStart</p>
     */
    @Override
    protected void onStart() {
        LOG.debug("Starting SyslogHandler");
        Thread rThread = new Thread(m_udpEventReceiver, m_udpEventReceiver.getName());

        try {
            rThread.start();
        } catch (RuntimeException e) {
            rThread.interrupt();
            throw e;
        }
    }

    /**
     * <p>onStop</p>
     */
    @Override
    protected void onStop() {
        if (m_udpEventReceiver != null) {
            LOG.debug("stop: Stopping the Syslogd UDP receiver");
            try {
                m_udpEventReceiver.stop();
            } catch (InterruptedException e) {
                LOG.info("stop: Exception when stopping the Syslog UDP receiver: " + e.getMessage());
            } catch (Throwable e) {
                LOG.error("stop: Failed to stop the Syslog UDP receiver", e);
                throw new UndeclaredThrowableException(e);
            }
            LOG.debug("stop: Stopped the Syslogd UDP receiver");
        }
    }

    private void handleConfigurationChanged() {
        stop();
        try {
            m_udpEventReceiver.reload();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        start();
    }

    @EventHandler(uei = EventConstants.RELOAD_DAEMON_CONFIG_UEI)
    public void handleReloadEvent(IEvent e) {
        DaemonTools.handleReloadEvent(e, Syslogd.LOG4J_CATEGORY, (event) -> handleConfigurationChanged());
    }
}
