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

package org.opennms.netmgt.telemetry.protocols.netflow.adapter.netflow5.proto;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Arrays;

import org.opennms.core.utils.InetAddressUtils;

public class Utils {
    
    static int getInt(final int startIndex, final int endIndex, final ByteBuffer data, final int offset) {
        final BigInteger bigInteger = getBigInteger(startIndex, endIndex, data, offset);
        return bigInteger.intValue();
    }
    
    static short getShort(final int startIndex, final int endIndex, final ByteBuffer data, final int offset) {
        final BigInteger bigInteger = getBigInteger(startIndex, endIndex, data, offset);
        return bigInteger.shortValue();
    }

    static long getLong(final int startIndex, final int endIndex, final ByteBuffer data, final int offset) {
        final BigInteger bigInteger = getBigInteger(startIndex, endIndex, data, offset);
        return bigInteger.longValue();
    }

    static String getInetAddress(final int startIndex, final int endIndex, final ByteBuffer data, final int offset) {
        final byte[] bytes = getBytes(data, offset + startIndex, endIndex - startIndex + 1);
        return InetAddressUtils.toIpAddrString(bytes);
    }
    
    private static BigInteger getBigInteger(final int startIndex, final int endIndex, final ByteBuffer data, final int offset) {
        final byte[] bytes = getBytes(data, offset + startIndex, endIndex - startIndex + 1);
        return new BigInteger(1, bytes);
    }

    private static byte[] getBytes(final ByteBuffer data, final int offset, final int length) {
        return Arrays.copyOfRange(data.array(), offset, offset + length);
    }

}
