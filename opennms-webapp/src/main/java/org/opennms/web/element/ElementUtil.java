//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2009 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// 2009 Aug 28: Restore search and display capabilities for non-ip interfaces
// 2007 Jun 02: Move map initialization into their own methods, use
//              Spring's Assert class for testing method arguments,
//              and add methods for working with nodes. - dj@opennms.org
// 2004 Jan 06: added support for STATUS_SUSPEND and STATUS_RESUME
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//

package org.opennms.web.element;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.opennms.web.MissingParameterException;
import org.springframework.util.Assert;

public class ElementUtil extends Object {
    /**
     * Do not use directly. Call {@link #getNodeStatusMap 
     * getInterfaceStatusMap} instead.
     */
    private static Map<Character, String> m_nodeStatusMap;

    /**
     * Do not use directly. Call {@link #getInterfaceStatusMap 
     * getInterfaceStatusMap} instead.
     */
    private static Map<Character, String> m_interfaceStatusMap;

    /**
     * Do not use directly. Call {@link #getSnmpInterfaceStatusMap 
     * getInterfaceStatusMap} instead.
     */
    private static Map<Character, String> m_interfaceSnmpStatusMap;

    /**
     * Do not use directly. Call {@link #getServiceStatusMap 
     * getServiceStatusMap} instead.
     */
    private static Map<Character, String> m_serviceStatusMap;

    /** Returns the interface status map, initializing a new one if necessary. */
    protected static Map<Character, String> getNodeStatusMap() {
        if (m_nodeStatusMap == null) {
            initNodeStatusMap();
        }

        return m_nodeStatusMap;
    }

    private synchronized static void initNodeStatusMap() {
        Map<Character, String> map = new HashMap<Character, String>();
        map.put(new Character('A'), "Active");
        map.put(new Character(' '), "Unknown");
        map.put(new Character('D'), "Deleted");
        
        m_nodeStatusMap = map;
    }

    /** Return the human-readable name for a node's status, may be null. */
    public static String getNodeStatusString(Node node) {
        Assert.notNull(node, "node argument cannot be null");

        return getNodeStatusString(node.getNodeType());
    }

    /**
     * Return the human-readable name for a interface status character, may be
     * null.
     */
    public static String getNodeStatusString(char c) {
        return getNodeStatusMap().get(new Character(c));
    }
    
    /** Returns the interface status map, initializing a new one if necessary. */
    protected static Map<Character, String> getInterfaceStatusMap() {
        if (m_interfaceStatusMap == null) {
            initInterfaceStatusMap();
        }

        return m_interfaceStatusMap;
    }

    private synchronized static void initInterfaceStatusMap() {
        Map<Character, String> map = new HashMap<Character, String>();
        map.put(new Character('M'), "Managed");
        map.put(new Character('U'), "Unmanaged");
        map.put(new Character('D'), "Deleted");
        map.put(new Character('F'), "Forced Unmanaged");
        map.put(new Character('N'), "Not Monitored");
        
        m_interfaceStatusMap = map;
    }

    /** Returns the interface status map, initializing a new one if necessary. */
    protected static Map<Character, String> getSnmpInterfaceStatusMap() {
        if (m_interfaceSnmpStatusMap == null) {
            initSnmpInterfaceStatusMap();
        }

        return m_interfaceSnmpStatusMap;
    }

    private synchronized static void initSnmpInterfaceStatusMap() {
        Map<Character, String> map = new HashMap<Character, String>();
        map.put(new Character('P'), "Polled");
        map.put(new Character('N'), "Not Monitored");
        
        m_interfaceSnmpStatusMap = map;
    }

    /** Return the human-readable name for a interface's status, may be null. */
    public static String getInterfaceStatusString(Interface intf) {
        Assert.notNull(intf, "intf argument cannot be null");

        return getInterfaceStatusString(intf.isManagedChar());
    }

    /**
     * Return the human-readable name for a interface status character, may be
     * null.
     */
    public static String getInterfaceStatusString(char c) {
        Map<Character, String> statusMap = getInterfaceStatusMap();
        return statusMap.get(new Character(c));
    }

    /** Return the human-readable name for a snmp interface's status, may be null. */
    public static String getSnmpInterfaceStatusString(Interface intf) {
        Assert.notNull(intf, "intf argument cannot be null");

        return getSnmpInterfaceStatusString(intf.isSnmpPollChar());
    }

    /**
     * Return the human-readable name for a interface status character, may be
     * null.
     */
    public static String getSnmpInterfaceStatusString(char c) {
        Map<Character, String> statusMap = getSnmpInterfaceStatusMap();
        return statusMap.get(new Character(c));
    }

    /** Returns the service status map, initializing a new one if necessary. */
    protected static Map<Character, String> getServiceStatusMap() {
        if (m_serviceStatusMap == null) {
            initServiceStatusMap();
        }

        return m_serviceStatusMap;
    }

    private synchronized static void initServiceStatusMap() {
        Map<Character, String> map = new HashMap<Character, String>();

        map.put(new Character('A'), "Managed");
        map.put(new Character('U'), "Unmanaged");
        map.put(new Character('D'), "Deleted");
        map.put(new Character('F'), "Forced Unmanaged");
        map.put(new Character('N'), "Not Monitored");
        map.put(new Character('R'), "Rescan to Resume");
        map.put(new Character('S'), "Rescan to Suspend");
        map.put(new Character('X'), "Remotely Monitored");

        m_serviceStatusMap = map;
    }

    /** Return the human-readable name for a service's status, may be null. */
    public static String getServiceStatusString(Service svc) {
        Assert.notNull(svc, "svc argument cannot be null");

        return getServiceStatusString(svc.getStatus());
    }

    /**
     * Return the human-readable name for a service status character, may be
     * null.
     */
    public static String getServiceStatusString(char c) {
        Map<Character, String> statusMap = getServiceStatusMap();
        return statusMap.get(new Character(c));
    }
    
    public static boolean hasLocallyMonitoredServices(Service[] svcs) {
        for(Service svc : svcs) {
            char status = svc.getStatus();
            if (status != 'X') {
                return true;
            }
        }
        return false;
    }

    public static final int DEFAULT_TRUNCATE_THRESHOLD = 28;

    public static String truncateLabel(String label) {
        return truncateLabel(label, DEFAULT_TRUNCATE_THRESHOLD);
    }

    public static String truncateLabel(String label, int truncateThreshold) {
        Assert.notNull(label, "label argument cannot be null");
        Assert.isTrue(truncateThreshold >= 3, "Cannot take a truncate position less than 3 (truncateThreshold is " + truncateThreshold + ")");

        String shortLabel = label;

        if (label.length() > truncateThreshold) {
            shortLabel = label.substring(0, truncateThreshold - 3) + "...";
        }

        return shortLabel;
    }
    

    public static Node getNodeByParams(HttpServletRequest request)
            throws ServletException, SQLException {
        return getNodeByParams(request, "node");
    }
    
    public static Node getNodeByParams(HttpServletRequest request,
            String nodeIdParam) throws ServletException, SQLException {
        if (request.getParameter(nodeIdParam) == null) {
            throw new MissingParameterException(nodeIdParam, new String[] { "node" });
        }

        String nodeIdString = request.getParameter(nodeIdParam);

        int nodeId;

        try {
            nodeId = Integer.parseInt(nodeIdString);
        } catch (NumberFormatException e) {
            throw new ElementIdNotFoundException("Wrong data type for \""
                    + nodeIdParam + "\", should be integer", nodeIdString, 
                    "node", "element/node.jsp", "node", "element/nodeList.htm");
        }

        Node node = NetworkElementFactory.getNode(nodeId);

        if (node == null) {
            throw new ElementNotFoundException("No such node in database", "node", "element/node.jsp", "node", "element/nodeList.htm");
        }
        
        return node;
}

    
    public static Interface getInterfaceByParams(HttpServletRequest request)
            throws ServletException, SQLException {
        return getInterfaceByParams(request, "ipinterfaceid", "node", "intf", "ifindex");
    }
    
    public static Interface getInterfaceByParams(HttpServletRequest request,
                                                 String ipInterfaceIdParam,
                                                 String nodeIdParam,
                                                 String ipAddrParam,
                                                 String ifIndexParam)
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

                intf = NetworkElementFactory.getInterface(ipInterfaceId);
        } else {
            String nodeIdString = request.getParameter(nodeIdParam);
            String ipAddr = request.getParameter(ipAddrParam);
            String ifIndexString = request.getParameter(ifIndexParam);
            
            int nodeId;
            int ifIndex = -1;

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

            if (ifIndexString != null) {
                try {
                    ifIndex = Integer.parseInt(ifIndexString);
                } catch (NumberFormatException e) {
                    throw new ElementIdNotFoundException("Wrong data type for \""
                            + ifIndexParam + "\", should be integer",
                            ifIndexString, "interface");
                }
            }

            if (ifIndex != -1) {
                intf = NetworkElementFactory.getInterface(nodeId, ipAddr,
                                                          ifIndex);
            } else {
                intf = NetworkElementFactory.getInterface(nodeId, ipAddr);
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
     * @param HttpServletRequest request
     * 
     * @return Interface
     * 
     * @throws ServletException, SQLException
     */
    public static Interface getSnmpInterfaceByParams(HttpServletRequest request)
            throws ServletException, SQLException {
        return getSnmpInterfaceByParams(request, "node", "ifindex");
    }

    /**
     * Return interface from snmpinterface table given a servlet request, nodeId
     * param name and ifIndex param name. Intended for use with non-ip interfaces.
     * 
     * @param HttpServletRequest request
     * 
     * @param String nodeIdParam
     * 
     * @param String ifIndexParam
     * 
     * @return Interface
     * 
     * @throws ServletException, SQLException
     * 
     */
    public static Interface getSnmpInterfaceByParams(HttpServletRequest request,
                                                     String nodeIdParam,
                                                     String ifIndexParam)
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

        intf = NetworkElementFactory.getSnmpInterface(nodeId, ifIndex);

        if (intf == null) {
            throw new ElementNotFoundException("No such snmp interface in database for nodeId "
                                               + nodeIdString + " ifIndex " + ifIndexString, "snmpinterface");
        }

    return intf;
    }
    
    
    public static Service getServiceByParams(HttpServletRequest request)
            throws ServletException, SQLException {
        return getServiceByParams(request, "ifserviceid", "node", "intf",
                                  "service");
    }
    
    public static Service getServiceByParams(HttpServletRequest request,
                                             String ifServiceIdParam,
                                             String nodeIdParam,
                                             String ipAddrParam,
                                             String serviceIdParam)
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

                service = NetworkElementFactory.getService(ifServiceId);
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

                service = NetworkElementFactory.getService(nodeId, ipAddr,
                                                           serviceId);
        }

        if (service == null) {
            String ipAddr = request.getParameter(ipAddrParam);
            String serviceIdString = request.getParameter(serviceIdParam);
            throw new ElementNotFoundException("No such service in database for " + ipAddr +
                                               " with service ID " +serviceIdString, "service");
        }
        
        return service;
    }
    
    public static Service[] getServicesOnNodeByParams(HttpServletRequest request, int serviceId) throws SQLException {
    	Service[] services;
    	int nodeId;
    	
    	try {
    		nodeId = Integer.parseInt(request.getParameter("node"));
    	} catch (NumberFormatException nfe) {
    		throw new ElementIdNotFoundException("Wrong type for parameter \"node\" (should be integer)",
    					request.getParameter("node"), "node", "element/node.jsp", "node", "element/nodeList.jsp");
    	}
    	services = NetworkElementFactory.getServicesOnNode(nodeId, serviceId);
    	return services;
    }
    
    public static boolean isRouteInfoNodeByParams(HttpServletRequest request) throws SQLException {
    	int nodeId;
    	
    	try {
    		nodeId = Integer.parseInt(request.getParameter("node"));
    	} catch (NumberFormatException nfe) {
    		throw new ElementIdNotFoundException("Wrong type for parameter \"node\" (should be integer)",
    					request.getParameter("node"), "node", "element/node.jsp", "node", "element/nodeList.jsp");
    	}
    	return NetworkElementFactory.isRouteInfoNode(nodeId);
    }
    
    @SuppressWarnings("unused")
    private static String encodeUrl(String in) {
    	String out = "";
		try {
			out = URLEncoder.encode(in, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// ignore
		}
		return out;
    }
    

    /** Private constructor so this class cannot be instantiated. */
    private ElementUtil() {
    }

}
