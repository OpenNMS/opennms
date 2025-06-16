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
