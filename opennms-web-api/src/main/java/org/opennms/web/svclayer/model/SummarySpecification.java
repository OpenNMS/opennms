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
 * <p>SummarySpecification class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @version $Id: $
 * @since 1.8.1
 */
public class SummarySpecification {
    private String m_filterRule;
    private long m_startTime;
    private long m_endTime;
    private String m_attributeSieve;
    
    /**
     * <p>getFilterRule</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getFilterRule() {
        return m_filterRule;
    }
    /**
     * <p>setFilterRule</p>
     *
     * @param filterRule a {@link java.lang.String} object.
     */
    public void setFilterRule(String filterRule) {
        m_filterRule = filterRule;
    }
    /**
     * <p>getStartTime</p>
     *
     * @return a long.
     */
    public long getStartTime() {
        return m_startTime;
    }
    /**
     * <p>setStartTime</p>
     *
     * @param startTime a long.
     */
    public void setStartTime(long startTime) {
        m_startTime = startTime;
    }
    /**
     * <p>getEndTime</p>
     *
     * @return a long.
     */
    public long getEndTime() {
        return m_endTime;
    }
    /**
     * <p>setEndTime</p>
     *
     * @param endTime a long.
     */
    public void setEndTime(long endTime) {
        m_endTime = endTime;
    }
    
    /**
     * <p>getAttributeSieve</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getAttributeSieve() {
        return m_attributeSieve;
    }
    
    /**
     * <p>setAttributeSieve</p>
     *
     * @param attributeSieve a {@link java.lang.String} object.
     */
    public void setAttributeSieve(String attributeSieve) {
        m_attributeSieve = attributeSieve;
    }
}

