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
