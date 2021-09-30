/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
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
