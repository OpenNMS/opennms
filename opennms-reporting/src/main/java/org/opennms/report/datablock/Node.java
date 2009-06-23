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
// Tab Size = 8
//

package org.opennms.report.datablock;

import java.util.ArrayList;
import java.util.List;

/**
 * This class holds the interface which has service information and list of
 * outages for the service.
 * 
 * @author <A HREF="mailto:jacinta@oculan.com">Jacinta Remedios </A>
 * @author <A HREF="http://www.oculan.com">oculan.com </A>
 * 
 */
public class Node extends StandardNamedObject {
    /**
     * The log4j category used to log debug messages and statements.
     */
    //private static final String LOG4J_CATEGORY = "OpenNMS.Report";

    private static class InterfaceComparator {
        private String m_intfname;

        private InterfaceComparator(String intf) {
            m_intfname = intf;
        }

        static InterfaceComparator make(String name) {
            return new InterfaceComparator(name);
        }

        @SuppressWarnings("unused")
        static InterfaceComparator make(Service svc) {
            return new InterfaceComparator(svc.getName());
        }

        public boolean equals(Object o) {
            boolean rc = false;
            if (o != null) {
                if (o == this)
                    rc = true;
                else if (o instanceof Interface)
                    rc = m_intfname.equals(((Interface) o).getName());
                else if (o instanceof String)
                    rc = m_intfname.equals(o);
            }
            return rc;
        }
    }

    /**
     * List of outages.
     */
    private ArrayList<Interface> m_interfaces;

    /**
     * Flag indicating an Outage
     */
    private boolean m_hasOutage = false;

    /**
     * Total Outage
     */
    long m_downTime;

    /**
     * Total Window Time.
     */
    long m_totalWindow;

    /**
     * Total Business Outage
     */
    long m_busDownTime;

    /**
     * Total Window Time during business hours.
     */
    long m_busTotalWindow;

    /**
     * Percentage availability.
     */
    double m_percentAvail;

    /**
     * Percentage availability during business hours.
     */
    double m_percentBusAvail;

    /**
     * Number of Interface/Service combinations
     */
    int m_serviceCount;

    /**
     * Node identifier.
     */
    int m_nodeid = -1;

    /**
     * Default Constructor.
     */
    public Node() {
        m_interfaces = new ArrayList<Interface>();
        m_downTime = 0;
    }

    /**
     * Constructor that initialises the nodeid.
     */
    public Node(String name, int id) {
        m_nodeid = id;
        setName(name);
        m_interfaces = new ArrayList<Interface>();
        m_downTime = 0;
    }

    /**
     * Constructor that sets the name and the outages.
     * 
     * @param name
     *            Name of the service.
     * @param interfaces
     *            interfaces to be set for this node.
     * @param id
     *            node id.
     */
    public Node(String name, ArrayList<Interface> interfaces, int id) {
        m_nodeid = id;
        setName(name);
        m_interfaces = interfaces;
        m_downTime = 0;
    }

    /**
     * Constructor that sets the outages.
     * 
     * @param interfaces
     *            Interfaces for this node to be set.
     */
    public Node(ArrayList<Interface> interfaces) {
        m_interfaces = interfaces;
        m_downTime = 0;
    }

    /**
     * Indicates whether the node has outages.
     * 
     * @return True if has outages.
     */
    public boolean hasOutages() {
        return m_hasOutage;
    }

    /**
     * Returns the total outage on the node.
     * 
     * @return The down time.
     */
    public long getDownTime() {
        return m_downTime;
    }

    /**
     * Returns the total outage on the node during business hours.
     * 
     * @return The business hours down time.
     */
    public long getBusDownTime() {
        return m_busDownTime;
    }

    /**
     * Returns the percentage availability on the node.
     * 
     * @return The percentage availability
     */
    public double getPercentAvail() {
        return m_percentAvail;
    }

    /**
     * Returns the percentage availability on the node during business hours.
     * 
     * @return The percentage availability during business hours.
     */
    public double getPercentBusAvail() {
        return m_percentBusAvail;
    }

    /**
     * Returns the number of unique interface/service combinations.
     * 
     * @return The service count
     */
    public int getServiceCount() {
        int count = 0;
        for (Interface intf : m_interfaces) {
			if (intf != null) {
				count += intf.getServiceCount();
			}
		}
        m_serviceCount = count;
        return count;
    }

    /**
     * Returns the number of interfaces.
     * 
     * @return The interface count
     */
    public int getInterfaceCount() {
        if (m_interfaces != null)
            return m_interfaces.size();
        return 0;
    }

    /**
     * Returns the total window for this node during business hours.
     * 
     * @return The totals for the business hours window.
     */
    public long getBusTotalWindow() {
        return m_busTotalWindow;
    }

    /**
     * Returns the total window for this node.
     * 
     * @return The totals
     */
    public long getTotalWindow() {
        return m_totalWindow;
    }

    /**
     * Return the interfaces
     * 
     * @return A list of interfaces.
     */
    public ArrayList<Interface> getInterfaces() {
        return m_interfaces;
    }

    /**
     * Return the nodeid
     * 
     * @return This node's id.
     */
    public int getNodeID() {
        return m_nodeid;
    }

    /**
     * Returns the service affected count.
     * 
     * @return The number of affected services.
     */
    public int getServiceAffectCount() {
        int count = 0;
        for (Interface intf : m_interfaces) {
			if (intf != null) {
				count += intf.getServiceAffectCount();
			}
		}
        return count;
    }

    /**
     * Add Interface with interface name.
     * 
     * @param intfname
     *            interface name Checks if the interface with name intfname
     *            exists. If not, adds a new interface with name intfname.
     */
    public void addInterface(String intfname) {
        if (intfname == null)
            return;

        int ndx = m_interfaces.indexOf(InterfaceComparator.make(intfname));
        if (ndx == -1) {
            Interface intf = new Interface(intfname);
            m_interfaces.add(intf);
        }
    }

    /**
     * Add an Interface with interface and service name.
     * 
     * @param intfname
     *            interface name
     * @param service
     *            service name Checks if the interface with name intfname
     *            exists. If so, adds service to that interface. Otherwise, adds
     *            a new interface with name intfname and service.
     */
    public void addInterface(String intfname, String service) {
        if (intfname == null)
            return;
        int ndx = m_interfaces.indexOf(InterfaceComparator.make(intfname));
        if (ndx != -1) {
            Interface intf = (Interface) m_interfaces.get(ndx);
            if (service != null)
                intf.addService(service);
            return;
        }
        Interface intf = new Interface(intfname);
        if (service != null)
            intf.addService(service);
        m_interfaces.add(intf);
    }

    /**
     * Add Interface with interface name, service id, lost time.
     * 
     * @param intfname
     *            Interface name
     * @param service
     *            Service name
     * @param lost
     *            Lost time Checks if the interface with name intfname exists.
     *            If so, adds service to that interface. Otherwise, adds a new
     *            interface with name intfname and service, and adds an outage
     *            with losttime as lost.
     */
    public void addInterface(String intfname, String service, long lost) {
        int ndx = m_interfaces.indexOf(InterfaceComparator.make(intfname));
        if (ndx != -1) {
            Interface intf = (Interface) m_interfaces.get(ndx);
            if (service != null) {
                intf.addService(service, lost);
                m_hasOutage = true;
            }
            return;
        }
        Interface intf = new Interface(intfname);
        if (service != null) {
            intf.addService(service, lost);
            m_hasOutage = true;
        }
        m_interfaces.add(intf);
    }

    /**
     * Searches the list of interfaces and returns the interface object with
     * name intfname.
     * 
     * @return Interface with name intfname
     */
    public Interface getInterface(String intfname) {
        if (intfname == null)
            return null;

        int ndx = m_interfaces.indexOf(InterfaceComparator.make(intfname));
        if (ndx != -1) {
            Interface intf = (Interface) m_interfaces.get(ndx);
            return intf;
        }
        return null;
    }

    /**
     * Adds Interface with interface name, service id, lost time, regained time.
     * 
     * @param intfname
     *            Interface name
     * @param service
     *            Service name
     * @param lost
     *            Lost time
     * @param regained
     *            Regained time. Checks if the interface with name intfname
     *            exists. If so, adds service to that interface. Otherwise, adds
     *            a new interface with name intfname and service, and adds an
     *            outage with losttime as lost and regained time as regained.
     */
    public void addInterface(String intfname, String service, long lost, long regained) {
        if (intfname == null)
            return;

        int ndx = m_interfaces.indexOf(InterfaceComparator.make(intfname));
        if (ndx != -1) {
            Interface intf = (Interface) m_interfaces.get(ndx);
            intf.addService(service, lost, regained);
            m_hasOutage = true;
            return;
        }

        Interface intf = new Interface(intfname);
        if (service != null) {
            intf.addService(service, lost, regained);
            m_hasOutage = true;
        }
        m_interfaces.add(intf);
    }

    /**
     * Computes the availability of the node. The rolling window (in
     * milliseconds)
     * 
     * @param endTime
     *            End Time of the rolling window in milliseconds.
     * @return percentage availability of node for the last week.
     */
    public double getPercentAvail(long endTime, long rollingWindow) {
        double percent = 0;
        long outage = 0;
        int serviceCount = 0;

        if (m_interfaces != null && m_interfaces.size() > 0) {
        	for (Interface intf : m_interfaces) {
                if (intf != null) {
                    long down = intf.getDownTime(endTime, rollingWindow);
                    if (down > 0)
                        outage += down;
                    serviceCount += intf.getServiceCount();
                }
			}
        }
        
        if (serviceCount > 0) {
            m_downTime = outage;
            m_totalWindow = rollingWindow * serviceCount;
            m_serviceCount = serviceCount;
            double denom = rollingWindow * serviceCount * 1.0d;
            double num = 1.0d * outage;
            percent = 100.0 * (1.0 - (num / denom));
        } else
            percent = 100.0;
        m_percentAvail = percent;
        return percent;
    }

    /**
     * Get the outage for this node.
     * 
     * @return The outage time.
     */
    public long getOutage(long endTime, long rollingWindow) {
        long outage = 0;
        int serviceCount = 0;

        for (Interface intf : m_interfaces) {
            if (intf != null) {
                long down = intf.getDownTime(endTime, rollingWindow);
                if (down > 0)
                    outage += down;
                serviceCount += intf.getServiceCount();
            }
		}
        
        if (serviceCount > 0) {
            m_serviceCount = serviceCount;
        }

        return outage;
    }

    /**
     * Returns the string that displays the Node/Interface/Service/Outages
     * combinations.
     * 
     * @return The string representation.
     */
    public String toString() {
        StringBuffer retVal = new StringBuffer();
        String nl = System.getProperty("line.separator");

        retVal.append(nl).append(nl).append("Nodeid : ").append(getName()).append(nl).append("Interfaces");

        for (Interface intf : m_interfaces) {
            if (intf != null) {
                retVal.append(nl).append("\t\t").append(intf.getName());

                List<Service> services = intf.getServices();
                for (Service service : services) {
                    retVal.append(nl).append("\t\t\t\t").append(service.getName());
                    if (service != null) {
                        retVal.append(nl).append("\t\t\t\t\t").append(service.getOutages());
                    }
				}
            }
		}
        
        return retVal.toString();
    }
}
