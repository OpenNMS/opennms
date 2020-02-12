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

import java.util.List;
import java.util.Objects;

import org.opennms.core.network.IPAddressRange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.net.InetAddresses;

public class IpValue {

    private static final Logger LOG = LoggerFactory.getLogger(IpValue.class);
    private final List<IPAddressRange> ranges = Lists.newArrayList();

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
                    verifyIpAddress(rangedValue);
                }
                // Verify the range itself
                final IPAddressRange range = new IPAddressRange(rangedValues.get(0).getValue(), rangedValues.get(1).getValue());
                ranges.add(range);
            } else {
                verifyIpAddress(eachValue);
                ranges.add(new IPAddressRange(eachValue.getValue(), eachValue.getValue()));
            }
        }
    }

    public boolean isInRange(final String address) {
        return ranges.stream().anyMatch(r -> r.contains(address));
    }

    private static void verifyIpAddress(final StringValue stringValue) {
        Objects.requireNonNull(stringValue);
        if (!InetAddresses.isInetAddress(stringValue.getValue())) {
            throw new IllegalArgumentException("Provided ip address '" + stringValue.getValue() + "' is invalid");
        }
    }
}
