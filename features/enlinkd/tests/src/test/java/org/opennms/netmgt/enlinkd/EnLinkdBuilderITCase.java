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
package org.opennms.netmgt.enlinkd;

import java.util.Set;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.config.EnhancedLinkdConfig;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.enlinkd.model.BridgeBridgeLink;
import org.opennms.netmgt.enlinkd.model.IpNetToMedia;
import org.opennms.netmgt.enlinkd.persistence.api.BridgeBridgeLinkDao;
import org.opennms.netmgt.enlinkd.persistence.api.BridgeElementDao;
import org.opennms.netmgt.enlinkd.persistence.api.BridgeMacLinkDao;
import org.opennms.netmgt.enlinkd.persistence.api.BridgeStpLinkDao;
import org.opennms.netmgt.enlinkd.persistence.api.CdpElementDao;
import org.opennms.netmgt.enlinkd.persistence.api.CdpLinkDao;
import org.opennms.netmgt.enlinkd.persistence.api.IpNetToMediaDao;
import org.opennms.netmgt.enlinkd.persistence.api.IsIsElementDao;
import org.opennms.netmgt.enlinkd.persistence.api.IsIsLinkDao;
import org.opennms.netmgt.enlinkd.persistence.api.LldpElementDao;
import org.opennms.netmgt.enlinkd.persistence.api.LldpLinkDao;
import org.opennms.netmgt.enlinkd.persistence.api.OspfAreaDao;
import org.opennms.netmgt.enlinkd.persistence.api.OspfElementDao;
import org.opennms.netmgt.enlinkd.persistence.api.OspfLinkDao;
import org.opennms.netmgt.enlinkd.service.api.BridgeTopologyService;
import org.opennms.netmgt.enlinkd.service.api.CdpTopologyService;
import org.opennms.netmgt.enlinkd.service.api.LldpTopologyService;
import org.opennms.netmgt.enlinkd.service.api.NodeTopologyService;
import org.opennms.netmgt.enlinkd.service.api.ProtocolSupported;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyDao;
import org.opennms.netmgt.topologies.service.impl.OnmsTopologyLogger;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations= {
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/META-INF/opennms/applicationContext-proxy-snmp.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath*:/META-INF/opennms/applicationContext-enhancedLinkdTest.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public abstract class EnLinkdBuilderITCase extends EnLinkdTestHelper implements InitializingBean {

    @Autowired
    protected EnhancedLinkd m_linkd;

    @Autowired
    protected EnhancedLinkdConfig m_linkdConfig;

    @Autowired
    protected NodeDao m_nodeDao;

    @Autowired
    protected IpInterfaceDao m_ipInterfaceDao;

    @Autowired
    protected CdpLinkDao m_cdpLinkDao;

    @Autowired
    protected CdpElementDao m_cdpElementDao;    

    @Autowired
    protected LldpLinkDao m_lldpLinkDao;
        
    @Autowired
    protected LldpElementDao m_lldpElementDao;
        
    @Autowired
    protected OspfLinkDao m_ospfLinkDao;

    @Autowired
    protected OspfElementDao m_ospfElementDao;

    @Autowired
    protected IsIsLinkDao m_isisLinkDao;

    @Autowired
    protected IsIsElementDao m_isisElementDao;

    @Autowired
    protected BridgeElementDao m_bridgeElementDao;

    @Autowired
    protected BridgeStpLinkDao m_bridgeStpLinkDao;

    @Autowired
    protected BridgeBridgeLinkDao m_bridgeBridgeLinkDao;

    @Autowired
    protected BridgeMacLinkDao m_bridgeMacLinkDao;

    @Autowired
    protected IpNetToMediaDao m_ipNetToMediaDao;

    @Autowired
    protected BridgeTopologyService m_bridgeTopologyService;

    @Autowired
    protected CdpTopologyService m_cdpTopologyService;

    @Autowired
    protected LldpTopologyService m_lldpTopologyService;

    @Autowired
    protected NodeTopologyService m_nodeTopologyService;

    @Autowired
    protected OnmsTopologyDao m_topologyDao;

    @Autowired
    protected OspfAreaDao m_ospfAreaDao;

    @Override
    public void afterPropertiesSet() {
        BeanUtils.assertAutowiring(this);
    }

    @Before
    public void setUp() throws Exception {
        MockLogAppender.setupLogging(true, "DEBUG");
    }

    @After
    public void tearDown() {
        for (final IpNetToMedia at: m_ipNetToMediaDao.findAll())
            m_ipNetToMediaDao.delete(at);
        m_ipNetToMediaDao.flush();
        for (final BridgeBridgeLink bb: m_bridgeBridgeLinkDao.findAll())
            m_bridgeBridgeLinkDao.delete(bb);
        m_bridgeBridgeLinkDao.flush();
        for (final OnmsNode node : m_nodeDao.findAll())
            m_nodeDao.delete(node);
        m_nodeDao.flush();
    }

    public OnmsTopologyLogger createAndSubscribe(String protocol) {
        OnmsTopologyLogger tl = new OnmsTopologyLogger(protocol);
        m_topologyDao.subscribe(tl);
        return tl;
    }

    Set<ProtocolSupported> getSupportedProtocolsAsProtocolSupported() {
        return m_topologyDao.getSupportedProtocols()
                .stream()
                .map(p -> ProtocolSupported.valueOf(p.getId()))
                .collect(Collectors.toSet());
    }
    
}
