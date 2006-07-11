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
import org.opennms.netmgt.model.OnmsIpInterface;
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
		setDirty(true);
		return super.toString();
	}

    private void load() {
		if (!m_loaded) {
			new FindByNotifyId(m_dataSource).findUnique(getNotifyId());
		}
	}

    public String getAnsweredBy() {
        load();
        return super.getAnsweredBy();
    }

    public OnmsEvent getEvent() {
        load();
        return super.getEvent();
    }

    public OnmsIpInterface getInterface() {
        load();
        return super.getInterface();
    }

    public OnmsNode getNode() {
        load();
        return super.getNode();
    }

    public String getNumericMsg() {
        load();
        return super.getNumericMsg();
    }

    public Date getPageTime() {
        load();
        return super.getPageTime();
    }

    public String getQueueId() {
        load();
        return super.getQueueId();
    }

    public Date getRespondTime() {
        load();
        return super.getRespondTime();
    }

    public OnmsServiceType getServiceType() {
        load();
        return super.getServiceType();
    }

    public String getSubject() {
        load();
        return super.getSubject();
    }

    public String getTextMsg() {
        load();
        return super.getTextMsg();
    }

    public Set getUsersNotified() {
        load();
        return super.getUsersNotified();
    }

    public void setAnsweredBy(String answeredby) {
        load();
        super.setAnsweredBy(answeredby);
    }

    public void setEvent(OnmsEvent event) {
        load();
        super.setEvent(event);
    }

    public void setInterface(OnmsIpInterface interfaceId) {
        load();
        super.setInterface(interfaceId);
    }

    public void setNode(OnmsNode node) {
        load();
        super.setNode(node);
    }

    public void setNumericMsg(String numericmsg) {
        load();
        super.setNumericMsg(numericmsg);
    }

    public void setPageTime(Date pagetime) {
        load();
        super.setPageTime(pagetime);
    }

    public void setQueueId(String queueid) {
        load();
        super.setQueueId(queueid);
    }

    public void setRespondTime(Date respondtime) {
        load();
        super.setRespondTime(respondtime);
    }

    public void setServiceType(OnmsServiceType serviceType) {
        load();
        super.setServiceType(serviceType);
    }

    public void setSubject(String subject) {
        load();
        super.setSubject(subject);
    }

    public void setTextMsg(String textmsg) {
        load();
        super.setTextMsg(textmsg);
    }

    public void setUsersNotified(Set usersnotifieds) {
        load();
        super.setUsersNotified(usersnotifieds);
    }

}
