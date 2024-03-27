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

import java.net.URI;
import java.util.Map;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.opennms.core.web.HttpClientWrapper;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.provision.persist.AbstractRequisitionProvider;
import org.opennms.netmgt.provision.persist.requisition.Requisition;

public class HttpRequisitionProvider extends AbstractRequisitionProvider<HttpRequisitionRequest> {

    public static final String TYPE_NAME = "http";

    public HttpRequisitionProvider() {
        super(HttpRequisitionRequest.class);
    }

    @Override
    public String getType() {
        return TYPE_NAME;
    }

    @Override
    public HttpRequisitionRequest getRequest(Map<String, String> parameters) {
        return new HttpRequisitionRequest(parameters);
    }

    @Override
    public Requisition getRequisitionFor(HttpRequisitionRequest request) {
        try (HttpClientWrapper client = HttpClientWrapper.create()) {
            final URI uri = new URI(request.getUrl());
            HttpGet get = new HttpGet(uri);
            if (Boolean.FALSE.equals(request.getStrictSsl())) {
                client.trustSelfSigned(uri.getScheme());
            }
            if(Boolean.TRUE.equals(request.getUseSystemProxy())){
                client.useSystemProxySettings();
            }
            if (request.getUsername() != null) {
                client.addBasicCredentials(request.getPassword(), request.getPassword());
            }
            try (CloseableHttpResponse response = client.execute(get)) {
                String responseString = new BasicResponseHandler().handleResponse(response);
                return JaxbUtils.unmarshal(Requisition.class, responseString);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
