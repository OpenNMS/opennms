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
package org.opennms.netmgt.provision;

import org.junit.Test;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.springframework.transaction.annotation.Transactional;

/**
 * The Test Class for Jira issue <a href="http://issues.opennms.org/browse/NMS-8506">NMS-8506</a>
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */

public class NMS8506_1_IT extends AbstractSingleHardwareAdapterTest {

    /**
     * Test adapter.
     *
     * @throws Exception the exception
     */
    @Test
    @Override
    @Transactional
    @JUnitSnmpAgent(host="192.168.0.1", resource="NMS-8506-cisco.properties")
    public void testAdapter() throws Exception {
        performTest(18);
    }

}
