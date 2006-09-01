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
package org.opennms.netmgt.dao.jdbc.usernotification;

import java.util.Date;

import javax.sql.DataSource;

import org.opennms.netmgt.model.EntityVisitor;
import org.opennms.netmgt.model.OnmsNotification;
import org.opennms.netmgt.model.OnmsUserNotification;

public class LazyUserNotification extends OnmsUserNotification {

	private static final long serialVersionUID = -8549615324373817847L;
	private boolean m_loaded;
	private boolean m_dirty;
	private DataSource m_dataSource;
	
	public LazyUserNotification(DataSource dataSource) {
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
            new FindById(m_dataSource).findUnique(getId());
		}
	}

    public String getAutoNotify() {
        load();
        return super.getAutoNotify();
    }

    public String getContactInfo() {
        load();
        return super.getContactInfo();
    }

    public String getMedia() {
        load();
        return super.getMedia();
    }

    public OnmsNotification getNotification() {
        load();
        return super.getNotification();
    }

    public Date getNotifyTime() {
        load();
        return super.getNotifyTime();
    }

    public String getUserId() {
        load();
        return super.getUserId();
    }

    public void setAutoNotify(String autoNotify) {
        load();
        super.setAutoNotify(autoNotify);
    }

    public void setContactInfo(String contactInfo) {
        load();
        super.setContactInfo(contactInfo);
    }

    public void setMedia(String media) {
        load();
        super.setMedia(media);
    }

    public void setNotification(OnmsNotification notification) {
        load();
        super.setNotification(notification);
    }

    public void setNotifyTime(Date notifyTime) {
        load();
        super.setNotifyTime(notifyTime);
    }

    public void setUserId(String userId) {
        load();
        super.setUserId(userId);
    }


}
