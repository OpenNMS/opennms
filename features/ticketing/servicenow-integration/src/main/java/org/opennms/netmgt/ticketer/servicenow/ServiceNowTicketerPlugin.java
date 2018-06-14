/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.ticketer.servicenow;

import static org.opennms.netmgt.ticketer.servicenow.ServiceNowConstants.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.opennms.api.integration.ticketing.Plugin;
import org.opennms.api.integration.ticketing.PluginException;
import org.opennms.api.integration.ticketing.Ticket;
import org.opennms.core.web.HttpClientWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServiceNowTicketerPlugin implements Plugin {
    private static final Logger LOG = LoggerFactory.getLogger(ServiceNowTicketerPlugin.class);

    public ServiceNowTicketerPlugin() {
        try {
            getProperties();
        } catch (IOException e) {
            LOG.error("Unable to load servicenow.properties", e);
        }
    }
    
    public Ticket get(String ticketId) throws PluginException {
        Ticket ticket = new Ticket();
        return ticket;
    }

    public void saveOrUpdate(Ticket ticket) throws PluginException {
        LOG.debug("saveOrUpdate called with ticket: {}", ticket);
        
        String ticketId = ticket.getId();
        if (StringUtils.isEmpty(ticketId)) {
            // We are making a new ticket
            JSONObject incident = new JSONObject();
            try {
                Properties props = getProperties();
                updateIncidentWithTicket(incident, ticket);
                HttpClientWrapper clientWrapper = buildClientWrapper(props);
                
                HttpPost postMethod = new HttpPost(props.getProperty(CFG_URL + "/api/now/table/incident"));
                postMethod.addHeader("Accept", "application/json");
                StringEntity entity = new StringEntity(incident.toString(), StandardCharsets.UTF_8);
                entity.setContentType("application/json");
                
                final CloseableHttpResponse response = clientWrapper.execute(postMethod);
                if (response.getStatusLine().getStatusCode() != 201) {
                    throw new PluginException("While creating an incident, expected a status code of 201 but got " + response.getStatusLine().getStatusCode() + " " + response.getStatusLine().getReasonPhrase());
                }
                JSONObject createdIncident = getResponseJson(response);
                if (!StringUtils.isEmpty(createdIncident.get(INCIDENT_NUMBER).toString())) {
                    ticket.setId(createdIncident.get(INCIDENT_NUMBER).toString());
                }
                
            } catch (IOException ioe) {
                throw new PluginException("Failed to create incident", ioe);
            } catch (ParseException pe) {
                throw new PluginException("Failed to parse JSON response to incident creation POST", pe);
            }
        } else {
            // We are updating an existing ticket
            LOG.warn("Updates to existing tickets are not yet implemented for ServiceNow.");
        }
        
    }

    private static Properties getProperties() throws IOException {
        File config = Paths.get(System.getProperty("opennms.home"), "etc", "servicenow.properties").toFile();
        Properties props = new Properties();
        try (InputStream in = new FileInputStream(config)) {
            props.load(in);

        } catch (IOException e) {
            LOG.error("Unable to load config  {} ", config, e);
            throw new IOException("Error loading properties", e);
        }

        LOG.debug("Loaded SNOW URL {}", props.get(CFG_URL));
        LOG.debug("Loaded SNOW username {}", props.get(CFG_USERNAME));
        
        return props;
    }
    
    private void updateIncidentWithTicket(JSONObject incident, Ticket ticket) throws IOException {
        Properties props = new Properties();
        try {
            props = getProperties();
        } catch (IOException ioe) {
            LOG.error("Failed to load servicenow.properties. Falling back to all defaults.", ioe);
        }
        if (!StringUtils.isEmpty(ticket.getAttribute(CALLER_ID))) {
            incident.put(CALLER_ID, ticket.getAttribute(CALLER_ID));
        }
        if (!StringUtils.isEmpty(ticket.getAttribute(CATEGORY))) {
            incident.put(CATEGORY, ticket.getAttribute(CATEGORY));
        }
        if (!StringUtils.isEmpty(ticket.getSummary())) {
            incident.put(SHORT_DESCRIPTION, ticket.getSummary());
        }
        if (!StringUtils.isEmpty(ticket.getDetails())) {
            incident.put(DESCRIPTION, ticket.getDetails());
        }        
    }
    
    private HttpClientWrapper buildClientWrapper(Properties props) {
        HttpClientWrapper clientWrapper = HttpClientWrapper.create()
                .addBasicCredentials(props.getProperty(CFG_USERNAME), props.getProperty(CFG_PASSWORD))
                .setConnectionTimeout(Integer.valueOf(props.getProperty(CFG_CONNECTION_TIMEOUT, CFG_CONNECTION_TIMEOUT_DEFAULT)))
                .setUserAgent("OpenNMS ticketer plugin for ServiceNow");
        if (!Boolean.valueOf(props.getProperty(CFG_SSL_STRICT, CFG_SSL_STRICT_DEFAULT))) {
            try {
                clientWrapper.useRelaxedSSL("https");
            } catch (GeneralSecurityException gse) {
                LOG.error("Failed to set relaxed SSL on HTTPS", gse);
            }
        }
        return clientWrapper;
    }
    
    private JSONObject getResponseJson(CloseableHttpResponse response) throws UnsupportedOperationException, IOException, ParseException {
        return (JSONObject) new JSONParser().parse(new InputStreamReader(response.getEntity().getContent()));
    }
}
