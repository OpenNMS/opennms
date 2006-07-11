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
package org.opennms.netmgt.dao.jdbc.outage;

import java.util.Date;

import javax.sql.DataSource;

import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsOutage;

public class LazyOutage extends OnmsOutage {

	private static final long serialVersionUID = -8549615324373817847L;
	private boolean m_loaded;
	private boolean m_dirty;
	private DataSource m_dataSource;
	
	public LazyOutage(DataSource dataSource) {
		m_dataSource = dataSource;
	}

	public void setLoaded(boolean loaded) {
		m_loaded = loaded;
	}
	
	public boolean getLoaded() {
		return m_loaded;
	}

	public boolean isDirty() {
		return m_dirty;
	}

	public void setDirty(boolean dirty) {
		m_dirty = dirty;
	}

	public String toString() {
		load();
		setDirty(true);
		return super.toString();
	}

    private void load() {
		if (!m_loaded) {
			new FindByOutageId(m_dataSource).findUnique(getId());
		}
	}

    public OnmsEvent getEventBySvcLostEvent() {
        load();
        return super.getEventBySvcLostEvent();
    }

    public OnmsEvent getEventBySvcRegainedEvent() {
        load();
        return super.getEventBySvcRegainedEvent();
    }

    public Date getIfLostService() {
        load();
        return super.getIfLostService();
    }

    public Date getIfRegainedService() {
        load();
        return super.getIfRegainedService();
    }

    public OnmsMonitoredService getMonitoredService() {
        load();
        return super.getMonitoredService();
    }

    public void setEventBySvcLostEvent(OnmsEvent eventBySvcLostEvent) {
        load();
        super.setEventBySvcLostEvent(eventBySvcLostEvent);
    }

    public void setEventBySvcRegainedEvent(OnmsEvent eventBySvcRegainedEvent) {
        load();
        super.setEventBySvcRegainedEvent(eventBySvcRegainedEvent);
    }

    public void setIfLostService(Date ifLostService) {
        load();
        super.setIfLostService(ifLostService);
    }

    public void setIfRegainedService(Date ifRegainedService) {
        load();
        super.setIfRegainedService(ifRegainedService);
    }

    public void setMonitoredService(OnmsMonitoredService monitoredService) {
        load();
        super.setMonitoredService(monitoredService);
    }

}
