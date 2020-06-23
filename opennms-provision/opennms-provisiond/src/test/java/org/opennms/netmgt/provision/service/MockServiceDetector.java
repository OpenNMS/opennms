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

package org.opennms.netmgt.provision.service;

import org.opennms.netmgt.provision.DetectRequest;
import org.opennms.netmgt.provision.DetectResults;
import org.opennms.netmgt.provision.SyncServiceDetector;
import org.opennms.netmgt.provision.support.DetectResultsImpl;

/**
 * MockServiceDetector
 *
 * @author brozow
 */
public class MockServiceDetector implements SyncServiceDetector {
    
    private String m_serviceName;
    private String m_ipMatch;

    @Override
    public void init() {
    }
    
    @Override
    public String getServiceName() {
        return m_serviceName;
    }

    @Override
    public void setServiceName(String serviceName) {
        m_serviceName = serviceName;
    }

    @Override
    public DetectResults detect(DetectRequest request) {
        return new DetectResultsImpl(true);
    }

    @Override
    public void dispose() {
    }

    @Override
    public int getPort() {
        return 12345;
    }

    @Override
    public void setPort(int port) {
    }

    @Override
    public int getTimeout() {
        return 2000;
    }

    @Override
    public void setTimeout(int timeout) {
    }

    @Override
    public String getIpMatch() {
        return m_ipMatch;
    }

    @Override
    public void setIpMatch(String ipMatch) {
        m_ipMatch = ipMatch;
    }
}
