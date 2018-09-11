/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.flows;

import java.nio.ByteBuffer;

import org.bson.BsonWriter;
import org.opennms.netmgt.telemetry.common.utils.BufferUtils;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.InvalidPacketException;

import com.google.common.base.MoreObjects;

// struct app_operations {
//   application application;
//   unsigned int success;
//   unsigned int other;
//   unsigned int timeout;
//   unsigned int internal_error;
//   unsigned int bad_request;
//   unsigned int forbidden;
//   unsigned int too_large;
//   unsigned int not_implemented;
//   unsigned int not_found;
//   unsigned int unavailable;
//   unsigned int unauthorized;
// };

public class AppOperations implements CounterData {
    public final Application application;
    public final long success;
    public final long other;
    public final long timeout;
    public final long internal_error;
    public final long bad_request;
    public final long forbidden;
    public final long too_large;
    public final long not_implemented;
    public final long not_found;
    public final long unavailable;
    public final long unauthorized;

    public AppOperations(final ByteBuffer buffer) throws InvalidPacketException {
        this.application = new Application(buffer);
        this.success = BufferUtils.uint32(buffer);
        this.other = BufferUtils.uint32(buffer);
        this.timeout = BufferUtils.uint32(buffer);
        this.internal_error = BufferUtils.uint32(buffer);
        this.bad_request = BufferUtils.uint32(buffer);
        this.forbidden = BufferUtils.uint32(buffer);
        this.too_large = BufferUtils.uint32(buffer);
        this.not_implemented = BufferUtils.uint32(buffer);
        this.not_found = BufferUtils.uint32(buffer);
        this.unavailable = BufferUtils.uint32(buffer);
        this.unauthorized = BufferUtils.uint32(buffer);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("application", this.application)
                .add("success", this.success)
                .add("other", this.other)
                .add("timeout", this.timeout)
                .add("internal_error", this.internal_error)
                .add("bad_request", this.bad_request)
                .add("forbidden", this.forbidden)
                .add("too_large", this.too_large)
                .add("not_implemented", this.not_implemented)
                .add("not_found", this.not_found)
                .add("unavailable", this.unavailable)
                .add("unauthorized", this.unauthorized)
                .toString();
    }

    @Override
    public void writeBson(final BsonWriter bsonWriter) {
        bsonWriter.writeStartDocument();
        bsonWriter.writeName("application");
        this.application.writeBson(bsonWriter);
        bsonWriter.writeInt64("success", this.success);
        bsonWriter.writeInt64("other", this.other);
        bsonWriter.writeInt64("timeout", this.timeout);
        bsonWriter.writeInt64("internal_error", this.internal_error);
        bsonWriter.writeInt64("bad_request", this.bad_request);
        bsonWriter.writeInt64("forbidden", this.forbidden);
        bsonWriter.writeInt64("too_large", this.too_large);
        bsonWriter.writeInt64("not_implemented", this.not_implemented);
        bsonWriter.writeInt64("not_found", this.not_found);
        bsonWriter.writeInt64("unavailable", this.unavailable);
        bsonWriter.writeInt64("unauthorized", this.unauthorized);
        bsonWriter.writeEndDocument();
    }
}
