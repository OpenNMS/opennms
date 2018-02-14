/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.service.requisition;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.opennms.core.spring.BeanUtils;
import org.opennms.core.utils.url.GenericURLConnection;
import org.opennms.core.xml.XmlHandler;
import org.opennms.netmgt.provision.persist.LocationAwareRequisitionClient;
import org.opennms.netmgt.provision.persist.requisition.Requisition;

/**
 * URL handler for the requisition:// protocol.
 *
 * @author jwhite
 */
public class RequisitionUrlConnection extends URLConnection {

    private final String type;

    private final Map<String, String> parameters;

    private static final XmlHandler<Requisition> s_xmlHandler = new XmlHandler<>(Requisition.class);

    private static LocationAwareRequisitionClient s_requisitionProviderClient;

    public RequisitionUrlConnection(URL url) {
        super(url);
        // Store the type and defer looking up the provider until we actually need it
        type = url.getHost();
        // Parse parameters immediately and propagate any related exceptions 
        parameters = getParameters(url);
    }

    @Override
    public InputStream getInputStream() throws IOException {
        try {
            final Requisition requisition = getClient().requisition()
                .withRequisitionProviderType(type)
                .withParameters(parameters)
                .execute()
                .get();

            if (requisition == null) {
                throw new IOException(String.format("Invalid (null) requisition was returned by the provider for type '%s'", type));
            }

            // The XmlHandler is not thread safe
            // Marshaling is quick, so we opt to use a single instance of the handler
            // instead of using thread-local variables
            final String requisitionXml;
            synchronized(s_xmlHandler) {
                requisitionXml = s_xmlHandler.marshal(requisition);
            }

            return new ByteArrayInputStream(requisitionXml.getBytes());
        } catch (ExecutionException|InterruptedException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void connect() {
        // pass
    }

    private LocationAwareRequisitionClient getClient() {
        if (s_requisitionProviderClient == null) {
            s_requisitionProviderClient = BeanUtils.getBean("daoContext", "locationAwareRequisitionClient", LocationAwareRequisitionClient.class);
        }
        return s_requisitionProviderClient;
    }

    public static void setClient(LocationAwareRequisitionClient client) {
        s_requisitionProviderClient = client;
    }

    public static Map<String, String> getParameters(URL url) {
        final Map<String, String> params = new HashMap<>();
        params.put("type", url.getHost());
        // Don't include the port, since it can be misleading. Instead, let
        // the implementation provide their own default port
        //params.put("port", Integer.toString(url.getPort()));
        params.put("path", url.getPath());

        // Extract the username and password
        final String userInfo = url.getUserInfo();
        if (userInfo != null && !userInfo.isEmpty()) {
            if (userInfo.contains(":")) {
                final String[] tokens = userInfo.split(":");
                params.put("username", tokens[0]);
                params.put("password", tokens[1]);
            } else {
                params.put("username", userInfo);
            }
        }

        // Parse the query string parametrs
        params.putAll(GenericURLConnection.getQueryStringParameters(url.getQuery()));
        return Collections.unmodifiableMap(params);
    }

}
