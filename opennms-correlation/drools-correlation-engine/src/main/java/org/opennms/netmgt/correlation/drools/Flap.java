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
package org.opennms.netmgt.correlation.drools;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>Flap class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @version $Id: $
 */
public class Flap implements Serializable {
    private static final long serialVersionUID = 2990480904303840611L;

    Long m_nodeid;
    String m_ipAddr;
    String m_svcName;
    Date m_startTime;
    Date m_endTime;
    Integer m_locationMonitor;
    boolean m_counted;
    Integer m_timerId;
    
    /**
     * <p>Constructor for Flap.</p>
     *
     * @param nodeid a {@link java.lang.Long} object.
     * @param ipAddr a {@link java.lang.String} object.
     * @param svcName a {@link java.lang.String} object.
     * @param locationMonitor a {@link java.lang.Integer} object.
     * @param timerId a {@link java.lang.Integer} object.
     */
    public Flap(final Long nodeid, final String ipAddr, final String svcName, final Integer locationMonitor, final Integer timerId) {
        m_nodeid = nodeid;
        m_ipAddr = ipAddr;
        m_svcName = svcName;
        m_locationMonitor = locationMonitor;
        m_timerId = timerId;
        m_startTime = new Date();
        m_counted = false;
    }
    
    /**
     * <p>getEndTime</p>
     *
     * @return a {@link java.util.Date} object.
     */
    public Date getEndTime() {
        return m_endTime;
    }
    /**
     * <p>setEndTime</p>
     *
     * @param end a {@link java.util.Date} object.
     */
    public void setEndTime(final Date end) {
        m_endTime = end;
    }
    /**
     * <p>getIpAddr</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getIpAddr() {
        return m_ipAddr;
    }
    /**
     * <p>setIpAddr</p>
     *
     * @param ipAddr a {@link java.lang.String} object.
     */
    public void setIpAddr(final String ipAddr) {
        m_ipAddr = ipAddr;
    }
    /**
     * <p>getLocationMonitor</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    public Integer getLocationMonitor() {
        return m_locationMonitor;
    }
    /**
     * <p>setLocationMonitor</p>
     *
     * @param locationMonitor a {@link java.lang.Integer} object.
     */
    public void setLocationMonitor(final Integer locationMonitor) {
        m_locationMonitor = locationMonitor;
    }
    /**
     * <p>getNodeid</p>
     *
     * @return a {@link java.lang.Long} object.
     */
    public Long getNodeid() {
        return m_nodeid;
    }
    /**
     * <p>setNodeid</p>
     *
     * @param nodeid a {@link java.lang.Long} object.
     */
    public void setNodeid(final Long nodeid) {
        m_nodeid = nodeid;
    }
    /**
     * <p>getStartTime</p>
     *
     * @return a {@link java.util.Date} object.
     */
    public Date getStartTime() {
        return m_startTime;
    }
    /**
     * <p>setStartTime</p>
     *
     * @param start a {@link java.util.Date} object.
     */
    public void setStartTime(final Date start) {
        m_startTime = start;
    }
    /**
     * <p>getSvcName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getSvcName() {
        return m_svcName;
    }
    /**
     * <p>setSvcName</p>
     *
     * @param svcName a {@link java.lang.String} object.
     */
    public void setSvcName(final String svcName) {
        m_svcName = svcName;
    }

    /**
     * <p>isCounted</p>
     *
     * @return a boolean.
     */
    public boolean isCounted() {
        return m_counted;
    }

    /**
     * <p>setCounted</p>
     *
     * @param counted a boolean.
     */
    public void setCounted(final boolean counted) {
        m_counted = counted;
    }

    /**
     * <p>getTimerId</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    public Integer getTimerId() {
        return m_timerId;
    }

    /**
     * <p>setTimerId</p>
     *
     * @param timerId a {@link java.lang.Integer} object.
     */
    public void setTimerId(final Integer timerId) {
        m_timerId = timerId;
    }
    
    
}
