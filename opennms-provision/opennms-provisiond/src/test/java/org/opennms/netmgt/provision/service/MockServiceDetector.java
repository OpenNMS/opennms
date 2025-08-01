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
