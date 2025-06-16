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
package org.opennms.netmgt.provision.detector;

import java.net.UnknownHostException;

import org.junit.After;
import org.junit.Test;

/**
 * @author Donald Desloge
 *
 */
public class SmbDetectorTest {
    
//    private SmbDetector m_detector;
    
//    @Before
//    public void setUp() {
//        MockLogAppender.setupLogging();
//        m_detector = new SmbDetector();
//        
//    }
    
    @After
    public void tearDown() {
        
    }
    
    //Tested against a Windows XP machine on local network. 
    @Test(timeout=30000)
    public void testMyDetector() throws UnknownHostException {
        //m_detector.init();
        //FIXME: This needs to be fixed
        //assertTrue(m_detector.isServiceDetected(InetAddressUtils.addr("192.168.1.103")));
    }
}
