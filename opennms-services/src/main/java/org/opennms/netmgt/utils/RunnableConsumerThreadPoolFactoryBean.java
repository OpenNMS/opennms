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
 * Created: October 18, 2007
 * 
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
package org.opennms.netmgt.utils;

import org.opennms.core.concurrent.RunnableConsumerThreadPool;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * <p>RunnableConsumerThreadPoolFactoryBean class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @version $Id: $
 */
public class RunnableConsumerThreadPoolFactoryBean implements FactoryBean, InitializingBean {
    
    private String m_name;
    private float m_lowMark = 0.0f;
    private float m_highMark = 0.0f;
    private int m_maxThreads = 0;
    
    private RunnableConsumerThreadPool m_pool;

    /**
     * <p>getObject</p>
     *
     * @return a {@link java.lang.Object} object.
     * @throws java.lang.Exception if any.
     */
    public Object getObject() throws Exception {
        return m_pool;
    }

    /**
     * <p>getObjectType</p>
     *
     * @return a {@link java.lang.Class} object.
     */
    public Class getObjectType() {
        return (m_pool == null ? RunnableConsumerThreadPoolFactoryBean.class : m_pool.getClass());
    }

    /**
     * <p>isSingleton</p>
     *
     * @return a boolean.
     */
    public boolean isSingleton() {
        return true;
    }

    /**
     * <p>afterPropertiesSet</p>
     *
     * @throws java.lang.Exception if any.
     */
    public void afterPropertiesSet() throws Exception {
        Assert.state(m_name != null, "name property must be set");

        m_pool = new RunnableConsumerThreadPool(m_name, m_lowMark, m_highMark, m_maxThreads);
    }

    /**
     * <p>getName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getName() {
        return m_name;
    }

    /**
     * <p>setName</p>
     *
     * @param name a {@link java.lang.String} object.
     */
    public void setName(String name) {
        m_name = name;
    }

    /**
     * <p>getLowMark</p>
     *
     * @return a float.
     */
    public float getLowMark() {
        return m_lowMark;
    }

    /**
     * <p>setLowMark</p>
     *
     * @param lowMark a float.
     */
    public void setLowMark(float lowMark) {
        m_lowMark = lowMark;
    }

    /**
     * <p>getHighMark</p>
     *
     * @return a float.
     */
    public float getHighMark() {
        return m_highMark;
    }

    /**
     * <p>setHighMark</p>
     *
     * @param highMark a float.
     */
    public void setHighMark(float highMark) {
        m_highMark = highMark;
    }

    /**
     * <p>getMaxThreads</p>
     *
     * @return a int.
     */
    public int getMaxThreads() {
        return m_maxThreads;
    }

    /**
     * <p>setMaxThreads</p>
     *
     * @param maxThreads a int.
     */
    public void setMaxThreads(int maxThreads) {
        m_maxThreads = maxThreads;
    }

}
