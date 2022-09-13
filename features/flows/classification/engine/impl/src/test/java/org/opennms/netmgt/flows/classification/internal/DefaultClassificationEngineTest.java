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
import static org.junit.Assert.assertNull;

import java.util.List;
import java.util.stream.IntStream;

import org.junit.Test;
import org.opennms.netmgt.flows.classification.ClassificationEngine;
import org.opennms.netmgt.flows.classification.ClassificationRequest;
import org.opennms.netmgt.flows.classification.IpAddr;
import org.opennms.netmgt.flows.classification.dto.RuleDTO;
import org.opennms.netmgt.flows.classification.internal.value.IpRange;
import org.opennms.netmgt.flows.classification.persistence.api.Protocol;
import org.opennms.netmgt.flows.classification.persistence.api.ProtocolType;
import org.opennms.netmgt.flows.classification.persistence.api.Protocols;
import org.opennms.netmgt.flows.classification.persistence.api.Rule;
import org.opennms.netmgt.flows.classification.persistence.api.RuleBuilder;

import com.google.common.collect.Lists;

public class DefaultClassificationEngineTest {

    private static ClassificationRequest classificationRequest(String location, int srcPort, String srcAddress, int dstPort, String dstAddress, Protocol protocol) {
        return new ClassificationRequest(location, srcPort, IpAddr.of(srcAddress), dstPort, IpAddr.of(dstAddress), protocol.getDecimal(), null);
    }

    @Test
    public void verifyRuleEngineBasic() throws InterruptedException {
        DefaultClassificationEngine engine = new DefaultClassificationEngine();
        engine.load(Lists.newArrayList(
                RuleDTO.builder().withName("rule1").withPosition(1).withSrcPort(80).build(),
                RuleDTO.builder().withName("rule2").withPosition(2).withDstPort(443).build(),
                RuleDTO.builder().withName("rule3").withPosition(3).withSrcPort(8888).withDstPort(9999).build(),
                RuleDTO.builder().withName("rule4").withPosition(4).withSrcPort(8888).withDstPort(80).build(),
                RuleDTO.builder().withName("rule5").withPosition(5).build()));

        assertEquals("rule2", engine.classify(ClassificationRequest.builder().withSrcPort(9999).withDstPort(443).build()));
        assertEquals("rule3", engine.classify(ClassificationRequest.builder().withSrcPort(8888).withDstPort(9999).build()));
        assertEquals("rule4", engine.classify(ClassificationRequest.builder().withSrcPort(8888).withDstPort(80).build()));
        assertEquals("rule5", engine.classify(ClassificationRequest.builder().withSrcPort(1234).withDstPort(1234).build()));
    }


    @Test
    public void verifyRuleEngineMinimal() throws InterruptedException {
        DefaultClassificationEngine engine = new DefaultClassificationEngine();
        engine.load(Lists.newArrayList(
                RuleDTO.builder().withName("rule").withPosition(0).build()));

        assertEquals("rule", engine.classify(ClassificationRequest.builder()
                                                                  .withLocation("Default")
                                                                  .withProtocol(0)
                                                                  .withSrcPort(0)
                                                                  .withSrcAddress("0.0.0.0")
                                                                  .withDstPort(0)
                                                                  .withDstAddress("0.0.0.0")
                                                                  .withExporterAddress("0.0.0.0")
                                                                  .build()));
    }

    @Test
    public void verifyRuleEngineExtended() throws InterruptedException {
        // Define Rule set
        DefaultClassificationEngine engine = new DefaultClassificationEngine();
        engine.load(Lists.newArrayList(
                RuleDTO.builder().withName("SSH").withDstPort("22").withPosition(1).build(),
                RuleDTO.builder().withName("HTTP_CUSTOM").withDstAddress("192.168.0.1").withDstPort("80").withPosition(2).build(),
                RuleDTO.builder().withName("HTTP").withDstPort("80").withPosition(3).build(),
                RuleDTO.builder().withName("DUMMY").withDstAddress("192.168.1.0-192.168.1.255,10.10.5.3,192.168.0.0/24").withDstPort("8000-9000,80,8080").withPosition(4).build(),
                RuleDTO.builder().withName("RANGE-TEST").withDstPort("7000-8000").withPosition(5).build(),
                RuleDTO.builder().withName("OpenNMS").withDstPort("8980").withPosition(6).build(),
                RuleDTO.builder().withName("OpenNMS Monitor").withDstPort("1077").withSrcPort("5347").withSrcAddress("10.0.0.5").withPosition(7).build()));

        // Verify concrete mappings
        assertEquals("SSH",         engine.classify(classificationRequest("Default", 0, null,  22, "127.0.0.1", ProtocolType.TCP)));
        assertEquals("HTTP_CUSTOM", engine.classify(classificationRequest("Default", 0, null, 80, "192.168.0.1", ProtocolType.TCP)));
        assertEquals("HTTP",        engine.classify(classificationRequest("Default", 0, null, 80, "192.168.0.2", ProtocolType.TCP)));
        assertEquals(null,          engine.classify(classificationRequest("Default", 0, null, 5000, "localhost", ProtocolType.UDP)));
        assertEquals(null,          engine.classify(classificationRequest("Default", 0, null, 5000, "localhost", ProtocolType.TCP)));
        assertEquals("OpenNMS",     engine.classify(classificationRequest("Default", 0, null, 8980, "127.0.0.1", ProtocolType.TCP)));
        assertEquals("OpenNMS Monitor", engine.classify(
                ClassificationRequest.builder()
                        .withLocation("Default")
                        .withSrcAddress("10.0.0.5")
                        .withSrcPort(5347)
                        .withDstPort(1077)
                        .withProtocol(ProtocolType.TCP.getDecimal()).build()));
        assertEquals("OpenNMS Monitor", engine.classify(
                ClassificationRequest.builder()
                        .withLocation("Default")
                        .withSrcAddress("10.0.0.5")
                        .withSrcPort(5347)
                        .withDstPort(1077)
                        .withDstAddress("192.168.0.2")
                        .withProtocol(ProtocolType.TCP.getDecimal()).build()));
        assertEquals("HTTP", engine.classify(
                ClassificationRequest.builder()
                        .withLocation("Default")
                        .withSrcAddress("10.0.0.5")
                        .withSrcPort(5347)
                        .withDstPort(80)
                        .withDstAddress("192.168.0.2")
                        .withProtocol(ProtocolType.TCP.getDecimal()).build()));
        assertEquals("DUMMY", engine.classify(ClassificationRequest.builder()
                .withLocation("Default")
                .withSrcAddress("127.0.0.1")
                .withDstAddress("10.10.5.3")
                .withSrcPort(5213)
                .withDstPort(8080)
                .withProtocol(ProtocolType.TCP.getDecimal()).build()));

        // Verify IP Range
        var ipAddresses = IpRange.of("192.168.1.0", "192.168.1.255");
        for (var ipAddress : ipAddresses) {
            final ClassificationRequest classificationRequest = new ClassificationRequest("Default", 0, null, 8080, ipAddress, ProtocolType.TCP.getDecimal(), null);
            assertEquals("DUMMY", engine.classify(classificationRequest));

            // Populate src address and port. Result must be the same
            classificationRequest.setSrcAddress("10.0.0.1");
            classificationRequest.setSrcPort(5123);
            assertEquals("DUMMY", engine.classify(classificationRequest));
        }

        // Verify CIDR expression
        for (var ipAddress : IpRange.of("192.168.0.0", "192.168.0.255")) {
            final ClassificationRequest classificationRequest = new ClassificationRequest("Default", 0, null, 8080, ipAddress, ProtocolType.TCP.getDecimal(), null);
            assertEquals("DUMMY", engine.classify(classificationRequest));
        }

        // Verify Port Range
        IntStream.range(7000, 8000).forEach(i -> assertEquals("RANGE-TEST", engine.classify(classificationRequest("Default", 0, null,  i, "192.168.0.2", ProtocolType.TCP))));

        // Verify Port Range with Src fields populated. Result must be the same
        IntStream.range(7000, 8000).forEach(src -> {
            IntStream.range(7000, 8000).forEach(dst -> {
                final ClassificationRequest classificationRequest = ClassificationRequest.builder()
                        .withLocation("Default")
                        .withProtocol(ProtocolType.TCP.getDecimal())
                        .withSrcAddress("10.0.0.1").withSrcPort(src)
                        .withDstAddress("192.168.0.2").withDstPort(dst).build();
                assertEquals("RANGE-TEST", engine.classify(classificationRequest));
            });
        });
    }

    @Test
    public void verifyAddressRuleWins() throws InterruptedException {
        final ReloadingClassificationEngine engine = new DefaultClassificationEngine();
        engine.load(Lists.newArrayList(
                RuleDTO.builder().withName("HTTP").withDstPort(80).withPosition(1).build(),
                RuleDTO.builder().withName("XXX2").withSrcAddress("192.168.2.1").withSrcPort(4789).build(),
                RuleDTO.builder().withName("XXX").withDstAddress("192.168.2.1").build()));

        final ClassificationRequest classificationRequest = classificationRequest("Default", 0, null, 80, "192.168.2.1", ProtocolType.TCP);
        assertEquals("XXX", engine.classify(classificationRequest));
        assertEquals("XXX2", engine.classify(ClassificationRequest.builder()
                .withLocation("Default")
                .withProtocol(ProtocolType.TCP.getDecimal())
                .withSrcAddress("192.168.2.1").withSrcPort(4789)
                .withDstAddress("52.31.45.219").withDstPort(80)
                .build()));
    }

    @Test
    public void verifyAllPortsToEnsureEngineIsProperlyInitialized() throws InterruptedException {
        final ClassificationEngine classificationEngine = new DefaultClassificationEngine();
        for (int i=Rule.MIN_PORT_VALUE; i<Rule.MAX_PORT_VALUE; i++) {
            classificationEngine.classify(classificationRequest("Default", 0, null, i, "127.0.0.1", ProtocolType.TCP));
        }
    }

    // See NMS-12429
    @Test
    public void verifyDoesNotRunOutOfMemory() throws InterruptedException {
        final List<RuleDTO> rules = Lists.newArrayList();
        for (int i=0; i<100; i++) {
            final var rule = RuleDTO.builder().withName("rule1").withPosition(i+1).withProtocol(17).withDstAddress("192.168.0." + i).build();
            rules.add(rule);
        }
        final DefaultClassificationEngine engine = new DefaultClassificationEngine();
        engine.load(rules);
        engine.classify(classificationRequest("localhost", 1234, "127.0.0.1", 80, "192.168.0.1", Protocols.getProtocol("UDP")));
    }

    @Test(timeout=5000)
    public void verifyInitializesQuickly() throws InterruptedException {
        final var engine = new DefaultClassificationEngine();
        engine.load(Lists.newArrayList(RuleDTO.builder().withName("Test").build()));
    }
}
