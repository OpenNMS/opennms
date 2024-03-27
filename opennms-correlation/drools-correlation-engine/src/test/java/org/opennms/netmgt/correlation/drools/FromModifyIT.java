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
package org.opennms.netmgt.correlation.drools;

import static org.junit.Assert.assertEquals;

import org.drools.core.common.InternalFactHandle;
import org.junit.Test;
import org.kie.api.definition.type.FactType;

public class FromModifyIT extends CorrelationRulesTestCase {
    @Test
    public void testMultipleRulesWithFromConstraintsAndModifyConsequences() throws Exception {
        DroolsCorrelationEngine engine = findEngineByName("fromWithModifyRules");
        FactType testFactType = engine.getKieSession().getKieBase()
                .getFactType("org.opennms.netmgt.correlation.drools", "FromModifyITFact");
        Object testFact = testFactType.newInstance();
        testFactType.set(testFact,"modified", "false");
        testFactType.set(testFact,"modifiedByRule", "");

        engine.getKieSession().insert(testFact);
        engine.getKieSession().fireAllRules();

        m_anticipatedMemorySize = 1;
        Object modifiedFact = ((InternalFactHandle) engine.getKieSession().getFactHandles().iterator().next()).getObject();
        assertEquals("FromModifyITFact has the wrong 'modified' value", "true", testFactType.get(modifiedFact, "modified"));
        assertEquals("FromModifyITFact has the wrong 'modifiedByRule' value", "A",
                testFactType.get(modifiedFact, "modifiedByRule"));
        verify(engine);
    }
}
