/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.model.topology;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BridgeTopology {
    private static final Logger LOG = LoggerFactory.getLogger(BridgeTopology.class);

    public enum BridgePortRole {
        BACKBONE,
        DIRECT
    };

    public class BridgeTopologyLinkCandidate implements Comparable<BridgeTopologyLinkCandidate> {
        private final BridgeTopologyPort bridgeTopologyPort;
        private Set<String> macs = new TreeSet<String>();
        private Set<Integer> targets = new TreeSet<Integer>();
        private BridgePortRole role;
        private BridgeTopologyPort linkportcandidate;

        public BridgeTopologyLinkCandidate(BridgeTopologyPort btp) {
            bridgeTopologyPort = btp;
            macs = bridgeTopologyPort.getMacs();
        }

        public void removeMacs(Set<String> otherMacs) {
            Set<String> curmacs = new HashSet<String>();
            for (String mac : getMacs()) {
                if (otherMacs.contains(mac)) {
                    continue;
                }
                curmacs.add(mac);
            }
            macs = curmacs;
        }

        public Set<String> getMacs() {
            return macs;
        }

        public boolean intersectionNull(BridgeTopologyLinkCandidate portcandidate) {
            for (String mac : getMacs()) {
                if (portcandidate.getMacs().contains(mac)) {
                    return false;
                }
            }
            return true;
        }

        public boolean strictContainedPort(BridgeTopologyLinkCandidate portcandidate) {
            if (portcandidate.getBridgeTopologyPort().getMacs().size() <= getBridgeTopologyPort().getMacs().size()) {
                return false;
            }
            for (String mac : getBridgeTopologyPort().getMacs()) {
                if (!portcandidate.getBridgeTopologyPort().getMacs().contains(mac)) {
                    return false;
                }
            }
            return true;

        }

        public boolean strictContained(BridgeTopologyLinkCandidate portcandidate) {
            if (portcandidate.getMacs().size() <= getMacs().size()) {
                return strictContainedPort(portcandidate);
            }
            for (String mac : getMacs()) {
                if (!portcandidate.getMacs().contains(mac)) {
                    return false;
                }
            }
            return true;
        }

        public BridgeTopologyPort getBridgeTopologyPort() {
            return bridgeTopologyPort;
        }

        public Set<Integer> getTargets() {
            return targets;
        }

        public void setTargets(Set<Integer> targets) {
            this.targets = targets;
        }

        public void addTarget(Integer target) {
            this.targets.add(target);
        }

        public BridgePortRole getRole() {
            return role;
        }

        public void setRole(BridgePortRole role) {
            this.role = role;
        }

        public BridgeTopologyPort getLinkPortCandidate() {
            return linkportcandidate;
        }

        public void setLinkPortCandidate(BridgeTopologyPort linkportcandidate) {
            this.linkportcandidate = linkportcandidate;
        }

        @Override
        public int compareTo(final BridgeTopologyLinkCandidate o) {
            return new CompareToBuilder()
                .append(bridgeTopologyPort, o.bridgeTopologyPort)
                .append(macs, o.macs)
                .append(targets, o.targets)
                .append(role, o.role)
                .append(linkportcandidate, o.linkportcandidate)
                .toComparison();
        }

    }

    public class BridgeTopologyPort implements Comparable<BridgeTopologyPort> {
        private final Integer nodeid;
        private final Integer bridgePort;
        private final Set<String> macs;

        public BridgeTopologyPort(Integer nodeid, Integer bridgePort, Set<String> macs) {
            super();
            this.nodeid = nodeid;
            this.bridgePort = bridgePort;
            this.macs = macs;
        }

        public Set<String> getMacs() {
            return macs;
        }

        public Integer getNodeid() {
            return nodeid;
        }

        public Integer getBridgePort() {
            return bridgePort;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((bridgePort == null) ? 0 : bridgePort.hashCode());
            result = prime * result + ((nodeid == null) ? 0 : nodeid.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            BridgeTopologyPort other = (BridgeTopologyPort) obj;
            if (bridgePort == null) {
                if (other.bridgePort != null) {
                    return false;
                }
            } else if (!bridgePort.equals(other.bridgePort)) {
                return false;
            }
            if (nodeid == null) {
                if (other.nodeid != null) {
                    return false;
                }
            } else if (!nodeid.equals(other.nodeid)) {
                return false;
            }
            return true;
        }

        @Override
        public int compareTo(final BridgeTopologyPort other) {
            return new CompareToBuilder()
                .append(nodeid, other.nodeid)
                .append(bridgePort, other.bridgePort)
                .append(macs, other.macs)
                .toComparison();
        }
    }

    public class BridgeTopologyLink {
        final private BridgeTopologyPort bridgePort;
        private BridgeTopologyPort designatebridgePort;

        private Set<String> macs = new TreeSet<String>();

        public BridgeTopologyLink(BridgeTopologyPort bridgeport) {
            super();
            this.bridgePort = bridgeport;
            macs = bridgeport.getMacs();
        }

        public BridgeTopologyLink(BridgeTopologyPort bridgeport, BridgeTopologyPort designatedbridgePort) {
            super();
            this.bridgePort = bridgeport;
            this.designatebridgePort = designatedbridgePort;
            for (String mac : bridgeport.getMacs()) {
                if (designatedbridgePort.getMacs().contains(mac)) {
                    macs.add(mac);
                }
            }
        }

        public Set<String> getMacs() {
            return macs;
        }

        public BridgeTopologyPort getBridgeTopologyPort() {
            return bridgePort;
        }

        public BridgeTopologyPort getDesignateBridgePort() {
            return designatebridgePort;
        }

        public boolean contains(BridgeTopologyPort bridgeport) {
            if (this.bridgePort.equals(bridgeport)) {
                return true;
            }
            if (this.designatebridgePort != null && this.designatebridgePort.equals(bridgeport)) {
                return true;
            }
            return false;
        }

    }

    private List<BridgeTopologyLink> bridgelinks = new ArrayList<BridgeTopologyLink>();
    private Map<String, Set<BridgeTopologyPort>> bridgeAssociatedMacAddressMap = new HashMap<String, Set<BridgeTopologyPort>>();
    private List<BridgeTopologyLinkCandidate> bridgeTopologyPortCandidates = new ArrayList<BridgeTopologyLinkCandidate>();

    public void addBridgeAssociatedMac(Integer nodeid, Integer port, Set<String> macsonport, String mac) {
        LOG.info( "addBridgeAssociatedMac: adding nodeid {}, bridge port {}, mac {}", nodeid, port, mac);
        if (bridgeAssociatedMacAddressMap.containsKey(mac)) {
            bridgeAssociatedMacAddressMap.get(mac).add(new BridgeTopologyPort(nodeid, port, macsonport));
        } else {
            Set<BridgeTopologyPort> ports = new TreeSet<BridgeTopologyPort>();
            ports.add(new BridgeTopologyPort(nodeid, port, macsonport));
            bridgeAssociatedMacAddressMap.put(mac, ports);
        }
    }

    private boolean parsed(BridgeTopologyPort bridgePort) {
        for (BridgeTopologyLink link : bridgelinks) {
            if (link.contains(bridgePort)) {
                return true;
            }
        }
        return false;
    }

    public void addTopology(Integer nodeid, Map<Integer, Set<String>> bridgeTopologyTable, Set<Integer> targets) {
        LOG.info("addTopology: -----------------------------------------------------");
        LOG.info("addTopology: adding bridge topology for node {} with targets {}", nodeid, targets);
        for (final Entry<Integer, Set<String>> curEntry : bridgeTopologyTable.entrySet()) {
            LOG.info("addTopology: node {}, port {}: mac {}", nodeid, curEntry.getKey(), curEntry.getValue());
            final BridgeTopologyPort btp = new BridgeTopologyPort(nodeid, curEntry.getKey(), curEntry.getValue());
            BridgeTopologyLinkCandidate candidate = new BridgeTopologyLinkCandidate(btp);
            candidate.setTargets(targets);
            bridgeTopologyPortCandidates.add(candidate);
        }
    }

    public void parseBFT(Integer nodeid, Map<Integer, Set<String>> bridgeForwardingTable) {
        LOG.info("parseBFT: -----------------------------------------------------");
        LOG.info("parseBFT: start: parsing bridge forwarding table for node {}", nodeid);

        // parsing bridge forwarding table
        for (final Entry<Integer, Set<String>> curEntry : bridgeForwardingTable.entrySet()) {
            BridgeTopologyPort bridgetopologyport = new BridgeTopologyPort(nodeid, curEntry.getKey(), curEntry.getValue());

            if (parsed(bridgetopologyport)) {
                LOG.info("parseBFT: node {}, port {} has been previuosly parsed. Skipping.", nodeid, curEntry.getKey());
                continue;
            }

            BridgeTopologyLinkCandidate topologycandidate = new BridgeTopologyLinkCandidate(bridgetopologyport);
            for (String mac : curEntry.getValue()) {
                if (bridgeAssociatedMacAddressMap.containsKey(mac)) {
                    for (BridgeTopologyPort swPort : bridgeAssociatedMacAddressMap.get(mac)) {
                        if (swPort.getNodeid().intValue() == nodeid) {
                            continue;
                        }
                        LOG.info("parseBFT: node {}, port {}: mac {} found on bridge adding target: targetnodeid {}, targetport {}", nodeid, curEntry.getKey(), mac, swPort.getNodeid(), swPort.getBridgePort());
                        topologycandidate.setLinkPortCandidate(swPort);
                        topologycandidate.addTarget(swPort.getNodeid());
                    }
                }
            }
            LOG.info("parseBFT: node {} port {} macs {} targets {} role {}",
                     topologycandidate.getBridgeTopologyPort().getNodeid(),
                     topologycandidate.getBridgeTopologyPort()
                     .getBridgePort(), topologycandidate.getMacs(),
                     topologycandidate.getTargets(), topologycandidate
                     .getRole());
            bridgeTopologyPortCandidates.add(parseBFTEntry(topologycandidate));
        }
        // first: cannot have two backbone from one bridge, so if a backbone and b with candidate, then b is direct
        Set<BridgeTopologyLinkCandidate> secondStep = new TreeSet<BridgeTopology.BridgeTopologyLinkCandidate>();
        for (BridgeTopologyLinkCandidate candidateA : bridgeTopologyPortCandidates) {
            if (candidateA.getRole() != BridgePortRole.BACKBONE) {
                continue;
            }
            for (BridgeTopologyLinkCandidate candidateB : bridgeTopologyPortCandidates) {
                if (candidateB.getBridgeTopologyPort().getNodeid().intValue() != candidateA.getBridgeTopologyPort().getNodeid().intValue()) {
                    continue;
                }
                if (candidateB.getRole() != null) {
                    continue;
                }
                if (candidateB.getLinkPortCandidate() == null) {
                    continue;
                }
                if (candidateA.getTargets().contains(candidateB.getLinkPortCandidate().getNodeid())) {
                    LOG.info("parseBFT: rule A: only one backbone port: BACKBONE node {} port {} targets {}: setting port {} to DIRECT",
                             candidateA.getBridgeTopologyPort().getNodeid(),
                             candidateA.getBridgeTopologyPort().getBridgePort(),
                             candidateA.getTargets(), candidateB
                             .getBridgeTopologyPort().getBridgePort());
                    candidateB.setRole(BridgePortRole.DIRECT);
                    candidateB.setLinkPortCandidate(null);
                    secondStep.add(candidateB);
                }
            }
        }
        // second: if a contains mac and is direct and b contains mac: then b is backbone
        for (BridgeTopologyLinkCandidate candidateA : secondStep) {
            for (BridgeTopologyLinkCandidate candidateB : bridgeTopologyPortCandidates) {
                if (candidateB.getBridgeTopologyPort().getNodeid().intValue() == candidateA.getBridgeTopologyPort().getNodeid().intValue()) {
                    continue;
                }
                if (candidateB.getRole() == BridgePortRole.DIRECT) {
                    continue;
                }
                Set<String> otherMacs = new TreeSet<String>();
                for (String mac : candidateA.getMacs()) {
                    if (candidateB.getMacs().contains(mac)) {
                        otherMacs.add(mac);
                    }
                }
                if (otherMacs.isEmpty()) {
                    continue;
                }

                LOG.info("parseBFT: rule B: found DIRECT: node {} BACKBONE port {}: removing mac {} and adding target {}",
                         candidateB.getBridgeTopologyPort().getNodeid(),
                         candidateB.getBridgeTopologyPort().getBridgePort(),
                         otherMacs, candidateA.getBridgeTopologyPort()
                         .getNodeid());
                candidateB.removeMacs(otherMacs);
                candidateB.addTarget(candidateA.getBridgeTopologyPort().getNodeid());
            }
        }

        // reset all roles
        for (BridgeTopologyLinkCandidate candidate : bridgeTopologyPortCandidates) {
            candidate.setRole(null);
        }
        LOG.info("parseBFT: end: bridge forwarding table for node {}", nodeid);
    }

    private BridgeTopologyLinkCandidate parseBFTEntry(BridgeTopologyLinkCandidate topologyLinkCandidate) {
        /*
         * This class is designed to get the topology on one bridge forwarding
         * table at a time so this means that the rules are written considering
         * port1 belonging always to the same bridge.
         * 
         * 
         * We assume the following:
         * 
         * 1) there where no loops into the network (so there is a hierarchy)
         * 
         * Corollary 1
         * 
         * If exists there is only one backbone port from sw1 and sw2 If exists
         * there is only one backbone port from sw2 and sw1
         * 
         * Corollary 2 There is only one "pseudo device" containing the bridge
         * 
         * Corollary 3 on a backbone port two different mac address must belong
         * to the same pseudo device
         */
        for (BridgeTopologyLinkCandidate linkcandidate : bridgeTopologyPortCandidates) {
            LOG.info("parseBFTEntry: cycle top: checking node {} port {} macs {} targets {} role {}",
                     linkcandidate.getBridgeTopologyPort()
                     .getNodeid(), linkcandidate.getBridgeTopologyPort()
                     .getBridgePort(), linkcandidate.getMacs(),
                     linkcandidate.getTargets(), linkcandidate.getRole());
            // regola same node non faccio niente
            if (linkcandidate.getBridgeTopologyPort().getNodeid().intValue() == topologyLinkCandidate.getBridgeTopologyPort().getNodeid().intValue()) {
                LOG.info("parseBFTEntry: rule 0: same node do nothing");
                continue;
            }
            // regola intersezione nulla non faccio niente
            if (linkcandidate.intersectionNull(topologyLinkCandidate)) {
                LOG.info("parseBFTEntry: rule 00: mac intesection null do nothing");
                continue;
            } 

            if (linkcandidate.getRole() == BridgePortRole.BACKBONE && topologyLinkCandidate.strictContainedPort(linkcandidate)) {
                LOG.info("parseBFTEntry: rule 1-d: BACKBONE checking: setting candidate to DIRECT");
                linkcandidate.removeMacs(topologyLinkCandidate.getMacs());
                topologyLinkCandidate.setRole(BridgePortRole.DIRECT);
                linkcandidate.addTarget(topologyLinkCandidate.getBridgeTopologyPort().getNodeid());
            } else if (topologyLinkCandidate.getRole() == BridgePortRole.BACKBONE && linkcandidate.strictContainedPort(topologyLinkCandidate)) {
                LOG.info("parseBFTEntry: rule 1-r: BACKBONE candidate: setting checking to DIRECT");
                topologyLinkCandidate.removeMacs(linkcandidate.getMacs());
                topologyLinkCandidate.addTarget(linkcandidate.getBridgeTopologyPort().getNodeid());
                linkcandidate.setRole(BridgePortRole.DIRECT);
            } else if (topologyLinkCandidate.strictContainedPort(linkcandidate)) {
                LOG.info("parseBFTEntry: rule 2-d: candidate strict contained: setting: candidate to DIRECT: checking to BACKBONE");
                linkcandidate.setRole(BridgePortRole.BACKBONE);
                linkcandidate.removeMacs(topologyLinkCandidate.getMacs());
                linkcandidate.addTarget(topologyLinkCandidate.getBridgeTopologyPort().getNodeid());
                topologyLinkCandidate.setRole(BridgePortRole.DIRECT);
            } else if (linkcandidate.strictContainedPort(topologyLinkCandidate)) {
                LOG.info("parseBFTEntry: rule 2-r: candidate strict contains: setting: candidate to BACKBONE: checking to DIRECT");
                topologyLinkCandidate.setRole(BridgePortRole.BACKBONE);
                topologyLinkCandidate.removeMacs(linkcandidate.getMacs());
                topologyLinkCandidate.addTarget(linkcandidate.getBridgeTopologyPort().getNodeid());
                linkcandidate.setRole(BridgePortRole.DIRECT);
            } else if (linkcandidate.getLinkPortCandidate() == null
                    && topologyLinkCandidate.getLinkPortCandidate() == null) {
                LOG.info("parseBFTEntry: rule 3: port candidate each other");
                linkcandidate.setLinkPortCandidate(topologyLinkCandidate.getBridgeTopologyPort());
                topologyLinkCandidate.setLinkPortCandidate(linkcandidate.getBridgeTopologyPort());
            } else if (linkcandidate.getLinkPortCandidate() != null
                    && topologyLinkCandidate.getBridgeTopologyPort().getNodeid().intValue() == linkcandidate.getLinkPortCandidate().getNodeid().intValue()
                    && topologyLinkCandidate.getBridgeTopologyPort().getBridgePort().intValue() != linkcandidate.getLinkPortCandidate().getBridgePort().intValue()) {
                LOG.info("parseBFTEntry: rule 4-d: checking forwards on two different ports on candidate: setting: candidate  to DIRECT: checking to BACKBONE");
                linkcandidate.setRole(BridgePortRole.BACKBONE);
                linkcandidate.removeMacs(topologyLinkCandidate.getMacs());
                linkcandidate.addTarget(topologyLinkCandidate.getBridgeTopologyPort().getNodeid());

                topologyLinkCandidate.setRole(BridgePortRole.DIRECT);
                topologyLinkCandidate.setLinkPortCandidate(null);
            } else if (topologyLinkCandidate.getLinkPortCandidate() != null
                    && linkcandidate.getBridgeTopologyPort().getNodeid().intValue() == topologyLinkCandidate.getLinkPortCandidate().getNodeid().intValue()
                    && linkcandidate.getBridgeTopologyPort().getBridgePort().intValue() != topologyLinkCandidate.getLinkPortCandidate().getBridgePort().intValue()) {
                LOG.info("parseBFTEntry: rule 4-r: candidate forwards on two different ports on checking: setting: candidate to BACKBONE: checking to DIRECT");
                topologyLinkCandidate.setRole(BridgePortRole.BACKBONE);
                topologyLinkCandidate.removeMacs(linkcandidate.getMacs());
                topologyLinkCandidate.addTarget(linkcandidate.getBridgeTopologyPort().getNodeid());

                linkcandidate.setRole(BridgePortRole.DIRECT);
                linkcandidate.setLinkPortCandidate(null);
            }
            LOG.info("parseBFTEntry: cycle end: node {} port {} macs {} targets {} role {}",
                     topologyLinkCandidate.getBridgeTopologyPort().getNodeid(),
                     topologyLinkCandidate.getBridgeTopologyPort().getBridgePort(),
                     topologyLinkCandidate.getMacs(),
                     topologyLinkCandidate.getTargets(),
                     topologyLinkCandidate.getRole());
            LOG.info("parseBFTEntry: cycle end: node {} port {} macs {} targets {} role {}",
                     linkcandidate.getBridgeTopologyPort().getNodeid(),
                     linkcandidate.getBridgeTopologyPort().getBridgePort(),
                     linkcandidate.getMacs(),
                     linkcandidate.getTargets(),
                     linkcandidate.getRole());

        }
        return topologyLinkCandidate;
    }

    public void parseSTPEntry(Integer nodeid, Integer bridgePort, Set<String> macs, Integer designatednodeid, Integer designatedport, Set<String> designatedmacs) {
        BridgeTopologyPort source = new BridgeTopologyPort(nodeid, bridgePort, macs);
        BridgeTopologyPort designated = new BridgeTopologyPort(designatednodeid, designatedport, designatedmacs);
        BridgeTopologyLinkCandidate sourceLink = new BridgeTopologyLinkCandidate(source);
        BridgeTopologyLinkCandidate designatedLink = new BridgeTopologyLinkCandidate(designated);
        BridgeTopologyLink link = new BridgeTopologyLink(source, designated);
        LOG.info("parseSTPEntry: macs on bridge {}.", macs);
        LOG.info("parseSTPEntry: macs on designated bridge {}.", designatedmacs);
        LOG.info("parseSTPEntry: nodeid {}, port {}, designated nodeid {}, designated port {}, macs on link {}.",
                 nodeid, bridgePort, designatednodeid, designatedport, link.getMacs());
        if (sourceLink.intersectionNull(designatedLink)) {
            bridgelinks.add(link);
        } else {
            sourceLink.addTarget(designatednodeid);
            designatedLink.addTarget(nodeid);
            bridgeTopologyPortCandidates.add(parseBFTEntry(sourceLink));
            bridgeTopologyPortCandidates.add(parseBFTEntry(designatedLink));
        }
    }

    public List<BridgeTopologyLink> getTopology() {
        for (BridgeTopologyLinkCandidate candidateA : bridgeTopologyPortCandidates) {
            if (parsed(candidateA.getBridgeTopologyPort())) {
                continue;
            }
            if (candidateA.getTargets().isEmpty()) {
                continue;
            }
            for (BridgeTopologyLinkCandidate candidateB : bridgeTopologyPortCandidates) {
                if (parsed(candidateB.getBridgeTopologyPort())) {
                    continue;
                }
                if (candidateB.getTargets().isEmpty()) {
                    continue;
                }
                if (candidateA.getBridgeTopologyPort().getNodeid().intValue() >= candidateB.getBridgeTopologyPort().getNodeid().intValue()) {
                    continue;
                }
                LOG.info("getTopology: bridgetobridge discovery: parsing nodeidA {}, portA {}, targetsA {}.",
                         candidateA.getBridgeTopologyPort().getNodeid(),
                         candidateA.getBridgeTopologyPort().getBridgePort(),
                         candidateA.getTargets());
                LOG.info(
                         "getTopology: bridgetobridge discovery: parsing nodeidB {}, portB {}, targetsB {}.",
                         candidateB.getBridgeTopologyPort().getNodeid(),
                         candidateB.getBridgeTopologyPort().getBridgePort(),
                         candidateB.getTargets());
                if (candidateA.getTargets().contains(candidateB.getBridgeTopologyPort().getNodeid())
                         && candidateB.getTargets().contains(candidateA.getBridgeTopologyPort().getNodeid())) {
                    // this means that there is a path from A to B but this must be compatible
                    // A--C--D---E---B
                    // Target Intersection must be null
                    boolean linkFound=true;
                    for (Integer targetA: candidateA.getTargets()) {
                        if (targetA.intValue() == candidateB.getBridgeTopologyPort().getNodeid().intValue()) {
                            continue;
                        }
                        if (candidateB.getTargets().contains(targetA)) {
                            LOG.info(
                                     "getTopology: bridgetobridge discovery: bridge found {} between A and B: skipping",
                                     targetA,
                                     candidateA.getBridgeTopologyPort().getNodeid(),
                                     candidateB.getBridgeTopologyPort().getNodeid());
                            linkFound=false;
                            break;
                        }
                    }
                    if (linkFound) {
                        BridgeTopologyLink link = new BridgeTopologyLink(candidateA.getBridgeTopologyPort(), candidateB.getBridgeTopologyPort());
                        LOG.info("getTopology: bridgetobridge discovery: link found {}", link);
                        bridgelinks.add(link);
                    }
                }
            }
        }
        for (BridgeTopologyLinkCandidate candidate : bridgeTopologyPortCandidates) {
            if (parsed(candidate.getBridgeTopologyPort())) {
                continue;
            }
            LOG.info("getTopology: mac discovery: parsing nodeid {}, port {}, macs {}, targets {}.",
                     candidate.getBridgeTopologyPort().getNodeid(),
                     candidate.getBridgeTopologyPort().getBridgePort(),
                     candidate.getMacs(),
                     candidate.getTargets());
            final BridgeTopologyPort btp = new BridgeTopologyPort(
                     candidate.getBridgeTopologyPort().getNodeid(),
                     candidate.getBridgeTopologyPort().getBridgePort(),
                     candidate.getMacs());
            BridgeTopologyLink link = new BridgeTopologyLink(btp);
            LOG.info("getTopology: bridgetomac link found {}", link);
            bridgelinks.add(link);
        }
        return bridgelinks;
    }

}
