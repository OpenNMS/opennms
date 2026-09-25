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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/** Answers requests from a table keyed by URI prefix; anything else fails the test. */
class CannedFetcher implements HttpFetcher {

    static final class Request {
        final URI uri;
        final Map<String, String> headers;

        Request(final URI uri, final Map<String, String> headers) {
            this.uri = uri;
            this.headers = headers == null ? new HashMap<>() : new HashMap<>(headers);
        }
    }

    private interface Answer {
        Response get() throws IOException;
    }

    final List<Request> requests = new ArrayList<>();
    private final Map<String, Answer> routes = new LinkedHashMap<>();

    CannedFetcher on(final String uriPrefix, final Supplier<Response> response) {
        routes.put(uriPrefix, response::get);
        return this;
    }

    CannedFetcher failing(final String uriPrefix, final IOException failure) {
        routes.put(uriPrefix, () -> {
            throw failure;
        });
        return this;
    }

    @Override
    public Response get(final URI uri, final Map<String, String> headers) throws IOException {
        requests.add(new Request(uri, headers));
        for (final Map.Entry<String, Answer> route : routes.entrySet()) {
            if (uri.toString().startsWith(route.getKey())) {
                return route.getValue().get();
            }
        }
        throw new AssertionError("unexpected request " + uri);
    }

    static Response json(final int status, final String body, final Map<String, String> headers) {
        return bytes(status, body.getBytes(StandardCharsets.UTF_8), headers);
    }

    static Response bytes(final int status, final byte[] body, final Map<String, String> headers) {
        return new Response(status, headers, new ByteArrayInputStream(body), null);
    }
}
