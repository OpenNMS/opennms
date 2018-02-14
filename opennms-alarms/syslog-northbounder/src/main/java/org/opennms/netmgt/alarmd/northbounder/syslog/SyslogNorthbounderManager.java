/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.alarmd.northbounder.syslog;

import java.util.HashMap;
import java.util.Map;

import org.graylog2.syslog4j.Syslog;
import org.opennms.core.soa.Registration;
import org.opennms.core.soa.ServiceRegistry;
import org.opennms.netmgt.alarmd.api.NorthboundAlarm;
import org.opennms.netmgt.alarmd.api.Northbounder;
import org.opennms.netmgt.alarmd.api.NorthbounderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

/**
 * The Class SyslogNorthbounderManager.
 * 
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class SyslogNorthbounderManager implements InitializingBean, Northbounder, DisposableBean {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(SyslogNorthbounderManager.class);

    /** The service registry. */
    @Autowired
    private ServiceRegistry m_serviceRegistry;

    /** The Syslog northbounder configuration DAO. */
    @Autowired
    private SyslogNorthbounderConfigDao m_configDao;

    /** The registrations map. */
    private Map<String, Registration> m_registrations = new HashMap<String, Registration>();

    /**
     * After properties set.
     *
     * @throws Exception the exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(m_configDao);
        Assert.notNull(m_serviceRegistry);

        // Registering itself as a northbounder
        m_registrations.put(getName(), m_serviceRegistry.register(this, Northbounder.class));

        // Registering each destination as a northbounder
        registerNorthnounders();
    }

    /**
     * Register northnounders.
     *
     * @throws Exception the exception
     */
    private void registerNorthnounders() throws Exception {
        if (! m_configDao.getConfig().isEnabled()) {
            LOG.warn("The Syslog NBI is globally disabled, the destinations won't be registered which means all the alarms will be rejected.");
            return;
        }
        for (SyslogDestination syslogDestination : m_configDao.getConfig().getDestinations()) {
            LOG.info("Registering syslog northbound configuration for destination {}.", syslogDestination.getName());
            SyslogNorthbounder nbi = new SyslogNorthbounder(m_configDao, syslogDestination.getName());
            nbi.afterPropertiesSet();
            m_registrations.put(nbi.getName(), m_serviceRegistry.register(nbi, Northbounder.class));
        }
    }

    /**
     * Destroy.
     *
     * @throws Exception the exception
     */
    @Override
    public void destroy() throws Exception {
        m_registrations.values().forEach(Registration::unregister);
        try {
            Syslog.shutdown(); // Shutdown all Syslog instances.
        } catch (Exception e) {
            LOG.error("Can't shutdown the syslog instances", e);
        }
    }

    /**
     * Start.
     *
     * @throws NorthbounderException the northbounder exception
     */
    @Override
    public void start() throws NorthbounderException {
        // There is no need to do something here. Only the reload method will be implemented
    }

    @Override
    public boolean isReady() {
        return false;
    }

    /**
     * On alarm.
     *
     * @param alarm the alarm
     * @throws NorthbounderException the northbounder exception
     */
    @Override
    public void onAlarm(NorthboundAlarm alarm) throws NorthbounderException {
        // There is no need to do something here. Only the reload method will be implemented        
    }

    /**
     * Stop.
     *
     * @throws NorthbounderException the northbounder exception
     */
    @Override
    public void stop() throws NorthbounderException {
        // There is no need to do something here. Only the reload method will be implemented
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    @Override
    public String getName() {
        return SyslogNorthbounder.NBI_NAME;
    }

    /**
     * Reloads the configuration.
     */
    @Override
    public void reloadConfig() throws NorthbounderException {
        LOG.info("Reloading Syslog northbound configuration.");
        try {
            m_configDao.reload();
            m_registrations.forEach((k,v) -> { if (k != getName()) v.unregister();}); // Unregistering Syslog destinations
            Syslog.shutdown(); // Shutdown all Syslog instances.
            registerNorthnounders(); // Re-registering all Syslog destinations.
        } catch (Exception e) {
            throw new NorthbounderException("Can't reload the Syslog northbound configuration", e);
        }
    }
}
