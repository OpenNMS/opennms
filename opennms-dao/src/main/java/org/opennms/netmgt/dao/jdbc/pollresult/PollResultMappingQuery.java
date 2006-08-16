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
package org.opennms.netmgt.dao.jdbc.pollresult;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

import org.opennms.netmgt.dao.jdbc.Cache;
import org.opennms.netmgt.dao.jdbc.JdbcSet;
import org.opennms.netmgt.dao.jdbc.monsvc.MonitoredServiceId;
import org.opennms.netmgt.model.DemandPoll;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.PollResult;
import org.opennms.netmgt.model.PollStatus;
import org.springframework.jdbc.object.MappingSqlQuery;

public class PollResultMappingQuery extends MappingSqlQuery {

    public PollResultMappingQuery(DataSource ds, String clause) {
    	
        super(ds, "SELECT r.id as id, " +
        		"r.pollId as pollId, " + //DemandPoll
        		"r.nodeId as nodeId, r.ipAddr as ipAddr, r.ifIndex as ifIndex, r.serviceId as serviceId " + //MonitoredService
        		"r.statusCode as statusCode, r.statusName, r.reason "+ //PollStatus
        		clause);
    }
    
    public DataSource getDataSource() {
        return getJdbcTemplate().getDataSource();
    }

    public Object mapRow(ResultSet rs, int rowNumber) throws SQLException {
    	
        final Integer id = (Integer) rs.getObject("id");

        LazyPollResult result = (LazyPollResult)Cache.obtain(PollResult.class, id);
        result.setLoaded(true);
        
        Integer pollId = (Integer)rs.getObject("pollId");
        DemandPoll poll = (DemandPoll)Cache.obtain(DemandPoll.class, pollId);
        result.setDemandPoll(poll);

        Integer nodeId = (Integer)rs.getObject("nodeId");
        String ipAddr = (String)rs.getObject("ipAddr");
        Integer ifIndex = (Integer)rs.getObject("ifIndex");
        Integer serviceId = (Integer)rs.getObject("serviceId");
        
        MonitoredServiceId monSvcId = new MonitoredServiceId(nodeId, ipAddr, ifIndex, serviceId);
        OnmsMonitoredService monSvc = (OnmsMonitoredService)Cache.obtain(OnmsMonitoredService.class, monSvcId);
        result.setMonitoredService(monSvc);
        
        // TODO add responseTime to pollResults table
        Integer statusCode = (Integer)rs.getObject("statusCode");
        String reason = rs.getString("reason");
        PollStatus status = PollStatus.get(statusCode, reason);
        
        result.setStatus(status);     
        
        result.setDirty(false);
        return result;
    }
    
    public PollResult findUnique() {
        return findUnique((Object[])null);
    }
    
    public PollResult findUnique(Object obj) {
        return findUnique(new Object[] { obj });
    }

    public PollResult findUnique(Object[] objs) {
        List nodes = execute(objs);
        if (nodes.size() > 0)
            return (PollResult) nodes.get(0);
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
        List pollResults = execute(objs);
        Set results = new JdbcSet(pollResults);
        return results;
    }
    
}