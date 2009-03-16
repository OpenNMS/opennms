/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
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
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package org.opennms.netmgt.provision.support;

import java.net.InetAddress;

import org.opennms.netmgt.provision.AsyncServiceDetector;
import org.opennms.netmgt.provision.DetectFuture;
import org.opennms.netmgt.provision.DetectorMonitor;

/**
 * @author thedesloge
 *
 */
public abstract class AsyncAbstractDetector implements AsyncServiceDetector {
    
    private static final int DEFAULT_RETRIES = 1;
    private static final int DEFAULT_TIMEOUT = 2000;
    
    private int m_port;
    private int m_retries;
    private int m_timeout;
    private String m_serviceName;
    
    
    protected AsyncAbstractDetector(String serviceName, int port) {
        this(serviceName, port, DEFAULT_TIMEOUT, DEFAULT_RETRIES);
    }
    
    protected AsyncAbstractDetector(String serviceName, int port, int timeout, int retries) {
        m_serviceName = serviceName;
        m_port = port;
        m_timeout = timeout;
        m_retries = retries;
    }

    public void init() {
        if (m_serviceName == null || m_timeout <= 0) {
            throw new IllegalStateException(String.format("ServiceName is null or timeout of %d is invalid.  Timeout must be > 0", m_timeout));
        }
        
        onInit();
    }
    
    abstract protected void onInit();
    
    /**
     * 
     * @param InetAddress address
     * @param DetectMonitor monitor
     * @return DetectFuture
     * @throws Exception 
     */
    abstract public DetectFuture isServiceDetected(InetAddress address, DetectorMonitor monitor) throws Exception;
    
    abstract public void dispose();
    
    public void setPort(int port) {
        m_port = port;
    }

    public int getPort() {
        return m_port;
    }

    public void setRetries(int retries) {
        m_retries = retries;
    }

    public int getRetries() {
        return m_retries;
    }

    public void setTimeout(int timeout) {
        m_timeout = timeout;
    }

    public int getTimeout() {
        return m_timeout;
    }

    public void setServiceName(String serviceName) {
        m_serviceName = serviceName;
    }

    public String getServiceName() {
        return m_serviceName;
    }

}
