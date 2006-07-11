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
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.netmgt.dao.jdbc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

import org.opennms.netmgt.dao.MonitoredServiceDao;
import org.opennms.netmgt.dao.jdbc.monsvc.FindAll;
import org.opennms.netmgt.dao.jdbc.monsvc.FindByInterfaceWithIfIndex;
import org.opennms.netmgt.dao.jdbc.monsvc.FindByInterfaceWithNulIfIndex;
import org.opennms.netmgt.dao.jdbc.monsvc.FindByNodeIpAddrSvcName;
import org.opennms.netmgt.dao.jdbc.monsvc.FindByType;
import org.opennms.netmgt.dao.jdbc.monsvc.LazyMonitoredService;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;

public class MonitoredServiceDaoJdbc extends AbstractDaoJdbc implements MonitoredServiceDao {
    
    public MonitoredServiceDaoJdbc() {
        super();
    }
    
    public MonitoredServiceDaoJdbc(DataSource ds) {
        super(ds);
    }

    public int countAll() {
        return getJdbcTemplate().queryForInt("select count(*) from ifservices");
    }

    private void delete(OnmsMonitoredService svc) {
        if (svc.getIfIndex() == null) {
            Object[] parms = new Object[] { svc.getNodeId(), svc.getIpAddress(), svc.getServiceId() };
            getJdbcTemplate().update("delete from ifServices where nodeId = ? and ipAddr = ? and serviceId = ? and ifIndex is null", parms);
        } else {
            Object[] parms = new Object[] { svc.getNodeId(), svc.getIpAddress(), svc.getServiceId(), svc.getIfIndex() };
            getJdbcTemplate().update("delete from ifServices where nodeId = ? and ipAddr = ? and serviceId = ? and ifIndex = ?", parms);
        }
        
    }
    
    private void deleteServicesForInterface(OnmsIpInterface iface) {
        if (iface.getIfIndex() == null) {
            Object[] parms = new Object[] { iface.getNode().getId(), iface.getIpAddress() };
            getJdbcTemplate().update("delete from ifServices where nodeId = ? and ipAddr = ? and ifIndex is null", parms);
        } else {
            Object[] parms = new Object[] { iface.getNode().getId(), iface.getIpAddress(), iface.getIfIndex() };
            getJdbcTemplate().update("delete from ifServices where nodeId = ? and ipAddr = ? and ifIndex = ?", parms);
        }
    }

    private void doSave(OnmsMonitoredService svc) {
        getJdbcTemplate().update("insert into ifservices (lastGood, lastFail, qualifier, status, source, notify, nodeId, ipAddr, serviceId, ifIndex) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
         new Object[] {
            svc.getLastGood(),
            svc.getLastFail(),
            svc.getQualifier(),
            svc.getStatus(),
            svc.getSource(),
            svc.getNotify(),
            svc.getNodeId(),
            svc.getIpAddress(),
            svc.getServiceType().getId(),
            svc.getIfIndex(),
         });
    }

    private void doUpdate(OnmsMonitoredService svc) {
        // THIS SUCKS!  Muck around with the statment to account for partially null keys. BYUCK!
    	
    	if (!isDirty(svc)) return;
        
        
        String updateStmt = "update ifservices set lastGood = ?, lastFail = ?, qualifier = ?, status = ?, source = ?, notify = ? " +
                "where nodeId = ? and ipAddr = ? and serviceId = ? and " +
                (svc.getIfIndex() == null ? "ifIndex is null" : "ifIndex = ?");
        
        // now to construct the correct array
        
        // all but the ifIndex
        Object[] parms = new Object[] {
            svc.getLastGood(),
            svc.getLastFail(),
            svc.getQualifier(),
            svc.getStatus(),
            svc.getSource(),
            svc.getNotify(),
            svc.getNodeId(),
            svc.getIpAddress(),
            svc.getServiceType().getId(),
         };
        
        if (svc.getIfIndex() != null) {
            List parmList = new ArrayList(Arrays.asList(parms));
            parmList.add(svc.getIfIndex());
            parms = parmList.toArray();
        }
        
        getJdbcTemplate().update(updateStmt, parms);
    }

    private boolean exists(OnmsMonitoredService svc) {
        
        // SAME CRAP HERE
        String query = "select count(*) from ifservices where nodeid = ? and ipAddr = ? and serviceId = ? and " +
                (svc.getIfIndex() == null ? "ifIndex is null" : "ifIndex = ?");
        
        Object[] parms = new Object[] {
                svc.getNodeId(),
                svc.getIpAddress(),
                svc.getServiceId(),
        };
        
        if (svc.getIfIndex() != null) {
            List parmList = new ArrayList(Arrays.asList(parms));
            parmList.add(svc.getIfIndex());
            parms = parmList.toArray();
        }
        int count = getJdbcTemplate().queryForInt(query, parms);
        return count > 0;
    }
    
	public OnmsMonitoredService get(int nodeId, String ipAddress, String svcName) {
		return (new FindByNodeIpAddrSvcName(getDataSource())).
			findUnique(new Object[] {new Integer(nodeId), ipAddress, svcName});
	}



    public Collection findAll() {
        return new FindAll(getDataSource()).findSet();
    }

    public Set findByInterface(OnmsIpInterface iface) {
        // MORE CRAP
        if (iface.getIfIndex() == null) {
            return (new FindByInterfaceWithNulIfIndex(getDataSource())).findSet(
                         new Object[] { iface.getNode().getId(), iface.getIpAddress() });
        }
        else {
            return (new FindByInterfaceWithIfIndex(getDataSource())).findSet(
                         new Object[] { iface.getNode().getId(), iface.getIpAddress(), iface.getIfIndex() });
        }
    }
    
	public Collection findByType(String type) {
		return (new FindByType(getDataSource())).execute(type);
	}
    
    public void flush() {
    }
    
    public OnmsMonitoredService get(Long id) {
        throw new RuntimeException("we are not able to locate ifservices by a single id yet!");
    }

    private boolean isDirty(OnmsMonitoredService svc) {
    	if (svc instanceof LazyMonitoredService) {
			LazyMonitoredService lazySvc = (LazyMonitoredService) svc;
			return lazySvc.isDirty();
		}
    	return true;
	}

    public OnmsMonitoredService load(Long id) {
        throw new RuntimeException("we are not able to locate ifservices by a single id yet!");
    }

    private void removeAndAddSet(OnmsIpInterface iface) {
        deleteServicesForInterface(iface);
        saveSvcsForIf(iface);
    }

    public void save(OnmsMonitoredService svc) {
        if (exists(svc))
            throw new IllegalArgumentException("cannot save svc that already exist in the db");
        
        doSave(svc);

    }

    public void saveOrUpdate(OnmsMonitoredService svc) {
        if (exists(svc)) {
            doUpdate(svc);
        } else {
            doSave(svc);
        }
    }

	public void saveOrUpdateSvcsForIf(OnmsIpInterface iface) {
        Set svcs = iface.getMonitoredServices();
        if (!isDirty(svcs)) return;
        if (svcs instanceof JdbcSet) {
            updateSetMembers((JdbcSet)svcs);
        } else {
            removeAndAddSet(iface);
        }
    }

    public void saveSvcsForIf(OnmsIpInterface iface) {
		for (Iterator it = iface.getMonitoredServices().iterator(); it.hasNext();) {
		    OnmsMonitoredService svc = (OnmsMonitoredService) it.next();
		    doSave(svc);
		}
	}

    public void update(OnmsMonitoredService svc) {
        
        if (!exists(svc))
            throw new IllegalArgumentException("cannot updates svcs that are already in the db");
        
        doUpdate(svc);
        
    }

	private void updateSetMembers(JdbcSet set) {
        for (Iterator it = set.getRemoved().iterator(); it.hasNext();) {
            OnmsMonitoredService svc = (OnmsMonitoredService) it.next();
            delete(svc);
        }
        
        for (Iterator it = set.getRemaining().iterator(); it.hasNext();) {
            OnmsMonitoredService svc = (OnmsMonitoredService) it.next();
            doUpdate(svc);
        }
        
        for (Iterator it = set.getAdded().iterator(); it.hasNext();) {
            OnmsMonitoredService svc = (OnmsMonitoredService) it.next();
            doSave(svc);
            
        }
        
        set.reset();
    }

    

}
