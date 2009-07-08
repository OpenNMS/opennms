/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2006-2008 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

package org.opennms.web.svclayer;

/**
 * 
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class ProgressMonitor {
	
	private int m_phaseCount = 1;
	private int m_phase = 0;
	private String m_phaseLabel = "Loading";
	private Object m_result = null;
	private Throwable m_throwable = null;

	public int getPhaseCount() {
		return m_phaseCount;
	}
	
	public void setPhaseCount(int phaseCount) {
		m_phaseCount = phaseCount;
	}
	
	public String getPhaseLabel() {
		return m_phaseLabel;
	}
	
	public int getPhase() {
		return m_phase;
	}

	public void beginNextPhase(String phaseLabel) {
		m_phaseLabel = phaseLabel;
		m_phase++;
	}

	public void finished(Object result) {
		m_result = result;
		m_phaseLabel = "Done";
		m_phase = m_phaseCount;
	}
	
	public boolean isFinished() {
		return m_result != null;
	}

	public Object getResult() {
		return m_result;
	}

	public boolean isError() {
		return m_throwable != null;
	}
	
	public Throwable getThrowable() {
		return m_throwable;
	}

	public void errorOccurred(Throwable t) {
		m_throwable = t;
	}

	
}
