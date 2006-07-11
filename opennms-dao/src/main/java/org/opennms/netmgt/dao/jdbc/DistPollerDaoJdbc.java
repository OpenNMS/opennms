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

import org.opennms.netmgt.dao.DistPollerDao;
import org.opennms.netmgt.dao.jdbc.distpoller.FindAll;
import org.opennms.netmgt.dao.jdbc.distpoller.FindByName;
import org.opennms.netmgt.model.OnmsDistPoller;

public class DistPollerDaoJdbc extends AbstractDaoJdbc implements DistPollerDao {
    
    public DistPollerDaoJdbc() {
        super();
    }
    
    public DistPollerDaoJdbc(DataSource ds) {
        super(ds);
    }
    


    public int countAll() {
        return getJdbcTemplate().queryForInt("select count(*) from distPoller");
    }
    
    public void delete(OnmsDistPoller distPoller) {
        getJdbcTemplate().update("delete from distPoller where dpName = ?", new Object[] { distPoller.getName() });
    }

    public Collection findAll() {
        return new FindAll(getDataSource()).findSet();
    }

    public void flush() {
    }

    public OnmsDistPoller get(String name) {
        return new FindByName(getDataSource()).findUnique(name);
    }

    public OnmsDistPoller load(String name) {
        OnmsDistPoller distPoller = get(name);
        if (distPoller == null)
            throw new IllegalArgumentException("unable to load distPoller with name "+name);
        
        return distPoller;

    }

    public void save(OnmsDistPoller distPoller) {
        if (exists(distPoller))
            throw new IllegalArgumentException("a distpoller with that name already exists");
        
        doSave(distPoller);
        
    }

    public void saveOrUpdate(OnmsDistPoller distPoller) {
        if (exists(distPoller))
            update(distPoller);
        else
            save(distPoller);
    }

    public void update(OnmsDistPoller distPoller) {
        if (!exists(distPoller))
            throw new IllegalArgumentException("a distpoller with that name does not exist");
        
        doUpdate(distPoller);
        
    }

    private void doSave(OnmsDistPoller distPoller) {
        getJdbcTemplate().update("insert into distPoller (dpIP, dpComment, dpDiscLimit, dpLastNodePull, dpLastEventPull, dpLastPackagePush, dpAdminState, dpRunState, dpName) values (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                 new Object[] {
                         distPoller.getIpAddress(), // dpIP                    varchar(16) not null,
                         distPoller.getComment(),   // dpComment               varchar(256),
                         distPoller.getDiscoveryLimit(), //dpDiscLimit             numeric(5,2),
                         distPoller.getLastNodePull(), // dpLastNodePull          timestamp without time zone,
                         distPoller.getLastEventPull(), //dpLastEventPull         timestamp without time zone,
                         distPoller.getLastPackagePush(), //dpLastPackagePush       timestamp without time zone,
                         distPoller.getAdminState(), // dpAdminState            integer,
                         distPoller.getRunState(), //dpRunState              integer,
                         distPoller.getName(), //dpName                  varchar(12) not null,
                 });
    }

    private void doUpdate(OnmsDistPoller distPoller) {
        getJdbcTemplate().update("update distPoller set dpIP = ?, dpComment = ?, dpDiscLimit = ?, dpLastNodePull = ?, dpLastEventPull = ?, dpLastPackagePush = ?, dpAdminState = ?, dpRunState = ? where dpName = ?",
                 new Object[] {
                         distPoller.getIpAddress(), // dpIP                    varchar(16) not null,
                         distPoller.getComment(),   // dpComment               varchar(256),
                         distPoller.getDiscoveryLimit(), //dpDiscLimit             numeric(5,2),
                         distPoller.getLastNodePull(), // dpLastNodePull          timestamp without time zone,
                         distPoller.getLastEventPull(), //dpLastEventPull         timestamp without time zone,
                         distPoller.getLastPackagePush(), //dpLastPackagePush       timestamp without time zone,
                         distPoller.getAdminState(), // dpAdminState            integer,
                         distPoller.getRunState(), //dpRunState              integer,
                         distPoller.getName(), //dpName                  varchar(12) not null,
                 });
    }

    private boolean exists(OnmsDistPoller distPoller) {
        int count = getJdbcTemplate().queryForInt("select count(*) from distPoller where dpName = ?", new Object[] { distPoller.getName() });
        return count > 0;
    }

}
