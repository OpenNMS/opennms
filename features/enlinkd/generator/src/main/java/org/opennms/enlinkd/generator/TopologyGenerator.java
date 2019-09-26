/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.enlinkd.generator;

import java.util.function.Consumer;

import org.opennms.enlinkd.generator.protocol.BridgeProtocol;
import org.opennms.enlinkd.generator.protocol.CdpProtocol;
import org.opennms.enlinkd.generator.protocol.IsIsProtocol;
import org.opennms.enlinkd.generator.protocol.LldpProtocol;
import org.opennms.enlinkd.generator.protocol.OspfProtocol;
import org.opennms.enlinkd.generator.protocol.UserDefinedProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Can be used to generate a Linkd Topology for testing purposes.
 * Usage:
 * <code>
 * TopologyGenerator generator = TopologyGenerator.builder()
 * .persister(topologyPersister)
 * ...
 * .build();
 * generator.generateTopology(); // create a topology
 * // do something with it
 * generator.deleteTopology(); // delete it again
 * </code>
 *
 * The generated nodes will belong to the category "GeneratedNode".
 */
public class TopologyGenerator {

    public static final String CATEGORY_NAME = "GeneratedNode";

    public static TopologyGeneratorBuilder builder() {
        return new TopologyGeneratorBuilder();
    }

    public enum Topology {
        ring, random, complete
    }

    public enum Protocol {
        cdp, isis, lldp, ospf, bridge, userdefined
    }

    private TopologyContext topologyContext;

    private TopologyGenerator(
            TopologyPersister persister,
            ProgressCallback progressCallback) {
        this.topologyContext = new TopologyContext(progressCallback, persister);
    }


    public void generateTopology(TopologySettings topologySettings) {
        org.opennms.enlinkd.generator.protocol.Protocol protocol = getProtocol(topologySettings);
        deleteTopology(); // Let's first get rid of old generated topologies
        protocol.createAndPersistNetwork();
    }

    public void deleteTopology() {
        this.topologyContext.getTopologyPersister().deleteTopology();
    }

    private org.opennms.enlinkd.generator.protocol.Protocol getProtocol(TopologySettings topologySettings) {
        Protocol protocol = topologySettings.getProtocol();
        if (Protocol.cdp == protocol) {
            return new CdpProtocol(topologySettings, topologyContext);
        } else if (Protocol.isis == protocol) {
            return new IsIsProtocol(topologySettings, topologyContext);
        } else if (Protocol.lldp == protocol) {
            return new LldpProtocol(topologySettings, topologyContext);
        } else if (Protocol.ospf == protocol) {
            return new OspfProtocol(topologySettings, topologyContext);
        } else if (Protocol.bridge == protocol) {
            return new BridgeProtocol(topologySettings, topologyContext);
        } else if (Protocol.userdefined == protocol) {
            return new UserDefinedProtocol(topologySettings, topologyContext);
        } else {
            throw new IllegalArgumentException("Don't know this protocol: " + topologySettings.getProtocol());
        }
    }

    public static class TopologyGeneratorBuilder {

        private TopologyPersister persister;
        private ProgressCallback progressCallback;

        private TopologyGeneratorBuilder() {
        }

        public TopologyGeneratorBuilder persister(TopologyPersister persister) {
            this.persister = persister;
            return this;
        }

        public TopologyGeneratorBuilder progressCallback(ProgressCallback progressCallback) {
            this.progressCallback = progressCallback;
            return this;
        }

        public TopologyGenerator build() {
            if(progressCallback == null) {
                // Default: use a logger
                Logger log = LoggerFactory.getLogger(TopologyGenerator.class);
                progressCallback = new ProgressCallback(log::info);
            }
            return new TopologyGenerator(persister, progressCallback);
        }
    }

    /** Used to record the current progress of the generation. */
    public static class ProgressCallback {

        private Consumer<String> consumer;

        public ProgressCallback(Consumer<String> consumer){
            this.consumer = consumer;
        }

        public void currentProgress(String progress){
            consumer.accept(progress);
        }

        public void currentProgress(String progress, Object ... args) {
            currentProgress(String.format(progress, args));
        }

    }
}
