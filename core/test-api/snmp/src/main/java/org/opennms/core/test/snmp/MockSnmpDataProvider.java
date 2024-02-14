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
package org.opennms.core.test.snmp;

import java.io.IOException;

import org.opennms.mock.snmp.MockSnmpAgent;
import org.opennms.netmgt.snmp.SnmpAgentAddress;
import org.springframework.core.io.Resource;

public interface MockSnmpDataProvider {

	void addAgent(SnmpAgentAddress address, MockSnmpAgent agent);

	void updateIntValue(SnmpAgentAddress address, String oid, int val);

	void updateStringValue(SnmpAgentAddress address, String oid, String val);

	void updateCounter32Value(SnmpAgentAddress address, String oid, int val);

	void updateCounter64Value(SnmpAgentAddress address, String oid, long val);

	void setDataForAddress(SnmpAgentAddress address, Resource resource) throws IOException ;

	void resetData();

}
