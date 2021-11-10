/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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
package org.opennms.netmgt.flows.classification.internal.decision;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.opennms.netmgt.flows.classification.ClassificationRequest;
import org.opennms.netmgt.flows.classification.IpAddr;
import org.opennms.netmgt.flows.classification.internal.value.IpValue;
import org.opennms.netmgt.flows.classification.internal.value.PortValue;

/**
 * Represents a threshold that divides rules sets during decision tree construction
 * and guides tree traversal during classification.
 */
public abstract class Threshold<T extends Comparable<T>> {

    /**
     * Holds the result of matching a collection of classification rules against a threshold.
     * <p>
     * Rules may be include in zero, one, or more collections.
     */
    public static class Matches {
        public final List<PreprocessedRule> lt, eq, gt, na;

        public Matches(List<PreprocessedRule> lt, List<PreprocessedRule> eq, List<PreprocessedRule> gt, List<PreprocessedRule> na) {
            this.lt = lt;
            this.eq = eq;
            this.gt = gt;
            this.na = na;
        }
    }

    /**
     * Indicates the order of a classification request relative to a threshold.
     */
    public enum Order {
        LT,
        EQ,
        GT,
        NA // indicates that a classification request can not be compared to a threshold because it does not have a corresponding value
    }

    /**
     * Bundles the information how a rule matches a threshold. More than one flag may be {@code true}.
     */
    private static class Match {
        final boolean lt, eq, gt, na;

        Match(boolean lt, boolean eq, boolean gt, boolean na) {
            this.lt = lt;
            this.eq = eq;
            this.gt = gt;
            this.na = na;
        }

        static Match NA = new Match(false, false, false, true);
    }

    protected final Function<Bounds, Bound<T>> getBound;
    private final BiFunction<Bounds, Bound<T>, Bounds> setBound;

    public Threshold(
            Function<Bounds, Bound<T>> getBound,
            BiFunction<Bounds, Bound<T>, Bounds> setBound
    ) {
        this.getBound = getBound;
        this.setBound = setBound;
    }

    // the threshold values are stored in subclasses for two reasons:
    // 1. in case of protocol and port thresholds are primitives that have far better performance
    // 2. avoid pitfall: while "<" and ">" work as expected when working with boxed numbers
    //    "==" checks object identity. This leads to subtle bugs in the match and compare methods.
    public abstract T getThreshold();

    /**
     * Checks for every rule if it matches values that are less than, equal to, or greater than this threshold and
     * adds that rule the corresponding collections. Rules that do not have a value corresponding to this
     * threshold are added to the {@link Matches#na} collection.
     * <p>
     * This method is used to build a decision tree for rule sets.
     * <p>
     * <strong>Note:</strong> A rule may be added to more than one collection. For example rules may cover
     * IP address ranges that include an address threshold. In that case the rule is added to the lt, eq, and gt
     * collections.
     */
    public Matches match(Collection<PreprocessedRule> ruleSet, Bounds bounds) {
        var lt = new ArrayList<PreprocessedRule>();
        var eq = new ArrayList<PreprocessedRule>();
        var gt = new ArrayList<PreprocessedRule>();
        var na = new ArrayList<PreprocessedRule>();
        for (var rule : ruleSet) {
            var cr = match(rule, bounds);
            if (cr.lt) {
                lt.add(rule);
            }
            if (cr.eq) {
                eq.add(rule);
            }
            if (cr.gt) {
                gt.add(rule);
            }
            if (cr.na) {
                na.add(rule);
            }
        }
        return new Matches(optimize(lt), optimize(eq), optimize(gt), optimize(na));
    }

    private static <T> List<T> optimize(List<T> list) {
        if (list.isEmpty()) {
            return Collections.emptyList();
        } else if (list.size() == 1) {
            return Collections.singletonList(list.get(0));
        } else {
            return list;
        }
    }

    /**
     * Checks if the given request has a value that is less than, equal to, or greater than the threshold value.
     * In case that the given request has no value that corresponds to this threshold {@link Order#NA} is returned.
     */
    public abstract Order compare(ClassificationRequest request);

    public final boolean canRestrict(Bounds bounds) {
        return getBound.apply(bounds).canBeRestrictedBy(getThreshold());
    }

    /** Uses this threshold to restrict the corresponding bound in the given bounds. */
    public final Bounds lt(Bounds bounds) {
        return setBound.apply(bounds, getBound.apply(bounds).lt(getThreshold()));
    }

    /** Uses this threshold to restrict the corresponding bound in the given bounds. */
    public final Bounds eq(Bounds bounds) {
        return setBound.apply(bounds, getBound.apply(bounds).eq(getThreshold()));
    }

    /** Uses this threshold to restrict the corresponding bound in the given bounds. */
    public final Bounds gt(Bounds bounds) {
        return setBound.apply(bounds, getBound.apply(bounds).gt(getThreshold()));
    }

    /**
     * Checks if the given rule matches values that are less than, equal to, or greater than this threshold.
     * <p>
     * The given bounds are also considered. A rule matches only if it specifies values within the given bounds.
     */
    protected abstract Match match(PreprocessedRule rule, Bounds bounds);

    public final static class Protocol extends Threshold<Integer> {

        private final int protocol;

        public Protocol(int protocol) {
            super(
                    bs -> bs.protocol,
                    (bs, b) -> new Bounds(b, bs.srcPort, bs.dstPort, bs.srcAddr, bs.dstAddr)
            );
            this.protocol = protocol;
        }

        @Override
        public final Integer getThreshold() {
            return protocol;
        }

        @Override
        protected final Match match(PreprocessedRule rule, Bounds bounds) {
            if (rule.protocol == null) {
                return Match.NA;
            } else {
                var lt = false;
                var eq = false;
                var gt = false;
                for (int p : rule.protocol.getProtocols()) {
                    if (!bounds.protocol.includes(p)) {
                        continue;
                    }
                    lt |= p < protocol;
                    eq |= p == protocol;
                    gt |= p > protocol;
                    if (lt && eq && gt) {
                        break;
                    }
                }
                return new Match(lt, eq, gt, false);
            }
        }

        @Override
        public final Order compare(ClassificationRequest request) {
            if (request.getProtocol() != null) {
                var p = request.getProtocol().getDecimal();
                return p < protocol ? Order.LT : p == protocol ? Order.EQ : Order.GT;
            } else {
                return Order.NA;
            }
        }

        @Override
        public final boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Protocol protocol1 = (Protocol) o;
            return protocol == protocol1.protocol;
        }

        @Override
        public final int hashCode() {
            return Objects.hash(protocol);
        }

        @Override
        public String toString() {
            return "Protocol{" +
                   "protocol=" + protocol +
                   '}';
        }

    }

    public abstract static class Port extends Threshold<Integer> {
        protected final int port;
        private final Function<PreprocessedRule, PortValue> getRulePort;
        private final Function<ClassificationRequest, Integer> getRequestPort;

        public Port(
                Function<Bounds, Bound<Integer>> getBound,
                BiFunction<Bounds, Bound<Integer>, Bounds> setBound,
                int port,
                Function<PreprocessedRule, PortValue> getRulePort,
                Function<ClassificationRequest, Integer> getRequestPort
        ) {
            super(getBound, setBound);
            this.port = port;
            this.getRulePort = getRulePort;
            this.getRequestPort = getRequestPort;
        }

        @Override
        public final Integer getThreshold() {
            return port;
        }

        @Override
        protected final Match match(PreprocessedRule rule, Bounds bounds) {
            var portValue = getRulePort.apply(rule);
            if (portValue == null) {
                return Match.NA;
            } else {
                var bound = getBound.apply(bounds);
                var lt = false;
                var eq = false;
                var gt = false;
                for (var range : portValue.getPortRanges()) {
                    if (!bound.overlaps(range.getBegin(), range.getEnd())) {
                        continue;
                    }
                    lt |= range.getBegin() < port;
                    eq |= range.contains(port);
                    gt |= range.getEnd() > port;
                    if (lt && eq && gt) {
                        break;
                    }
                }
                return new Match(lt, eq, gt, false);
            }
        }

        @Override
        public final Order compare(ClassificationRequest request) {
            var p = getRequestPort.apply(request);
            if (p != null) {
                return p < port ? Order.LT : p == port ? Order.EQ : Order.GT;
            } else {
                return Order.NA;
            }
        }

        @Override
        public final boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Port port1 = (Port) o;
            return port == port1.port;
        }

        @Override
        public final int hashCode() {
            return Objects.hash(port);
        }
    }

    public final static class SrcPort extends Port {
        public SrcPort(int port) {
            super(
                    bs -> bs.srcPort,
                    (bs, b) -> new Bounds(bs.protocol, b, bs.dstPort, bs.srcAddr, bs.dstAddr),
                    port,
                    pr -> pr.srcPort,
                    ClassificationRequest::getSrcPort
            );
        }

        @Override
        public String toString() {
            return "SrcPort{" +
                   "port=" + port +
                   '}';
        }
    }

    public final static class DstPort extends Port {
        public DstPort(int port) {
            super(
                    bs -> bs.dstPort,
                    (bs, b) -> new Bounds(bs.protocol, bs.srcPort, b, bs.srcAddr, bs.dstAddr),
                    port,
                    pr -> pr.dstPort,
                    ClassificationRequest::getDstPort
            );
        }

        @Override
        public String toString() {
            return "DstPort{" +
                   "port=" + port +
                   '}';
        }
    }

    public static abstract class Address extends Threshold<IpAddr> {
        protected final IpAddr address;
        private final Function<PreprocessedRule, IpValue> getRuleAddress;
        private final Function<ClassificationRequest, IpAddr> getRequestAddress;

        public Address(
                Function<Bounds, Bound<IpAddr>> getBound,
                BiFunction<Bounds, Bound<IpAddr>, Bounds> setBound,
                IpAddr address,
                Function<PreprocessedRule, IpValue> getRuleAddress,
                Function<ClassificationRequest, IpAddr> getRequestAddress
        ) {
            super(getBound, setBound);
            this.address = address;
            this.getRuleAddress = getRuleAddress;
            this.getRequestAddress = getRequestAddress;
        }

        @Override
        public final IpAddr getThreshold() {
            return address;
        }

        @Override
        protected final Match match(PreprocessedRule rule, Bounds bounds) {
            var ipValue = getRuleAddress.apply(rule);
            if (ipValue == null) {
                return Match.NA;
            } else {
                var lt = false;
                var eq = false;
                var gt = false;
                var bound = getBound.apply(bounds);
                for (var range : ipValue.getIpAddressRanges()) {
                    if (!bound.overlaps(range.begin, range.end)) {
                        continue;
                    }
                    lt |= range.begin.compareTo(address) < 0;
                    eq |= range.contains(address);
                    gt |= range.end.compareTo(address) > 0;
                    if (lt && eq && gt) {
                        break;
                    }
                }
                return new Match(lt, eq, gt, false);
            }
        }

        @Override
        public final Order compare(ClassificationRequest request) {
            var s = getRequestAddress.apply(request);
            if (s != null) {
                var c = s.compareTo(address);
                return c < 0 ? Order.LT : c == 0 ? Order.EQ : Order.GT;
            } else {
                return Order.NA;
            }
        }

        @Override
        public final boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Address address1 = (Address) o;
            return address.equals(address1.address);
        }

        @Override
        public final int hashCode() {
            return Objects.hash(address);
        }
    }

    public final static class SrcAddress extends Address {
        public SrcAddress(IpAddr address) {
            super(
                    bs -> bs.srcAddr,
                    (bs, b) -> new Bounds(bs.protocol, bs.srcPort, bs.dstPort, b, bs.dstAddr),
                    address,
                    pr -> pr.srcAddr,
                    ClassificationRequest::getSrcAddress
            );
        }

        @Override
        public String toString() {
            return "SrcAddress{" +
                   "address=" + address +
                   '}';
        }
    }

    public final static class DstAddress extends Address {
        public DstAddress(IpAddr address) {
            super(
                    bs -> bs.dstAddr,
                    (bs, b) -> new Bounds(bs.protocol, bs.srcPort, bs.dstPort, bs.srcAddr, b),
                    address,
                    pr -> pr.dstAddr,
                    ClassificationRequest::getDstAddress
            );
        }

        @Override
        public String toString() {
            return "DstAddress{" +
                   "address=" + address +
                   '}';
        }
    }

}
