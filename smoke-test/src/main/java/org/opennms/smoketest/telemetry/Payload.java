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
package org.opennms.smoketest.telemetry;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;

import com.google.common.io.ByteStreams;

public interface Payload {

    @NotNull byte[] load() throws IOException;

    static Payload resource(final String filename) {
        Objects.requireNonNull(filename);

        return () -> {
            try (final InputStream is = Packet.class.getResourceAsStream(filename)) {
                return ByteStreams.toByteArray(is);
            }
        };
    }

    static Payload direct(final byte... payload) {
        Objects.requireNonNull(payload);
        return () -> payload;
    }
}
