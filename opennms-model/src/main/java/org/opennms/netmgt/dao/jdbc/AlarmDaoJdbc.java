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

import org.opennms.netmgt.dao.AlarmDao;
import org.opennms.netmgt.dao.jdbc.alarm.AlarmDelete;
import org.opennms.netmgt.dao.jdbc.alarm.AlarmSave;
import org.opennms.netmgt.dao.jdbc.alarm.AlarmUpdate;
import org.opennms.netmgt.dao.jdbc.alarm.FindAll;
import org.opennms.netmgt.dao.jdbc.alarm.FindByAlarmId;
import org.opennms.netmgt.dao.jdbc.alarm.LazyAlarm;
import org.opennms.netmgt.model.OnmsAlarm;

public class AlarmDaoJdbc extends AbstractDaoJdbc implements AlarmDao {
    
    public AlarmDaoJdbc() {
        super();
    }
    
    public AlarmDaoJdbc(DataSource ds) {
        super(ds);
    }
    
    public int countAll() {
        return getJdbcTemplate().queryForInt("select count(*) from alarm");
    }
    
    public void delete(OnmsAlarm alarm) {
        if (alarm.getId() == null)
            throw new IllegalArgumentException("cannot delete null alarm");
        
        getAlarmDeleter().doDelete(alarm);
    }

    public Collection findAll() {
        return new FindAll(getDataSource()).findSet();
    }

    public void flush() {
    }

    public OnmsAlarm get(int id) {
        return get(new Integer(id));
    }

    public OnmsAlarm get(Integer id) {
        if (id == null)
            throw new IllegalArgumentException("cannot retrieve null alarm");
        if (Cache.retrieve(OnmsAlarm.class, id) == null)
            return new FindByAlarmId(getDataSource()).findUnique(id);
        else
            return (OnmsAlarm)Cache.retrieve(OnmsAlarm.class, id);
    }

    public OnmsAlarm load(int id) {
        return load(new Integer(id));
    }

    public OnmsAlarm load(Integer id) {
        OnmsAlarm alarm = get(id);
        if (alarm == null)
            throw new IllegalArgumentException("unable to load alarm with id "+id);
        
        return alarm;
    }

    public void save(OnmsAlarm alarm) {
        if (alarm.getId() != null)
            throw new IllegalArgumentException("Cannot save an alarm that already has a alarmid");
        
        alarm.setId(allocateId());
        getAlarmSaver().doInsert(alarm);
        cascadeSaveAssociations(alarm);
    }

    public void saveOrUpdate(OnmsAlarm alarm) {
        if (alarm.getId() == null)
            save(alarm);
        else
            update(alarm);
    }

    public void update(OnmsAlarm alarm) {
        if (alarm.getId() == null)
            throw new IllegalArgumentException("Cannot update an alarm without a null alarm");
        
        if (isDirty(alarm))
        	getAlarmUpdater().doUpdate(alarm);
        cascadeUpdateAssociations(alarm);
    }

    private boolean isDirty(OnmsAlarm alarm) {
    		if (alarm instanceof LazyAlarm) {
    			LazyAlarm lazyAlarm = (LazyAlarm) alarm;
    			return lazyAlarm.isDirty();
    		}
    		return true;
    }

	private Integer allocateId() {
        return new Integer(getJdbcTemplate().queryForInt("SELECT nextval('alarmsNxtId')"));
    }

    private void cascadeSaveAssociations(OnmsAlarm alarm) {
    	//TODO: cascade the save to foreign tables
    }

    private void cascadeUpdateAssociations(OnmsAlarm alarm) {
    	//TODO: cascade the save to foreign tables
    }

    private AlarmDelete getAlarmDeleter() {
        return new AlarmDelete(getDataSource());
    }

    private AlarmSave getAlarmSaver() {
        return new AlarmSave(getDataSource());
    }

    private AlarmUpdate getAlarmUpdater() {
        return new AlarmUpdate(getDataSource());
    }


	public Set findAlarms(OnmsAlarm alarm) {
		// TODO Auto-generated method stub
		return null;
	}

}
