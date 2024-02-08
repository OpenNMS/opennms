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
package org.opennms.netmgt.nb;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.opennms.netmgt.enlinkd.model.IpNetToMedia;
import org.opennms.netmgt.enlinkd.model.IpNetToMedia.IpNetToMediaType;
import org.opennms.netmgt.model.NetworkBuilder;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsNode.NodeType;
import org.opennms.netmgt.model.SnmpInterfaceBuilder;

public class Nms4930NetworkBuilder extends NmsNetworkBuilder {

    //NMS4943
    public static final String DLINK1_IP = "10.1.1.2";
    public static final String DLINK1_NAME = "dlink1";
    public static final String DLINK1_SNMP_RESOURCE = "classpath:/linkd/nms4930/dlink_DES-3026.properties";

    public static final String DLINK2_IP = "10.1.2.2";
    public static final String DLINK2_NAME = "dlink2";
    public static final String DLINK2_SNMP_RESOURCE = "classpath:/linkd/nms4930/dlink_DGS-3612G.properties";

    public OnmsNode getHost1() {
            NetworkBuilder nb = getNetworkBuilder();
            nb.addNode("host1").setForeignSource("linkd").setForeignId("host1").setType(NodeType.ACTIVE);
            SnmpInterfaceBuilder snmpbuilder = nb.addSnmpInterface(101).setIfName("eth0").setIfType(6).setPhysAddr("001e58a6aed7").setIfDescr("eth0");
            nb.addInterface("10.1.2.7",snmpbuilder.getSnmpInterface()).setIsSnmpPrimary("N").setIsManaged("M").setNetMask("255.255.255.0");
            return nb.getCurrentNode();
        }

        public IpNetToMedia getMac1() {

            IpNetToMedia at0 = new IpNetToMedia();
            at0.setSourceIfIndex(101);
            at0.setPhysAddress("001e58a6aed7");
            at0.setLastPollTime(at0.getCreateTime());
            at0.setSourceNode(getHost1());
            try {
                at0.setNetAddress(InetAddress.getByName("10.1.2.7"));
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            at0.setIpNetToMediaType(IpNetToMediaType.IPNETTOMEDIA_TYPE_DYNAMIC);
            at0.setNode(getHost1());
            return at0;
        }
        
        public OnmsNode getHost2() {
            NetworkBuilder nb = getNetworkBuilder();
            nb.addNode("host2").setForeignSource("linkd").setForeignId("host2").setType(NodeType.ACTIVE);
            nb.addInterface("10.1.2.6").setIsSnmpPrimary("N").setIsManaged("M");
            return nb.getCurrentNode();
        }

        public IpNetToMedia getMac2() {

        IpNetToMedia at = new IpNetToMedia();
            at.setSourceIfIndex(101);
            at.setPhysAddress("000ffeb10e26");
            at.setLastPollTime(at.getCreateTime());
            at.setSourceNode(getHost1());
            try {
                at.setNetAddress(InetAddress.getByName("10.1.2.6"));
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            at.setNode(getHost2());
            return at;

        }

        public OnmsNode getDlink1() {
            NetworkBuilder nb = getNetworkBuilder();

            nb.addNode(DLINK1_NAME).setForeignSource("linkd").setForeignId(DLINK1_NAME).setSysObjectId(".1.3.6.1.4.1.9.1.122").setType(NodeType.ACTIVE);
            SnmpInterfaceBuilder builderA = nb.addSnmpInterface(3).setIfType(6).setCollectionEnabled(true).setIfSpeed(100000000).setPhysAddr("c2007db90010");
            nb.addInterface(DLINK1_IP, builderA.getSnmpInterface()).setIsSnmpPrimary("P").setIsManaged("M").setNetMask("255.255.255.0");
            SnmpInterfaceBuilder builderB = nb.addSnmpInterface(1).setIfType(6).setCollectionEnabled(true).setIfSpeed(100000000).setPhysAddr("c2007db90000");
            nb.addInterface("10.1.2.1", builderB.getSnmpInterface()).setIsSnmpPrimary("S").setIsManaged("M").setNetMask("255.255.255.0");
            SnmpInterfaceBuilder builderC = nb.addSnmpInterface(2).setIfType(6).setCollectionEnabled(true).setIfSpeed(100000000).setPhysAddr("c2007db90001");
            nb.addInterface("10.1.3.1",builderC.getSnmpInterface()).setIsSnmpPrimary("S").setIsManaged("M").setNetMask("255.255.255.0");
            nb.addSnmpInterface(24).setIfType(6).setIfName("Fa0/24").setIfSpeed(100000000);
            return nb.getCurrentNode();
        }

    public OnmsNode getDlink2() {
        NetworkBuilder nb = getNetworkBuilder();
        nb.addNode(DLINK2_NAME).setForeignSource("linkd").setForeignId(DLINK2_NAME).setSysObjectId(".1.3.6.1.4.1.9.1.122").setType(NodeType.ACTIVE);
        SnmpInterfaceBuilder builderA = nb.addSnmpInterface(1).setIfType(6).setCollectionEnabled(true).setIfSpeed(100000000).setPhysAddr("c2017db90000");
        nb.addInterface(DLINK2_IP, builderA.getSnmpInterface()).setIsSnmpPrimary("P").setIsManaged("M").setNetMask("255.255.255.0");

        SnmpInterfaceBuilder builderB = nb.addSnmpInterface(2).setIfType(6).setCollectionEnabled(true).setIfSpeed(100000000).setPhysAddr("c2017db90001");
        nb.addInterface("10.1.5.1",builderB.getSnmpInterface()).setIsSnmpPrimary("S").setIsManaged("M").setNetMask("255.255.255.0");
        nb.addSnmpInterface(10).setIfType(6).setIfName("FastEthernet0/10").setIfSpeed(100000000);

        return nb.getCurrentNode();
    }

}
