//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
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

/**
 * <p>ElementUtil class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.6.12
 */
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
     * Do not use directly. Call {@link #getServiceStatusMap 
     * getServiceStatusMap} instead.
     */
    private static Map<Character, String> m_serviceStatusMap;

    /**
     * Returns the interface status map, initializing a new one if necessary.
     *
     * @return a java$util$Map object.
     */
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

    /**
     * Return the human-readable name for a node's status, may be null.
     *
     * @param node a {@link org.opennms.web.element.Node} object.
     * @return a {@link java.lang.String} object.
     */
    public static String getNodeStatusString(Node node) {
        Assert.notNull(node, "node argument cannot be null");

        return getNodeStatusString(node.getNodeType());
    }

    /**
     * Return the human-readable name for a interface status character, may be
     * null.
     *
     * @param c a char.
     * @return a {@link java.lang.String} object.
     */
    public static String getNodeStatusString(char c) {
        return getNodeStatusMap().get(new Character(c));
    }
    
    /**
     * Returns the interface status map, initializing a new one if necessary.
     *
     * @return a java$util$Map object.
     */
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
        Map<Character, String> statusMap = getInterfaceStatusMap();
        return statusMap.get(new Character(c));
    }

    /**
     * Returns the service status map, initializing a new one if necessary.
     *
     * @return a java$util$Map object.
     */
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
        Map<Character, String> statusMap = getServiceStatusMap();
        return statusMap.get(new Character(c));
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
     * @return a {@link org.opennms.web.element.Node} object.
     * @throws javax.servlet.ServletException if any.
     * @throws java.sql.SQLException if any.
     */
    public static Node getNodeByParams(HttpServletRequest request)
            throws ServletException, SQLException {
        return getNodeByParams(request, "node");
    }
    
    /**
     * <p>getNodeByParams</p>
     *
     * @param request a {@link javax.servlet.http.HttpServletRequest} object.
     * @param nodeIdParam a {@link java.lang.String} object.
     * @return a {@link org.opennms.web.element.Node} object.
     * @throws javax.servlet.ServletException if any.
     * @throws java.sql.SQLException if any.
     */
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
            //handle this WAY better, very awful
            throw new ServletException("No such node in database");
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
    public static Interface getInterfaceByParams(HttpServletRequest request)
            throws ServletException, SQLException {
        return getInterfaceByParams(request, "ipinterfaceid", "node", "intf",
                                    "ifindex");
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
            //handle this WAY better, very awful
            throw new ServletException("No such interface in database");
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
    public static Service getServiceByParams(HttpServletRequest request)
            throws ServletException, SQLException {
        return getServiceByParams(request, "ifserviceid", "node", "intf",
                                  "service");
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
            //handle this WAY better, very awful
            throw new ServletException("No such service in database");
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
    
    /**
     * <p>isRouteInfoNodeByParams</p>
     *
     * @param request a {@link javax.servlet.http.HttpServletRequest} object.
     * @return a boolean.
     * @throws java.sql.SQLException if any.
     */
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
