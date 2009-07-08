/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * Copyright (C) 2007-2008 The OpenNMS Group, Inc.  All rights reserved.
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
    
    public InvokerResult(Service service, ObjectInstance mbean, Object result, Throwable throwable) {
        m_service = service;
        m_mbean = mbean;
        m_result = result;
        m_throwable = throwable;
    }
    
    public ObjectInstance getMbean() {
        return m_mbean;
    }
    
    public Object getResult() {
        return m_result;
    }
    
    public Throwable getThrowable() {
        return m_throwable;
    }

    public Service getService() {
        return m_service;
    }

}