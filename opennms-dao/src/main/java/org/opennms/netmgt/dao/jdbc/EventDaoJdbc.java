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
import java.util.Set;

import javax.sql.DataSource;

import org.opennms.netmgt.dao.EventDao;
import org.opennms.netmgt.dao.jdbc.event.EventDelete;
import org.opennms.netmgt.dao.jdbc.event.EventSave;
import org.opennms.netmgt.dao.jdbc.event.EventUpdate;
import org.opennms.netmgt.dao.jdbc.event.FindAll;
import org.opennms.netmgt.dao.jdbc.event.FindByEventId;
import org.opennms.netmgt.dao.jdbc.event.LazyEvent;
import org.opennms.netmgt.model.OnmsEvent;

public class EventDaoJdbc extends AbstractDaoJdbc implements EventDao {
    
    public EventDaoJdbc() {
        super();
    }
    
    public EventDaoJdbc(DataSource ds) {
        super(ds);
    }
    
    public int countAll() {
        return getJdbcTemplate().queryForInt("select count(*) from event");
    }
    
    public void delete(OnmsEvent event) {
        if (event.getId() == null)
            throw new IllegalArgumentException("cannot delete null event");
        
        getEventDeleter().doDelete(event);
    }

    public Collection findAll() {
        return new FindAll(getDataSource()).findSet();
    }

    public void flush() {
    }

    public OnmsEvent get(int id) {
        return get(new Integer(id));
    }

    public OnmsEvent get(Integer id) {
        if (Cache.retrieve(OnmsEvent.class, id) == null)
            return new FindByEventId(getDataSource()).findUnique(id);
        else
            return (OnmsEvent)Cache.retrieve(OnmsEvent.class, id);
    }

    public OnmsEvent load(int id) {
        return load(new Integer(id));
    }

    public OnmsEvent load(Integer id) {
        OnmsEvent event = get(id);
        if (event == null)
            throw new IllegalArgumentException("unable to load event with id "+id);
        
        return event;
    }

    public void save(OnmsEvent event) {
        if (event.getId() != null)
            throw new IllegalArgumentException("Cannot save an event that already has a eventid");
        
        event.setId(allocateId());
        getEventSaver().doInsert(event);
        cascadeSaveAssociations(event);
    }

    public void saveOrUpdate(OnmsEvent event) {
        if (event.getId() == null)
            save(event);
        else
            update(event);
    }

    public void update(OnmsEvent event) {
        if (event.getId() == null)
            throw new IllegalArgumentException("Cannot update an event without a null event");
        
        if (isDirty(event))
        	getEventUpdater().doUpdate(event);
        cascadeUpdateAssociations(event);
    }

    private boolean isDirty(OnmsEvent event) {
    		if (event instanceof LazyEvent) {
    			LazyEvent lazyEvent = (LazyEvent) event;
    			return lazyEvent.isDirty();
    		}
    		return true;
    }

	private Integer allocateId() {
        return new Integer(getJdbcTemplate().queryForInt("SELECT nextval('eventsNxtId')"));
    }

    private void cascadeSaveAssociations(OnmsEvent event) {
    	//TODO: cascade the save to foreign tables
    }

    private void cascadeUpdateAssociations(OnmsEvent event) {
    	//TODO: cascade the save to foreign tables
    }

    private EventDelete getEventDeleter() {
        return new EventDelete(getDataSource());
    }

    private EventSave getEventSaver() {
        return new EventSave(getDataSource());
    }

    private EventUpdate getEventUpdater() {
        return new EventUpdate(getDataSource());
    }


	public Set findEvents(OnmsEvent event) {
		// TODO Auto-generated method stub
		return null;
	}

}
