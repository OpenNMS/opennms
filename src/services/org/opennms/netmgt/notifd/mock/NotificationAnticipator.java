//
//This file is part of the OpenNMS(R) Application.
//
//OpenNMS(R) is Copyright (C) 2004 The OpenNMS Group, Inc.  All rights reserved.
//OpenNMS(R) is a derivative work, containing both original code, included code and modified
//code that was published under the GNU General Public License. Copyrights for modified 
//and included code are below.
//
//OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
//Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
//This program is free software; you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation; either version 2 of the License, or
//(at your option) any later version.
//
//This program is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.
//
//You should have received a copy of the GNU General Public License
//along with this program; if not, write to the Free Software
//Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
//For more information contact:
//OpenNMS Licensing       <license@opennms.org>
//http://www.opennms.org/
//http://www.opennms.com/
//
package org.opennms.netmgt.notifd.mock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author brozow
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class NotificationAnticipator {
    
    List m_anticipated = new ArrayList();
    
    List m_unanticipated = new ArrayList();
    
    /**
     */
    public NotificationAnticipator() {
    }
    
    /**
     * @param event
     * 
     */
    public void anticipateNotification(MockNotification mn) {
        m_anticipated.add(mn);
    }
    
    /**
     * @param event
     */
    public synchronized void notificationReceived(MockNotification mn) {
        if (m_anticipated.contains(mn)) {
            m_anticipated.remove(mn);
            notifyAll();
        } else {
            m_unanticipated.add(mn);
        }
    }
    
    public Collection getAnticipatedNotifications() {
        return Collections.unmodifiableCollection(m_anticipated);
    }
    
    public void reset() {
        m_anticipated = new ArrayList();
        m_unanticipated = new ArrayList();
    }
    
    /**
     * @return
     */
    public Collection getUnanticipated() {
        return Collections.unmodifiableCollection(m_unanticipated);
    }
    
    /**
     * @param i
     * @return
     */
    public synchronized Collection waitForAnticipated(long millis) {
        long waitTime = millis;
        long start = System.currentTimeMillis();
        long now = start;
        while (waitTime > 0) {
            if (m_anticipated.isEmpty())
                return Collections.EMPTY_LIST;
            try {
                wait(waitTime);
            } catch (InterruptedException e) {
            }
            now = System.currentTimeMillis();
            waitTime -= (now - start);
        }
        return getAnticipatedNotifications();
    }
    
}
