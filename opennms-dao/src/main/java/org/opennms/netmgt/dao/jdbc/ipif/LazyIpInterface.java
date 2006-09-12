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
package org.opennms.netmgt.dao.jdbc.ipif;

import java.util.Date;
import java.util.Set;

import javax.sql.DataSource;

import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;

public class LazyIpInterface extends OnmsIpInterface {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3388830282688753457L;
	private boolean m_loaded = false;
	private DataSource m_dataSource;
	private boolean m_dirty;
	
	public LazyIpInterface(DataSource dataSource) {
		m_dataSource = dataSource;
	}

	public String getIpHostName() {
		load();
		return super.getIpHostName();
	}

	public Date getIpLastCapsdPoll() {
		load();
		return super.getIpLastCapsdPoll();
	}

	public Integer getIpStatus() {
		load();
		return super.getIpStatus();
	}

	public String getIsManaged() {
		load();
		return super.getIsManaged();
	}

	public CollectionType getIsSnmpPrimary() {
		load();
		return super.getIsSnmpPrimary();
	}

	public Set getMonitoredServices() {
		load();
		return super.getMonitoredServices();
	}

	public void setIpHostName(String iphostname) {
		load();
		setDirty(true);
		super.setIpHostName(iphostname);
	}

	public void setIpLastCapsdPoll(Date iplastcapsdpoll) {
		load();
		setDirty(true);
		super.setIpLastCapsdPoll(iplastcapsdpoll);
	}

	public void setIpStatus(Integer ipstatus) {
		load();
		setDirty(true);
		super.setIpStatus(ipstatus);
	}

	public void setIsManaged(String ismanaged) {
		load();
		setDirty(true);
		super.setIsManaged(ismanaged);
	}

	public void setIsSnmpPrimary(CollectionType issnmpprimary) {
		load();
		setDirty(true);
		super.setIsSnmpPrimary(issnmpprimary);
	}

	public void setMonitoredServices(Set ifServices) {
		load();
		setDirty(true);
		super.setMonitoredServices(ifServices);
	}

	private void load() {
		if (!m_loaded) {
			IpInterfaceId id = new IpInterfaceId(this);
			FindById.get(m_dataSource, id).find(id);
		}
	}

	public void setLoaded(boolean loaded) {
		m_loaded = loaded;
	}
	
	public boolean isLoaded() {
		return m_loaded;
	}

	public boolean isDirty() {
		return m_dirty;
	}
	
	public void setDirty(boolean dirty) {
		m_dirty = dirty;
	}

	public void setIpAddress(String ipaddr) {
		setDirty(true);
		super.setIpAddress(ipaddr);
	}

	public void setNode(OnmsNode node) {
		setDirty(true);
		super.setNode(node);
	}

}
