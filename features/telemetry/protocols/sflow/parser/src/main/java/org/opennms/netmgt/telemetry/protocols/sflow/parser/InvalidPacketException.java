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
package org.opennms.netmgt.telemetry.protocols.sflow.parser;

import io.netty.buffer.ByteBuf;

public class InvalidPacketException extends Exception {

    public InvalidPacketException(final ByteBuf buffer, final String fmt, final Object... args) {
        super(appendPosition(String.format(fmt, args), buffer));
    }

    public InvalidPacketException(final ByteBuf buffer, final String message, final Throwable cause) {
        super(appendPosition(message, buffer), cause);
    }

    private static String appendPosition(final String message, final ByteBuf buffer) {
        if (buffer.hasArray()) {
            return String.format("%s [0x%04X]", message, buffer.arrayOffset() + buffer.readerIndex());
        } else {
            return message;
        }
    }
}
