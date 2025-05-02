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
package org.opennms.netmgt.trapd;

import java.io.IOException;
import java.util.stream.Collectors;

import org.opennms.core.ipc.twin.api.TwinPublisher;
import org.opennms.core.mate.api.Interpolator;
import org.opennms.core.mate.api.Scope;
import org.opennms.core.mate.api.SecureCredentialsVaultScope;
import org.opennms.core.spring.BeanUtils;
import org.opennms.features.scv.api.SecureCredentialsVault;
import org.opennms.netmgt.config.TrapdConfig;
import org.opennms.netmgt.config.TrapdConfigFactory;
import org.opennms.netmgt.config.trapd.Snmpv3User;
import org.opennms.netmgt.daemon.AbstractServiceDaemon;
import org.opennms.netmgt.daemon.DaemonTools;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.annotations.EventHandler;
import org.opennms.netmgt.events.api.annotations.EventListener;
import org.opennms.netmgt.events.api.model.IEvent;
import org.opennms.netmgt.snmp.SnmpV3User;
import org.opennms.netmgt.snmp.TrapListenerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * <p>
 * The Trapd listens for SNMP traps on the standard port(162). Creates a
 * SnmpTrapSession and implements the SnmpTrapHandler to get callbacks when
 * traps are received.
 * </p>
 *
 * <p>
 * The received traps are converted into XML and sent to eventd.
 * </p>
 *
 * <p>
 * <strong>Note: </strong>Trapd is a PausableFiber so as to receive control
 * events. However, a 'pause' on Trapd has no impact on the receiving and
 * processing of traps.
 * </p>
 *
 * @author <A HREF="mailto:weave@oculan.com">Brian Weaver </A>
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya Nataraj </A>
 * @author <A HREF="mailto:larry@opennms.org">Lawrence Karnowski </A>
 * @author <A HREF="mailto:mike@opennms.org">Mike Davidson </A>
 * @author <A HREF="mailto:tarus@opennms.org">Tarus Balog </A>
 * @author <A HREF="http://www.opennms.org">OpenNMS.org </A>
 */
@EventListener(name=Trapd.LOG4J_CATEGORY, logPrefix=Trapd.LOG4J_CATEGORY)
public class Trapd extends AbstractServiceDaemon {
    
    private static final Logger LOG = LoggerFactory.getLogger(Trapd.class);


    public static final String LOG4J_CATEGORY = "trapd";
    
    /**
     * The last status sent to the service control manager.
     */
    private int m_status = START_PENDING;

    /**
     * The class instance used to receive new events from for the system.
     */
    @Autowired
    private TrapListener m_trapListener;

    @Autowired
    private SecureCredentialsVault secureCredentialsVault;

    private TrapdConfig m_config;

    @Autowired
    private TwinPublisher m_twinPublisher;

    private TwinPublisher.Session<TrapListenerConfig> m_twinSession;

    /**
     * <P>
     * Constructs a new Trapd object that receives and forwards trap messages
     * via JSDT. The session is initialized with the default client name of <EM>
     * OpenNMS.trapd</EM>. The trap session is started on the default port, as
     * defined by the SNMP library.
     * </P>
     *
     * @see org.opennms.protocols.snmp.SnmpTrapSession
     */
    public Trapd() {
        super(LOG4J_CATEGORY);

        m_config = TrapdConfigFactory.getInstance();
    }

    public void setSecureCredentialsVault(final SecureCredentialsVault secureCredentialsVault) {
        this.secureCredentialsVault = secureCredentialsVault;
    }

    /**
     * <p>onInit</p>
     */
    @Override
    protected synchronized void onInit() {
        BeanUtils.assertAutowiring(this);
    }

    /**
     * Create the SNMP trap session and create the communication channel
     * to communicate with eventd.
     *
     * @exception java.lang.reflect.UndeclaredThrowableException
     *                if an unexpected database, or IO exception occurs.
     * @see org.opennms.protocols.snmp.SnmpTrapSession
     * @see org.opennms.protocols.snmp.SnmpTrapHandler
     */
    @Override
    protected synchronized void onStart() {
        m_status = STARTING;

        LOG.debug("start: Initializing the Trapd receiver");
        // Register session with Publisher for once.
        try {
            m_twinSession = m_twinPublisher.register(TrapListenerConfig.TWIN_KEY, TrapListenerConfig.class, null);
        } catch (IOException e) {
            LOG.error("Failed to register twin for trap listener config", e);
            throw new RuntimeException(e);
        }
        // Publish existing config.
        try {
            m_twinSession.publish(from(m_config));
        } catch (IOException e) {
            LOG.error("Failed to register twin for trap listener config", e);
            throw new RuntimeException(e);
        }

        m_trapListener.start();

        m_status = RUNNING;

        LOG.debug("start: Trapd is ready to receive traps");
    }

    /**
     * Pauses Trapd
     */
    @Override
    protected void onPause() {
        if (m_status != RUNNING) {
            return;
        }

        m_status = PAUSE_PENDING;

        LOG.debug("pause: Calling pause on trap receiver");
        m_trapListener.stop();

        m_status = PAUSED;

        LOG.debug("pause: Trapd paused");
    }

    /**
     * Resumes Trapd
     */
    @Override
    protected void onResume() {
        if (m_status != PAUSED) {
            return;
        }

        m_status = RESUME_PENDING;

        LOG.debug("resume: Calling resume on trap receiver");
        m_trapListener.start();

        m_status = RUNNING;

        LOG.debug("resume: Trapd resumed");
    }

    /**
     * Stops the currently running service. If the service is not running then
     * the command is silently discarded.
     */
    @Override
    protected synchronized void onStop() {
        m_status = STOP_PENDING;

        // shutdown and wait on the background processing thread to exit.
        LOG.debug("stop: Closing communication paths");

        if (m_trapListener != null) {
            m_trapListener.stop();
        }

        m_status = STOPPED;

        LOG.debug("stop: Trapd stopped");
    }

    /**
     * Returns the current status of the service.
     *
     * @return The service's status.
     */
    @Override
    public synchronized int getStatus() {
        return m_status;
    }

    @EventHandler(uei = EventConstants.RELOAD_DAEMON_CONFIG_UEI)
    public void handleReloadEvent(IEvent e) {
        DaemonTools.handleReloadEvent(e, Trapd.LOG4J_CATEGORY, (event) -> handleConfigurationChanged());
    }

    private void handleConfigurationChanged() {
        stop();

        try {
            m_trapListener.reload();

            publishListenerConfig();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        start();
    }

    public void publishListenerConfig() throws IOException {
        m_config = TrapdConfigFactory.getInstance();
        m_twinSession.publish(from(m_config));
    }

    public static SnmpV3User interpolateUser(final SnmpV3User snmpV3User, final Scope scope) {
        final SnmpV3User interpolatedSnmpV3User = new SnmpV3User();
        interpolatedSnmpV3User.setEngineId(Interpolator.interpolate(snmpV3User.getEngineId(), scope).output);
        interpolatedSnmpV3User.setSecurityLevel(snmpV3User.getSecurityLevel());
        interpolatedSnmpV3User.setSecurityName(Interpolator.interpolate(snmpV3User.getSecurityName(), scope).output);
        interpolatedSnmpV3User.setAuthProtocol(Interpolator.interpolate(snmpV3User.getAuthProtocol(), scope).output);
        interpolatedSnmpV3User.setPrivProtocol(Interpolator.interpolate(snmpV3User.getPrivProtocol(), scope).output);
        interpolatedSnmpV3User.setPrivPassPhrase(Interpolator.interpolate(snmpV3User.getPrivPassPhrase(), scope).output);
        interpolatedSnmpV3User.setAuthPassPhrase(Interpolator.interpolate(snmpV3User.getAuthPassPhrase(), scope).output);
        return interpolatedSnmpV3User;
    }

    public static Snmpv3User interpolateUser(final Snmpv3User snmpv3User, final Scope scope) {
        final Snmpv3User interpolatedSnmpV3User = new Snmpv3User();
        interpolatedSnmpV3User.setEngineId(Interpolator.interpolate(snmpv3User.getEngineId(), scope).output);
        interpolatedSnmpV3User.setSecurityLevel(snmpv3User.getSecurityLevel());
        interpolatedSnmpV3User.setSecurityName(Interpolator.interpolate(snmpv3User.getSecurityName(), scope).output);
        interpolatedSnmpV3User.setAuthProtocol(Interpolator.interpolate(snmpv3User.getAuthProtocol(), scope).output);
        interpolatedSnmpV3User.setPrivacyProtocol(Interpolator.interpolate(snmpv3User.getPrivacyProtocol(), scope).output);
        interpolatedSnmpV3User.setPrivacyPassphrase(Interpolator.interpolate(snmpv3User.getPrivacyPassphrase(), scope).output);
        interpolatedSnmpV3User.setAuthPassphrase(Interpolator.interpolate(snmpv3User.getAuthPassphrase(), scope).output);
        return interpolatedSnmpV3User;
    }

    public SnmpV3User interpolateUser(final SnmpV3User snmpV3User) {
        return interpolateUser(snmpV3User, new SecureCredentialsVaultScope(this.secureCredentialsVault));
    }

    public Snmpv3User interpolateUser(final Snmpv3User snmpv3User) {
        return interpolateUser(snmpv3User, new SecureCredentialsVaultScope(this.secureCredentialsVault));
    }

    public TrapListenerConfig from(final TrapdConfig config) {
        final TrapListenerConfig result = new TrapListenerConfig();
        final Scope scope = new SecureCredentialsVaultScope(this.secureCredentialsVault);
        result.setSnmpV3Users(config.getSnmpV3Users().stream()
                .map(e->interpolateUser(e, scope))
                .collect(Collectors.toList())
        );
        return result;
    }

    public static String getLoggingCategory() {
        return LOG4J_CATEGORY;
    }
}
