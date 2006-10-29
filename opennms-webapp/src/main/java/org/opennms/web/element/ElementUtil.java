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

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.opennms.web.MissingParameterException;

public class ElementUtil extends Object {
    /**
     * Do not use directly. Call {@link #getInterfaceStatusMap 
     * getInterfaceStatusMap} instead.
     */
    private static HashMap<Character, String> m_interfaceStatusMap;

    /**
     * Do not use directly. Call {@link #getServiceStatusMap 
     * getServiceStatusMap} instead.
     */
    private static HashMap<Character, String> m_serviceStatusMap;

    /** Returns the interface status map, initializing a new one if necessary. */
    protected static Map<Character, String> getInterfaceStatusMap() {
        if (m_interfaceStatusMap == null) {
            synchronized (ElementUtil.class) {
                m_interfaceStatusMap = new HashMap<Character, String>();
                m_interfaceStatusMap.put(new Character('M'), "Managed");
                m_interfaceStatusMap.put(new Character('U'), "Unmanaged");
                m_interfaceStatusMap.put(new Character('D'), "Deleted");
                m_interfaceStatusMap.put(new Character('F'), "Forced Unmanaged");
                m_interfaceStatusMap.put(new Character('N'), "Not Monitored");
            }
        }

        return (m_interfaceStatusMap);
    }

    /** Return the human-readable name for a interface's status, may be null. */
    public static String getInterfaceStatusString(Interface intf) {
        if (intf == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

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

    /** Returns the service status map, initializing a new one if necessary. */
    protected static Map<Character, String> getServiceStatusMap() {
        if (m_serviceStatusMap == null) {
            synchronized (ElementUtil.class) {
                m_serviceStatusMap = new HashMap<Character, String>();

                m_serviceStatusMap.put(new Character('A'), "Managed");
                m_serviceStatusMap.put(new Character('U'), "Unmanaged");
                m_serviceStatusMap.put(new Character('D'), "Deleted");
                m_serviceStatusMap.put(new Character('F'), "Forced Unmanaged");
                m_serviceStatusMap.put(new Character('N'), "Not Monitored");
                m_serviceStatusMap.put(new Character('R'), "Rescan to Resume");
                m_serviceStatusMap.put(new Character('S'), "Rescan to Suspend");
            }
        }

        return (m_serviceStatusMap);
    }

    /** Return the human-readable name for a service's status, may be null. */
    public static String getServiceStatusString(Service svc) {
        if (svc == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

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

    public static final int DEFAULT_TRUNCATE_THRESHOLD = 28;

    public static String truncateLabel(String label) {
        return truncateLabel(label, DEFAULT_TRUNCATE_THRESHOLD);
    }

    public static String truncateLabel(String label, int truncateThreshold) {
        if (label == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        if (truncateThreshold < 3) {
            throw new IllegalArgumentException("Cannot take a truncate position less than 3.");
        }

        String shortLabel = label;

        if (label.length() > truncateThreshold) {
            shortLabel = label.substring(0, truncateThreshold - 3) + "...";
        }

        return shortLabel;
    }
    
    
    public static Interface getInterfaceByParams(HttpServletRequest request)
            throws ServletException, SQLException {
        return getInterfaceByParams(request, "ipinterfaceid", "node", "intf",
                                    "ifindex");
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
                throw new ServletException("Wrong data type for \""
                                           + ipInterfaceIdParam + "\" "
                                           + "(value: \"" + ipInterfaceIdParam
                                           + "\"), should be integer", e);
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
                throw new ServletException("Wrong data type for \""
                                           + nodeIdParam + "\" "
                                           + "(value: \"" + nodeIdString
                                           + "\"), should be integer", e);
            }

            if (ifIndexString != null) {
                try {
                    ifIndex = Integer.parseInt(ifIndexString);
                } catch (NumberFormatException e) {
                    throw new ServletException("Wrong data type for \""
                                               + ifIndexParam + "\" "
                                               + "(value: \""
                                               + ifIndexString
                                               + "\"), should be integer", e);
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
                throw new ServletException("Wrong data type for \""
                                           + ifServiceIdParam + "\" "
                                           + "(value: \"" + ifServiceIdString
                                           + "\"), should be integer", e);
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
                throw new ServletException("Wrong data type for \""
                                           + nodeIdParam + "\" "
                                           + "(value: \"" + nodeIdString
                                           + "\"), should be integer", e);
            }
        
            try {
                serviceId = Integer.parseInt(serviceIdString);
            } catch (NumberFormatException e) {
                throw new ServletException("Wrong data type for \""
                                           + serviceIdParam + "\" "
                                           + "(value: \"" + serviceIdString
                                           + "\"), should be integer", e);
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
    

    /** Private constructor so this class cannot be instantiated. */
    private ElementUtil() {
    }

}
