//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
// Tab Size = 8
//

package org.opennms.netmgt.notifd.jmx;

import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.DatabaseConnectionFactory;
import org.opennms.netmgt.config.DestinationPathFactory;
import org.opennms.netmgt.config.GroupFactory;
import org.opennms.netmgt.config.NotifdConfigFactory;
import org.opennms.netmgt.config.NotificationCommandFactory;
import org.opennms.netmgt.config.NotificationFactory;
import org.opennms.netmgt.config.UserFactory;
import org.opennms.netmgt.eventd.EventIpcManagerFactory;

public class Notifd implements NotifdMBean {
    public void init() {
        EventIpcManagerFactory.init();

        try {
            NotifdConfigFactory.init();
        } catch (Throwable t) {
            ThreadCategory.getInstance(getClass()).warn("start: Failed to init NotifdConfigFactory.", t);
        }
        
        try {
            NotificationFactory.init();
        } catch( Throwable t) {
            ThreadCategory.getInstance(getClass()).warn("start: Failed to init NotificationFactory.", t);
        }
        
        try {
            DatabaseConnectionFactory.init();
        } catch (Exception e) {
            ThreadCategory.getInstance(getClass()).warn("start: Failed to init database connection factory.", e);
        }

        try {
            GroupFactory.init();
        } catch (Exception e) {
            ThreadCategory.getInstance(getClass()).warn("start: Failed to init group factory.", e);
        }

        try {
            UserFactory.init();
        } catch (Exception e) {
            ThreadCategory.getInstance(getClass()).warn("start: Failed to init user factory.", e);
        }
        
        try {
            DestinationPathFactory.init();
        } catch (Exception e) {
            ThreadCategory.getInstance(getClass()).warn("start: Failed to init destination path factory.", e);
        }
        
        try {
            NotificationCommandFactory.init();
        } catch (Exception e) {
            ThreadCategory.getInstance(getClass()).warn("start: Failed to init notification command factory.", e);            
        }

        getNotifd().setDbConnectionFactory(DatabaseConnectionFactory.getInstance());
        getNotifd().setEventManager(EventIpcManagerFactory.getIpcManager());
        
        getNotifd().setConfigManager(NotifdConfigFactory.getInstance());
        getNotifd().setNotificationManager(NotificationFactory.getInstance());
        getNotifd().setGroupManager(GroupFactory.getInstance());
        getNotifd().setUserManager(UserFactory.getInstance());
        getNotifd().setDestinationPathManager(DestinationPathFactory.getInstance());
        getNotifd().setNotificationCommandManager(NotificationCommandFactory.getInstance());
        getNotifd().init();
        
    }

    /**
     * @return
     */
    private org.opennms.netmgt.notifd.Notifd getNotifd() {
        return org.opennms.netmgt.notifd.Notifd.getInstance();
    }

    public void start() {
        getNotifd().start();
    }

    public void stop() {
        getNotifd().stop();
    }

    public int getStatus() {
        return getNotifd().getStatus();
    }

    public String status() {
        return org.opennms.core.fiber.Fiber.STATUS_NAMES[getStatus()];
    }

    public String getStatusText() {
        return org.opennms.core.fiber.Fiber.STATUS_NAMES[getStatus()];
    }
}
