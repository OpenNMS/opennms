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

package org.opennms.netmgt.flows.classification.internal;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.stream.IntStream;

import org.junit.Test;
import org.opennms.core.network.IPAddress;
import org.opennms.core.network.IPAddressRange;
import org.opennms.netmgt.flows.classification.ClassificationEngine;
import org.opennms.netmgt.flows.classification.ClassificationRequest;
import org.opennms.netmgt.flows.classification.ClassificationRequestBuilder;
import org.opennms.netmgt.flows.classification.FilterService;
import org.opennms.netmgt.flows.classification.persistence.api.ProtocolType;
import org.opennms.netmgt.flows.classification.persistence.api.Rule;
import org.opennms.netmgt.flows.classification.persistence.api.RuleBuilder;

import com.google.common.collect.Lists;

public class DefaultClassificationEngineTest {

    @Test
    public void verifyRuleEngineBasic() {
        DefaultClassificationEngine engine = new DefaultClassificationEngine(() ->
            Lists.newArrayList(
                    new RuleBuilder().withName("rule1").withSrcPort(80).build(),
                    new RuleBuilder().withName("rule2").withDstPort(443).build(),
                    new RuleBuilder().withName("rule3").withSrcPort(8888).withDstPort(9999).build(),
                    new RuleBuilder().withName("rule4").withSrcPort(8888).withDstPort(80).build(),
                    new RuleBuilder().withName("rule5").build()
            ), FilterService.NOOP);

        assertEquals("rule2", engine.classify(new ClassificationRequestBuilder().withSrcPort(9999).withDstPort(443).build()));
        assertEquals("rule3", engine.classify(new ClassificationRequestBuilder().withSrcPort(8888).withDstPort(9999).build()));
        assertEquals("rule4", engine.classify(new ClassificationRequestBuilder().withSrcPort(8888).withDstPort(80).build()));
    }

    @Test
    public void verifyRuleEngineExtended() {
        // Define Rule set
        DefaultClassificationEngine engine = new DefaultClassificationEngine(() -> Lists.newArrayList(
                new Rule("SSH", "22"),
                new Rule("HTTP", "80"),
                new Rule("HTTP_CUSTOM", "192.168.0.1", "80"),
                new Rule("DUMMY", "192.168.1.*", "8000-9000,80,8080"),
                new Rule("RANGE-TEST", "7000-8000"),
                new Rule("OpenNMS", "8980"),
                new RuleBuilder().withName("OpenNMS Monitor").withDstPort("1077").withSrcPort("5347").withSrcAddress("10.0.0.5").build()
            ), FilterService.NOOP
        );
      
        // Verify concrete mappings
        assertEquals("SSH",         engine.classify(new ClassificationRequest("Default", 22, "127.0.0.1", ProtocolType.TCP)));
        assertEquals("HTTP_CUSTOM", engine.classify(new ClassificationRequest("Default", 80, "192.168.0.1", ProtocolType.TCP)));
        assertEquals("HTTP",        engine.classify(new ClassificationRequest("Default", 80, "192.168.0.2", ProtocolType.TCP)));
        assertEquals(null,          engine.classify(new ClassificationRequest("Default", 5000, "localhost", ProtocolType.UDP)));
        assertEquals(null,          engine.classify(new ClassificationRequest("Default", 5000, "localhost", ProtocolType.TCP)));
        assertEquals("OpenNMS",     engine.classify(new ClassificationRequest("Default", 8980, "127.0.0.1", ProtocolType.TCP)));
        assertEquals("OpenNMS Monitor", engine.classify(
                new ClassificationRequestBuilder()
                        .withLocation("Default")
                        .withSrcAddress("10.0.0.5")
                        .withSrcPort(5347)
                        .withDstPort(1077)
                        .withProtocol(ProtocolType.TCP).build()));
        assertEquals("OpenNMS Monitor", engine.classify(
                new ClassificationRequestBuilder()
                        .withLocation("Default")
                        .withSrcAddress("10.0.0.5")
                        .withSrcPort(5347)
                        .withDstPort(1077)
                        .withDstAddress("192.168.0.2")
                        .withProtocol(ProtocolType.TCP).build()));
        assertEquals("HTTP", engine.classify(
                new ClassificationRequestBuilder()
                        .withLocation("Default")
                        .withSrcAddress("10.0.0.5")
                        .withSrcPort(5347)
                        .withDstPort(80)
                        .withDstAddress("192.168.0.2")
                        .withProtocol(ProtocolType.TCP).build()));

        // Verify IP Range
        final IPAddressRange ipAddresses = new IPAddressRange("192.168.1.0", "192.168.1.255");
        for (IPAddress ipAddress : ipAddresses) {
            final ClassificationRequest classificationRequest = new ClassificationRequest("Default", 8080, ipAddress.toString(), ProtocolType.TCP);
            assertEquals("DUMMY", engine.classify(classificationRequest));

            // Populate src address and port. Result must be the same
            classificationRequest.setSrcAddress("10.0.0.1");
            classificationRequest.setSrcPort(5123);
            assertEquals("DUMMY", engine.classify(classificationRequest));
        }

        // Verify Port Range
        IntStream.range(7000, 8000).forEach(i -> assertEquals("RANGE-TEST", engine.classify(new ClassificationRequest("Default", i, "192.168.0.2", ProtocolType.TCP))));

        // Verify Port Range with Src fields populated. Result must be the same
        IntStream.range(7000, 8000).forEach(src -> {
            IntStream.range(7000, 8000).forEach(dst -> {
                final ClassificationRequest classificationRequest = new ClassificationRequestBuilder()
                        .withLocation("Default")
                        .withProtocol(ProtocolType.TCP)
                        .withSrcAddress("10.0.0.1").withSrcPort(src)
                        .withDstAddress("192.168.0.2").withDstPort(dst).build();
                assertEquals("RANGE-TEST", engine.classify(classificationRequest));
            });
        });
    }

    @Test
    public void verifyAddressRuleWins() {
        final ClassificationEngine engine = new DefaultClassificationEngine(() -> Lists.newArrayList(
            new RuleBuilder().withName("HTTP").withDstPort(80).build(),
            new RuleBuilder().withName("XXX2").withSrcAddress("192.168.2.1").withSrcPort(4789).build(),
            new RuleBuilder().withName("XXX").withDstAddress("192.168.2.1").build()
        ), FilterService.NOOP);

        final ClassificationRequest classificationRequest = new ClassificationRequest("Default", 80, "192.168.2.1", ProtocolType.TCP);
        assertEquals("XXX", engine.classify(classificationRequest));
        assertEquals("XXX2", engine.classify(new ClassificationRequestBuilder()
                .withLocation("Default")
                .withProtocol(ProtocolType.TCP)
                .withSrcAddress("192.168.2.1").withSrcPort(4789)
                .withDstAddress("52.31.45.219").withDstPort(80)
                .build()));
    }

    @Test
    public void verifyAllPortsToEnsureEngineIsProperlyInitialized() {
        final ClassificationEngine classificationEngine = new DefaultClassificationEngine(() -> new ArrayList<>(), FilterService.NOOP);
        for (int i=Rule.MIN_PORT_VALUE; i<Rule.MAX_PORT_VALUE; i++) {
            classificationEngine.classify(new ClassificationRequest("Default", i, "127.0.0.1", ProtocolType.TCP));
        }
    }

    @Test(timeout=5000)
    public void verifyInitializesQuickly() {
        new DefaultClassificationEngine(() -> Lists.newArrayList(new Rule("Test", "0-10000")), FilterService.NOOP);
    }
}