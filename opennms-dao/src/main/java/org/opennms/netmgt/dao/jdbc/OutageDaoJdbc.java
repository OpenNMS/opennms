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

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

import org.opennms.netmgt.dao.OutageDao;
import org.opennms.netmgt.dao.jdbc.outage.FindAllOutages;
import org.opennms.netmgt.dao.jdbc.outage.FindByOutageId;
import org.opennms.netmgt.dao.jdbc.outage.FindCurrentOutages;
import org.opennms.netmgt.dao.jdbc.outage.FindOpenAndResolvedOutages;
import org.opennms.netmgt.dao.jdbc.outage.FindResolvedOutages;
import org.opennms.netmgt.dao.jdbc.outage.FindSuppressedOutages;
import org.opennms.netmgt.dao.jdbc.outage.LazyOutage;
import org.opennms.netmgt.dao.jdbc.outage.OutageSave;
import org.opennms.netmgt.dao.jdbc.outage.OutageUpdate;
import org.opennms.netmgt.filter.Filter;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsOutage;
import org.opennms.netmgt.model.ServiceSelector;

/**
 * @author mhuot
 *
 */
public class OutageDaoJdbc extends AbstractDaoJdbc implements OutageDao {

    public OutageDaoJdbc() {
            super();
    }

    public OutageDaoJdbc(DataSource ds) {
            super(ds);
    }
    
    public OnmsOutage load(int id) {
        return load(new Integer(id));
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.dao.OutageDao#load(java.lang.Integer)
     */
    public OnmsOutage load(Integer id) {
        return new FindByOutageId(getDataSource()).findUnique(id);
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.dao.OutageDao#save(org.opennms.netmgt.model.OnmsOutage)
     */
    public void save(OnmsOutage outage) {
        if (outage.getId() != null)
            throw new IllegalArgumentException("Cannot save an outage that already has a outageid");
        
        outage.setId(allocateOutageId());
        getOutageSaver().doInsert(outage);

    }

    private OutageSave getOutageSaver() {
        return new OutageSave(getDataSource());
    }

    private Integer allocateOutageId() {
        return new Integer(getJdbcTemplate().queryForInt("SELECT nextval('outageNxtId')"));
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.dao.OutageDao#update(org.opennms.netmgt.model.OnmsOutage)
     */
    public void update(OnmsOutage outage) {
        if (outage.getId() == null)
            throw new IllegalArgumentException("Cannot update a outage without a outageid");
        
        if (isDirty(outage))
        		getOutageUpdater().doUpdate(outage);
        
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.dao.OutageDao#saveOrUpdate(org.opennms.netmgt.model.OnmsOutage)
     */
    public void saveOrUpdate(OnmsOutage outage) {
        if (outage.getId() == null)
            save(outage);
        else
            update(outage);
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.dao.OnmsDao#flush()
     */
    public void flush() {
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.dao.OnmsDao#clear()
     */
    public void clear() {
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.dao.OnmsDao#countAll()
     */
    public int countAll() {
        return getJdbcTemplate().queryForInt("select count(*) from outages");
    }
    
    private boolean isDirty(OnmsOutage outage) {
		if (outage instanceof LazyOutage) {
			LazyOutage lazyOutage = (LazyOutage) outage;
			return lazyOutage.isDirty();
		}
		return true;
    }
    
    private OutageUpdate getOutageUpdater() {
        return new OutageUpdate(getDataSource());
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.dao.OutageDao#findAll()
     */
    public Collection<OnmsOutage> findAll() {
        return new FindAllOutages(getDataSource()).findSet();
	}

    public OnmsOutage get(int id) {
        return get(new Integer(id));
    }

    public OnmsOutage get(Integer id) {
        if (Cache.retrieve(OnmsOutage.class, id) == null)
            return new FindByOutageId(getDataSource()).findUnique(id);
        else
            return (OnmsOutage)Cache.retrieve(OnmsOutage.class, id);
    }

    public Integer currentOutageCount() {
        return getJdbcTemplate().queryForInt("select distinct count(outages.iflostservice) from outages, node, ipinterface, ifservices " + "where outages.ifregainedservice is null " + "and node.nodeid = outages.nodeid and ipinterface.nodeid = outages.nodeid and ifservices.nodeid = outages.nodeid " + "and ipinterface.ipaddr = outages.ipaddr and ifservices.ipaddr = outages.ipaddr " + "and ifservices.serviceid = outages.serviceid " + "and node.nodeType != 'D' and ipinterface.ismanaged != 'D' and ifservices.status != 'D' " + " and (suppresstime is null or suppresstime < now()) ");
    }

    public Integer currentSuppressedOutageCount() {
        return getJdbcTemplate().queryForInt("select distinct count(outages.iflostservice) from outages, node, ipinterface, ifservices " + "where outages.ifregainedservice is null " + "and node.nodeid = outages.nodeid and ipinterface.nodeid = outages.nodeid and ifservices.nodeid = outages.nodeid " + "and ipinterface.ipaddr = outages.ipaddr and ifservices.ipaddr = outages.ipaddr " + "and ifservices.serviceid = outages.serviceid " + "and node.nodeType != 'D' and ipinterface.ismanaged != 'D' and ifservices.status != 'D' " + " and suppresstime > now() ");
    }
    
    public Integer outageCount() {
        return getJdbcTemplate().queryForInt("select distinct count(outages.iflostservice) from outages, node, ipinterface, ifservices " + "where " + " node.nodeid = outages.nodeid and ipinterface.nodeid = outages.nodeid and ifservices.nodeid = outages.nodeid " + "and ipinterface.ipaddr = outages.ipaddr and ifservices.ipaddr = outages.ipaddr " + "and ifservices.serviceid = outages.serviceid " + "and node.nodeType != 'D' and ipinterface.ismanaged != 'D' and ifservices.status != 'D' " + " and (suppresstime is null or suppresstime < now()) ");
    }

    public Collection current() {
        
        return new FindCurrentOutages(getDataSource()).findSet();        
        
    }

    public Collection<OnmsOutage> currentOutages() {
        return new FindCurrentOutages(getDataSource()).findSet();   
    }

    public Collection<OnmsOutage> suppressedOutages() {
        return new FindSuppressedOutages(getDataSource()).findSet();
    }

    public Collection<OnmsOutage> openAndResolvedOutages() {
       return new FindOpenAndResolvedOutages(getDataSource()).findSet();
    }

    
    
    public Collection<OnmsOutage> currentOutages(Integer offset, Integer limit, String orderBy, String direction) {
        return new FindCurrentOutages(getDataSource(), offset, limit, orderBy, direction).findSet();
    }

    public Collection<OnmsOutage> suppressedOutages(Integer offset, Integer limit) {
        return new FindSuppressedOutages(getDataSource(), offset, limit).findSet();
    }

    public Collection<OnmsOutage> findAll(Integer offset, Integer limit) {
        return new FindAllOutages(getDataSource(), offset, limit).findSet();
    }

    @SuppressWarnings("unchecked")
	public Collection<OnmsOutage> matchingCurrentOutages(ServiceSelector selector) {
    	Filter filter = new Filter();
    	Set<String> matchingIps = new HashSet<String>(filter.getIPList(selector.getFilterRule()));
    	Set<String> matchingSvcs = new HashSet<String>(selector.getServiceNames());
    	
    	List<OnmsOutage> matchingOutages = new LinkedList<OnmsOutage>();
    	Collection<OnmsOutage> outages = currentOutages();
		for (OnmsOutage outage : outages) {
    		OnmsMonitoredService svc = outage.getMonitoredService();
    		if ((matchingSvcs.contains(svc.getServiceName()) || matchingSvcs.isEmpty()) &&
    			matchingIps.contains(svc.getIpAddress())) {
    			
    			matchingOutages.add(outage);
    		}
			
		}
    	
    	
    	return matchingOutages;
    }

	public Collection<OnmsOutage> currentOutages(String orderBy) {
		  return new FindCurrentOutages(getDataSource(), orderBy).findSet();
	}
	
	public Collection<OnmsOutage> getOutagesByRange(Integer offset, Integer limit, String order, String direction) {
		return new FindAllOutages(getDataSource(),offset, limit, order, direction).findSet();
	}

	public Collection<OnmsOutage> getOutagesByRange(Integer offset, Integer limit, String order, String direction, String filter) {
			return new FindAllOutages(getDataSource(),offset, limit, order, direction, filter).findSet();
	}

	public Integer outageCountFiltered(String filter) {
        return getJdbcTemplate().queryForInt("select distinct count(outages.iflostservice) from outages, node, ipinterface, ifservices " + "where " + " node.nodeid = outages.nodeid and ipinterface.nodeid = outages.nodeid and ifservices.nodeid = outages.nodeid " + "and ipinterface.ipaddr = outages.ipaddr and ifservices.ipaddr = outages.ipaddr " + "and ifservices.serviceid = outages.serviceid " + "and node.nodeType != 'D' and ipinterface.ismanaged != 'D' and ifservices.status != 'D' " + " and (suppresstime is null or suppresstime < now()) " + filter);
    }

	public Collection<OnmsOutage> suppressedOutages(Integer offset, Integer limit, String order, String direction) {
		 return new FindSuppressedOutages(getDataSource(), offset, limit, order, direction).findSet(); 
	}

	public Collection<OnmsOutage> getResolvedOutagesByRange(Integer offset, Integer limit, String order, String direction, String filter) {
			return new FindResolvedOutages(getDataSource(),offset, limit, order, direction, filter).findSet();
	}

	public Integer outageResolvedCountFiltered(String filter) {
			return getJdbcTemplate().queryForInt("select distinct count(outages.iflostservice) from outages, node, ipinterface, ifservices " + "where " + " node.nodeid = outages.nodeid and ipinterface.nodeid = outages.nodeid and ifservices.nodeid = outages.nodeid " + "and ipinterface.ipaddr = outages.ipaddr and ifservices.ipaddr = outages.ipaddr " + "and ifservices.serviceid = outages.serviceid " + "and node.nodeType != 'D' and ipinterface.ismanaged != 'D' and ifservices.status != 'D' " + " and ifregainedservice > 1 and (suppresstime is null or suppresstime < now() ) " + filter);
	}

}
