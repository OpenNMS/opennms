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
package org.opennms.netmgt.config.tokenauth;

import java.util.Objects;

import org.apache.commons.lang.StringUtils;
import org.opennms.netmgt.config.TokenAuthConfigFactory;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventIpcManager;
import org.opennms.netmgt.events.api.EventIpcManagerFactory;
import org.opennms.netmgt.events.api.EventListener;
import org.opennms.netmgt.events.api.model.IEvent;
import org.opennms.netmgt.events.api.model.IParm;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 * Reacts to a {@code reloadDaemonConfig} event with
 * {@code daemonName=TokenAuth} by re-reading
 * {@code etc/token-auth-configuration.xml}. Cached tokens are
 * flushed as part of the reload so the next request through a collector
 * drives a fresh acquisition against the (possibly updated) token-auth
 * definition.
 *
 * <p>To trigger:
 * <pre>
 *   uei.opennms.org/internal/reloadDaemonConfig
 *   parm: daemonName=TokenAuth
 * </pre>
 *
 * <p>Late-binding registration: this bean lives in the daoContext, which
 * is loaded as a parent of the eventDaemonContext while that context is
 * still being constructed -- so the {@link EventIpcManager} OSGi service
 * isn't published yet when this bean is wired. We listen for
 * {@link ContextRefreshedEvent}s, and on each one we try
 * {@link EventIpcManagerFactory#getIpcManager()}; once it succeeds we
 * register as a listener (one-shot, guarded by {@code registered}).</p>
 */
public class TokenAuthConfigReloadListener
        implements EventListener, ApplicationListener<ContextRefreshedEvent>, DisposableBean {

    /** Name advertised for daemon-reload routing. */
    public static final String NAME = "TokenAuth";

    private static final Logger LOG = LoggerFactory.getLogger(TokenAuthConfigReloadListener.class);

    private final TokenCache tokenCache;
    private volatile EventIpcManager eventIpcManager;
    private volatile boolean registered;

    public TokenAuthConfigReloadListener(final TokenCache tokenCache) {
        this.tokenCache = Objects.requireNonNull(tokenCache, "tokenCache");
    }

    /**
     * Test-only constructor. Lets tests inject a mock manager up front
     * and skip the lazy registration dance.
     */
    TokenAuthConfigReloadListener(final TokenCache tokenCache,
                                           final EventIpcManager eventIpcManager) {
        this.tokenCache = Objects.requireNonNull(tokenCache, "tokenCache");
        this.eventIpcManager = Objects.requireNonNull(eventIpcManager, "eventIpcManager");
    }

    @Override
    public void onApplicationEvent(final ContextRefreshedEvent event) {
        if (registered) {
            return;
        }
        final EventIpcManager mgr;
        try {
            mgr = EventIpcManagerFactory.getIpcManager();
        } catch (final IllegalStateException notReady) {
            // eventDaemonContext has not yet finished building. We will
            // try again on the next ContextRefreshedEvent that bubbles
            // up to us.
            return;
        }
        this.eventIpcManager = mgr;
        mgr.addEventListener(this, EventConstants.RELOAD_DAEMON_CONFIG_UEI);
        registered = true;
        LOG.info("Registered as listener for {} (daemonName={})",
                EventConstants.RELOAD_DAEMON_CONFIG_UEI, NAME);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void onEvent(final IEvent e) {
        if (e == null || !EventConstants.RELOAD_DAEMON_CONFIG_UEI.equals(e.getUei())) {
            return;
        }
        final IParm daemonNameParm = e.getParm(EventConstants.PARM_DAEMON_NAME);
        if (daemonNameParm == null || daemonNameParm.getValue() == null) {
            return;
        }
        if (!NAME.equalsIgnoreCase(daemonNameParm.getValue().getContent())) {
            return;
        }
        LOG.info("Reloading token-auth-configuration.xml");
        try {
            TokenAuthConfigFactory.reload();
            tokenCache.invalidateAll();
            LOG.info("token-auth-configuration.xml reload successful; cache flushed");
            sendEventQuietly(new EventBuilder(EventConstants.RELOAD_DAEMON_CONFIG_SUCCESSFUL_UEI, NAME)
                    .addParam(EventConstants.PARM_DAEMON_NAME, NAME)
                    .getEvent());
        } catch (final Exception t) {
            LOG.error("token-auth-configuration.xml reload failed", t);
            sendEventQuietly(new EventBuilder(EventConstants.RELOAD_DAEMON_CONFIG_FAILED_UEI, NAME)
                    .addParam(EventConstants.PARM_DAEMON_NAME, NAME)
                    .addParam(EventConstants.PARM_REASON, StringUtils.abbreviate(t.getLocalizedMessage(), 128))
                    .getEvent());
        }
    }

    /**
     * Spring {@link DisposableBean} hook. On context shutdown,
     * unregister from {@link EventIpcManager} so the manager does not
     * retain a reference to a dead listener.
     */
    @Override
    public void destroy() {
        if (eventIpcManager != null && registered) {
            try {
                eventIpcManager.removeEventListener(this);
            } catch (final Exception t) {
                LOG.warn("Failed to remove token-auth reload listener on shutdown", t);
            }
            registered = false;
        }
    }

    private void sendEventQuietly(final Event event) {
        if (eventIpcManager == null) {
            return;
        }
        try {
            eventIpcManager.sendNow(event);
        } catch (final Exception t) {
            LOG.warn("Failed to publish {}", event.getUei(), t);
        }
    }
}
