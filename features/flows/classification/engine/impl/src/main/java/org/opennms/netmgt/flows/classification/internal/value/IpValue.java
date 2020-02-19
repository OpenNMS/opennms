/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020-2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.flows.classification.internal.value;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.opennms.core.network.IPAddressRange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.google.common.collect.Lists;
import com.google.common.net.InetAddresses;

public class IpValue {

    private static final Logger LOG = LoggerFactory.getLogger(IpValue.class);
    private final List<IpAddressRange> ranges = Lists.newArrayList();

    public IpValue(final String input) {
        this(new StringValue(input));
    }

    public IpValue(final StringValue input) {
        Objects.requireNonNull(input);
        parse(input);
    }

    private void parse(final StringValue input) {
        if (input.isNullOrEmpty()) {
            throw new IllegalArgumentException("input may not be null or empty");
        }
        final List<StringValue> actualValues = input.splitBy(",");
        for (StringValue eachValue : actualValues) {
            // In case it is ranged, verify the range
            if (eachValue.isRanged()) {
                final List<StringValue> rangedValues = eachValue.splitBy("-");
                // either a-, or a-b-c, etc.
                if (rangedValues.size() != 2) {
                    LOG.warn("Received multiple ranges {}. Will only use {}", rangedValues, rangedValues.subList(0, 2));
                }
                // Ensure each range is an ip address
                for (StringValue rangedValue : rangedValues) {
                    if (rangedValue.contains("/")) {
                        throw new IllegalArgumentException("Ranged value may not contain a CIDR expression");
                    }
                }
                ranges.add(new IpAddressRange(rangedValues.get(0), rangedValues.get(1)));
            } else {
                ranges.add(new IpAddressRange(eachValue));
            }
        }
    }

    public boolean isInRange(final String address) {
        return ranges.stream().anyMatch(r -> r.isInRange(address));
    }

    private static class IpAddressRange {

        private final IpAddressMatcher matcher;

        private IpAddressRange(final StringValue from, final StringValue to) {
            this.matcher = new IpAddressRangeMatcher(from.getValue(), to.getValue());
        }

        private IpAddressRange(final StringValue eachValue) {
            if (eachValue.contains("/")) {
                this.matcher = new IpV6CidrExpressionMatcher(eachValue.getValue());
            } else {
                this.matcher = new IpAddressRangeMatcher(eachValue.getValue());
            }
        }

        public boolean isInRange(String input) {
            return matcher.matches(input);
        }
    }

    private interface IpAddressMatcher {
        boolean matches(String address);
    }

    private static class IpAddressRangeMatcher implements IpAddressMatcher {

        private final IPAddressRange range;

        private IpAddressRangeMatcher(final String from, final String to) {
            verifyIpAddress(from);
            verifyIpAddress(to);
            this.range = new IPAddressRange(from, to);
        }

        public IpAddressRangeMatcher(String value) {
            verifyIpAddress(value);
            this.range = new IPAddressRange(value);
        }

        @Override
        public boolean matches(String address) {
            return range.contains(address);
        }

        private static void verifyIpAddress(final String value) {
            Objects.requireNonNull(value);
            if (!InetAddresses.isInetAddress(value)) {
                throw new IllegalArgumentException("Provided ip address '" + value + "' is invalid");
            }
        }
    }

    // Inspired by spring-security-web's IpAddressMatcher
    public final static class IpV6CidrExpressionMatcher implements IpAddressMatcher {

        private final int nMaskBits;
        private final InetAddress requiredAddress;

        public IpV6CidrExpressionMatcher(String ipAddress) {
            if (ipAddress.indexOf('/') > 0) {
                String[] addressAndMask = StringUtils.split(ipAddress, "/");
                ipAddress = addressAndMask[0];
                nMaskBits = Integer.parseInt(addressAndMask[1]);
            } else {
                nMaskBits = -1;
            }
            requiredAddress = parseAddress(ipAddress);
            boolean isIpV6 = requiredAddress instanceof Inet6Address;
            rangeCheck(nMaskBits, 0, isIpV6 ? 128 : 32);
        }

        @Override
        public boolean matches(String address) {
            InetAddress remoteAddress = parseAddress(address);

            if (!requiredAddress.getClass().equals(remoteAddress.getClass())) {
                return false;
            }

            if (nMaskBits < 0) {
                return remoteAddress.equals(requiredAddress);
            }

            byte[] remAddr = remoteAddress.getAddress();
            byte[] reqAddr = requiredAddress.getAddress();

            int oddBits = nMaskBits % 8;
            int nMaskBytes = nMaskBits/8 + (oddBits == 0 ? 0 : 1);
            byte[] mask = new byte[nMaskBytes];

            Arrays.fill(mask, 0, oddBits == 0 ? mask.length : mask.length - 1, (byte)0xFF);

            if (oddBits != 0) {
                int finalByte = (1 << oddBits) - 1;
                finalByte <<= 8-oddBits;
                mask[mask.length - 1] = (byte) finalByte;
            }

            for (int i=0; i < mask.length; i++) {
                if ((remAddr[i] & mask[i]) != (reqAddr[i] & mask[i])) {
                    return false;
                }
            }

            return true;
        }

        private InetAddress parseAddress(String address) {
            return InetAddresses.forString(address);
        }

        /*
         * Convenience function to check integer boundaries.
         * Checks if a value x is in the range [begin,end].
         * Returns x if it is in range, throws an exception otherwise.
         */
        private int rangeCheck(int value, int begin, int end) {
            if (value >= begin && value <= end) { // (begin,end]
                return value;
            }
            throw new IllegalArgumentException("Value [" + value + "] not in range [" + begin + "," + end + "]");
        }
    }
}
