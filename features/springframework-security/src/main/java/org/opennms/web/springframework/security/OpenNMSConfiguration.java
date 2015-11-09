package org.opennms.web.springframework.security;

import java.util.Collections;

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
 */
public class OpenNMSConfiguration extends Configuration {
    private static volatile Logger LOG = LoggerFactory.getLogger(OpenNMSConfiguration.class);
    Configuration m_parentConfiguration;

    public void init() throws InterruptedException {
        LOG.debug("OpenNMSConfiguration initializing.");
        new Thread(new Runnable() {
            @Override public void run() {
                // wait up to 2 minutes for Karaf's JAAS Configuration to become active so we can put a facade on top of it.
                final long giveUp = System.currentTimeMillis() + Long.getLong("org.opennms.web.springframework.security.jaas-timeout", 120000);
                do {
                    final Configuration c = Configuration.getConfiguration();
                    if (c != null) {
                        LOG.trace("OpenNMSConfiguration found existing configuration: " + c.getClass().getName());
                        if (c.getClass().getName().contains("OsgiConfiguration")) {
                            LOG.debug("Found Karaf OSGi JAAS configuration.  Inserting OpenNMS redirector.");
                            break;
                        }
                    }
                    LOG.trace("OpenNMSConfiguration still waiting for Karaf OsgiConfiguration to activate...");
                    try {
                        Thread.sleep(1000);
                    } catch (final InterruptedException e) {
                        LOG.warn("Interrupted while waiting for Karaf's OSGi Configuration to initialize.", e);
                    }
                } while (System.currentTimeMillis() < giveUp);

                m_parentConfiguration = Configuration.getConfiguration();
                Configuration.setConfiguration(OpenNMSConfiguration.this);
            }
        }).start();
    }

    public void close() {
        Configuration.setConfiguration(m_parentConfiguration);
        m_parentConfiguration = null;
    }

    @Override
    public AppConfigurationEntry[] getAppConfigurationEntry(final String name) {
        LOG.debug("getAppConfigurationEntry(" + name +")");
        if ("opennms".equals(name)) {
            LOG.debug("getAppConfigurationEntry: Overriding.");
            return new AppConfigurationEntry[] { new AppConfigurationEntry(OpenNMSProxyLoginModule.class.getName(), LoginModuleControlFlag.REQUIRED, Collections.emptyMap()) };
        } else {
            LOG.debug("getAppConfigurationEntry: Passing through to Karaf.");
            if (m_parentConfiguration != null) {
                return m_parentConfiguration.getAppConfigurationEntry(name);
            } else {
                return null;
            }
        }
    }

    public void refresh() {
        if (m_parentConfiguration != null) {
            m_parentConfiguration.refresh();
        }
    }
}
