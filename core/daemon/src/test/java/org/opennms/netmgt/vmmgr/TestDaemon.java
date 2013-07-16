/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.vmmgr;

import org.opennms.netmgt.daemon.AbstractServiceDaemon;

public class TestDaemon extends AbstractServiceDaemon {

	public TestDaemon() {
		super("test-daemon");
		System.err.println("Creating: "+getName());
	}

        @Override
	protected void onPause() {
		System.err.println("Pausing: "+getName());
	}

        @Override
	public String status() {
		String status = super.getStatusText();
		System.err.println("Status: "+getName()+" = "+status);
		return status;
	}

        @Override
	protected void onResume() {
		System.err.println("Resuming: "+getName());
	}

        @Override
	protected void onStart() {
		System.err.println("Starting: "+getName());
	}

        @Override
	protected void onStop() {
		System.err.println("Stopping: "+getName());
	}

        @Override
    protected void onInit() {
    }

}
