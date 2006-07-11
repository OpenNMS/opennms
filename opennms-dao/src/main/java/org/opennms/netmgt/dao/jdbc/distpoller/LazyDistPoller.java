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

import java.math.BigDecimal;
import java.util.Date;

import javax.sql.DataSource;

import org.opennms.netmgt.model.OnmsDistPoller;

public class LazyDistPoller extends OnmsDistPoller {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -836488410614505130L;
	private boolean m_loaded = false;
	private DataSource m_dataSource;
	
	public LazyDistPoller(DataSource dataSource) {
		m_dataSource = dataSource;
	}

	public Integer getAdminState() {
		load();
		return super.getAdminState();
	}

	public String getComment() {
		load();
		return super.getComment();
	}

	public BigDecimal getDiscoveryLimit() {
		load();
		return super.getDiscoveryLimit();
	}

	public String getIpAddress() {
		load();
		return super.getIpAddress();
	}

	public Date getLastEventPull() {
		load();
		return super.getLastEventPull();
	}

	public Date getLastNodePull() {
		load();
		return super.getLastNodePull();
	}

	public Date getLastPackagePush() {
		load();
		return super.getLastPackagePush();
	}

	public Integer getRunState() {
		load();
		return super.getRunState();
	}

	public void setAdminState(Integer dpadminstate) {
		load();
		super.setAdminState(dpadminstate);
	}

	public void setComment(String dpcomment) {
		load();
		super.setComment(dpcomment);
	}

	public void setDiscoveryLimit(BigDecimal dpdisclimit) {
		load();
		super.setDiscoveryLimit(dpdisclimit);
	}

	public void setIpAddress(String dpip) {
		load();
		super.setIpAddress(dpip);
	}

	public void setLastEventPull(Date dplasteventpull) {
		load();
		super.setLastEventPull(dplasteventpull);
	}

	public void setLastNodePull(Date dplastnodepull) {
		load();
		super.setLastNodePull(dplastnodepull);
	}

	public void setLastPackagePush(Date dplastpackagepush) {
		load();
		super.setLastPackagePush(dplastpackagepush);
	}

	public void setRunState(Integer dprunstate) {
		load();
		super.setRunState(dprunstate);
	}

	private void load() {
		if (!m_loaded) {
			new FindByName(m_dataSource).findUnique(getName());
		}
	}

	public boolean isLoaded() {
		return m_loaded;
	}

	public void setLoaded(boolean loaded) {
		m_loaded = loaded;
	}
	

}
