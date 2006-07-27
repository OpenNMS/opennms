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
import java.util.Iterator;
import java.util.Set;

import javax.sql.DataSource;

import org.opennms.netmgt.dao.PollResultDao;
import org.opennms.netmgt.dao.jdbc.pollresult.FindAll;
import org.opennms.netmgt.dao.jdbc.pollresult.FindById;
import org.opennms.netmgt.dao.jdbc.pollresult.LazyPollResult;
import org.opennms.netmgt.dao.jdbc.pollresult.PollResultSave;
import org.opennms.netmgt.dao.jdbc.pollresult.PollResultUpdate;
import org.opennms.netmgt.model.DemandPoll;
import org.opennms.netmgt.model.PollResult;

/**
 * @author mhuot@opennms.org
 * @author david@opennms.org
 *
 */
public class PollResultDaoJdbc extends AbstractDaoJdbc implements PollResultDao {

    public PollResultDaoJdbc() {
            super();
    }

    public PollResultDaoJdbc(DataSource ds) {
            super(ds);
    }
    
    public PollResult load(int id) {
        return load(new Integer(id));
    }

    public PollResult load(Integer id) {
        return new FindById(getDataSource()).findUnique(id);
    }

    public void save(PollResult result) {
        if (result.getId() != null)
            throw new IllegalArgumentException("Cannot save a poll result that already has an id");
        
        result.setId(allocatePollResultId());
        getPollResultSaver().doInsert(result);

    }

    private PollResultSave getPollResultSaver() {
        return new PollResultSave(getDataSource());
    }

    private Integer allocatePollResultId() {
        return new Integer(getJdbcTemplate().queryForInt("SELECT nextval('pollResultNxtId')"));
    }

    public void update(PollResult result) {
        if (result.getId() == null)
            throw new IllegalArgumentException("Cannot update a pollresult without a outageid");
        
        if (isDirty(result))
        		getPollResultUpdater().doUpdate(result);
        
    }

    public void saveOrUpdate(PollResult result) {
        if (result.getId() == null)
            save(result);
        else
            update(result);
    }

    public void flush() {
    }

    public void clear() {
    }

    public int countAll() {
        return getJdbcTemplate().queryForInt("select count(*) from outages");
    }
    
    private boolean isDirty(PollResult result) {
		if (result instanceof LazyPollResult) {
			LazyPollResult lazyResult = (LazyPollResult) result;
			return lazyResult.isDirty();
		}
		return true;
    }
    
    private PollResultUpdate getPollResultUpdater() {
        return new PollResultUpdate(getDataSource());
    }

    public Collection findAll() {
        return new FindAll(getDataSource()).findSet();
	}

    public PollResult get(int id) {
        return get(new Integer(id));
    }

    public PollResult get(Integer id) {
        if (Cache.retrieve(PollResult.class, id) == null)
            return new FindById(getDataSource()).findUnique(id);
        else
            return (PollResult)Cache.retrieve(PollResult.class, id);
    }

	public PollResult get(Long id) {
		// TODO Auto-generated method stub
		return null;
	}

    public PollResult load(Long id) {
        return new FindById(getDataSource()).findUnique(id);
    }

	public void saveOrUpdateResultsForPoll(DemandPoll poll) {
        Set results = poll.getPollResults();
        if (!isDirty(results)) return;
        if (results instanceof JdbcSet) {
            updateSetMembers((JdbcSet)results);
        } else {
            removeAndAddSet(poll);
        }
	}
	
    private void removeAndAddSet(DemandPoll poll) {
        deletePollResultsForPoll(poll);
        saveResultsForPoll(poll);
    }
    
    private void deletePollResultsForPoll(DemandPoll poll) {
    	// this us used only fore recreating the set of interfaces for a node... we don't need to cascase to
    	// the ifservices here
        getJdbcTemplate().update("delete from pollresults where pollId = ?", new Object[] { poll.getId() });
        
    }
    
    public void saveResultsForPoll(DemandPoll poll) {
    	if (poll.getPollResults() != null) {
    		for (Iterator it = poll.getPollResults().iterator(); it.hasNext();) {
    			PollResult result = (PollResult) it.next();
    			doSave(result);
    		}
    	}
    }
    
    public void delete(PollResult result) {

    	Object[] parms = new Object[] { result.getId() };
    	getJdbcTemplate().update("delete from pollresults where id = ?", parms);
    }

    private void updateSetMembers(JdbcSet set) {
        for (Iterator it = set.getRemoved().iterator(); it.hasNext();) {
            PollResult result = (PollResult) it.next();
            delete(result);
        }
        
        for (Iterator it = set.getRemaining().iterator(); it.hasNext();) {
            PollResult result = (PollResult) it.next();
            doUpdate(result);
        }
        
        for (Iterator it = set.getAdded().iterator(); it.hasNext();) {
            PollResult result = (PollResult) it.next();
            doSave(result);
        }
        
        set.reset();
    }
    
    private void doSave(PollResult result) {
        getJdbcTemplate().update("insert into pollresults (pollId, nodeId, ipAddr, ifIndex, serviceId, statusCode, statusName, reason, id) values (?, ?, ?, ?, ?, ?, ?, ?, ?)",
         new Object[] {
            result.getDemandPoll().getId(),
            result.getMonitoredService().getNodeId(),
            result.getMonitoredService().getIpAddress(),
            result.getMonitoredService().getIfIndex(),
            result.getMonitoredService().getServiceId(),
            result.getStatus().getStatusCode(),
            result.getStatus().getStatusName(),
            result.getStatus().getReason(),
            result.getId(),
         });
    }
    
    private void doUpdate(PollResult result) {
    	if (isDirty(result)) {

    		// THIS SUCKS!  Muck around with the statment to account for partially null keys. BYUCK!

    		String updateStmt = "update pollresults set pollId = ? nodeId = ?, ipAddr = ?, ifIndex = ?, serviceId = ?, statusCode = ?, statusName = ?, reason = ? where id = ? ";

    		// now to construct the correct array

    		// all but the ifIndex
    		Object[] parms = new Object[] {
    				result.getDemandPoll().getId(),
    				result.getMonitoredService().getNodeId(),
    				result.getMonitoredService().getIpAddress(),
    				result.getMonitoredService().getIfIndex(),
    				result.getMonitoredService().getServiceId(),
    				result.getStatus().getStatusCode(),
    				result.getStatus().getStatusName(),
    				result.getStatus().getReason()
    		};

    		getJdbcTemplate().update(updateStmt, parms);
    	}

    }


}
