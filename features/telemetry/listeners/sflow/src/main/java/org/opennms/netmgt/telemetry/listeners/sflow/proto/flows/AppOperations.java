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

package org.opennms.netmgt.telemetry.listeners.sflow.proto.flows;

import java.nio.ByteBuffer;

import org.opennms.netmgt.telemetry.listeners.api.utils.BufferUtils;
import org.opennms.netmgt.telemetry.listeners.sflow.InvalidPacketException;

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

public class AppOperations {
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
}
