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
import org.jsoup.Jsoup;
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
            LOG.debug("Creating a new ticket for alarmID {}", ticket.getAlarmId());
            JSONObject incident = new JSONObject();
            try {
                Properties props = getProperties();
                updateIncidentWithTicket(incident, ticket);
                HttpClientWrapper clientWrapper = buildClientWrapper(props);
                LOG.debug("Got an HttpClientWrapper: {}", clientWrapper);
                
                HttpPost postMethod = new HttpPost(props.getProperty(CFG_URL) + "/api/now/table/incident");
                postMethod.addHeader("Accept", "application/json");
                LOG.debug("Build POST method: {}", postMethod);
                
                StringEntity entity = new StringEntity(incident.toString(), StandardCharsets.UTF_8);
                entity.setContentType("application/json");
                LOG.debug("Executing POST with incident JSON entity: {}", incident);
                
                postMethod.setEntity(entity);
                
                final CloseableHttpResponse response = clientWrapper.execute(postMethod);
                if (response.getStatusLine().getStatusCode() != 201) {
                    LOG.error("Expected a 201 response from POST but got: {}", response.getStatusLine());
                    throw new PluginException("While creating an incident, expected a status code of 201 but got " + response.getStatusLine().getStatusCode() + " " + response.getStatusLine().getReasonPhrase());
                }
                LOG.debug("Received POST response with entity: {}", response.getEntity());
                JSONObject createdIncident = getResponseJson(response);
                LOG.debug("Created JSON object from POST response entity: {}", createdIncident);
                if (!StringUtils.isEmpty(createdIncident.get(INCIDENT_NUMBER).toString())) {
                    ticket.setId(createdIncident.get(INCIDENT_NUMBER).toString());
                }
                LOG.debug("Created ServiceNow incident with number: {}", createdIncident.get(INCIDENT_NUMBER).toString());
                
            } catch (IOException ioe) {
                LOG.error("Caught IOException while creating ServiceNow incident", ioe);
                throw new PluginException("Failed to create incident", ioe);
            } catch (ParseException pe) {
                LOG.error("Caught ParseException while creating ServiceNow incident", pe);
                throw new PluginException("Failed to parse JSON response to incident creation POST", pe);
            }
        } else {
            // We are updating an existing ticket
            LOG.warn("Updates to existing incidents are not yet implemented for the ServiceNow ticketer.");
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
    
    @SuppressWarnings("unchecked")
    private void updateIncidentWithTicket(JSONObject incident, Ticket ticket) throws IOException {
        if (!StringUtils.isEmpty(ticket.getAttribute(CALLER_ID))) {
            incident.put(CALLER_ID, JSONObject.escape(ticket.getAttribute(CALLER_ID)));
        }
        if (!StringUtils.isEmpty(ticket.getAttribute(CATEGORY))) {
            incident.put(CATEGORY, JSONObject.escape(ticket.getAttribute(CATEGORY)));
        }
        if (!StringUtils.isEmpty(ticket.getSummary())) {
            incident.put(SHORT_DESCRIPTION, JSONObject.escape(Jsoup.parse(ticket.getSummary()).text()));
        }
        if (!StringUtils.isEmpty(ticket.getDetails())) {
            incident.put(DESCRIPTION, JSONObject.escape(Jsoup.parse(ticket.getDetails()).text()));
        }        
    }
    
    private HttpClientWrapper buildClientWrapper(Properties props) {
        HttpClientWrapper clientWrapper = HttpClientWrapper.create()
                .addBasicCredentials(props.getProperty(CFG_USERNAME), props.getProperty(CFG_PASSWORD))
                .setConnectionTimeout(Integer.valueOf(props.getProperty(CFG_CONNECTION_TIMEOUT, CFG_CONNECTION_TIMEOUT_DEFAULT)))
                .setUserAgent("OpenNMS ticketer plugin for ServiceNow")
                .usePreemptiveAuth();
        if (!Boolean.valueOf(props.getProperty(CFG_SSL_STRICT, CFG_SSL_STRICT_DEFAULT))) {
            try {
                clientWrapper.useRelaxedSSL("https");
            } catch (GeneralSecurityException gse) {
                LOG.error("Failed to set relaxed SSL on HTTPS", gse);
            }
        }
        LOG.debug("Built an HttpClientWrapper: {}", clientWrapper);
        return clientWrapper;
    }
    
    private JSONObject getResponseJson(CloseableHttpResponse response) throws UnsupportedOperationException, IOException, ParseException {
        JSONObject outerResponse = (JSONObject) new JSONParser().parse(new InputStreamReader(response.getEntity().getContent()));
        LOG.debug("Received outer response from ServiceNow API: {}", outerResponse);
        return (JSONObject) new JSONParser().parse(outerResponse.get("result").toString());
    }
}
