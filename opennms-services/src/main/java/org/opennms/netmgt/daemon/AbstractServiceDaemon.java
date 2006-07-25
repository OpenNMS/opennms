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
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.model.ServiceDaemon;
import org.springframework.beans.factory.InitializingBean;

public abstract class AbstractServiceDaemon implements ServiceDaemon, InitializingBean {
    public void afterPropertiesSet() throws Exception {
        init();
    }
    
    /**
     * The current status of this fiber
     */
    private int m_status;
    
    private String m_name;
    
    abstract protected void onInit();

    protected void onPause() {}

    protected void onResume() {}

    protected void onStart() {}

    protected void onStop() {}

    final public String getName() { return m_name; }
    
    protected AbstractServiceDaemon(String name) {
    	m_name = name;
    	setStatus(START_PENDING);
    }

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
    	return ThreadCategory.getInstance();
    }
    
    final public void init() {
		String prefix = ThreadCategory.getPrefix();
		try {
			ThreadCategory.setPrefix(getName());
			log().debug(getName()+" initializing.");
			
			onInit();
			
			log().debug(getName()+" initialization complete.");
		} finally {
			ThreadCategory.setPrefix(prefix);
		}
    }
    


	final public void pause() {
		String prefix = ThreadCategory.getPrefix();
		try {
			ThreadCategory.setPrefix(getName());

			if (!isRunning())
				return;

			setStatus(PAUSE_PENDING);

			onPause();

			setStatus(PAUSED);

			log().debug(getName()+" paused.");

		} finally {
			ThreadCategory.setPrefix(prefix);
		}
	}

	final public void resume() {
		String prefix = ThreadCategory.getPrefix();
		try {
			ThreadCategory.setPrefix(getName());
			if (!isPaused())
				return;

			setStatus(RESUME_PENDING);

			onResume();

			setStatus(RUNNING);

			log().debug(getName()+" resumed.");
		} finally {
			ThreadCategory.setPrefix(prefix);
		}
	}

	final public synchronized void start() {
		String prefix = ThreadCategory.getPrefix();
		try {
			ThreadCategory.setPrefix(getName());
			log().debug(getName()+" starting.");

			setStatus(STARTING);

			onStart();

			setStatus(RUNNING);

			log().debug(getName()+" started.");
		} finally {
			ThreadCategory.setPrefix(prefix);
		}
	
	}

	/**
	 * Stops the currently running service. If the service is not running then
	 * the command is silently discarded.
	 */
	final public synchronized void stop() {
		String prefix = ThreadCategory.getPrefix();
		try {
			ThreadCategory.setPrefix(getName());
			log().debug(getName()+" stopping.");
			setStatus(STOP_PENDING);

			onStop();

			setStatus(STOPPED);

			log().debug(getName()+" stopped.");
		} finally {
			ThreadCategory.setPrefix(prefix);
		}
	}

}
