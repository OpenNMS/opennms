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
package org.opennms.netmgt.provision.detector.snmp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * <p>Win32ServiceDetector class.</p>
 *
 * @author ranger
 * @version $Id: $
 */

public class Win32ServiceDetector extends SnmpDetector {

    private static final Logger LOG = LoggerFactory.getLogger(Win32ServiceDetector.class);

    private static final String SV_SVC_OPERATING_STATE_OID = ".1.3.6.1.4.1.77.1.2.3.1.3";
    private static final String DEFAULT_SERVICE_NAME = "Win32Service";

    private String m_win32SvcName;
    
    /**
     * <p>Constructor for Win32ServiceDetector.</p>
     */
    public Win32ServiceDetector(){
        setServiceName(DEFAULT_SERVICE_NAME);
        setVbvalue("1");
    }

    public String getWin32ServiceName() {
        return m_win32SvcName;
    }

    public void setWin32ServiceName(String serviceName) {
        m_win32SvcName = serviceName;
        LOG.debug("setWin32ServiceName: setting service name to {}", serviceName);
        int snLength = serviceName.length();
        
        final StringBuilder serviceOidBuf = new StringBuilder(SV_SVC_OPERATING_STATE_OID);
        serviceOidBuf.append(".").append(Integer.toString(snLength));
        for (byte thisByte : serviceName.getBytes()) {
            serviceOidBuf.append(".").append(Byte.toString(thisByte));
        }
        
        LOG.debug("setWin32ServiceName: the OID for the Win32 service  is {}", serviceOidBuf.toString());
        setOid(serviceOidBuf.toString());
    }
}
