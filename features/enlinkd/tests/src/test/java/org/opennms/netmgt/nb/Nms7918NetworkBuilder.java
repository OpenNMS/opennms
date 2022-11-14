/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.nb;


import org.opennms.netmgt.model.NetworkBuilder;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsNode.NodeType;

public class Nms7918NetworkBuilder extends NmsNetworkBuilder {
    //NMS7918 only bridge discovery
    public static final String PE01_IP   = "10.25.19.1";
    public static final String PE01_NAME = "pe01";
    public static final String PE01_SYSOID = ".1.3.6.1.4.1.9.1.534";
    public static final String PE01_SNMP_RESOURCE = "classpath:/linkd/nms7918/"+PE01_NAME+".txt";

    public static final String ASW01_IP   = "10.25.19.2";
    public static final String ASW01_NAME = "asw01";
    public static final String ASW01_SYSOID = ".1.3.6.1.4.1.6486.800.1.1.2.1.10.1.1";
    public static final String ASW01_SNMP_RESOURCE = "classpath:/linkd/nms7918/"+ASW01_NAME+".txt";

    public static final String OSPESS01_IP   = "10.25.19.3";
    public static final String OSPESS01_NAME = "osp.ess01";
    public static final String OSPESS01_SYSOID = ".1.3.6.1.4.1.8072.3.2.10";
    public static final String OSPESS01_SNMP_RESOURCE = "classpath:/linkd/nms7918/"+OSPESS01_NAME+".txt";

    public static final String OSPWL01_IP   = "10.25.19.4";
    public static final String OSPWL01_NAME = "ospedale-wl1";
    public static final String OSPWL01_SYSOID = ".1.3.6.1.4.1.14988.1";
    public static final String OSPWL01_SNMP_RESOURCE = "classpath:/linkd/nms7918/"+OSPWL01_NAME+".txt";

    public static final String SAMASW01_IP   = "10.25.19.211";
    public static final String SAMASW01_NAME = "sam.asw01";
    public static final String SAMASW01_SYSOID = ".1.3.6.1.4.1.6486.800.1.1.2.2.4.1.1";
    public static final String SAMASW01_SNMP_RESOURCE = "classpath:/linkd/nms7918/"+SAMASW01_NAME+".txt";

    public static final String STCASW01_IP   = "10.25.19.216";
    public static final String STCASW01_NAME = "stc.asw01";
    public static final String STCASW01_SYSOID = ".1.3.6.1.4.1.6486.800.1.1.2.1.10.1.1";
    public static final String STCASW01_SNMP_RESOURCE = "classpath:/linkd/nms7918/"+STCASW01_NAME+".txt";

    public OnmsNode getPe01() {
        NetworkBuilder nb = getNetworkBuilder();

        nb.addNode(PE01_NAME).setForeignSource("linkd").setForeignId(PE01_NAME).setSysObjectId(PE01_SYSOID).setType(NodeType.ACTIVE);
        nb.addInterface(PE01_IP).setIsSnmpPrimary("P").setIsManaged("M").setNetMask("255.255.255.0");
        return nb.getCurrentNode();
    }

    public OnmsNode getAsw01() {
        NetworkBuilder nb = getNetworkBuilder();
        nb.addNode(ASW01_NAME).setForeignSource("linkd").setForeignId(ASW01_NAME).setSysObjectId(ASW01_SYSOID).setType(NodeType.ACTIVE);
        nb.addInterface(ASW01_IP).setIsSnmpPrimary("P").setIsManaged("M");
        return nb.getCurrentNode();
    }

    public OnmsNode getOspss01() {
        NetworkBuilder nb = getNetworkBuilder();
        nb.addNode(OSPESS01_NAME).setForeignSource("linkd").setForeignId(OSPESS01_NAME).setSysObjectId(OSPESS01_SYSOID).setType(NodeType.ACTIVE);
        nb.addInterface(OSPESS01_IP).setIsSnmpPrimary("P").setIsManaged("M");
        return nb.getCurrentNode();
    }

    public OnmsNode getOspwl01() {
        NetworkBuilder nb = getNetworkBuilder();
        nb.addNode(OSPWL01_NAME).setForeignSource("linkd").setForeignId(OSPWL01_NAME).setSysObjectId(OSPWL01_SYSOID).setType(NodeType.ACTIVE);
        nb.addInterface(OSPWL01_IP).setIsSnmpPrimary("P").setIsManaged("M");
        return nb.getCurrentNode();
    }

    public OnmsNode getSamasw01() {
        NetworkBuilder nb = getNetworkBuilder();
        nb.addNode(SAMASW01_NAME).setForeignSource("linkd").setForeignId(SAMASW01_NAME).setSysObjectId(SAMASW01_SYSOID).setType(NodeType.ACTIVE);
        nb.addInterface(SAMASW01_IP).setIsSnmpPrimary("P").setIsManaged("M");
        return nb.getCurrentNode();
    }

    public OnmsNode getStcasw01() {
        NetworkBuilder nb = getNetworkBuilder();
        nb.addNode(STCASW01_NAME).setForeignSource("linkd").setForeignId(STCASW01_NAME).setSysObjectId(STCASW01_SYSOID).setType(NodeType.ACTIVE);
        nb.addInterface(STCASW01_IP).setIsSnmpPrimary("P").setIsManaged("M");
        return nb.getCurrentNode();
    }

}
