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
package org.opennms.netmgt.dao.jdbc.monsvc;

import java.util.Date;

import javax.sql.DataSource;

import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsServiceType;

public class LazyMonitoredService extends OnmsMonitoredService {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8190622405479491506L;
	private boolean m_loaded = false;
	private DataSource m_dataSource;
	private boolean m_dirty;
	
	public LazyMonitoredService(DataSource dataSource) {
		m_dataSource = dataSource;
	}

	public Date getLastFail() {
		load();
		return super.getLastFail();
	}

	public Date getLastGood() {
		load();
		return super.getLastGood();
	}

	public String getNotify() {
		load();
		return super.getNotify();
	}

	public String getQualifier() {
		load();
		return super.getQualifier();
	}

	public String getSource() {
		load();
		return super.getSource();
	}

	public String getStatus() {
		load();
		return super.getStatus();
	}

	public void setLastFail(Date lastfail) {
		load();
		setDirty(true);
		super.setLastFail(lastfail);
	}

	public void setLastGood(Date lastgood) {
		load();
		setDirty(true);
		super.setLastGood(lastgood);
	}

	public void setNotify(String notify) {
		load();
		setDirty(true);
		super.setNotify(notify);
	}

	public void setQualifier(String qualifier) {
		load();
		setDirty(true);
		super.setQualifier(qualifier);
	}

	public void setSource(String source) {
		load();
		setDirty(true);
		super.setSource(source);
	}

	public void setStatus(String status) {
		load();
		setDirty(true);
		super.setStatus(status);
	}

	private void load() {
		if (!m_loaded) {
			MonitoredServiceId id = new MonitoredServiceId(this);
			FindById.get(m_dataSource, id).find(id);
		}
	}

	public boolean isLoaded() {
		return m_loaded;
	}

	public void setLoaded(boolean loaded) {
		m_loaded = loaded;
	}

	public boolean isDirty() {
		return m_dirty;
	}
	
	public void setDirty(boolean dirty) {
		m_dirty = dirty;
	}

	public void setIpInterface(OnmsIpInterface ipInterface) {
		setDirty(true);
		super.setIpInterface(ipInterface);
	}

	public void setServiceType(OnmsServiceType service) {
		setDirty(true);
		super.setServiceType(service);
	}
	
}
