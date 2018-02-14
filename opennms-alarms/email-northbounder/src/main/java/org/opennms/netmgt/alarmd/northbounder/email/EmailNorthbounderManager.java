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

package org.opennms.netmgt.alarmd.northbounder.email;

import java.util.HashMap;
import java.util.Map;

import org.opennms.core.soa.Registration;
import org.opennms.core.soa.ServiceRegistry;
import org.opennms.netmgt.alarmd.api.NorthboundAlarm;
import org.opennms.netmgt.alarmd.api.Northbounder;
import org.opennms.netmgt.alarmd.api.NorthbounderException;
import org.opennms.netmgt.dao.api.JavaMailConfigurationDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

/**
 * The Class EmailNorthbounderManager.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class EmailNorthbounderManager implements InitializingBean, Northbounder, DisposableBean {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(EmailNorthbounderManager.class);

    /** The service registry. */
    @Autowired
    private ServiceRegistry m_serviceRegistry;

    /** The Email northbounder configuration DAO. */
    @Autowired
    private EmailNorthbounderConfigDao m_configDao;

    /** The JavaMail configuration DAO. */
    @Autowired
    private JavaMailConfigurationDao m_javaMailDao;

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
            LOG.warn("The Email NBI is globally disabled, the destinations won't be registered which means all the alarms will be rejected.");
            return;
        }
        for (EmailDestination destination : m_configDao.getConfig().getEmailDestinations()) {
            LOG.info("Registering Email northbound configuration for destination {}.", destination.getName());
            EmailNorthbounder nbi = new EmailNorthbounder(m_configDao, m_javaMailDao, destination.getName());
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
        return EmailNorthbounder.NBI_NAME;
    }

    /**
     * Reloads the configuration.
     */
    @Override
    public void reloadConfig() {
        LOG.info("Reloading Email northbound configuration.");
        try {
            m_configDao.reload();
            m_javaMailDao.reloadConfiguration();
            m_registrations.forEach((k,v) -> { if (k != getName()) v.unregister();});
            registerNorthnounders();
        } catch (Exception e) {
            throw new NorthbounderException("Can't reload the SNMP trap northbound configuration", e);
        }
    }

}