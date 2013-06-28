/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.correlation.drools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.style.ToStringCreator;

/**
 * <p>FlapCount class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @version $Id: $
 */
public class FlapCount {
    private static final Logger LOG = LoggerFactory.getLogger(FlapCount.class);
    Long m_nodeid;
    String m_ipAddr;
    String m_svcName;
    Integer m_locationMonitor;
    boolean m_alerted;

    Integer m_count;
    
    /**
     * <p>Constructor for FlapCount.</p>
     *
     * @param nodeid a {@link java.lang.Long} object.
     * @param ipAddr a {@link java.lang.String} object.
     * @param svcName a {@link java.lang.String} object.
     * @param locationMonitor a {@link java.lang.Integer} object.
     */
    public FlapCount(final Long nodeid, final String ipAddr, final String svcName, final Integer locationMonitor) {
        m_nodeid = nodeid;
        m_ipAddr = ipAddr;
        m_svcName = svcName;
        m_locationMonitor = locationMonitor;
        m_count = 1;
        m_alerted = false;
        
        LOG.debug("FlapCount created.");
    }
    
    /**
     * <p>increment</p>
     */
    public void increment() {
        m_count += 1;
        LOG.debug("FlapCount incremented ({}).", m_count);
    }
    
    /**
     * <p>decrement</p>
     */
    public void decrement() {
        m_count -= 1;
        LOG.debug("FlapCount decremented ({}).", m_count);
    }

    /**
     * <p>getCount</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    public Integer getCount() {
        return m_count;
    }

    /**
     * <p>setCount</p>
     *
     * @param count a {@link java.lang.Integer} object.
     */
    public void setCount(final Integer count) {
        m_count = count;
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
     * <p>isAlerted</p>
     *
     * @return a boolean.
     */
    public boolean isAlerted() {
        return m_alerted;
    }

    /**
     * <p>setAlerted</p>
     *
     * @param alerted a boolean.
     */
    public void setAlerted(final boolean alerted) {
        m_alerted = alerted;
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
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String toString() {
        return new ToStringCreator(this)
        	.append("nodeid", m_nodeid)
        	.append("ipAddr", m_ipAddr)
        	.append("svcName", m_svcName)
        	.append("locMon", m_locationMonitor)
        	.append("count", m_count)
        	.toString();
    }
}
