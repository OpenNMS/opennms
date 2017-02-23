/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.notifd.jmx;

import org.opennms.netmgt.daemon.AbstractSpringContextJmxServiceDaemon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>Notifd class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class Notifd extends AbstractSpringContextJmxServiceDaemon<org.opennms.netmgt.notifd.Notifd> implements NotifdMBean {

    private static final Logger LOG = LoggerFactory.getLogger(Notifd.class);

    /** {@inheritDoc} */
    @Override
    protected String getLoggingPrefix() {
        return org.opennms.netmgt.notifd.Notifd.getLoggingCategory();
    }

    /** {@inheritDoc} */
    @Override
    protected String getSpringContext() {
        return "notifdContext";
    }

    @Override
    /** {@inheritDoc} */
    public long getNotificationTasksQueued() {
        return getDaemon().getNotificationManager().getNotificationTasksQueued();
    }

    @Override
    /** {@inheritDoc} */
    public long getBinaryNoticesAttempted() {
        return getDaemon().getNotificationManager().getBinaryNoticesAttempted();
    }

    @Override
    /** {@inheritDoc} */
    public long getJavaNoticesAttempted() {
        return getDaemon().getNotificationManager().getJavaNoticesAttempted();
    }

    @Override
    /** {@inheritDoc} */
    public long getBinaryNoticesSucceeded() {
        return getDaemon().getNotificationManager().getBinaryNoticesSucceeded();
    }

    @Override
    /** {@inheritDoc} */
    public long getJavaNoticesSucceeded() {
        return getDaemon().getNotificationManager().getJavaNoticesSucceeded();
    }

    @Override
    /** {@inheritDoc} */
    public long getBinaryNoticesFailed() {
        return getDaemon().getNotificationManager().getBinaryNoticesFailed();
    }

    @Override
    /** {@inheritDoc} */
    public long getJavaNoticesFailed() {
        return getDaemon().getNotificationManager().getJavaNoticesFailed();
    }

    @Override
    /** {@inheritDoc} */
    public long getBinaryNoticesInterrupted() {
        return getDaemon().getNotificationManager().getBinaryNoticesInterrupted();
    }

    @Override
    /** {@inheritDoc} */
    public long getJavaNoticesInterrupted() {
        return getDaemon().getNotificationManager().getJavaNoticesInterrupted();
    }

    @Override
    /** {@inheritDoc} */
    public long getUnknownNoticesInterrupted() {
        return getDaemon().getNotificationManager().getUnknownNoticesInterrupted();
    }
}
