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
package org.opennms.netmgt.enlinkd.service.api;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BridgeSimpleConnection implements Topology {

    static final Logger LOG = LoggerFactory.getLogger(BridgeSimpleConnection.class);

    private final BridgeForwardingTable m_xBridge;
    private final BridgeForwardingTable m_yBridge;
    private BridgePort m_xyPort;
    private BridgePort m_yxPort;

    public BridgePort getFirstPort() {
        return m_xyPort;
    }
    
    public BridgePort getSecondPort() {
        return m_yxPort;
    }

    public Integer getFirstBridgePort() {
        return m_xyPort.getBridgePort();
    }
    
    public Integer getSecondBridgePort() {
        return m_yxPort.getBridgePort();
    }

    private BridgeSimpleConnection(BridgeForwardingTable xBridge, 
            BridgeForwardingTable yBridge) {
        super();
        m_xBridge = xBridge;
        m_yBridge = yBridge;
    }

    /**
     *
     * Least condition theorem for simple connections
     * X and Y are bridges
     * pxy is a port of X forwarding to port pyx on Y
     * m_1 m_2 m_3 are mac addresses assigned on not backbone ports
     * m_s are mac addresses assigned on backbone ports (on the shared segment between the bridges)
     * m_x is a mac address of bridge X
     * m_y is a mac address of bridge Y
     * px1, px2: are port on bridgeX
     * py1, py2: are port on bridgeY
     * BFT(px,X) is the Bridge Forwarding Set/Table for port px on bridge X
     * TS(px,X) is the Through Set for port px on bridge X
     * TS is a set of all the macs that are not forwarded on px
     *
     * First Method:
     * BridgePort findPortUsingBridgeMacAddress(BFT(X), macs(Y))
     * for m_y on macs(Y) return pxy such that m_y belongs to BFT(pxy,X)
     *
     * Second Method:
     * BridgePort findPortUsingSimpleConnectionAlg(BFT(X), BFT(Y))
     * for m_1 and m_2 return pxy such that: m_1 belongs to FDB(pxy,X) and FDB(py1,Y)
     *                                       m_2 belongs to FDB(pxy,X) and FDB(py2,Y)
     * Third Method
     * BridgePort findPortUsingSimpleConnectionAlg(BFT(Y), BFT(X), pxy)
     * m_3 belongs to TS(pxy, X)
     *
     * X and Y are simple connected by xy on X and yx on Y
     *
     * condition 1 (2 macs, 2 ports 1 on 1 and 1 on Y)
     * X(m_x)=pxy->{}<-pyx=Y(m_y)
     * exists m_x belonging to macs(X) and exists m_y belonging to macs(Y):
     * m_x belongs BFT(pyx,Y)
     * m_y belongs BFT(pxy,X)
     *
     * methods call condition 1
     * *pxy=findPortUsingBridgeMacAddress(BFT(X), macs(Y))
     * *pyx=findPortUsingBridgeMacAddress(BFT(Y), macs(X))
     *
     * condition 2A XThenY (3 macs, 3 ports - 2 on X and 1 on Y)
     * exists m_y belonging to macs(Y), m_1 and m_2, px1 on X:
     * m_1<-px1=X=pxy->{m_2}<-pyx=Y(m_y)
     * m_y belongs to BFT(pxy,X)
     * m_1 belongs to BFT(px1,X) BFT(pyx,Y)
     * m_2 belongs to BFT(pxy,X) BFT(pyx,Y)
     *
     * condition 2B XThenY (3 macs, 4 ports, 3 on X and 1 on Y)
     *         m_1<-px1=|X|
     *                  |X|=pxy->{}<-pyx=Y(m_y)
     *         m_2<-px2=|X|
     * exists m_y, m_1 and m_2, px1 and px2 on X :
     * m_y belongs to BFT(pxy,X)
     * m_1 belongs to BFT(px1,X) BFT(pyx,Y)
     * m_2 belongs to BFT(px2,X) BFT(pyx,Y)
     *
     * methods call conditions 2A 2B XThenY
     * *pxy=findPortUsingBridgeMacAddress(X, BFT(X), macs(Y))
     * *pyx=findPortUsingSimpleConnectionAlg(BFT(Y), BFT(X))
     *
     * condition 2A YThenX (3 macs, 3 ports, 1 on X and 2 on Y)
     * exists m_x belonging to macs(X), m_1 and m_2, py1 on Y :
     *         m_1<-py1=Y=pyx->m_2<-pxy=X(m_x)
     * m_x belongs to BFT(pyx,Y)
     * m_1 belongs to BFT(py1,Y) BFT(pxy,X)
     * m_2 belongs to BFT(pyx,Y) BFT(pxy,X)
     *
     * condition 2B YThenX (3 macs, 4 ports, 1 on X and 3 on Y)
     * exists m_x, m_1 and m_2, py1 and py2 on Y :
     *         m_1<-py1=|Y|
     *                  |Y|=pyx->{}<-pxy=X(m_x)
     *         m_2<-py2=|Y|
     * m_x belongs to BFT(pyx,Y)
     * m_1 belongs to BFT(py1,Y) FDB(pxy,X)
     * m_2 belongs to BFT(py2,Y) FDB(pxy,X)
     *
     * methods call conditions 2A 2B YThenX
     * *pyx=findPortUsingBridgeMacAddress(Y, BFT(Y), macs(X))
     * *pxy=findPortUsingSimpleConnectionAlg(BFT(X), BFT(Y))
     *
     * condition 3A XThenY (3 macs, 4 ports, 2 on X and 2 on Y)
     * exist m_1,m_2,m_3 and py1 on Y and px3 on X:
     *        m_1<-py1=Y=pyx->{m_2}<-pxy=X=px3->m_3
     * m_1 belongs to BFT(py1,Y) BFT(pxy,X)
     * m_2 belongs to BFT(pyx,Y) BFT(pxy,X)
     * m_3 belongs to BFT(pyx,Y) BFT(px3,X)
     * condition 3B XThenY (3 macs, 5 ports, 2 on X and 3 on Y)
     * exist m_1,m_2,m_3 and py1,py2 on Y and px3 on X:
     *         m_1<-py1=|Y|
     *                  |Y|=pyx->{}<-pxy=X=px3->m_3
     *         m_2<-py2=|Y|
     * m_1 belongs to BFT(py1,Y) BFT(pxy,X)
     * m_2 belongs to BFT(py2,Y) BFT(pxy,X)
     * m_3 belongs to BFT(pyx,Y) BFT(px3,X)
     * methods call condition 3A 3B XThenY
     * *pxy=findPortUsingSimpleConnectionAlg(BFT(X), BFT(Y))
     * *pyx=findPortUsingSimpleConnectionAlg(BFT(Y), BFT(X), pxy)
     *
     * condition 3A YThenX (3 macs, 4 ports, 2 on X and 2 on Y)
     * exist m_1,m_2,m_3 and py1 on Y and px3 on X:
     *        m_1<-py1=Y=pyx->{m_2}<-pxy=X=px3->m_3
     * m_1 belongs to BFT(py1,Y) BFT(pxy,X)
     * m_2 belongs to BFT(pyx,Y) BFT(pxy,X)
     * m_3 belongs to BFT(pyx,Y) BFT(px3,X)
     * condition 3B YThenX (3 macs, 5 ports, 3 on X and 2 on Y)
     * exist m_1,m_2,m_3 and px1,px2 on X and py3 on Y:
     *         m_1<-px1=|X|
     *                  |X|=pxy->{}<-pyx=Y=py3->m_3
     *         m_2<-px2=|X|
     * m_1 belongs to BFT(px1,X) BFT(pyx,Y)
     * m_2 belongs to BFT(px2,X) BFT(pyx,Y)
     * m_3 belongs to BFT(pxy,X) BFT(py3,Y)
     * methods call condition 3A 3B YThenX
     * *pyx=findPortUsingSimpleConnectionAlg(BFT(Y), BFT(X))
     * *pxy=findPortUsingSimpleConnectionAlg(BFT(X), BFT(Y), pyx)
     *
     * @throws BridgeTopologyException if the connection  is not find
     */
    public void findSimpleConnection() throws BridgeTopologyException {

        if (LOG.isDebugEnabled()) {
            LOG.debug("findSimpleConnection: \n first bridge -> \n{}\n second bridge -> \n{}",
                      m_xBridge.printTopology(),
                      m_yBridge.printTopology());
        }

        // BFT only 1 port for X
        if (m_xBridge.getPorttomac().size() == 1) {
            m_xyPort=m_xBridge.getPorttomac().iterator().next().getPort();
            if (LOG.isDebugEnabled()) {
                LOG.debug("findSimpleConnectionForSizeOne: only one port found: bridge:[{}] <- {} ",
                        m_yBridge.getNodeId(),
                        m_xyPort.printTopology());
            }
        }

        // BFT only 1 port for Y
        if (m_yBridge.getPorttomac().size() == 1) {
            m_yxPort=m_yBridge.getPorttomac().iterator().next().getPort();
            if (LOG.isDebugEnabled()) {
                LOG.debug("findSimpleConnectionForSizeOne: only one port found: bridge:[{}] <- {} ",
                        m_xBridge.getNodeId(),
                        m_yxPort.printTopology());
            }
        }

        if (m_xyPort == null) {
            m_xyPort = findPortUsingBridgeIdentifiers(m_xBridge, m_yBridge.getIdentifiers());
        }
        if (m_yxPort == null) {
            m_yxPort = findPortUsingBridgeIdentifiers(m_yBridge, m_xBridge.getIdentifiers());
        }

        // condition 1 match: return
        if (m_xyPort != null && m_yxPort != null) {
            if (LOG.isDebugEnabled()) {
               LOG.debug("findSimpleConnection: success on condition 1 {} -> {}", m_xyPort.printTopology(), m_yxPort.printTopology());
            }
            return;
        }

        // condition 2 YThenX
        if (m_xyPort == null && m_yxPort != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("findSimpleConnection: try condition 2 YThenX {} -> {}", m_yxPort.printTopology(), m_xBridge.getNodeId());
            }
            m_xyPort = findPortUsingSimpleConnectionAlgorithm(m_xBridge, m_yBridge, m_yxPort);
            if (m_xyPort != null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("findSimpleConnection: success on condition 2 YThenX {} -> {}", m_xyPort.printTopology(), m_yxPort.printTopology());
                }
                return;
            }
        }

        // condition 2 XThenY
        if (m_yxPort == null && m_xyPort != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("findSimpleConnection: try condition 2 XThenY {} -> {}", m_xyPort.printTopology(), m_yBridge.getNodeId());
            }
            m_yxPort = findPortUsingSimpleConnectionAlgorithm(m_yBridge, m_xBridge, m_xyPort);
            if (m_yxPort != null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("findSimpleConnection: success on condition 2 XThenY {} -> {}", m_xyPort.printTopology(), m_yxPort.printTopology());
                }
                return;
            }
        }

        // try condition 3A 3B XThenY
        m_xyPort = findPortUsingSimpleConnectionAlgorithm(m_xBridge, m_yBridge);
        if (m_xyPort != null) {
            m_yxPort = findPortUsingSimpleConnectionAlgorithm(m_yBridge, m_xBridge, m_xyPort);
            if (m_yxPort != null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("findSimpleConnection: success on condition 3 XThenY {} -> {}", m_xyPort.printTopology(), m_yxPort.printTopology());
                }
                return;
            }
        }

        // try condition 3A 3B YThenX
        m_yxPort = findPortUsingSimpleConnectionAlgorithm(m_yBridge, m_xBridge);
        if (m_yxPort != null) {
            m_xyPort = findPortUsingSimpleConnectionAlgorithm(m_xBridge, m_yBridge, m_yxPort);
            if (m_xyPort != null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("findSimpleConnection: success on condition 3 YThenX {} -> {}", m_xyPort.printTopology(), m_yxPort.printTopology());
                }
                return;
            }
        }

        // condition 4 to be discussed
        // intersection is made only by macs living on xy of X and yx of Y
        // only one common port on X and Y
        // there are no other common forwarding port
        // first step is to find the common macs.
        if (m_xBridge.getPorttomac().size() == 2 && m_yBridge.getPorttomac().size() == 2) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("findSimpleConnectionForSizeTwo: bridge [{}] {} ports -> bridge [{}] {} ports",
                        m_xBridge.getNodeId(),
                        m_xBridge.getPorttomac().size(),
                        m_yBridge.getNodeId(),
                        m_yBridge.getPorttomac().size());
            }
            BridgePort bridgeXElectedPort = m_xBridge
                    .getPorttomac().iterator().next().getPort();

            Set<String> commonSegmentMacAddress = m_xBridge.getBridgePortWithMacs(bridgeXElectedPort).getMacs();
            NEXT:   for (BridgePortWithMacs yBridgeBridgePortWithMac : m_yBridge.getPorttomac()) {
                for (String mac : yBridgeBridgePortWithMac.getMacs()) {
                    if (commonSegmentMacAddress.contains(mac)) {
                        continue NEXT;
                    }
                }
                m_xyPort=bridgeXElectedPort;
                m_yxPort= yBridgeBridgePortWithMac.getPort();
                if (LOG.isDebugEnabled()) {
                    LOG.debug("findSimpleConnectionForSizeTwo: simple connection ports found [{}] -> [{}]", m_xyPort.printTopology(), m_yxPort.printTopology());
                }
                return;
            }
            LOG.warn("findSimpleConnectionForSizeTwo: no simple connection ports found [{}] -> [{}]", m_xBridge.getNodeId(), m_yBridge.getNodeId());
        }

        // finally, if none found throw exception
        if (m_xyPort == null || m_yxPort == null) {
            throw new BridgeTopologyException("findSimpleConnection: no simple connection found", m_xBridge);
        }
    }


    // Once found
    // BridgePort findPortUsingSimpleConnectionAlg(BFT(Y), BFT(X), m_1, m_2, pxy)
    // m_1 belongs to FDB(pyx,Y) and FDB(px1,X)
    // m_2 belongs to FDB(pyx,Y) and FDB(py2,Y)
    // m_3 belongs to TS(pyx,Y)
    private static BridgePort findPortUsingSimpleConnectionAlgorithm(
            BridgeForwardingTable bftA,
            BridgeForwardingTable bftB,
            BridgePort pba) {

        for (String mac : bftA.getBftMacs()) {
            if (!bftB.getBftMacs().contains(mac) || pba.equals(bftB.getMactoport().get(mac))) {
                continue;
            }
            return bftA.getMactoport().get(mac);
        }
        return null;
    }

    // if exists m_1 and m_2, p1 and p2 on Y pxy on X :      m_1 belongs to BFT(py1,Y) BFT(pxy,X)
    //                                                       m_2 belongs to BFT(py2,Y) BFT(pxy,X)
    private static BridgePort findPortUsingSimpleConnectionAlgorithm(
            BridgeForwardingTable bftA,
            BridgeForwardingTable bftB) {

        Set<String> commonLearnedMacs = new HashSet<>(bftA.getBftMacs());
        commonLearnedMacs.retainAll(new HashSet<>(bftB.getBftMacs()));
        String[] array = commonLearnedMacs.toArray(new String[0]);

        for (int i=0; i < array.length; i++) {
            BridgePort pab = bftA.getMactoport().get(array[i]);
            BridgePort pb1 = bftB.getMactoport().get(array[i]);
            for (int j=i+1; j < array.length; j++) {
                if (bftB.getMactoport().get(array[j]).getBridgePort().intValue() == pb1.getBridgePort().intValue()) {
                    continue;
                }
                if (bftA.getMactoport().get(array[j]).getBridgePort().intValue() == pab.getBridgePort().intValue()) {
                    return pab;
                }
            }
        }
        return null;
    }

    private static BridgePort findPortUsingBridgeIdentifiers(BridgeForwardingTable bft, Set<String> identifiers) {
        for (String mac: identifiers) {
            if (bft.getMactoport().containsKey(mac)) {
                return bft.getMactoport().get(mac);
            }
        }
        return null;
    }

    public static BridgeSimpleConnection create(BridgeForwardingTable xBridge,
                                                BridgeForwardingTable yBridge) {
        return new BridgeSimpleConnection(xBridge, yBridge);
        
    }

    @Override
    public String printTopology() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("simple connection: [");
        if (m_xyPort != null) {
            stringBuilder.append(m_xyPort.printTopology());
        } else {
            stringBuilder.append("null");
        }
        stringBuilder.append("], <--> [");
        if (m_yxPort != null) {
            stringBuilder.append(m_yxPort.printTopology());
        } else {
            stringBuilder.append("null");
        }
       stringBuilder.append("]");
       return stringBuilder.toString();
    }

}