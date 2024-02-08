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
package org.opennms.netmgt.provision.detector.wsman;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import org.opennms.core.wsman.WSManEndpoint;
import org.opennms.core.wsman.WSManVersion;

public class WsmanEndpointUtils {

    private static final String URL = "url";
    private static final String SERVER_VERSION = "server-version";
    private static final String GSS_AUTH = "gss-auth";
    private static final String STRICT_SSL = "strict-ssl";
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final String CONNECTION_TIMEOUT = "connection-timeout";
    private static final String MAX_ELEMENTS = "max-elements";
    private static final String MAX_ENVELOPE_SIZE = "max-envelope-size";
    private static final String RECEIVE_TIMEOUT = "receive-timeout";

    public static Map<String, String> toMap(WSManEndpoint endpoint) {
        Map<String, String> attributes = new HashMap<>();
        attributes.put(URL, endpoint.getUrl().toString());
        attributes.put(SERVER_VERSION, endpoint.getServerVersion().toString());
        attributes.put(GSS_AUTH, Boolean.toString(endpoint.isGSSAuth()));
        attributes.put(STRICT_SSL, Boolean.toString(endpoint.isStrictSSL()));
        if (endpoint.isBasicAuth()) {
            attributes.put(USERNAME, endpoint.getUsername());
            attributes.put(PASSWORD, endpoint.getPassword());
        }
        if (endpoint.getConnectionTimeout() != null) {
            attributes.put(CONNECTION_TIMEOUT, endpoint.getConnectionTimeout().toString());
        }
        if (endpoint.getMaxElements() != null) {
            attributes.put(MAX_ELEMENTS, endpoint.getMaxElements().toString());
        }
        if (endpoint.getMaxEnvelopeSize() != null) {
            attributes.put(MAX_ENVELOPE_SIZE, endpoint.getMaxEnvelopeSize().toString());
        }
        if (endpoint.getReceiveTimeout() != null) {
            attributes.put(RECEIVE_TIMEOUT, endpoint.getReceiveTimeout().toString());
        }
        return attributes;
    }

    public static WSManEndpoint fromMap(Map<String, String> attributes) throws MalformedURLException {
        WSManEndpoint.Builder builder = new WSManEndpoint.Builder(attributes.get(URL));
        builder.withServerVersion(WSManVersion.valueOf(attributes.get(SERVER_VERSION)));
        if (Boolean.parseBoolean(attributes.get(GSS_AUTH))) {
            builder.withGSSAuth();
        }
        builder.withStrictSSL(Boolean.parseBoolean(attributes.get(STRICT_SSL)));
        if (attributes.containsKey(USERNAME)) {
            builder.withBasicAuth(attributes.get(USERNAME), attributes.get(PASSWORD));
        }
        if (attributes.containsKey(CONNECTION_TIMEOUT)) {
            builder.withConnectionTimeout(Integer.parseInt(attributes.get(CONNECTION_TIMEOUT)));
        }
        if (attributes.containsKey(MAX_ELEMENTS)) {
            builder.withMaxElements(Integer.parseInt(attributes.get(MAX_ELEMENTS)));
        }
        if (attributes.containsKey(MAX_ENVELOPE_SIZE)) {
            builder.withMaxEnvelopeSize(Integer.parseInt(attributes.get(MAX_ENVELOPE_SIZE)));
        }
        if (attributes.containsKey(RECEIVE_TIMEOUT)) {
            builder.withReceiveTimeout(Integer.parseInt(attributes.get(RECEIVE_TIMEOUT)));
        }
        return builder.build();
    }

}
