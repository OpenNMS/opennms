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

import java.util.Date;
import java.util.Set;

import javax.sql.DataSource;

import org.opennms.netmgt.model.DemandPoll;
import org.opennms.netmgt.model.PollResult;

public class LazyDemandPoll extends DemandPoll {
	
	private static final long serialVersionUID = 1L;

	private DataSource m_dataSource;
	private boolean m_dirty;
	private boolean m_loaded = false;
	
	public LazyDemandPoll(DataSource dataSource) {
		m_dataSource = dataSource;
	}

	@Override
	public Date getRequestTime() {
		load();
		return super.getRequestTime();
	}

	@Override
	public String getUser() {
		load();
		return super.getUser();
	}

	@Override
	public String getDescription() {
		load();
		return super.getDescription();
	}

	@Override
	public Set<PollResult> getPollResults() {
		load();
		return super.getPollResults();
	}

	@Override
	public void setRequestTime(Date requestTime) {
		load();
		setDirty(true);
		super.setRequestTime(requestTime);
	}

	public void setDirty(boolean dirty) {
		m_dirty = dirty;
	}

	@Override
	public void setPollResults(Set<PollResult> pollResults) {
		load();
		setDirty(true);
		super.setPollResults(pollResults);
	}

	@Override
	public void setDescription(String description) {
		load();
		setDirty(true);
		super.setDescription(description);
	}


	public boolean isDirty() {
		return m_dirty;
	}

	public boolean isLoaded() {
		return m_loaded;
	}

	public void setLoaded(boolean loaded) {
		m_loaded = loaded;
	}

    public String toString() {
		load();
		setDirty(true);
		return super.toString();
	}

    private void load() {
		if (!m_loaded) {
			new FindById(m_dataSource).findUnique(getId());
		}
	}	

}
