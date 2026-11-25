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
package org.opennms.web.rest.v2.plugins;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * The single HTTP primitive the repository features need: one GET that does not
 * follow redirects, so every hop can be validated by the caller. Tests inject
 * canned responses through it.
 */
public interface HttpFetcher {

    Response get(URI uri, Map<String, String> headers) throws IOException;

    /** Releases the underlying client; a fetcher that holds no resources needs nothing here. */
    default void close() throws IOException {
    }

    final class Response implements Closeable {
        private final int status;
        private final Map<String, String> headers;
        private final InputStream body;
        private final Closeable resource;

        public Response(final int status, final Map<String, String> headers, final InputStream body, final Closeable resource) {
            this.status = status;
            final Map<String, String> normalised = new HashMap<>();
            if (headers != null) {
                for (final Map.Entry<String, String> e : headers.entrySet()) {
                    normalised.putIfAbsent(e.getKey().toLowerCase(Locale.ROOT), e.getValue());
                }
            }
            this.headers = Collections.unmodifiableMap(normalised);
            this.body = body == null ? InputStream.nullInputStream() : body;
            this.resource = resource;
        }

        public int getStatus() {
            return status;
        }

        public String header(final String name) {
            return headers.get(name.toLowerCase(Locale.ROOT));
        }

        public Map<String, String> getHeaders() {
            return headers;
        }

        /** The Content-Length header, or -1 when absent or unparseable. */
        public long contentLength() {
            final String value = header("Content-Length");
            if (value == null) {
                return -1;
            }
            try {
                return Long.parseLong(value.trim());
            } catch (final NumberFormatException e) {
                return -1;
            }
        }

        public InputStream getBody() {
            return body;
        }

        /** At most {@code maxBytes} of the body; ask for one byte more than the cap to learn whether it was exceeded. */
        public byte[] bodyBytes(final int maxBytes) throws IOException {
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            final byte[] buffer = new byte[8192];
            int read;
            while (out.size() < maxBytes && (read = body.read(buffer, 0, Math.min(buffer.length, maxBytes - out.size()))) >= 0) {
                out.write(buffer, 0, read);
            }
            return out.toByteArray();
        }

        public String bodyAsString(final int maxBytes) throws IOException {
            return new String(bodyBytes(maxBytes), StandardCharsets.UTF_8);
        }

        /**
         * The connection goes first: closing an httpclient body stream drains it to
         * EOF so the connection can be reused, which on an aborted download means
         * reading the rest of the file.
         */
        @Override
        public void close() throws IOException {
            try {
                if (resource != null) {
                    resource.close();
                }
            } finally {
                body.close();
            }
        }
    }
}
