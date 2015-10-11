/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
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

package org.opennms.web.element;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsNode.NodeType;
import org.opennms.web.servlet.MissingParameterException;
import org.springframework.util.Assert;

/**
 * <p>ElementUtil class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public abstract class ElementUtil {
    /**
     * Do not use directly. Call {@link #getNodeStatusMap 
     * getInterfaceStatusMap} instead.
     */
    private static final EnumMap<NodeType, String> m_nodeStatusMap;

    /**
     * Do not use directly. Call {@link #getInterfaceStatusMap 
     * getInterfaceStatusMap} instead.
     */
    private static final Map<Character, String> m_interfaceStatusMap;

    /**
     * Do not use directly. Call {@link #getSnmpInterfaceStatusMap 
     * getInterfaceStatusMap} instead.
     */
    private static final Map<Character, String> m_interfaceSnmpStatusMap;

    /**
     * Do not use directly. Call {@link #getServiceStatusMap 
     * getServiceStatusMap} instead.
     */
    private static final Map<Character, String> m_serviceStatusMap;

    static {
        m_nodeStatusMap = new EnumMap<NodeType,String>(NodeType.class);
        m_nodeStatusMap.put(NodeType.ACTIVE, "Active");
        m_nodeStatusMap.put(NodeType.UNKNOWN, "Unknown");
        m_nodeStatusMap.put(NodeType.DELETED, "Deleted");
        
        m_interfaceStatusMap = new HashMap<Character, String>();
        m_interfaceStatusMap.put('M', "Managed");
        m_interfaceStatusMap.put('U', "Unmanaged");
        m_interfaceStatusMap.put('D', "Deleted");
        m_interfaceStatusMap.put('F', "Forced Unmanaged");
        m_interfaceStatusMap.put('N', "Not Monitored");
        
        m_interfaceSnmpStatusMap = new HashMap<Character, String>();
        m_interfaceSnmpStatusMap.put('P', "Polled");
        m_interfaceSnmpStatusMap.put('N', "Not Monitored");
        
        m_serviceStatusMap = new HashMap<Character, String>();
        m_serviceStatusMap.put('A', "Managed");
        m_serviceStatusMap.put('U', "Unmanaged");
        m_serviceStatusMap.put('D', "Deleted");
        m_serviceStatusMap.put('F', "Forced Unmanaged");
        m_serviceStatusMap.put('N', "Not Monitored");
        m_serviceStatusMap.put('R', "Rescan to Resume");
        m_serviceStatusMap.put('S', "Rescan to Suspend");
        m_serviceStatusMap.put('X', "Remotely Monitored");
        
        
    }

    static final String[] IFTYPES = new String[] {
        "&nbsp;",                     //0 (not supported)
        "other",                    //1
        "regular1822",              //2
        "hdh1822",                  //3
        "ddn-x25",                  //4
        "rfc877-x25",               //5
        "ethernetCsmacd",           //6
        "iso88023Csmacd",           //7
        "iso88024TokenBus",         //8
        "iso88025TokenRing",        //9
        "iso88026Man",              //10
        "starLan",                  //11
        "proteon-10Mbit",           //12
        "proteon-80Mbit",           //13
        "hyperchannel",             //14
        "fddi",                     //15
        "lapb",                     //16
        "sdlc",                     //17
        "ds1",                      //18
        "e1",                       //19
        "basicISDN",                //20
        "primaryISDN",              //21
        "propPointToPointSerial",   //22
        "ppp",                      //23
        "softwareLoopback",         //24
        "eon",                      //25
        "ethernet-3Mbit",           //26
        "nsip",                     //27
        "slip",                     //28
        "ultra",                    //29
        "ds3",                      //30
        "sip",                      //31
        "frame-relay",              //32
        "rs232",                    //33
        "para",                     //34
        "arcnet",                   //35
        "arcnetPlus",               //36
        "atm",                      //37
        "miox25",                   //38
        "sonet",                    //39
        "x25ple",                   //40
        "is0880211c",               //41
        "localTalk",                //42
        "smdsDxi",                  //43
        "frameRelayService",        //44
        "v35",                      //45
        "hssi",                     //46
        "hippi",                    //47
        "modem",                    //48
        "aa15",                     //49
        "sonetPath",                //50
        "sonetVT",                  //51
        "smdsIcip",                 //52
        "propVirtual",              //53
        "propMultiplexor",          //54
        "ieee80212",                //55
        "fibreChannel",             //56
        "hippiInterface",           //57
        "frameRelayInterconnect",   //58
        "aflane8023",               //59
        "aflane8025",               //60
        "cctEmul",                  //61
        "fastEther",                //62
        "isdn",                     //63
        "v11",                      //64
        "v36",                      //65
        "g703at64k",                //66
        "g703at2mb",                //67
        "qllc",                     //68
        "fastEtherFX",              //69
        "channel",                  //70
        "ieee80211",                //71
        "ibm370parChan",            //72
        "escon",                    //73
        "dlsw",                     //74
        "isdns",                    //75
        "isdnu",                    //76
        "lapd",                     //77
        "ipSwitch",                 //78
        "rsrb",                     //79
        "atmLogical",               //80
        "ds0",                      //81
        "ds0Bundle",                //82
        "bsc",                      //83
        "async",                    //84
        "cnr",                      //85
        "iso88025Dtr",              //86
        "eplrs",                    //87
        "arap",                     //88
        "propCnls",                 //89
        "hostPad",                  //90
        "termPad",                  //91
        "frameRelayMPI",            //92
        "x213",                     //93
        "adsl",                     //94
        "radsl",                    //95
        "sdsl",                     //96
        "vdsl",                     //97
        "iso88025CRFPInt",          //98
        "myrinet",                  //99
        "voiceEM",                  //100
        "voiceFXO",                 //101
        "voiceFXS",                 //102
        "voiceEncap",               //103
        "voiceOverIp",              //104
        "atmDxi",                   //105
        "atmFuni",                  //106
        "atmIma",                   //107
        "pppMultilinkBundle",       //108
        "ipOverCdlc",               //109
        "ipOverClaw",               //110
        "stackToStack",             //111
        "virtualIpAddress",         //112
        "mpc",                      //113
        "ipOverAtm",                //114
        "iso88025Fiber",            //115
        "tdlc",                     //116
        "gigabitEthernet",          //117
        "hdlc",                     //118
        "lapf",                     //119
        "v37",                      //120
        "x25mlp",                   //121
        "x25huntGroup",             //122
        "trasnpHdlc",               //123
        "interleave",               //124
        "fast",                     //125
        "ip",                       //126
        "docsCableMaclayer",        //127
        "docsCableDownstream",      //128
        "docsCableUpstream",        //129
        "a12MppSwitch",             //130
        "tunnel",                   //131
        "coffee",                   //132
        "ces",                      //133
        "atmSubInterface",          //134
        "l2vlan",                   //135
        "l3ipvlan",                 //136
        "l3ipxvlan",                //137
        "digitalPowerline",         //138
        "mediaMailOverIp",          //139
        "dtm",                      //140
        "dcn",                      //141
        "ipForward",                //142
        "msdsl",                    //143
        "ieee1394",                 //144
        "if-gsn",                   //145
        "dvbRccMacLayer",           //146
        "dvbRccDownstream",         //147
        "dvbRccUpstream",           //148
        "atmVirtual",               //149
        "mplsTunnel",               //150
        "srp",                      //151
        "voiceOverAtm",             //152
        "voiceOverFrameRelay",      //153
        "idsl",                     //154
        "compositeLink",            //155
        "ss7SigLink",               //156
        "propWirelessP2P",          //157
        "frForward",                //158
        "rfc1483",                  //159
        "usb",                      //160
        "ieee8023adLag",            //161
        "bgppolicyaccounting",      //162
        "frf16MfrBundle",           //163
        "h323Gatekeeper",           //164
        "h323Proxy",                //165
        "mpls",                     //166
        "mfSigLink",                //167
        "hdsl2",                    //168
        "shdsl",                    //169
        "ds1FDL",                   //170
        "pos",                      //171
        "dvbAsiIn",                 //172
        "dvbAsiOut",                //173
        "plc",                      //174
        "nfas",                     //175
        "tr008",                    //176
        "gr303RDT",                 //177
        "gr303IDT",                 //178
        "isup",                     //179
        "propDocsWirelessMaclayer",      //180
        "propDocsWirelessDownstream",    //181
        "propDocsWirelessUpstream",      //182
        "hiperlan2",                //183
        "propBWAp2Mp",              //184
        "sonetOverheadChannel",     //185
        "digitalWrapperOverheadChannel", //186
        "aal2",                     //187
        "radioMAC",                 //188
        "atmRadio",                 //189
        "imt",                      //190
        "mvl",                      //191
        "reachDSL",                 //192
        "frDlciEndPt",              //193
        "atmVciEndPt",              //194
        "opticalChannel",           //195
        "opticalTransport",         //196
        "propAtm",                  //197
        "voiceOverCable",           //198
        "infiniband",               //199
        "teLink",                   //200
        "q2931",                    //201
        "virtualTg",                //202
        "sipTg",                    //203
        "sipSig",                   //204
        "docsCableUpstreamChannel", //205
        "econet",                   //206
        "pon155",                   //207
        "pon622",                   //208
        "bridge",                   //209
        "linegroup",                //210
        "voiceEMFGD",               //211
        "voiceFGDEANA",             //212
        "voiceDID",                 //213
        "mpegTransport",            //214
        "sixToFour",                //215
        "gtp",                      //216
        "pdnEtherLoop1",            //217
        "pdnEtherLoop2",            //218
        "opticalChannelGroup",      //219
        "homepna",                  //220
        "gfp",                      //221
        "ciscoISLvlan",             //222
        "actelisMetaLOOP",          //223
        "fcipLink",                 //224
        "rpr",                      //225
        "qam",                      //226
        "lmp",                      //227
        "cblVectaStar",             //228
        "docsCableMCmtsDownstream", //229
        "adsl2",                    //230
        "macSecControlledIF",       //231
        "macSecUncontrolledIF",     //232
        "aviciOpticalEther",        //233
        "atmbond",                  //234
        "voiceFGDOS",               //235
        "mocaVersion1",             //236
        "ieee80216WMAN",            //237
        "adsl2plus",                //238
        "dvbRcsMacLayer",           //239
        "dvbTdm",                   //240
        "dvbRcsTdma",               //241
        "x86Laps",                  //242
        "wwanPP",                   //243
        "wwanPP2",                  //244
        "voiceEBS",                 //245
        "ifPwType",                 //246
        "ilan",                     //247
        "pip",                      //248
        "aluELP",                   //249
        "gpon",                     //250
      };
    
     static final String[] OPER_ADMIN_STATUS = new String[] {
        "&nbsp;",          //0 (not supported)
        "Up",              //1
        "Down",            //2
        "Testing",         //3
        "Unknown",         //4
        "Dormant",         //5
        "NotPresent",      //6
        "LowerLayerDown"   //7
      };
      
     static final String[] IP_ROUTE_TYPE = new String[] {
    	    "&nbsp;",         //0 (not supported)
    	    "Other",          //1
    	    "Invalid",        //2
    	    "Direct",         //3
    	    "Indirect",       //4
    	  };

    static final String[] IP_ROUTE_PROTO = new String[] {
    	    "&nbsp;",         //0 (not supported)
    	    "Other",          //1
    	    "Local",          //2
    	    "Netmgmt",        //3
    	    "icmp",           //4
    	    "egp",            //5
    	    "ggp",            //6
    	    "hello",          //7
    	    "rip",            //8
    	    "is-is",          //9
    	    "es-is",          //10
    	    "CiscoIGRP",      //11
    	    "bbnSpfIgp",      //12
    	    "ospf",           //13
    	    "bgp",            //14
    	  };

      public static String getIpRouteProtocolString(int iprouteprotocol) {
    	  if (IP_ROUTE_PROTO.length > iprouteprotocol)
    	  return IP_ROUTE_PROTO[iprouteprotocol];
    	  return IP_ROUTE_PROTO[0];
      }

      public static String getIpRouteTypeString(int iproutetype) {
    	  if (IP_ROUTE_TYPE.length > iproutetype)
    	  return IP_ROUTE_TYPE[iproutetype];
    	  return IP_ROUTE_TYPE[0];
      }

      public static String getIfStatusString(int ifStatusNum) {
          if (ifStatusNum < OPER_ADMIN_STATUS.length) {
              return OPER_ADMIN_STATUS[ifStatusNum];
          } else {
              return "Unknown (" + ifStatusNum + ")";
          }
      }
      
    /**
     * Return the human-readable name for a interface type, should never be null.
     *
     * @param int ifTypeNum.
     * @return a {@link java.lang.String} object.
     */
    public static String getIfTypeString(int ifTypeNum) {
        if (ifTypeNum < IFTYPES.length) {
            return IFTYPES[ifTypeNum];
        } else {
            return "Unknown (" + ifTypeNum + ")";
        }
    }

    /**
     * Return the human-readable name for a node's status, may be null.
     *
     * @param node a {@link OnmsNode} object.
     * @return a {@link java.lang.String} object.
     */
    public static String getNodeStatusString(OnmsNode node) {
        Assert.notNull(node, "node argument cannot be null");

        return getNodeStatusString(node.getType());
    }

    /**
     * Return the human-readable name for a interface status character, may be
     * null.
     *
     * @param c a char.
     * @return a {@link java.lang.String} object.
     */
    public static String getNodeStatusString(NodeType c) {
        return m_nodeStatusMap.get(c);
    }
    
    /**
     * Return the human-readable name for a interface's status, may be null.
     *
     * @param intf a {@link org.opennms.web.element.Interface} object.
     * @return a {@link java.lang.String} object.
     */
    public static String getInterfaceStatusString(Interface intf) {
        Assert.notNull(intf, "intf argument cannot be null");

        return getInterfaceStatusString(intf.isManagedChar());
    }

    /**
     * Return the human-readable name for a interface status character, may be
     * null.
     *
     * @param c a char.
     * @return a {@link java.lang.String} object.
     */
    public static String getInterfaceStatusString(char c) {
        return m_interfaceStatusMap.get(c);
    }

    /**
     * Return the human-readable name for an SNMP interface's status, may be null.
     *
     * @param intf a {@link org.opennms.web.element.Interface} object.
     * @return a {@link java.lang.String} object.
     */
    public static String getSnmpInterfaceStatusString(Interface intf) {
        Assert.notNull(intf, "intf argument cannot be null");

        return getSnmpInterfaceStatusString(intf.isSnmpPollChar());
    }

    /**
     * Return the human-readable name for a interface status character, may be
     * null.
     *
     * @param c a char.
     * @return a {@link java.lang.String} object.
     */
    public static String getSnmpInterfaceStatusString(char c) {
        return m_interfaceSnmpStatusMap.get(c);
    }

    /**
     * Return the human-readable name for a service's status, may be null.
     *
     * @param svc a {@link org.opennms.web.element.Service} object.
     * @return a {@link java.lang.String} object.
     */
    public static String getServiceStatusString(Service svc) {
        Assert.notNull(svc, "svc argument cannot be null");

        return getServiceStatusString(svc.getStatus());
    }

    /**
     * Return the human-readable name for a service status character, may be
     * null.
     *
     * @param c a char.
     * @return a {@link java.lang.String} object.
     */
    public static String getServiceStatusString(char c) {
        return m_serviceStatusMap.get(c);
    }
    
    /**
     * <p>hasLocallyMonitoredServices</p>
     *
     * @param svcs an array of {@link org.opennms.web.element.Service} objects.
     * @return a boolean.
     */
    public static boolean hasLocallyMonitoredServices(Service[] svcs) {
        for(Service svc : svcs) {
            char status = svc.getStatus();
            if (status != 'X') {
                return true;
            }
        }
        return false;
    }

    /** Constant <code>DEFAULT_TRUNCATE_THRESHOLD=28</code> */
    public static final int DEFAULT_TRUNCATE_THRESHOLD = 28;

    /**
     * <p>truncateLabel</p>
     *
     * @param label a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    public static String truncateLabel(String label) {
        return truncateLabel(label, DEFAULT_TRUNCATE_THRESHOLD);
    }

    /**
     * <p>truncateLabel</p>
     *
     * @param label a {@link java.lang.String} object.
     * @param truncateThreshold a int.
     * @return a {@link java.lang.String} object.
     */
    public static String truncateLabel(String label, int truncateThreshold) {
        Assert.notNull(label, "label argument cannot be null");
        Assert.isTrue(truncateThreshold >= 3, "Cannot take a truncate position less than 3 (truncateThreshold is " + truncateThreshold + ")");

        String shortLabel = label;

        if (label.length() > truncateThreshold) {
            shortLabel = label.substring(0, truncateThreshold - 3) + "...";
        }

        return shortLabel;
    }
    

    /**
     * <p>getNodeByParams</p>
     *
     * @param request a {@link javax.servlet.http.HttpServletRequest} object.
     * @return a {@link OnmsNode} object.
     * @throws javax.servlet.ServletException if any.
     * @throws java.sql.SQLException if any.
     */
    public static OnmsNode getNodeByParams(HttpServletRequest request, ServletContext servletContext)
            throws ServletException, SQLException {
        return getNodeByParams(request, "node", servletContext);
    }
    
    /**
     * <p>getNodeByParams</p>
     *
     * @param request a {@link javax.servlet.http.HttpServletRequest} object.
     * @param nodeLookupParam a {@link java.lang.String} object.
     * @return a {@link OnmsNode} object.
     * @throws javax.servlet.ServletException if any.
     * @throws java.sql.SQLException if any.
     */
    public static OnmsNode getNodeByParams(HttpServletRequest request,
            String nodeLookupParam, ServletContext servletContext) throws ServletException, SQLException {
        if (request.getParameter(nodeLookupParam) == null) {
            throw new MissingParameterException(nodeLookupParam, new String[] { "node" });
        }

        String nodeLookupString = request.getParameter(nodeLookupParam);
        if (!nodeLookupString.contains(":")) {
            try {
                Integer.parseInt(nodeLookupString);
            } catch (NumberFormatException e) {
                throw new ElementIdNotFoundException("Wrong data type for \""
                        + nodeLookupString + "\", should be integer", nodeLookupString, 
                        "node", "element/node.jsp", "node", "element/nodeList.htm");
            }
        }

        OnmsNode node = NetworkElementFactory.getInstance(servletContext).getNode(nodeLookupString);

        if (node == null) {
            throw new ElementNotFoundException("No such node in database", "node", "element/node.jsp", "node", "element/nodeList.htm");
        }
        
        return node;
    }

	/**
     * <p>getInterfaceByParams</p>
     *
     * @param request a {@link javax.servlet.http.HttpServletRequest} object.
     * @return a {@link org.opennms.web.element.Interface} object.
     * @throws javax.servlet.ServletException if any.
     * @throws java.sql.SQLException if any.
     */
    public static Interface getInterfaceByParams(HttpServletRequest request, ServletContext servletContext)
            throws ServletException, SQLException {
        return getInterfaceByParams(request, "ipinterfaceid", "node", "intf", servletContext);
    }
    
    /**
     * <p>getInterfaceByParams</p>
     *
     * @param request a {@link javax.servlet.http.HttpServletRequest} object.
     * @param ipInterfaceIdParam a {@link java.lang.String} object.
     * @param nodeIdParam a {@link java.lang.String} object.
     * @param ipAddrParam a {@link java.lang.String} object.
     * @param ifIndexParam a {@link java.lang.String} object.
     * @return a {@link org.opennms.web.element.Interface} object.
     * @throws javax.servlet.ServletException if any.
     * @throws java.sql.SQLException if any.
     */
    public static Interface getInterfaceByParams(HttpServletRequest request,
                                                 String ipInterfaceIdParam,
                                                 String nodeIdParam,
                                                 String ipAddrParam,
                                                 ServletContext servletContext)
            throws ServletException, SQLException {
        Interface intf;
        
        if (request.getParameter(ipInterfaceIdParam) != null) {
            String ifServiceIdString = request.getParameter(ipInterfaceIdParam);
            
            int ipInterfaceId;
            
            try {
                ipInterfaceId = Integer.parseInt(ifServiceIdString);
            } catch (NumberFormatException e) {
                throw new ElementIdNotFoundException("Wrong data type for \""
                        + ipInterfaceIdParam + "\", should be integer",
                        ifServiceIdString, "service");
            }

            intf = NetworkElementFactory.getInstance(servletContext).getInterface(ipInterfaceId);
            if (intf !=  null && intf.getIfIndex() <= 0)
            	intf.m_ifIndex = NetworkElementFactory.getInstance(servletContext).getIfIndex(ipInterfaceId);
            	
        } else {
            String nodeIdString = request.getParameter(nodeIdParam);
            String ipAddr = request.getParameter(ipAddrParam);
            
            int nodeId;

            final String[] requiredParameters = new String[] {
                nodeIdParam,
                ipAddrParam
            };

            if (nodeIdString == null) {
                throw new MissingParameterException(nodeIdParam,
                                                    requiredParameters);
            }

            if (ipAddr == null) {
                throw new MissingParameterException(ipAddrParam,
                                                    requiredParameters);
            }

            try {
                nodeId = Integer.parseInt(nodeIdString);
            } catch (NumberFormatException e) {
                throw new ElementIdNotFoundException("Wrong data type for \""
                        + nodeIdParam + "\", should be integer",
                        nodeIdString, "node");
            }

            intf = NetworkElementFactory.getInstance(servletContext).getInterface(nodeId, ipAddr);
            if (intf != null && intf.getIfIndex() <= 0) {
            		intf.m_ifIndex = NetworkElementFactory.getInstance(servletContext).getIfIndex(nodeId, ipAddr);
            }
        }

        if (intf == null) {
            throw new ElementNotFoundException("No such interface in database", "interface", "element/interface.jsp", "ipinterfaceid", "element/interface.jsp");
        }
        
        return intf;
    }

    /**
     * Return interface from snmpinterface table given a servlet request.
     * Intended for use with non-ip interfaces.
     *
     * @return Interface
     * @throws javax.servlet.ServletException, SQLException
     * @param request a {@link javax.servlet.http.HttpServletRequest} object.
     * @throws java.sql.SQLException if any.
     */
    public static Interface getSnmpInterfaceByParams(HttpServletRequest request, ServletContext servletContext)
            throws ServletException, SQLException {
        return getSnmpInterfaceByParams(request, "node", "ifindex", servletContext);
    }

    /**
     * Return interface from snmpinterface table given a servlet request, nodeId
     * param name and ifIndex param name. Intended for use with non-ip interfaces.
     *
     * @return Interface
     * @throws javax.servlet.ServletException, SQLException
     * @param request a {@link javax.servlet.http.HttpServletRequest} object.
     * @param nodeIdParam a {@link java.lang.String} object.
     * @param ifIndexParam a {@link java.lang.String} object.
     * @throws java.sql.SQLException if any.
     */
    public static Interface getSnmpInterfaceByParams(HttpServletRequest request,
                                                     String nodeIdParam,
                                                     String ifIndexParam, ServletContext servletContext)
            throws ServletException, SQLException {
        Interface intf;

        String nodeIdString = request.getParameter(nodeIdParam);
        String ifIndexString = request.getParameter(ifIndexParam);

        int nodeId;
        int ifIndex;

        final String[] requiredParameters = new String[] {
            nodeIdParam,
            ifIndexParam
            };

        if (nodeIdString == null) {
            throw new MissingParameterException(nodeIdParam,
                    requiredParameters);
        }

        if (ifIndexString == null) {
            throw new MissingParameterException(ifIndexParam,
                    requiredParameters);
        }

        try {
            nodeId = Integer.parseInt(nodeIdString);
        } catch (NumberFormatException e) {
            throw new ElementIdNotFoundException("Wrong data type for \""
                    + nodeIdParam + "\", should be integer",
                    nodeIdString, "node");
        }

        try {
            ifIndex = Integer.parseInt(ifIndexString);
        } catch (NumberFormatException e) {
            throw new ElementIdNotFoundException("Wrong data type for \""
                    + ifIndexParam + "\", should be integer",
                    ifIndexString, "interface");
        }

        intf = NetworkElementFactory.getInstance(servletContext).getSnmpInterface(nodeId, ifIndex);

        if (intf == null) {
            throw new ElementNotFoundException("No such SNMP interface in database for nodeId "
                                               + nodeIdString + " ifIndex " + ifIndexString, "snmpinterface");
        }

    return intf;
    }
    
    
    /**
     * <p>getServiceByParams</p>
     *
     * @param request a {@link javax.servlet.http.HttpServletRequest} object.
     * @return a {@link org.opennms.web.element.Service} object.
     * @throws javax.servlet.ServletException if any.
     * @throws java.sql.SQLException if any.
     */
    public static Service getServiceByParams(HttpServletRequest request, ServletContext servletContext)
            throws ServletException, SQLException {
        return getServiceByParams(request, "ifserviceid", "node", "intf",
                                  "service", servletContext);
    }
    
    /**
     * <p>getServiceByParams</p>
     *
     * @param request a {@link javax.servlet.http.HttpServletRequest} object.
     * @param ifServiceIdParam a {@link java.lang.String} object.
     * @param nodeIdParam a {@link java.lang.String} object.
     * @param ipAddrParam a {@link java.lang.String} object.
     * @param serviceIdParam a {@link java.lang.String} object.
     * @return a {@link org.opennms.web.element.Service} object.
     * @throws javax.servlet.ServletException if any.
     * @throws java.sql.SQLException if any.
     */
    public static Service getServiceByParams(HttpServletRequest request,
                                             String ifServiceIdParam,
                                             String nodeIdParam,
                                             String ipAddrParam,
                                             String serviceIdParam,
                                             ServletContext servletContext)
            throws ServletException, SQLException {
        Service service;
        
        if (request.getParameter(ifServiceIdParam) != null) {
            String ifServiceIdString = request.getParameter(ifServiceIdParam);
            
            int ifServiceId;
            
            try {
                ifServiceId = Integer.parseInt(ifServiceIdString);
            } catch (NumberFormatException e) {
                throw new ElementIdNotFoundException("Wrong data type for \""
                        + ifServiceIdParam + "\", should be integer",
                        ifServiceIdString, "service");
            }

                service = NetworkElementFactory.getInstance(servletContext).getService(ifServiceId);
        } else {
            String nodeIdString = request.getParameter(nodeIdParam);
            String ipAddr = request.getParameter(ipAddrParam);
            String serviceIdString = request.getParameter(serviceIdParam);
            
            int nodeId;
            int serviceId;

            final String[] requiredParameters = new String[] {
                nodeIdParam,
                ipAddrParam,
                serviceIdParam
            };

            if (nodeIdString == null) {
                throw new MissingParameterException(nodeIdParam,
                                                    requiredParameters);
            }

            if (ipAddr == null) {
                throw new MissingParameterException(ipAddrParam,
                                                    requiredParameters);
            }

            if (serviceIdString == null) {
                throw new MissingParameterException(serviceIdParam,
                                                    requiredParameters);
            }

            try {
                nodeId = Integer.parseInt(nodeIdString);
            } catch (NumberFormatException e) {
                throw new ElementIdNotFoundException("Wrong data type for \""
                        + nodeIdParam + "\", should be integer",
                        nodeIdString, "node");
            }
        
            try {
                serviceId = Integer.parseInt(serviceIdString);
            } catch (NumberFormatException e) {
                throw new ElementIdNotFoundException("Wrong data type for \""
                        + serviceIdParam + "\", should be integer",
                        serviceIdString, "service");
            }

                service = NetworkElementFactory.getInstance(servletContext).getService(nodeId, ipAddr, serviceId);
        }

        if (service == null) {
            String ipAddr = request.getParameter(ipAddrParam);
            String serviceIdString = request.getParameter(serviceIdParam);
            throw new ElementNotFoundException("No such service in database for " + ipAddr +
                                               " with service ID " +serviceIdString, "service");
        }
        
        return service;
    }
    
    /**
     * <p>getServicesOnNodeByParams</p>
     *
     * @param request a {@link javax.servlet.http.HttpServletRequest} object.
     * @param serviceId a int.
     * @return an array of {@link org.opennms.web.element.Service} objects.
     * @throws java.sql.SQLException if any.
     */
    public static Service[] getServicesOnNodeByParams(HttpServletRequest request, int serviceId, ServletContext servletContext) throws SQLException {
    	Service[] services;
    	int nodeId;
    	
    	try {
    		nodeId = Integer.parseInt(request.getParameter("node"));
    	} catch (NumberFormatException nfe) {
    		throw new ElementIdNotFoundException("Wrong type for parameter \"node\" (should be integer)",
    					request.getParameter("node"), "node", "element/node.jsp", "node", "element/nodeList.jsp");
    	}
    	services = NetworkElementFactory.getInstance(servletContext).getServicesOnNode(nodeId, serviceId);
    	return services;
    }
    
    
    
    public static Service[] getServicesOnInterface(int nodeId, String ipAddr, ServletContext servletContext) throws java.sql.SQLException {

    	ServiceNameComparator m_serviceComparator = new ServiceNameComparator();
    	
        Service[] svcs = NetworkElementFactory.getInstance(servletContext).getServicesOnInterface(nodeId, ipAddr);
        
        if (svcs != null) {
            Arrays.sort(svcs, m_serviceComparator); 
        }
        
        return svcs;
    }
}
