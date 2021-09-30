/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
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
 * OpenNMS(R) Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *******************************************************************************/

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
