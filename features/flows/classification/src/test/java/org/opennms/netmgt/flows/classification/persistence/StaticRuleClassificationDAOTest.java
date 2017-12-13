/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.flows.classification.persistence;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;
import org.opennms.netmgt.flows.classification.ClassificationEngine;
import org.opennms.netmgt.flows.classification.ClassificationRequest;
import org.opennms.netmgt.flows.classification.DefaultClassificationEngine;

public class StaticRuleClassificationDAOTest {

    @Test
    public void testParsing() throws IOException {
        final ClassificationRuleDAO classificationDAO = new StaticRuleClassificationDAO();
        final ClassificationEngine classificationEngine = new DefaultClassificationEngine(classificationDAO);

        // Verify some port mappings
        assertEquals("rtmp", classificationEngine.classify(new ClassificationRequest("Default", 1, null, ProtocolType.DDP)));
        assertEquals("tcpmux", classificationEngine.classify(new ClassificationRequest("Default", 1, null, ProtocolType.TCP)));
        assertEquals("tcpmux", classificationEngine.classify(new ClassificationRequest("Default", 1, null, ProtocolType.UDP)));
        assertEquals("nicname", classificationEngine.classify(new ClassificationRequest("Default", 43, null, ProtocolType.TCP)));
        assertEquals("nicname", classificationEngine.classify(new ClassificationRequest("Default", 43, null, ProtocolType.UDP)));
        assertEquals("http", classificationEngine.classify(new ClassificationRequest("Default", 80, null, ProtocolType.TCP)));
        assertEquals("com-bardac-dw", classificationEngine.classify(new ClassificationRequest("Default", 48556, null, ProtocolType.TCP)));
        assertEquals("com-bardac-dw", classificationEngine.classify(new ClassificationRequest("Default", 48556, null, ProtocolType.UDP)));

        // unassigned ports
        assertEquals(null, classificationEngine.classify(new ClassificationRequest("Default", 8, null, ProtocolType.TCP)));
        assertEquals(null, classificationEngine.classify(new ClassificationRequest("Default", 8, null, ProtocolType.UDP)));

        // define "" as name
        assertEquals(null, classificationEngine.classify(new ClassificationRequest("Default", 24, null, ProtocolType.TCP)));
        assertEquals(null, classificationEngine.classify(new ClassificationRequest("Default", 24, null, ProtocolType.UDP)));
    }

}