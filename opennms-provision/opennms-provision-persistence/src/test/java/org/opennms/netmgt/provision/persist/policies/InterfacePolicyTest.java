/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.persist.policies;

import static org.junit.Assert.assertEquals;
import static org.opennms.core.utils.InetAddressUtils.str;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-mockDao.xml"
})
@JUnitConfigurationEnvironment
public class InterfacePolicyTest implements InitializingBean {
    @Autowired
    private IpInterfaceDao m_ipInterfaceDao;

    @Autowired
    private DatabasePopulator m_populator;

    private List<OnmsIpInterface> m_interfaces;
    
    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    @Before
    public void setUp() {
        MockLogAppender.setupLogging();
        m_populator.populateDatabase();
        m_interfaces = m_ipInterfaceDao.findAll();
    }
    
    @After
    public void tearDown() {
        m_populator.resetDatabase();
    }

    @Test
    @Transactional
    public void testMatchingPolicy() {
        OnmsIpInterface o = null;
        
        final MatchingIpInterfacePolicy p = new MatchingIpInterfacePolicy();
        p.setAction("DO_NOT_PERSIST");
        p.setMatchBehavior("NO_PARAMETERS");
        p.setIpAddress("~^10\\..*$");

        final List<OnmsIpInterface> populatedInterfaces = new ArrayList<>();
        final List<OnmsIpInterface> matchedInterfaces = new ArrayList<>();
        
        for (final OnmsIpInterface iface : m_interfaces) {
            System.err.println(iface);
            o = p.apply(iface);
            if (o != null) {
                matchedInterfaces.add(o);
            }
            InetAddress addr = iface.getIpAddress();
            
            if (str(addr).startsWith("10.")) {
                populatedInterfaces.add(iface);
            }
        }
        
        assertEquals(populatedInterfaces, matchedInterfaces);
    }

}
