/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.linkd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

import org.opennms.netmgt.dao.api.AtInterfaceDao;
import org.opennms.netmgt.dao.api.DataLinkInterfaceDao;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.IpRouteInterfaceDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.SnmpInterfaceDao;
import org.opennms.netmgt.dao.api.StpInterfaceDao;
import org.opennms.netmgt.dao.api.StpNodeDao;
import org.opennms.netmgt.dao.api.VlanDao;
import org.opennms.netmgt.model.DataLinkInterface;
import org.opennms.netmgt.model.DataLinkInterface.DiscoveryProtocol;
import org.opennms.netmgt.model.OnmsAtInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.topology.CdpInterface;
import org.opennms.netmgt.model.topology.RouterInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author <a href="mailto:antonio@opennme.it">Antonio Russo</a>
 * 
 */

public abstract class LinkdTestHelper implements InitializingBean {
    private static final Logger LOG = LoggerFactory.getLogger(LinkdTestHelper.class);

    @Autowired
    protected NodeDao m_nodeDao;

    @Autowired
    protected SnmpInterfaceDao m_snmpInterfaceDao;

    @Autowired
    protected IpInterfaceDao m_ipInterfaceDao;

    @Autowired
    protected DataLinkInterfaceDao m_dataLinkInterfaceDao;

    @Autowired
    protected StpNodeDao m_stpNodeDao;

    @Autowired
    protected StpInterfaceDao m_stpInterfaceDao;

    @Autowired
    protected IpRouteInterfaceDao m_ipRouteInterfaceDao;

    @Autowired
    protected AtInterfaceDao m_atInterfaceDao;

    @Autowired
    protected VlanDao m_vlanDao;

    protected void printRouteInterface(int nodeid, RouterInterface route) {
        System.err.println("-----------------------------------------------------------");
        System.err.println("Local Route nodeid: "+nodeid);
        System.err.println("Local Route ifIndex: "+route.getIfindex());
        System.err.println("Next Hop Address: " +route.getNextHop());
        System.err.println("Next Hop Network: " +Linkd.getNetwork(route.getNextHop(), route.getNextHopNetmask()));
        System.err.println("Next Hop Netmask: " +route.getNextHopNetmask());
        System.err.println("Next Hop nodeid: "+route.getNextHopIfindex());
        System.err.println("Next Hop ifIndex: "+route.getNextHopIfindex());
        System.err.println("-----------------------------------------------------------");
        System.err.println("");        
    }

    protected void printCdpInterface(int nodeid, CdpInterface cdp) {
        System.err.println("-----------------------------------------------------------");
        System.err.println("Local cdp nodeid: "+nodeid);
        System.err.println("Local cdp ifindex: "+cdp.getCdpIfIndex());
        System.err.println("Local cdp port: "+cdp.getCdpIfName());
        System.err.println("Target cdp deviceId: "+cdp.getCdpTargetDeviceId());
        System.err.println("Target cdp nodeid: "+cdp.getCdpTargetNodeId());
        System.err.println("Target cdp ifname: "+cdp.getCdpTargetIfName());
        System.err.println("-----------------------------------------------------------");
        System.err.println("");        

    }


    protected void printAtInterface(OnmsAtInterface at) {
        System.out.println("----------------net to media------------------");
        System.out.println("id: " + at.getId());
        System.out.println("nodeid: " + at.getNode().getId());
        System.out.println("nodelabel: " + m_nodeDao.get(at.getNode().getId()).getLabel());       
        System.out.println("ip: " + at.getIpAddress());
        System.out.println("mac: " + at.getMacAddress());
        System.out.println("ifindex: " + at.getIfIndex());
        System.out.println("source: " + at.getSourceNodeId());
        System.out.println("sourcenodelabel: " + m_nodeDao.get(at.getSourceNodeId()).getLabel());       
        System.out.println("--------------------------------------");
        System.out.println("");

    }

    protected void printLink(DataLinkInterface datalinkinterface) {
        System.out.println("----------------Link------------------");
        Integer nodeid = datalinkinterface.getNode().getId();
        System.out.println("linkid: " + datalinkinterface.getId());
        System.out.println("nodeid: " + nodeid);
        System.out.println("nodelabel: " + m_nodeDao.get(nodeid).getLabel());       
        Integer ifIndex = datalinkinterface.getIfIndex();
        System.out.println("ifindex: " + ifIndex);
        if (ifIndex > 0)
            System.out.println("ifname: " + m_snmpInterfaceDao.findByNodeIdAndIfIndex(nodeid,ifIndex).getIfName());
        Integer nodeparent = datalinkinterface.getNodeParentId();
        System.out.println("nodeparent: " + nodeparent);
        System.out.println("parentnodelabel: " + m_nodeDao.get(nodeparent).getLabel());
        Integer parentifindex = datalinkinterface.getParentIfIndex();
        System.out.println("parentifindex: " + parentifindex);        
        if (parentifindex > 0)
            System.out.println("parentifname: " + m_snmpInterfaceDao.findByNodeIdAndIfIndex(nodeparent,parentifindex).getIfName());
        System.out.println("source: " + datalinkinterface.getSource());        
        System.out.println("protocol: " + datalinkinterface.getProtocol());        
        System.out.println("--------------------------------------");
        System.out.println("");

    }

    protected void checkLinks(final Collection<DataLinkInterface> dataLinkInterfaceCollection, final DataLinkTestMatcher... dataLinkTestData) {
        assertEquals(dataLinkInterfaceCollection.size(), dataLinkTestData.length);

        final List<DataLinkInterface> links = new ArrayList<>(dataLinkInterfaceCollection);
        final List<DataLinkTestMatcher> testData = new ArrayList<>(Arrays.asList(dataLinkTestData));

        LINKS: for (final DataLinkInterface link: links) {
            final ListIterator<DataLinkTestMatcher> it = testData.listIterator();
            while (it.hasNext()) {
                final DataLinkTestMatcher testDatum = it.next();
                if (testDatum.matches(link)) {
                    LOG.debug("Link {} matches test data {}", link, testDatum);
                    it.remove();
                    continue LINKS;
                }
            }
            LOG.debug("Remaining test data: {}", testData);
            LOG.debug("Failed link: {}", link);
            fail("No match found for DataLinkInterface: " + link);
        }
    }

    protected void printNode(OnmsNode node) {
        System.err.println("----------------Node------------------");
        System.err.println("nodeid: " + node.getId());
        System.err.println("nodelabel: " + node.getLabel());
        System.err.println("nodesysname: " + node.getSysName());
        System.err.println("nodesysoid: " + node.getSysObjectId());
        System.err.println("");

    }

    protected int getStartPoint(List<DataLinkInterface> links) {
        int start = 0;
        for (final DataLinkInterface link:links) {
            if (start==0 || link.getId().intValue() < start) {
                start = link.getId().intValue();
            }
        }
        return start;
    }

    final class DataLinkTestMatcher {
        private final OnmsNode m_node;
        private final OnmsNode m_parentNode;
        private final int m_ifIndex;
        private final int m_parentIfIndex;
        private final DiscoveryProtocol m_discoveryProtocol;

        public DataLinkTestMatcher(final OnmsNode node, final OnmsNode parentNode, final int ifIndex, final int parentIfIndex, final DiscoveryProtocol dp) {
            m_node = node;
            m_parentNode = parentNode;
            m_ifIndex = ifIndex;
            m_parentIfIndex = parentIfIndex;
            m_discoveryProtocol = dp;
        }

        public boolean matches(final DataLinkInterface link) {
            if (m_discoveryProtocol != link.getProtocol()) {
                return false;
            }
            if (m_node.getId() == link.getNodeId()
                    && m_parentNode.getId() == link.getNodeParentId()
                    && m_ifIndex == link.getIfIndex().intValue()
                    && m_parentIfIndex == link.getParentIfIndex().intValue()) {
                return true;
                //            } else if (m_parentNode.getId() == link.getNodeId()
                //                && m_node.getId() == link.getNodeParentId()
                //                && m_parentIfIndex == link.getIfIndex().intValue()
                //                && m_ifIndex == link.getParentIfIndex().intValue()) {
                //                return true;
            }
            return false;
        }

        @Override
        public String toString() {
            return "DataLinkTestMatcher [node=" + m_node.getId() + ", parentNode=" + m_parentNode.getId() + ", ifIndex=" + m_ifIndex + ", parentIfIndex=" + m_parentIfIndex + ", protocol=" + m_discoveryProtocol + "]";
        }
    };
}
