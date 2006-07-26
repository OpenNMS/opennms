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
package org.opennms.netmgt.dao.jdbc.demandpoll;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

import org.opennms.netmgt.dao.jdbc.Cache;
import org.opennms.netmgt.dao.jdbc.JdbcSet;
import org.opennms.netmgt.dao.jdbc.LazySet;
import org.opennms.netmgt.dao.jdbc.pollresult.FindByDemandPoll;
import org.opennms.netmgt.model.DemandPoll;
import org.springframework.jdbc.object.MappingSqlQuery;

public class DemandPollMappingQuery extends MappingSqlQuery {

    public DemandPollMappingQuery(DataSource ds, String clause) {
        super(ds, "SELECT p.id as id, p.requestTime as requestTime, p.user as user, p.description as description "+clause);
    }
    
    public DataSource getDataSource() {
        return getJdbcTemplate().getDataSource();
    }

    public Object mapRow(ResultSet rs, int rowNumber) throws SQLException {
        final Integer id = (Integer) rs.getObject("id");

        LazyDemandPoll poll = (LazyDemandPoll)Cache.obtain(DemandPoll.class, id);
        poll.setLoaded(true);
        
        poll.setRequestTime(rs.getTime("requestTime"));
        poll.setUser(rs.getString("user"));
        poll.setDescription(rs.getString("description"));
        
        LazySet.Loader pollLoader = new LazySet.Loader() {

			public Set load() {
				return new FindByDemandPoll(getDataSource()).findSet(id);
			}
        	
        };
        
		poll.setPollResults(new LazySet(pollLoader));
                
        poll.setDirty(false);
        return poll;
    }
    
    public DemandPoll findUnique() {
        return findUnique((Object[])null);
    }
    
    public DemandPoll findUnique(Object obj) {
        return findUnique(new Object[] { obj });
    }

    public DemandPoll findUnique(Object[] objs) {
        List poll = execute(objs);
        if (poll.size() > 0)
            return (DemandPoll) poll.get(0);
        else
            return null;
    }
    
    public Set findSet() {
        return findSet((Object[])null);
    }
    
    public Set findSet(Object obj) {
        return findSet(new Object[] { obj });
    }
    
    public Set findSet(Object[] objs) {
        List polls = execute(objs);
        Set results = new JdbcSet(polls);
        return results;
    }
    
}