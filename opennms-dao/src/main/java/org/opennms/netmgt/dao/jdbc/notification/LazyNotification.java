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
package org.opennms.netmgt.dao.jdbc.notification;

import java.util.Date;
import java.util.Set;

import javax.sql.DataSource;

import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsNotification;
import org.opennms.netmgt.model.OnmsServiceType;

public class LazyNotification extends OnmsNotification {

	private static final long serialVersionUID = -8549615324373817847L;
	private boolean m_loaded;
	private boolean m_dirty;
	private DataSource m_dataSource;
	
	public LazyNotification(DataSource dataSource) {
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
		return super.toString();
	}

    private void load() {
		if (!m_loaded) {
			new FindByNotifyId(m_dataSource).findUnique(getNotifyId());
		}
	}

    @Override
    public String getAnsweredBy() {
        load();
        return super.getAnsweredBy();
    }

    @Override
    public OnmsEvent getEvent() {
        load();
        return super.getEvent();
    }

    @Override
    public String getIpAddress() {
        load();
        return super.getIpAddress();
    }

    @Override
    public OnmsNode getNode() {
        load();
        return super.getNode();
    }

    @Override
    public String getNumericMsg() {
        load();
        return super.getNumericMsg();
    }

    @Override
    public Date getPageTime() {
        load();
        return super.getPageTime();
    }

    @Override
    public String getQueueId() {
        load();
        return super.getQueueId();
    }

    @Override
    public Date getRespondTime() {
        load();
        return super.getRespondTime();
    }

    @Override
    public OnmsServiceType getServiceType() {
        load();
        return super.getServiceType();
    }

    @Override
    public String getSubject() {
        load();
        return super.getSubject();
    }

    @Override
    public String getTextMsg() {
        load();
        return super.getTextMsg();
    }

    @Override
    public Set getUsersNotified() {
        load();
        return super.getUsersNotified();
    }
    
    @Override
    public String getNotifConfigName() {
        load();
        return super.getNotifConfigName();
    }

    @Override
    public void setAnsweredBy(String answeredby) {
        load();
		setDirty(true);
        super.setAnsweredBy(answeredby);
    }

    @Override
    public void setEvent(OnmsEvent event) {
        load();
		setDirty(true);
        super.setEvent(event);
    }

    @Override
    public void setIpAddress(String ipAddress) {
        load();
		setDirty(true);
        super.setIpAddress(ipAddress);
    }

    @Override
    public void setNode(OnmsNode node) {
        load();
		setDirty(true);
        super.setNode(node);
    }

    @Override
    public void setNumericMsg(String numericmsg) {
        load();
		setDirty(true);
        super.setNumericMsg(numericmsg);
    }

    @Override
    public void setPageTime(Date pagetime) {
        load();
		setDirty(true);
        super.setPageTime(pagetime);
    }

    @Override
    public void setQueueId(String queueid) {
        load();
		setDirty(true);
        super.setQueueId(queueid);
    }

    @Override
    public void setRespondTime(Date respondtime) {
        load();
		setDirty(true);
        super.setRespondTime(respondtime);
    }

    @Override
    public void setServiceType(OnmsServiceType serviceType) {
        load();
		setDirty(true);
        super.setServiceType(serviceType);
    }

    @Override
    public void setSubject(String subject) {
        load();
		setDirty(true);
        super.setSubject(subject);
    }

    @Override
    public void setTextMsg(String textmsg) {
        load();
		setDirty(true);
        super.setTextMsg(textmsg);
    }

    @Override
    public void setUsersNotified(Set usersnotifieds) {
        load();
		setDirty(true);
		super.setUsersNotified(usersnotifieds);
    }

    @Override
    public void setNotifConfigName(String notifConfigName) {
        load();
        setDirty(true);
        super.setNotifConfigName(notifConfigName);
    }
    

}
