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
package org.opennms.netmgt.collection.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;

import org.junit.Test;

public class ServiceParametersAgentProtocolTest {

    @Test
    public void readsAgentProtocolParameter() {
        final ServiceParameters params = new ServiceParameters(
                Collections.singletonMap(ServiceParameters.ParameterName.AGENT_PROTOCOL.toString(), "SNMP"));
        assertEquals("SNMP", params.getAgentProtocol());
    }

    @Test
    public void missingAgentProtocolIsNull() {
        assertNull(new ServiceParameters(Collections.emptyMap()).getAgentProtocol());
    }

    @Test
    public void forAgentProtocolCarriesOnlyTheProtocol() {
        final ServiceParameters params = ServiceParameters.forAgentProtocol("GNMI");
        assertEquals("GNMI", params.getAgentProtocol());
        assertEquals(1, params.getParameters().size());
    }

    @Test
    public void forNullAgentProtocolIsEmpty() {
        final ServiceParameters params = ServiceParameters.forAgentProtocol(null);
        assertNull(params.getAgentProtocol());
        assertTrue(params.getParameters().isEmpty());
    }

}
