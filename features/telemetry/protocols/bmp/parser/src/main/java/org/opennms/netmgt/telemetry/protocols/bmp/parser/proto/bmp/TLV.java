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

package org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp;

import static org.opennms.netmgt.telemetry.common.utils.BufferUtils.skip;
import static org.opennms.netmgt.telemetry.common.utils.BufferUtils.slice;
import static org.opennms.netmgt.telemetry.common.utils.BufferUtils.uint16;

import java.nio.ByteBuffer;
import java.util.function.IntFunction;

import org.opennms.netmgt.telemetry.protocols.bmp.parser.BmpParser;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.InvalidPacketException;

import com.google.common.base.MoreObjects;

public class TLV<T extends TLV.Type<V, P>, V, P> {
    public final T type;     // uint16
    public final int length; // uint16
    public final V value;    // byte[length]

    public TLV(final ByteBuffer buffer, final IntFunction<T> typer, final P parameter)  throws InvalidPacketException {
        final int type = uint16(buffer);

        this.type = typer.apply(type);
        this.length = uint16(buffer);

        if (this.type != null) {
            this.value = this.type.parse(slice(buffer, this.length), parameter);
        } else {
            BmpParser.LOG.debug("Unknown type: {}", type);
            this.value = null;
            skip(buffer, this.length);
        }
    }

    public boolean isValid() {
        return this.type != null;
    }

    @FunctionalInterface
    public interface Type<V, P> {
        V parse(final ByteBuffer buffer, final P parameter) throws InvalidPacketException;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("type", this.type)
                .add("length", this.length)
                .add("value", this.value)
                .toString();
    }
}
