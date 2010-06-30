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

import java.lang.reflect.UndeclaredThrowableException;

import org.opennms.netmgt.config.DataSourceFactory;
import org.opennms.netmgt.config.DestinationPathFactory;
import org.opennms.netmgt.config.GroupFactory;
import org.opennms.netmgt.config.NotifdConfigFactory;
import org.opennms.netmgt.config.NotificationCommandFactory;
import org.opennms.netmgt.config.NotificationFactory;
import org.opennms.netmgt.config.PollOutagesConfigFactory;
import org.opennms.netmgt.config.UserFactory;
import org.opennms.netmgt.daemon.AbstractServiceDaemon;
import org.opennms.netmgt.dao.hibernate.NodeDaoHibernate;
import org.opennms.netmgt.eventd.EventIpcManagerFactory;

/**
 * <p>Notifd class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class Notifd extends AbstractServiceDaemon implements NotifdMBean {
    /**
     * Logging category for log4j
     */
    private static String LOG4J_CATEGORY = "OpenNMS.Notifd";

    /**
     * <p>Constructor for Notifd.</p>
     */
    public Notifd() {
        super(LOG4J_CATEGORY);
    }

    /**
     * <p>onInit</p>
     */
    protected void onInit() {
        EventIpcManagerFactory.init();

        try {
            NotifdConfigFactory.init();
        } catch (Throwable t) {
            log().error("start: Failed to init NotifdConfigFactory.", t);
            throw new UndeclaredThrowableException(t);
        }
        
        try {
            NotificationFactory.init();
        } catch (Throwable t) {
            log().error("start: Failed to init NotificationFactory.", t);
            throw new UndeclaredThrowableException(t);
        }
        
        try {
            DataSourceFactory.init();
        } catch (Throwable t) {
            log().error("start: Failed to init database connection factory.", t);
            throw new UndeclaredThrowableException(t);
        }

        try {
            GroupFactory.init();
        } catch (Throwable t) {
            log().error("start: Failed to init group factory.", t);
            throw new UndeclaredThrowableException(t);
        }

        try {
            UserFactory.init();
        } catch (Throwable t) {
            log().error("start: Failed to init user factory.", t);
            throw new UndeclaredThrowableException(t);
        }
        
        try {
            DestinationPathFactory.init();
        } catch (Throwable t) {
            log().error("start: Failed to init destination path factory.", t);
            throw new UndeclaredThrowableException(t);
        }
        
        try {
            NotificationCommandFactory.init();
        } catch (Throwable t) {
            log().error("start: Failed to init notification command factory.", t);
            throw new UndeclaredThrowableException(t);
        }

        try {
            PollOutagesConfigFactory.init();
        } catch (Throwable t) {
            log().error("start: Failed to init poll outage config factory.", t);
            throw new UndeclaredThrowableException(t);
        }
        
        getNotifd().setEventManager(EventIpcManagerFactory.getIpcManager());
        getNotifd().setConfigManager(NotifdConfigFactory.getInstance());
        getNotifd().setNotificationManager(NotificationFactory.getInstance());
        getNotifd().setGroupManager(GroupFactory.getInstance());
        getNotifd().setUserManager(UserFactory.getInstance());
        getNotifd().setDestinationPathManager(DestinationPathFactory.getInstance());
        getNotifd().setNotificationCommandManager(NotificationCommandFactory.getInstance());
        getNotifd().setPollOutagesConfigManager(PollOutagesConfigFactory.getInstance());
        getNotifd().setNodeDao(new NodeDaoHibernate());
        getNotifd().init();
    }

    /**
     * @return Notifd instance
     */
    private org.opennms.netmgt.notifd.Notifd getNotifd() {
        return org.opennms.netmgt.notifd.Notifd.getInstance();
    }

    /**
     * <p>onStart</p>
     */
    protected void onStart() {
        getNotifd().start();
    }

    /**
     * <p>onStop</p>
     */
    protected void onStop() {
        getNotifd().stop();
    }

    /**
     * Override {@link AbstractServiceDaemon#getStatus()} to use the status of
     * the {@link org.opennms.netmgt.notifd.Notifd} instance.
     *
     * @return a int.
     */
    public int getStatus() {
        return getNotifd().getStatus();
    }
}
