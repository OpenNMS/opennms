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

import javax.sql.DataSource;

import org.opennms.netmgt.dao.DemandPollDao;
import org.opennms.netmgt.dao.jdbc.demandpoll.DemandPollDelete;
import org.opennms.netmgt.dao.jdbc.demandpoll.DemandPollSave;
import org.opennms.netmgt.dao.jdbc.demandpoll.DemandPollUpdate;
import org.opennms.netmgt.dao.jdbc.demandpoll.FindAll;
import org.opennms.netmgt.dao.jdbc.demandpoll.FindById;
import org.opennms.netmgt.dao.jdbc.demandpoll.LazyDemandPoll;
import org.opennms.netmgt.model.DemandPoll;

public class DemandPollDaoJdbc extends AbstractDaoJdbc implements DemandPollDao {
	
	private String m_allocateIdStmt = "SELECT nextval('demandPollNxtId')"; 
    
    public DemandPollDaoJdbc() {
        super();
    }
    
    
    
    public DemandPollDaoJdbc(DataSource ds) {
        super(ds);
    }
    
    public void setAllocateIdStmt(String allocateIdStmt) {
		m_allocateIdStmt = allocateIdStmt;
	}
    
    public int countAll() {
        return getJdbcTemplate().queryForInt("select count(*) from demandpoll");
    }
    
    public void delete(DemandPoll poll) {
        if (poll.getId() == null)
            throw new IllegalArgumentException("cannot delete a demand poll with id = null");
        
        getDemandPollDeleter().doDelete(poll);
    }

    public Collection findAll() {
        return new FindAll(getDataSource()).findSet();
    }

    public DemandPoll get(Integer id) {
        if (Cache.retrieve(DemandPoll.class, id) == null)
            return new FindById(getDataSource()).findUnique(id);
        else
            return (DemandPoll)Cache.retrieve(DemandPoll.class, id);
    }

    public DemandPoll load(Integer id) {
        DemandPoll poll = get(id);
        if (poll == null)
            throw new IllegalArgumentException("unable to load a demand poll with id "+id);
        
        return poll;
    }

    public void save(DemandPoll poll) {
        if (poll.getId() != null)
            throw new IllegalArgumentException("Cannot save a poll that already has a id");
        
        poll.setId(allocateId());
        getDemandPollSaver().doInsert(poll);
        cascadeSaveAssociations(poll);
    }

    public void saveOrUpdate(DemandPoll poll) {
        if (poll.getId() == null)
            save(poll);
        else
            update(poll);
    }

    public void update(DemandPoll poll) {
        if (poll.getId() == null)
            throw new IllegalArgumentException("Cannot update a demand poll without a id");
        
        if (isDirty(poll))
        		getDemandPollUpdater().doUpdate(poll);
        cascadeUpdateAssociations(poll);
    }

    private boolean isDirty(DemandPoll poll) {
    		if (poll instanceof LazyDemandPoll) {
    			LazyDemandPoll lazyPoll = (LazyDemandPoll) poll;
    			return lazyPoll.isDirty();
    		}
    		return true;
    }

	private Integer allocateId() {
        return new Integer(getJdbcTemplate().queryForInt(m_allocateIdStmt));
    }

    private void cascadeSaveAssociations(DemandPoll poll) {
        getPollResultDao().saveOrUpdateResultsForPoll(poll);
    }

    private void cascadeUpdateAssociations(DemandPoll poll) {
        getPollResultDao().saveOrUpdateResultsForPoll(poll);
    }
    
    private PollResultDaoJdbc getPollResultDao() {
    	return new PollResultDaoJdbc(getDataSource());
    }

    private DemandPollDelete getDemandPollDeleter() {
        return new DemandPollDelete(getDataSource());
    }

    private DemandPollSave getDemandPollSaver() {
        return new DemandPollSave(getDataSource());
    }

    private DemandPollUpdate getDemandPollUpdater() {
        return new DemandPollUpdate(getDataSource());
    }

	public void flush() {
	}

}
