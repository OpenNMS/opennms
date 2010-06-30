/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created February 1, 2007
 *
 * Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
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
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.correlation.drools;

import org.opennms.core.utils.ThreadCategory;
import org.springframework.core.style.ToStringCreator;

/**
 * <p>FlapCount class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @version $Id: $
 */
public class FlapCount {
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
    public FlapCount(Long nodeid, String ipAddr, String svcName, Integer locationMonitor) {
        m_nodeid = nodeid;
        m_ipAddr = ipAddr;
        m_svcName = svcName;
        m_locationMonitor = locationMonitor;
        m_count = 1;
        m_alerted = false;
        
        log().info("FlapCount.created : "+this);
    }
    
    /**
     * <p>increment</p>
     */
    public void increment() {
        m_count += 1;
        log().info("FlapCount.increment : "+this);
    }
    
    /**
     * <p>decrement</p>
     */
    public void decrement() {
        m_count -= 1;
        log().info("FlapCount.decrement : "+this);
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
    public void setCount(Integer count) {
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
    public void setIpAddr(String ipAddr) {
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
    public void setNodeid(Long nodeid) {
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
    public void setSvcName(String svcName) {
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
    public void setAlerted(boolean alerted) {
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
    public void setLocationMonitor(Integer locationMonitor) {
        m_locationMonitor = locationMonitor;
    }
    
    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String toString() {
        ToStringCreator creator = new ToStringCreator(this);
        creator.append("nodeid", m_nodeid);
        creator.append("ipAddr", m_ipAddr);
        creator.append("svcName", m_svcName);
        creator.append("locMon", m_locationMonitor);
        creator.append("count", m_count);
        return creator.toString();
    }

    /**
     * <p>log</p>
     *
     * @return a {@link org.opennms.core.utils.ThreadCategory} object.
     */
    public ThreadCategory log() {
        return ThreadCategory.getInstance(getClass());
    }
}
