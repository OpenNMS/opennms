/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.alarmd.northbounder.jms;

import java.util.HashMap;
import java.util.Map;

import javax.jms.ConnectionFactory;

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
 * The Class JmsNorthbounderManager.
 *
 * @author <a href="mailto:dschlenk@converge-one.com">David Schlenk</a>
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class JmsNorthbounderManager implements InitializingBean, Northbounder, DisposableBean {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(JmsNorthbounderManager.class);

    /** The service registry. */
    @Autowired
    private ServiceRegistry m_serviceRegistry;

    /** The JMX northbounder connection factory. */
    @Autowired
    private ConnectionFactory m_jmsNorthbounderConnectionFactory;

    /** The JMS Configuration DAO. */
    @Autowired
    private JmsNorthbounderConfigDao m_configDao;

    /** The m_registrations. */
    private Map<String, Registration> m_registrations = new HashMap<String, Registration>();

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
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
            LOG.warn("The JMS NBI is globally disabled, the destinations won't be registered which means all the alarms will be rejected.");
            return;
        }
        for (JmsDestination jmsDestination : m_configDao.getConfig().getDestinations()) {
            LOG.info("Registering JMS northbound configuration for destination {}.", jmsDestination.getName());
            JmsNorthbounder nbi = new JmsNorthbounder(m_configDao.getConfig(), m_jmsNorthbounderConnectionFactory, jmsDestination);
            nbi.afterPropertiesSet();
            m_registrations.put(nbi.getName(), m_serviceRegistry.register(nbi, Northbounder.class));
        }
    }

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.DisposableBean#destroy()
     */
    @Override
    public void destroy() throws Exception {
        m_registrations.values().forEach(Registration::unregister);
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.alarmd.api.Northbounder#start()
     */
    @Override
    public void start() throws NorthbounderException {
        // There is no need to do something here. Only the reload method will be implemented
    }

    @Override
    public boolean isReady() {
        return false;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.alarmd.api.Northbounder#onAlarm(org.opennms.netmgt.alarmd.api.NorthboundAlarm)
     */
    @Override
    public void onAlarm(NorthboundAlarm alarm) throws NorthbounderException {
        // There is no need to do something here. Only the reload method will be implemented
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.alarmd.api.Northbounder#stop()
     */
    @Override
    public void stop() throws NorthbounderException {
        // There is no need to do something here. Only the reload method will be implemented
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.alarmd.api.Northbounder#getName()
     */
    @Override
    public String getName() {
        return JmsNorthbounder.NBI_NAME;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.alarmd.api.Northbounder#reloadConfig()
     */
    @Override
    public void reloadConfig() throws NorthbounderException {
        LOG.info("Reloading JMS northbound configuration.");
        try {
            m_configDao.reload();
            m_registrations.forEach((k,v) -> { if (k != getName()) v.unregister();});
            registerNorthnounders();
        } catch (Exception e) {
            throw new NorthbounderException("Can't reload the JMS northbound configuration", e);
        }
    }

}
