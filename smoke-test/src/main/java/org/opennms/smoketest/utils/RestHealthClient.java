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
package org.opennms.smoketest.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Optional;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;

/**
 * Rest Health client used to verify HealCheck implementations statuses
 */
public class RestHealthClient {

    private URL url;

    private Optional<String> alias;

    private Client client;

    private final static String PROBE = "/rest/health/probe";
    private final static String SUCCESS_PROBE = "Everything is awesome";
    private final static String HEALTH_KEY = "Health";

    /**
     * HealthRestclient constructor
     * @param webUrl: implementation url required to create the http requests
     * @param alias: container alias
     */
    public RestHealthClient(final URL webUrl, final Optional<String> alias){
        this.alias = alias;
        this.url = webUrl;
        this.client = ClientBuilder.newClient();
    }

    private WebTarget getTargetFor(final String path){

        return alias.isPresent() ? client.target(url.toString()).path(alias.get()).path(path) : client.target(url.toString()).path(path);
    }

    public String getProbeHealthResponse(){
        Response response
                = getTargetFor(PROBE).request(MediaType.TEXT_PLAIN).get();
        /*
        return response.getStatus() == 200 && response.getHeaders().containsKey(HEALTH_KEY + "foo") ?
                response.getHeaders().get(HEALTH_KEY +"foo").toString() : { throw new RuntimeException("Health key not found in: " + response.toString()); return ""; };
                */

        if (response.getStatus() == 200 && response.getHeaders().containsKey(HEALTH_KEY)) {
            return response.getHeaders().get(HEALTH_KEY).toString();
        }

        try {
            return "Response status != 200 or " + HEALTH_KEY + " header not found. Dumping response.\nStatus: " + response.getStatus() + "\nHeaders: " + response.getStringHeaders() + "\n" + IOUtils.toString((InputStream)response.getEntity());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getProbeSuccessMessage(){return SUCCESS_PROBE;}
}
