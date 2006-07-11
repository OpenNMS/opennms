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

import org.opennms.netmgt.dao.IpInterfaceDao;
import org.opennms.netmgt.dao.jdbc.ipif.FindAll;
import org.opennms.netmgt.dao.jdbc.ipif.FindById;
import org.opennms.netmgt.dao.jdbc.ipif.FindByIpInterface;
import org.opennms.netmgt.dao.jdbc.ipif.FindByNode;
import org.opennms.netmgt.dao.jdbc.ipif.FindByNodeAndIp;
import org.opennms.netmgt.dao.jdbc.ipif.FindByServiceType;
import org.opennms.netmgt.dao.jdbc.ipif.IpInterfaceId;
import org.opennms.netmgt.dao.jdbc.ipif.LazyIpInterface;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;

public class IpInterfaceDaoJdbc extends AbstractDaoJdbc implements IpInterfaceDao {
    
    public IpInterfaceDaoJdbc() {
        super();
    }
    
    public IpInterfaceDaoJdbc(DataSource ds) {
        super(ds);
    }
    
    

    public int countAll() {
        return getJdbcTemplate().queryForInt("select count(*) from ipInterface");
    }
    
    public void delete(OnmsIpInterface iface) {
        if (iface.getIfIndex() == null) {
            Object[] parms = new Object[] { iface.getNode().getId(), iface.getIpAddress() };
            getJdbcTemplate().update("delete from ipInterface where nodeId = ? and ipAddr = ? and ifIndex is null", parms);
            
            // cascase deleting the ifServices as well
            getJdbcTemplate().update("delete from ifServices where nodeId = ? and ipAddr = ? and ifIndex is null", parms);
        } else {
            Object[] parms = new Object[] { iface.getNode().getId(), iface.getIpAddress(), iface.getIfIndex() };
            getJdbcTemplate().update("delete from ipInterface where nodeId = ? and ipAddr = ? and ifIndex = ?", parms);

            // cascase deleting the ifServices as well
            getJdbcTemplate().update("delete from ifServices where nodeId = ? and ipAddr = ? and ifIndex = ?", parms);
        }
    }
    
    private void deleteInterfacesForNode(OnmsNode node) {
    	// this us used only fore recreating the set of interfaces for a node... we don't need to cascase to
    	// the ifservices here
        getJdbcTemplate().update("delete from ipInterface where nodeId = ?", new Object[] { node.getId() });
        
    }

    public Collection findAll() {
        return new FindAll(getDataSource()).findSet();
    }

    public Collection findByIpAddress(String ipAddress) {
        return new FindByIpInterface(getDataSource()).execute(ipAddress);
    }

    public Set findByNode(OnmsNode node) {
        return new FindByNode(getDataSource()).findSet(node.getId());
    }
    
	public Collection findByServiceType(String svcName) {
		return new FindByServiceType(getDataSource()).findSet(svcName);
	}

    public void flush() {
    }

    public OnmsIpInterface get(Integer dbNodeId, String dbIpAddr, Integer dbIfIndex) {
    	return get(new IpInterfaceId(dbNodeId, dbIpAddr, dbIfIndex));
    }
    
    public OnmsIpInterface get(IpInterfaceId id) {
    	OnmsIpInterface iface = (OnmsIpInterface)Cache.obtain(OnmsIpInterface.class, id);
    	if (iface != null) return iface;
    	
    	return FindById.get(getDataSource(), id).find(id);
    }
    
	public OnmsIpInterface get(OnmsNode node, String ipAddress) {
		return (new FindByNodeAndIp(getDataSource())).findUnique(node.getId(), ipAddress);
	}


    public OnmsIpInterface get(Long id) {
        throw new RuntimeException("cannot lookup interface by a single int id yet!");
    }

    public OnmsIpInterface load(Long id) {
        throw new RuntimeException("cannot lookup interface by a single int id yet!");
    }
    
    public void save(OnmsIpInterface svc) {
        if (exists(svc))
            throw new IllegalArgumentException("cannot save svc that already exist in the db");
        
        doSave(svc);
    }

    public void saveIfsForNode(OnmsNode node) {
        for (Iterator it = node.getIpInterfaces().iterator(); it.hasNext();) {
            OnmsIpInterface iface = (OnmsIpInterface) it.next();
            doSave(iface);
        }
    }
    
    public void saveOrUpdate(OnmsIpInterface svc) {
        if (exists(svc)) {
            doUpdate(svc);
        } else {
            doSave(svc);
        }
    }

    public void saveOrUpdateIfsForNode(OnmsNode node) {
        Set ipInterfaces = node.getIpInterfaces();
        if (!isDirty(ipInterfaces)) return;
        if (ipInterfaces instanceof JdbcSet) {
            updateSetMembers((JdbcSet)ipInterfaces);
        } else {
            removeAndAddSet(node);
        }
    }

    public void update(OnmsIpInterface svc) {
        
        if (!exists(svc))
            throw new IllegalArgumentException("cannot updates svcs that are already in the db");
        
        doUpdate(svc);
        
    }

    private void cascadeSaveAssociations(OnmsIpInterface iface) {
        new MonitoredServiceDaoJdbc(getDataSource()).saveSvcsForIf(iface);
    }

    private void cascadeUpdateAssociations(OnmsIpInterface iface) {
        new MonitoredServiceDaoJdbc(getDataSource()).saveOrUpdateSvcsForIf(iface);
    }

    private void doSave(OnmsIpInterface iface) {
        getJdbcTemplate().update("insert into ipInterface (ipHostname, isManaged, ipStatus, ipLastCapsdPoll, isSnmpPrimary, nodeId, ipAddr, ifIndex) values (?, ?, ?, ?, ?, ?, ?, ?)",
         new Object[] {
            iface.getIpHostName(),
            iface.getIsManaged(),
            iface.getIpStatus(),
            iface.getIpLastCapsdPoll(),
            iface.getIsSnmpPrimary().toString(),
            iface.getNode().getId(),
            iface.getIpAddress(),
            iface.getIfIndex(),
         });
        cascadeSaveAssociations(iface);
    }

    private void doUpdate(OnmsIpInterface iface) {
    	if (isDirty(iface)) {

    		// THIS SUCKS!  Muck around with the statment to account for partially null keys. BYUCK!

    		String updateStmt = "update ipInterface set ipHostname = ?, isManaged = ?, ipStatus = ?, ipLastCapsdPoll = ?, isSnmpPrimary = ? where nodeId = ? and ipAddr = ? and " +
    		(iface.getIfIndex() == null ? "ifIndex is null" : "ifIndex = ?");

    		// now to construct the correct array

    		// all but the ifIndex
    		Object[] parms = new Object[] {
    				iface.getIpHostName(),
    				iface.getIsManaged(),
    				iface.getIpStatus(),
    				iface.getIpLastCapsdPoll(),
    				iface.getIsSnmpPrimary().toString(),
    				iface.getNode().getId(),
    				iface.getIpAddress(),
    		};

    		if (iface.getIfIndex() != null) {
    			List parmList = new ArrayList(Arrays.asList(parms));
    			parmList.add(iface.getIfIndex());
    			parms = parmList.toArray();
    		}

    		getJdbcTemplate().update(updateStmt, parms);
    	}

    	cascadeUpdateAssociations(iface);
    }

    private boolean isDirty(OnmsIpInterface iface) {
    	if (iface instanceof LazyIpInterface) {
			LazyIpInterface lazyIface = (LazyIpInterface) iface;
			return lazyIface.isDirty();
		}
    	return true;
	}

	private boolean exists(OnmsIpInterface iface) {
        
        // SAME CRAP HERE
        String query = "select count(*) from ipInteface where nodeid = ? and ipAddr = ? and " +
                (iface.getIfIndex() == null ? "ifIndex is null" : "ifIndex = ?");
        
        Object[] parms = new Object[] {
                iface.getNode().getId(),
                iface.getIpAddress(),
        };
        
        if (iface.getIfIndex() != null) {
            List parmList = new ArrayList(Arrays.asList(parms));
            parmList.add(iface.getIfIndex());
            parms = parmList.toArray();
        }
        int count = getJdbcTemplate().queryForInt(query, parms);
        return count > 0;
    }

    private void removeAndAddSet(OnmsNode node) {
        deleteInterfacesForNode(node);
        saveIfsForNode(node);
    }

    private void updateSetMembers(JdbcSet set) {
        for (Iterator it = set.getRemoved().iterator(); it.hasNext();) {
            OnmsIpInterface iface = (OnmsIpInterface) it.next();
            delete(iface);
        }
        
        for (Iterator it = set.getRemaining().iterator(); it.hasNext();) {
            OnmsIpInterface iface = (OnmsIpInterface) it.next();
            doUpdate(iface);
        }
        
        for (Iterator it = set.getAdded().iterator(); it.hasNext();) {
            OnmsIpInterface iface = (OnmsIpInterface) it.next();
            doSave(iface);
            
        }
        
        set.reset();
    }

}
