//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
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
package org.opennms.netmgt.daemon;

import org.apache.log4j.Category;
import org.opennms.core.fiber.PausableFiber;
import org.opennms.core.utils.ThreadCategory;
import org.springframework.beans.factory.InitializingBean;

public abstract class ServiceDaemon implements PausableFiber, InitializingBean {
    public void afterPropertiesSet() throws Exception {
        init();
    }

    /**
     * The current status of this fiber
     */
    private int m_status;
    
    abstract public void init();

    abstract public void pause();

    abstract public void resume();

    abstract public void start();

    abstract public void stop();

    abstract public String getName();

    protected synchronized void setStatus(int status) {
        m_status = status;
    }

    public synchronized int getStatus() {
        return m_status;
    }
    
    public String getStatusText() {
        return STATUS_NAMES[getStatus()];
    }

    public String status() {
        return getStatusText();
    }

    protected synchronized boolean isStartPending() {
        return m_status == START_PENDING;
    }

    protected synchronized boolean isRunning() {
        return m_status == RUNNING;
    }

    protected synchronized boolean isPaused() {
        return m_status == PAUSED;
    }

    protected synchronized boolean isStarting() {
        return m_status == STARTING;
    }
    
    protected Category log() {
    	return ThreadCategory.getInstance(getClass());
    }

}
