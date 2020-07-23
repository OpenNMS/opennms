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

package org.opennms.netmgt.correlation.drools;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;

import org.drools.core.common.InternalFactHandle;
import org.junit.Test;
import org.kie.api.definition.type.FactType;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.test.mock.EasyMockUtils;

public class FromModifyTest extends CorrelationRulesTestCase {
    @Test
    public void testMultipleRulesWithFromConstraintsAndModifyConsequences() throws Exception {
        DroolsCorrelationEngine engine = findEngineByName("fromWithModifyRules");
        FactType testFactType = engine.getKieSession().getKieBase()
                .getFactType("org.opennms.netmgt.correlation.drools", "FromModifyTestFact");
        Object testFact = testFactType.newInstance();
        testFactType.set(testFact,"modified", "false");
        testFactType.set(testFact,"modifiedByRule", "");

        engine.getKieSession().insert(testFact);
        engine.getKieSession().fireAllRules();

        m_anticipatedMemorySize = 1;
        Object modifiedFact = ((InternalFactHandle) engine.getKieSession().getFactHandles().iterator().next()).getObject();
        assertEquals("FromModifyTestFact has the wrong 'modified' value", "true", testFactType.get(modifiedFact, "modified"));
        assertEquals("FromModifyTestFact has the wrong 'modifiedByRule' value", "A",
                testFactType.get(modifiedFact, "modifiedByRule"));
        verify(engine);
    }
}
