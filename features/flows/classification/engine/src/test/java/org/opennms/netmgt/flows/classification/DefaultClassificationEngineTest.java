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

package org.opennms.netmgt.flows.classification;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.stream.IntStream;

import org.junit.Test;
import org.opennms.core.network.IPAddress;
import org.opennms.core.network.IPAddressRange;
import org.opennms.netmgt.flows.classification.persistence.api.ProtocolType;
import org.opennms.netmgt.flows.classification.persistence.api.Rule;
import org.opennms.netmgt.flows.classification.persistence.api.RuleBuilder;
import org.opennms.netmgt.flows.classification.provider.ClassificationRuleProvider;
import org.opennms.netmgt.flows.classification.provider.StaticClassificationRuleProvider;

import com.google.common.collect.Lists;

public class DefaultClassificationEngineTest {

    @Test
    public void verifyRuleEngine() {
        // Define Rule set
        DefaultClassificationEngine engine = new DefaultClassificationEngine(() -> Lists.newArrayList(
                new Rule("SSH", "22"),
                new Rule("HTTP", "80"),
                new Rule("HTTP_CUSTOM", "192.168.0.1", "80"),
                new Rule("DUMMY", "192.168.1.*", "8000-9000,80,8080"),
                new Rule("RANGE-TEST", "7000-8000"),
                new Rule("OpenNMS", "8980")
            )
        );
      
        // Verify concrete mappings
        assertEquals("SSH", engine.classify(new ClassificationRequest("Default", 22, "127.0.0.1", ProtocolType.TCP)));
        assertEquals("HTTP_CUSTOM", engine.classify(new ClassificationRequest("Default", 80, "192.168.0.1", ProtocolType.TCP)));
        assertEquals("HTTP", engine.classify(new ClassificationRequest("Default", 80, "192.168.0.2", ProtocolType.TCP)));
        assertEquals(null, engine.classify(new ClassificationRequest("Default", 5000, "localhost", ProtocolType.UDP)));
        assertEquals(null, engine.classify(new ClassificationRequest("Default", 5000, "localhost", ProtocolType.TCP)));
        assertEquals("OpenNMS", engine.classify(new ClassificationRequest("Default", 8980, "127.0.0.1", ProtocolType.TCP)));

        // Verify IP Range
        final IPAddressRange ipAddresses = new IPAddressRange("192.168.1.0", "192.168.1.255");
        for (IPAddress ipAddress : ipAddresses) {
            assertEquals("DUMMY", engine.classify(new ClassificationRequest("Default", 8080, ipAddress.toString(), ProtocolType.TCP)));
        }

        // Verify Port Range
        IntStream.range(7000, 8000).forEach(i -> assertEquals("RANGE-TEST", engine.classify(new ClassificationRequest("Default", i, "192.168.0.2", ProtocolType.TCP))));
    }

    @Test
    public void verifyIpRuleWins() {
        final ClassificationEngine engine = new DefaultClassificationEngine(() -> Lists.newArrayList(
            new RuleBuilder().withName("HTTP").withPort(80).build(),
            new RuleBuilder().withName("XXX").withIpAddress("192.168.2.1").build()
        ));

        final ClassificationRequest classificationRequest = new ClassificationRequest("Default", 80, "192.168.2.1", ProtocolType.TCP);
        assertEquals("XXX", engine.classify(classificationRequest));
    }

    @Test
    public void verifyStaticRules() throws IOException {
        final ClassificationRuleProvider classificationRuleProvider = new StaticClassificationRuleProvider();
        final ClassificationEngine classificationEngine = new DefaultClassificationEngine(classificationRuleProvider);

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