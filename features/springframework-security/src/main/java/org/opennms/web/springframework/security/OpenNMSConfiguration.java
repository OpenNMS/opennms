/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.web.springframework.security;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.AppConfigurationEntry.LoginModuleControlFlag;
import javax.security.auth.login.Configuration;

import org.opennms.bootstrap.OpenNMSProxyLoginModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Outside of Karaf (ie, in the "system" bundle) we need to use the springframework-security version of
 * the OpenNMSLoginModule run.  We can't share login modules because of classloading boundaries, and
 * the peculiar way that Karaf wraps login modules in a proxy class.
 *
 * Additionally, there are some cases where we wish to use the system's JAAS configuration.
 *
 * To achieve this, we gather all of the configuration implementations until the
 * Karaf implementation is loaded (this implementation is currently loaded last in the startup
 * process). When a request is made, we then delegate to all of the known implementations.
 *
 */
public class OpenNMSConfiguration extends Configuration {
    private static volatile Logger LOG = LoggerFactory.getLogger(OpenNMSConfiguration.class);

    private static final String JAAS_TIMEOUT_SYS_PROP = "org.opennms.web.springframework.security.jaas-timeout";
    private static final long DEFAULT_JAAS_TIMEOUT_MS = 120000;

    private final Set<Configuration> m_delegates = new LinkedHashSet<>();

    public void init() throws InterruptedException {
        LOG.debug("OpenNMSConfiguration initializing.");
        new Thread(new Runnable() {
            @Override public void run() {
                // wait up to 2 minutes for Karaf's JAAS Configuration to become active so we can put a facade on top of it.
                final long giveUp = System.currentTimeMillis() + Long.getLong(JAAS_TIMEOUT_SYS_PROP, DEFAULT_JAAS_TIMEOUT_MS);
                do {
                    final Configuration c = Configuration.getConfiguration();
                    if (c != null) {
                        // gather all configurations that are found, until the Karaf implementation is loaded
                        if (m_delegates.add(c)) {
                            LOG.trace("OpenNMSConfiguration found existing configuration: " + c.getClass().getName());
                            if (c.getClass().getName().contains("OsgiConfiguration")) {
                                LOG.debug("Found Karaf OSGi JAAS configuration.  Inserting OpenNMS redirector.");
                                break;
                            }
                        }
                    }
                    LOG.trace("OpenNMSConfiguration still waiting for Karaf OsgiConfiguration to activate...");
                    try {
                        Thread.sleep(200);
                    } catch (final InterruptedException e) {
                        LOG.warn("Interrupted while waiting for Karaf's OSGi Configuration to initialize.", e);
                        break;
                    }
                } while (System.currentTimeMillis() < giveUp);
                Configuration.setConfiguration(OpenNMSConfiguration.this);
            }
        }).start();
    }

    public void close() {
        final Iterator<Configuration> it = m_delegates.iterator();
        Configuration.setConfiguration(it.hasNext() ? it.next() : null);
        m_delegates.clear();
    }

    @Override
    public AppConfigurationEntry[] getAppConfigurationEntry(final String name) {
        LOG.debug("getAppConfigurationEntry(" + name +")");
        if ("opennms".equals(name)) {
            LOG.debug("getAppConfigurationEntry: Overriding.");
            return new AppConfigurationEntry[] { new AppConfigurationEntry(OpenNMSProxyLoginModule.class.getName(), LoginModuleControlFlag.REQUIRED, Collections.emptyMap()) };
        } else {
            LOG.debug("getAppConfigurationEntry: Passing through.");
            return m_delegates.stream()
                .map(c -> c.getAppConfigurationEntry(name))
                .reduce(null, (a,b) -> {
                    if (a == null) {
                        return b;
                    } else if (b == null) {
                        return a;
                    } else {
                        // Concatenate the arrays
                        final AppConfigurationEntry[] c = new AppConfigurationEntry[a.length + b.length];
                        System.arraycopy(a, 0, c, 0, a.length);
                        System.arraycopy(b, 0, c, a.length, b.length);
                        return c;
                    }
                });
        }
    }

    public void refresh() {
        m_delegates.stream().forEach(Configuration::refresh);
    }
}
