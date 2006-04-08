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
package org.opennms.netmgt.dao.jdbc.distpoller;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

import org.opennms.netmgt.dao.jdbc.Cache;
import org.opennms.netmgt.dao.jdbc.JdbcSet;
import org.opennms.netmgt.model.OnmsDistPoller;
import org.springframework.jdbc.object.MappingSqlQuery;

public class DistPollerMappingQuery extends MappingSqlQuery {

    public DistPollerMappingQuery(DataSource ds, String clause) {
        super(ds, "SELECT dpName, dpIP, dpComment, dpDiscLimit, dpLastNodePull, dpLastEventPull, dpLastPackagePush, dpAdminState, dpRunState "+clause);


    }
    
    public DataSource getDataSource() {
        return getJdbcTemplate().getDataSource();
    }

    public Object mapRow(ResultSet rs, int rowNumber) throws SQLException {
        String dpName = rs.getString("dpName"); //dpName                  varchar(12) not null,
        LazyDistPoller distPoller = (LazyDistPoller)Cache.obtain(OnmsDistPoller.class, dpName);
        distPoller.setLoaded(true);
        distPoller.setIpAddress(rs.getString("dpIP"));                 //dpIP                    varchar(16) not null,
        distPoller.setComment(rs.getString("dpComment"));              //dpComment               varchar(256),
        distPoller.setDiscoveryLimit(rs.getBigDecimal("dpDiscLimit")); //dpDiscLimit             numeric(5,2),
        distPoller.setLastNodePull(rs.getTimestamp("dpLastNodePull")); //dpLastNodePull          timestamp without time zone,
        distPoller.setLastEventPull(rs.getTimestamp("dpLastEventPull"));//dpLastEventPull         timestamp without time zone,
        distPoller.setLastPackagePush(rs.getTimestamp("dpLastPackagePush"));//dpLastPackagePush       timestamp without time zone,
        distPoller.setAdminState((Integer)rs.getObject("dpAdminState"));//dpAdminState            integer,
        distPoller.setRunState((Integer)rs.getObject("dpRunState"));      //dpRunState              integer,
        return distPoller;
    }
    
    public OnmsDistPoller findUnique() {
        return findUnique((Object[])null);
    }
    
    public OnmsDistPoller findUnique(Object obj) {
        return findUnique(new Object[] { obj });
    }

    public OnmsDistPoller findUnique(Object[] objs) {
        List nodes = execute(objs);
        if (nodes.size() > 0)
            return (OnmsDistPoller) nodes.get(0);
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
        List nodes = execute(objs);
        Set results = new JdbcSet(nodes);
        return results;
    }
    
}