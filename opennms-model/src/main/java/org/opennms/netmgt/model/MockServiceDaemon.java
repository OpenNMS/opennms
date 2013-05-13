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

package org.opennms.netmgt.model;

public class MockServiceDaemon implements MockServiceDaemonMBean {
	
	private boolean startCalled = false;
	private String statusStr = "UNDEFINED";
	private String name;

	public MockServiceDaemon(String name) {
		this.name = name;
	}
        @Override
	public String getStatusText() {
		// TODO Auto-generated method stub
		return statusStr;
	}

        @Override
	public void pause() {
		// TODO Auto-generated method stub

	}

        @Override
	public void resume() {
		// TODO Auto-generated method stub

	}

        @Override
	public String getName() {
		// TODO Auto-generated method stub
		return name;
	}

        @Override
	public int getStatus() {
		// TODO Auto-generated method stub
		return 0;
	}

        @Override
	public void start() {
		// TODO Auto-generated method stub
		startCalled = true;
		statusStr = "Started";
	}

	public boolean getStartCalled() {
		return startCalled;
	}
	
        @Override
	public void stop() {
		// TODO Auto-generated method stub

	}

}