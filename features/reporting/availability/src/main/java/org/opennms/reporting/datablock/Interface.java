/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.reporting.datablock;

import java.util.ArrayList;
import java.util.List;

/**
 * This class holds the interface information and list of services that belong
 * to the interface.
 *
 * @author <A HREF="mailto:jacinta@oculan.com">Jacinta Remedios </A>
 */
public class Interface extends StandardNamedObject {
    /**
     * List of services.
     */
    private ArrayList<Service> m_services;

    private static class ServiceComparator {
        private String m_svcname;

        private ServiceComparator(String svc) {
            m_svcname = svc;
        }

        static ServiceComparator make(String name) {
            return new ServiceComparator(name);
        }

        @SuppressWarnings("unused")
        static ServiceComparator make(Service svc) {
            return new ServiceComparator(svc.getName());
        }

        @Override
        public boolean equals(Object o) {
            boolean rc = false;
            if (o != null) {
                if (o == this)
                    rc = true;
                else if (o instanceof Service)
                    rc = m_svcname.equals(((Service) o).getName());
                else if (o instanceof String)
                    rc = m_svcname.equals(o);
            }
            return rc;
        }
    }

    /**
     * Default Constructor.
     */
    public Interface() {
        m_services = new ArrayList<Service>();
    }

    /**
     * Constructor that sets the name of interface and sets the services.
     *
     * @param name
     *            Name of the interface.
     * @param services
     *            Services to be set for this interface.
     */
    public Interface(String name, ArrayList<Service> services) {
        setName(name);
        if (services == null)
            m_services = new ArrayList<Service>();
        else
            m_services = services;
    }

    /**
     * Constructor that sets the name and adds service. If there is already an
     * interface with name, this method adds a service to this found service.
     * Otherwise adds a new interface with service.
     *
     * @param name
     *            Name of the interface.
     * @param service
     *            Name of the service to be added
     */
    public Interface(String name, String service) {
        setName(name);
        m_services = new ArrayList<Service>();
        Service svc;
        if (service != null) {
            svc = new Service(service);
            m_services.add(svc);
        }
    }

    /**
     * Constructor that sets the name to interface and adds service and outage
     * with lost time. If there is already an interface with name, adds a
     * service depending upon whether it exists or not. Otherwise adds a new
     * interface with service.
     *
     * @param name
     *            Name of the interface.
     * @param service
     *            Name of the service to be added
     * @param losttime
     *            Lost time
     */
    public Interface(String name, String service, long losttime) {
        setName(name);
        Service svc;
        m_services = new ArrayList<Service>();
        if (service != null) {
            svc = new Service(service);
            if (losttime > 0)
                svc.addOutage(losttime);
            m_services.add(svc);
        }
    }

    /**
     * Constructor that sets the name to interface and adds service and outage
     * with lost time and regained time. If there is already an interface with
     * name, adds a service depending upon whether it exists or not. Otherwise
     * adds a new interface with service.
     *
     * @param name
     *            Name of the interface.
     * @param service
     *            Name of the service to be added
     * @param losttime
     *            Lost time
     * @param regainedtime
     *            Regained Time
     */
    public Interface(String name, String service, long losttime, long regainedtime) {
        setName(name);
        m_services = new ArrayList<Service>();
        Service svc;
        if (service != null) {
            svc = new Service(service);
            if (losttime > 0) {
                if (regainedtime > 0)
                    svc.addOutage(losttime, regainedtime);
                else
                    svc.addOutage(losttime);
            }
            m_services.add(svc);
        }
    }

    /**
     * Constructor that sets the name.
     *
     * @param name
     *            Name of the interface.
     */
    public Interface(String name) {
        m_services = new ArrayList<Service>();
        setName(name);
    }

    /**
     * Constructor that sets the services.
     *
     * @param services
     *            Services for this interface to be set.
     */
    public Interface(ArrayList<Service> services) {
        if (services == null)
            m_services = new ArrayList<Service>();
        else
            m_services = services;
    }

    /**
     * Return the services
     *
     * @return Services to be set.
     */
    public List<Service> getServices() {
        return m_services;
    }

    /**
     * Return the Service object given the service name
     *
     * @param svcname
     *            The service name to lookup.
     * @return Service with matching service name.
     */
    public Service getService(String svcname) {
        int ndx = m_services.indexOf(ServiceComparator.make(svcname));
        if (ndx != -1) {
            return (Service) m_services.get(ndx);
        }

        return null;
    }

    /**
     * Adds a new service object to this interface
     *
     * @param service
     *            The service to be add.
     */
    public void addService(Service service) {
        if (service != null)
            m_services.add(service);
    }

    /**
     * Adds a new service to this interface
     *
     * @param service
     *            The name of the service to add.
     */
    public void addService(String service) {
        int ndx = m_services.indexOf(ServiceComparator.make(service));
        if (ndx != -1) {
            return;
        }

        if (service != null) {
            Service svc = new Service(service);
            m_services.add(svc);
        }
    }

    /**
     * Adds a new service to this interface.
     *
     * @param service
     *            Service name to be added
     * @param losttime
     *            Outage with lost time to be added to service
     */
    public void addService(String service, long losttime) {
        int ndx = m_services.indexOf(ServiceComparator.make(service));
        if (ndx != -1) {
            Service svc = (Service) m_services.get(ndx);
            if (svc != null) {
                if (losttime > 0)
                    svc.addOutage(losttime);
            }
            return;
        }

        if (service != null) {
            Service svc = new Service(service);
            if (losttime > 0)
                svc.addOutage(losttime);
            m_services.add(svc);
        }
    }

    /**
     * Adds a service to this interface with outage having lost time and
     * regained time.
     *
     * @param service
     *            Service name
     * @param losttime
     *            Lost time
     * @param regainedtime
     *            Regained Time
     */
    public void addService(String service, long losttime, long regainedtime) {
        if (service == null)
            return;

        int ndx = m_services.indexOf(ServiceComparator.make(service));
        if (ndx != -1) {
            Service svc = (Service) m_services.get(ndx);
            if (svc != null) {
                if (losttime > 0) {
                    if (regainedtime > 0)
                        svc.addOutage(losttime, regainedtime);
                    else
                        svc.addOutage(losttime);
                }
            }
            return;
        }

        Service svc = new Service(service);
        if (losttime > 0) {
            if (regainedtime > 0)
                svc.addOutage(losttime, regainedtime);
            else
                svc.addOutage(losttime);
        }
        m_services.add(svc);
    }

    /**
     * Returns the down time for this interface.
     *
     * @param currentTime
     *            End of rolling window
     * @param rollingWindow
     *            Rolling Window
     * @return the down time for this interface.
     */
    public long getDownTime(long currentTime, long rollingWindow) {
        long outageTime = 0;
        if (m_services != null && m_services.size() > 0) {
        	for (Service service : m_services) {
				long down = service.getDownTime(currentTime, rollingWindow);
				if (down > 0) {
					outageTime += down;
				}
			}
        }
        return outageTime;
    }

    /**
     * Returns the number of services that this node/interface has.
     *
     * @return a int.
     */
    public int getServiceCount() {
        if (m_services != null && m_services.size() > 0)
            return m_services.size();
        return -1;
    }

    /**
     * Returns the number of services affected.
     *
     * @return a int.
     */
    public int getServiceAffectCount() {
        int count = 0;
        if (m_services != null && m_services.size() > 0) {
        	for (Service service : m_services) {
				if (service.getOutages().size() > 0) {
					count++;
				}
			}
        }
        return count;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        if (obj != null) {
            if (obj instanceof String)
                return ((String) obj).equals(getName());
            else if (obj instanceof Interface)
                return obj == this;
        }
        return false;
    }
}
