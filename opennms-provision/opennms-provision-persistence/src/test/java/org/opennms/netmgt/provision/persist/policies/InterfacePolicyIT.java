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
package org.opennms.netmgt.provision.persist.policies;

import static org.junit.Assert.assertEquals;
import static org.opennms.core.utils.InetAddressUtils.str;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
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
public class InterfacePolicyIT implements InitializingBean {
    @Autowired
    private IpInterfaceDao m_ipInterfaceDao;

    @Autowired
    private DatabasePopulator m_populator;

    private List<OnmsIpInterface> m_interfaces;

    @Override
    public void afterPropertiesSet() throws Exception {

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
            o = p.apply(iface, Collections.emptyMap());
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
