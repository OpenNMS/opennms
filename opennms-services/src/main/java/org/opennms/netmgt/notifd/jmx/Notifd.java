/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.notifd.jmx;

import java.lang.reflect.UndeclaredThrowableException;

import org.opennms.core.db.DataSourceFactory;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>Notifd class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class Notifd extends AbstractServiceDaemon implements NotifdMBean {
    
    private static final Logger LOG = LoggerFactory.getLogger(Notifd.class);
    
    /**
     * Logging category for log4j
     */
    private static String LOG4J_CATEGORY = "notifd";

    /**
     * <p>Constructor for Notifd.</p>
     */
    public Notifd() {
        super(LOG4J_CATEGORY);
    }

    /**
     * <p>onInit</p>
     */
    @Override
    protected void onInit() {
        EventIpcManagerFactory.init();

        try {
            NotifdConfigFactory.init();
        } catch (Throwable t) {
            LOG.error("start: Failed to init NotifdConfigFactory.", t);
            throw new UndeclaredThrowableException(t);
        }
        
        try {
            NotificationFactory.init();
        } catch (Throwable t) {
            LOG.error("start: Failed to init NotificationFactory.", t);
            throw new UndeclaredThrowableException(t);
        }
        
        try {
            DataSourceFactory.init();
        } catch (Throwable t) {
            LOG.error("start: Failed to init database connection factory.", t);
            throw new UndeclaredThrowableException(t);
        }

        try {
            GroupFactory.init();
        } catch (Throwable t) {
            LOG.error("start: Failed to init group factory.", t);
            throw new UndeclaredThrowableException(t);
        }

        try {
            UserFactory.init();
        } catch (Throwable t) {
            LOG.error("start: Failed to init user factory.", t);
            throw new UndeclaredThrowableException(t);
        }
        
        try {
            DestinationPathFactory.init();
        } catch (Throwable t) {
            LOG.error("start: Failed to init destination path factory.", t);
            throw new UndeclaredThrowableException(t);
        }
        
        try {
            NotificationCommandFactory.init();
        } catch (Throwable t) {
            LOG.error("start: Failed to init notification command factory.", t);
            throw new UndeclaredThrowableException(t);
        }

        try {
            PollOutagesConfigFactory.init();
        } catch (Throwable t) {
            LOG.error("start: Failed to init poll outage config factory.", t);
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
    @Override
    protected void onStart() {
        getNotifd().start();
    }

    /**
     * <p>onStop</p>
     */
    @Override
    protected void onStop() {
        getNotifd().stop();
    }

    /**
     * Override {@link AbstractServiceDaemon#getStatus()} to use the status of
     * the {@link org.opennms.netmgt.notifd.Notifd} instance.
     *
     * @return a int.
     */
    @Override
    public int getStatus() {
        return getNotifd().getStatus();
    }
}
