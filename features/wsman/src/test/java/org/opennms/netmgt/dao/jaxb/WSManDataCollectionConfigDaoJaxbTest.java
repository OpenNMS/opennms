/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao.jaxb;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.net.UnknownHostException;

import org.junit.Test;
import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.config.wsman.Definition;
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
