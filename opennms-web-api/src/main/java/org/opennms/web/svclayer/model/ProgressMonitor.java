/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
