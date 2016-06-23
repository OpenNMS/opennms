/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
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

package org.opennms.web.svclayer.model;

/**
 * <p>ProgressMonitor class.</p>
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

	/**
	 * <p>getPhaseCount</p>
	 *
	 * @return a int.
	 */
	public int getPhaseCount() {
		return m_phaseCount;
	}
	
	/**
	 * <p>setPhaseCount</p>
	 *
	 * @param phaseCount a int.
	 */
	public void setPhaseCount(int phaseCount) {
		m_phaseCount = phaseCount;
	}
	
	/**
	 * <p>getPhaseLabel</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getPhaseLabel() {
		return m_phaseLabel;
	}
	
	/**
	 * <p>getPhase</p>
	 *
	 * @return a int.
	 */
	public int getPhase() {
		return m_phase;
	}

	/**
	 * <p>beginNextPhase</p>
	 *
	 * @param phaseLabel a {@link java.lang.String} object.
	 */
	public void beginNextPhase(String phaseLabel) {
		m_phaseLabel = phaseLabel;
		m_phase++;
	}

	/**
	 * <p>finished</p>
	 *
	 * @param result a {@link java.lang.Object} object.
	 */
	public void finished(Object result) {
		m_result = result;
		m_phaseLabel = "Done";
		m_phase = m_phaseCount;
	}
	
	/**
	 * <p>isFinished</p>
	 *
	 * @return a boolean.
	 */
	public boolean isFinished() {
		return m_result != null;
	}

	/**
	 * <p>getResult</p>
	 *
	 * @return a {@link java.lang.Object} object.
	 */
	public Object getResult() {
		return m_result;
	}

	/**
	 * <p>isError</p>
	 *
	 * @return a boolean.
	 */
	public boolean isError() {
		return m_throwable != null;
	}
	
	/**
	 * <p>getThrowable</p>
	 *
	 * @return a {@link java.lang.Throwable} object.
	 */
	public Throwable getThrowable() {
		return m_throwable;
	}

	/**
	 * <p>errorOccurred</p>
	 *
	 * @param t a {@link java.lang.Throwable} object.
	 */
	public void errorOccurred(Throwable t) {
		m_throwable = t;
	}

	
}
