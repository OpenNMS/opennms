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
package org.opennms.netmgt.dao.jaxb;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.net.UnknownHostException;

import org.junit.Test;
import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.config.wsman.credentials.Definition;
import org.opennms.netmgt.config.wsman.SystemDefinition;
import org.opennms.netmgt.model.OnmsNode;

public class WSManDataCollectionConfigDaoJaxbTest {

    @Test
    public void canEvaluteSystemDefinitionRules() throws UnknownHostException {
        OnmsNode node = mock(OnmsNode.class);
        CollectionAgent agent = mock(CollectionAgent.class);
        Definition config = new Definition();
        config.setProductVendor("Dell Inc.");
        config.setProductVersion(" iDRAC 6");

        SystemDefinition sysDef = new SystemDefinition();
        sysDef.addRule("#productVendor matches 'Dell.*' and #productVersion matches '.*DRAC.*'");
        
        assertTrue("agent should be matched", WSManDataCollectionConfigDaoJaxb.isAgentSupportedBySystemDefinition(sysDef, agent, config, node));
    }
}
