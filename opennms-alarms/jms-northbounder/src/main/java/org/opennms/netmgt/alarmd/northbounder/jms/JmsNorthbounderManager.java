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
import java.util.List;
import java.util.Map;

import javax.jms.ConnectionFactory;

import org.opennms.core.soa.Registration;
import org.opennms.core.soa.ServiceRegistry;
import org.opennms.netmgt.alarmd.api.Northbounder;
import org.opennms.netmgt.dao.api.NodeDao;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

/**
 * The Class JmsNorthbounderManager.
 *
 * @author <a href="mailto:dschlenk@converge-one.com">David Schlenk</a>
 */
public class JmsNorthbounderManager implements InitializingBean,
        DisposableBean {

    /** The m_service registry. */
    @Autowired
    private ServiceRegistry m_serviceRegistry;

    /** The m_jms northbounder connection factory. */
    @Autowired
    private ConnectionFactory m_jmsNorthbounderConnectionFactory;

    /** The m_config dao. */
    @Autowired
    private JmsNorthbounderConfigDao m_configDao;

    /** The m_node dao. */
    @Autowired
    private NodeDao m_nodeDao;

    /** The m_registrations. */
    private Map<String, Registration> m_registrations = new HashMap<String, Registration>();

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    @Override
    public void afterPropertiesSet() throws Exception {

        Assert.notNull(m_nodeDao);
        Assert.notNull(m_configDao);
        Assert.notNull(m_serviceRegistry);

        JmsNorthbounderConfig config = m_configDao.getConfig();
        List<JmsDestination> destinations = config.getDestinations();
        for (JmsDestination jmsDestination : destinations) {
            JmsNorthbounder nbi = new JmsNorthbounder(
                                                      config,
                                                      m_jmsNorthbounderConnectionFactory,
                                                      jmsDestination);
            nbi.afterPropertiesSet();
            m_registrations.put(nbi.getName(),
                                m_serviceRegistry.register(nbi,
                                                           Northbounder.class));
        }
    }

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.DisposableBean#destroy()
     */
    @Override
    public void destroy() throws Exception {
        for (Registration r : m_registrations.values()) {
            r.unregister();
        }
    }

}
