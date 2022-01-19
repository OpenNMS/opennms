/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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


package org.opennms.smoketest.utils;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URL;
import java.util.Optional;

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
        return response.getStatus() == 200 && response.getHeaders().containsKey(HEALTH_KEY) ?
                response.getHeaders().get(HEALTH_KEY).toString() : "Health key not found";
    }

    public String getProbeSuccessMessage(){return SUCCESS_PROBE;}
}
