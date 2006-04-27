//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
//   OpenNMS Licensing       <license@opennms.org>
//   http://www.opennms.org/
//   http://www.opennms.com/
//
// Tab Size = 8

package org.opennms.netmgt.importer.operations;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;

public class UpdateOperation extends AbstractSaveOrUpdateOperation {
    
    public class ServiceUpdater {
        
        private OnmsIpInterface m_iface;
        Map m_svcTypToSvcMap;

        public ServiceUpdater(OnmsIpInterface iface, OnmsIpInterface imported) {
            m_iface = iface;
            
            createSvcTypeToSvcMap(imported);
        }

        private void createSvcTypeToSvcMap(OnmsIpInterface imported) {
            m_svcTypToSvcMap = new HashMap();
            for (Iterator it = imported.getMonitoredServices().iterator(); it.hasNext();) {
                OnmsMonitoredService svc = (OnmsMonitoredService) it.next();
                m_svcTypToSvcMap.put(svc.getServiceType(), svc);
            }
        }

        public void execute(List events) {
            for (Iterator it = getExisting().iterator(); it.hasNext();) {
                OnmsMonitoredService svc = (OnmsMonitoredService) it.next();
                OnmsMonitoredService imported = getImportedVersion(svc);
                if (imported == null) {
                    it.remove();
                    svc.visit(new DeleteEventVisitor(events));
                }
                else {
                    update(svc, events);
                }
                markAsProcessed(svc);
            }
            addNewServices(events);
        }

        private void addNewServices(List events) {
            Collection newServices = getNewServices();
            System.err.println(getNode().getLabel()+" has "+newServices.size()+" new services.");
            for (Iterator it = newServices.iterator(); it.hasNext();) {
                OnmsMonitoredService svc = (OnmsMonitoredService) it.next();
                svc.setIpInterface(m_iface);
                m_iface.getMonitoredServices().add(svc);
                svc.visit(new AddEventVisitor(events));
            }
        }

        private Collection getNewServices() {
            return m_svcTypToSvcMap.values();
        }

        private void markAsProcessed(OnmsMonitoredService svc) {
            m_svcTypToSvcMap.remove(svc.getServiceType());
        }

        private void update(OnmsMonitoredService svc, List events) {
            // nothing to do here
        }

        private OnmsMonitoredService getImportedVersion(OnmsMonitoredService svc) {
            return (OnmsMonitoredService)m_svcTypToSvcMap.get(svc.getServiceType());
        }

        Set getExisting() {
            return m_iface.getMonitoredServices();
        }

    }

    public class InterfaceUpdater {
        
        private OnmsNode m_node;
        private Map m_ipAddrToImportIfs;

        public InterfaceUpdater(OnmsNode node, OnmsNode imported) {
            m_node = node;
            m_ipAddrToImportIfs = getIpAddrToInterfaceMap(imported);
        }

        public void execute(List events) {
            for (Iterator it = getExistingInterfaces().iterator(); it.hasNext();) {
                OnmsIpInterface iface = (OnmsIpInterface) it.next();
                OnmsIpInterface imported = getImportedVersion(iface);
                
                if (imported == null) {
                    it.remove();
                    iface.visit(new DeleteEventVisitor(events));
                    markAsProcessed(iface);
                } else if (isSnmpDataForInterfacesUpToDate() && !nullSafeEquals(iface.getIfIndex(), imported.getIfIndex())) {
                	// we just remove this here but let the imported version get added back
                	// FIXME: for hibernate we don't need to do this since ifIndex won't be port of the key then
                	it.remove();
                } else {
                    update(imported, iface, events);
                    markAsProcessed(iface);
                }
                
            }
            addNewInterfaces(events);
        }

        private void addNewInterfaces(List events) {
            for (Iterator it = getNewInterfaces().iterator(); it.hasNext();) {
                OnmsIpInterface iface = (OnmsIpInterface) it.next();
                iface.setNode(m_node);
                m_node.getIpInterfaces().add(iface);
                iface.visit(new AddEventVisitor(events));
            }
        }

        private OnmsIpInterface getImportedVersion(OnmsIpInterface iface) {
            return (OnmsIpInterface)m_ipAddrToImportIfs.get(iface.getIpAddress());
        }

        private Collection getNewInterfaces() {
            return m_ipAddrToImportIfs.values();
        }

        private void markAsProcessed(OnmsIpInterface iface) {
            m_ipAddrToImportIfs.remove(iface.getIpAddress());
        }

        private void update(OnmsIpInterface imported, OnmsIpInterface iface, List events) {
            if (!nullSafeEquals(iface.getIsManaged(), imported.getIsManaged()))
                iface.setIsManaged(imported.getIsManaged());
            
            if (!nullSafeEquals(iface.getIsSnmpPrimary(), imported.getIsSnmpPrimary())) {
                iface.setIsSnmpPrimary(imported.getIsSnmpPrimary());
                // TODO: send snmpPrimary event
            }
            
            if (isSnmpDataForInterfacesUpToDate()) {

            	if (!nullSafeEquals(iface.getIfIndex(), imported.getIfIndex())) {
            		iface.setIfIndex(imported.getIfIndex());
            	}
            	
            }
            
           if (!nullSafeEquals(iface.getIpStatus(), imported.getIpStatus()))
               iface.setIpStatus(imported.getIpStatus());
           
           if (!nullSafeEquals(iface.getIpHostName(), imported.getIpHostName()))
        	   iface.setIpHostName(imported.getIpHostName());
           
           updateServices(iface, imported, events);
        }

        private void updateServices(OnmsIpInterface iface, OnmsIpInterface imported, List events) {
            new ServiceUpdater(iface, imported).execute(events);
        }

        private Set getExistingInterfaces() {
            return m_node.getIpInterfaces();
        }

    }

    public UpdateOperation(Integer nodeId, String foreignSource, String foreignId, String nodeLabel, String building, String city) {
		super(nodeId, foreignSource, foreignId, nodeLabel, building, city);
	}

	public List doPersist() {
		OnmsNode imported = getNode();
		OnmsNode db = getNodeDao().getHierarchy(imported.getId());

		List events = new LinkedList();

		// verify that the node label is still the same
		if (!db.getLabel().equals(imported.getLabel())) {
			db.setLabel(imported.getLabel());
			// TODO: nodeLabelChanged event
		}

		if (isSnmpDataForNodeUpToDate()) {

			if (!nullSafeEquals(db.getSysContact(), imported.getSysContact())) {
				db.setSysContact(imported.getSysContact());
			}

			if (!nullSafeEquals(db.getSysDescription(), imported.getSysDescription())) {
				db.setSysDescription(imported.getSysDescription());
			}

			if (!nullSafeEquals(db.getSysLocation(), imported.getSysLocation())) {
				db.setSysLocation(imported.getSysLocation());
			}

			if (!nullSafeEquals(db.getSysName(), imported.getSysName())) {
				db.setSysName(imported.getSysName());
			}

			if (!nullSafeEquals(db.getSysObjectId(), imported.getSysObjectId())) {
				db.setSysObjectId(imported.getSysObjectId());
			}
			
		}

		updateInterfaces(db, imported, events);
		updateCategories(db, imported);

		if (isSnmpDataForInterfacesUpToDate())
			updateSnmpInterfaces(db, imported);

		getNodeDao().update(db);

		return events;

	}

    private void updateSnmpInterfaces(OnmsNode db, OnmsNode imported) {
		db.setSnmpInterfaces(imported.getSnmpInterfaces());
	}

	private void updateCategories(OnmsNode db, OnmsNode imported) {
        if (!db.getCategories().equals(imported.getCategories()))
            db.setCategories(imported.getCategories());
    }

    private void updateInterfaces(OnmsNode db, OnmsNode imported, List events) {
        new InterfaceUpdater(db, imported).execute(events);
    }

    public String toString() {
       return "UPDATE: Node: "+getNode().getId()+": "+getNode().getLabel();
    }
}
