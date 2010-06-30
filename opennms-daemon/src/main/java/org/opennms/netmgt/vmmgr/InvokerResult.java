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
 * Created: May 12, 2007
 * 
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
package org.opennms.netmgt.vmmgr;

import javax.management.ObjectInstance;

import org.opennms.netmgt.config.service.Service;

/**
 * 
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
class InvokerResult {
    private Service m_service;
    private ObjectInstance m_mbean;
    private Object m_result;
    private Throwable m_throwable;
    
    /**
     * <p>Constructor for InvokerResult.</p>
     *
     * @param service a {@link org.opennms.netmgt.config.service.Service} object.
     * @param mbean a {@link javax.management.ObjectInstance} object.
     * @param result a {@link java.lang.Object} object.
     * @param throwable a {@link java.lang.Throwable} object.
     */
    public InvokerResult(Service service, ObjectInstance mbean, Object result, Throwable throwable) {
        m_service = service;
        m_mbean = mbean;
        m_result = result;
        m_throwable = throwable;
    }
    
    /**
     * <p>getMbean</p>
     *
     * @return a {@link javax.management.ObjectInstance} object.
     */
    public ObjectInstance getMbean() {
        return m_mbean;
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
     * <p>getThrowable</p>
     *
     * @return a {@link java.lang.Throwable} object.
     */
    public Throwable getThrowable() {
        return m_throwable;
    }

    /**
     * <p>getService</p>
     *
     * @return a {@link org.opennms.netmgt.config.service.Service} object.
     */
    public Service getService() {
        return m_service;
    }

}
