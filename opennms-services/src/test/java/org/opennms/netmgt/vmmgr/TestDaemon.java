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

package org.opennms.netmgt.vmmgr;

import org.opennms.netmgt.daemon.ServiceDaemon;

public class TestDaemon extends ServiceDaemon {

	public TestDaemon() {
		System.err.println("Creating: "+getName());
		setStatus(START_PENDING);
	}

	public String getName() {
		return "TestDaemon";
	}

	public void pause() {
		if (!isRunning()) {
			return;
		}

		setStatus(PAUSE_PENDING);
		System.err.println("Pausing: "+getName());
		setStatus(PAUSED);

	}

	public String status() {
		String status = super.status();
		System.err.println("Status: "+getName()+" = "+status);
		return status;
	}

	public void resume() {
		if (!isPaused()) {
			return;
		}

		setStatus(RESUME_PENDING);
		System.err.println("Resuming: "+getName());
		setStatus(RUNNING);
	}

	public void start() {
		setStatus(STARTING);
		System.err.println("Starting: "+getName());
		setStatus(RUNNING);

	}

	public void stop() {
		setStatus(STOP_PENDING);
		System.err.println("Stopping: "+getName());
		setStatus(STOPPED);

	}

    public void init() {
    }

}
