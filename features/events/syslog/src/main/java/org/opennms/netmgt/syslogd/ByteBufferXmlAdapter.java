/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.syslogd;

import java.nio.ByteBuffer;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class ByteBufferXmlAdapter extends XmlAdapter<byte[], ByteBuffer> {

    @Override
    public ByteBuffer unmarshal(byte[] bytes) {
        return ByteBuffer.wrap(bytes);
    }

    @Override
    public byte[] marshal(ByteBuffer bb) {
        if (bb.hasArray()) {
            // Use the backing array when available
            return bb.array();
        } else {
            // Otherwise, create a new array, and copy the available
            // bytes while preserving the original position
            final int originalPosition = bb.position();
            bb.rewind();
            byte[] bytes = new byte[bb.remaining()];
            bb.get(bytes);
            bb.position(originalPosition);
            return bytes;
        }
    }

}
