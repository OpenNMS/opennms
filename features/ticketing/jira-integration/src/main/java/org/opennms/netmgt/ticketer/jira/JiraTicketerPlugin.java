/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.ticketer.jira;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.joda.time.DateTime;
import org.opennms.api.integration.ticketing.Plugin;
import org.opennms.api.integration.ticketing.PluginException;
import org.opennms.api.integration.ticketing.Ticket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClientFactory;
import com.atlassian.jira.rest.client.api.domain.BasicIssue;
import com.atlassian.jira.rest.client.api.domain.Comment;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.Transition;
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder;
import com.atlassian.jira.rest.client.api.domain.input.TransitionInput;
import com.atlassian.jira.rest.client.auth.AnonymousAuthenticationHandler;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;

/**
 * OpenNMS Trouble Ticket Plugin API implementation for Atlassian JIRA.
 * This implementation relies on the JIRA REST interface and is compatible
 * with JIRA 5.0+.
 *
 * @see http://www.atlassian.com/software/jira/overview
 * @see https://docs.atlassian.com/jira-rest-java-client-api/3.0.0/jira-rest-java-client-api/apidocs/
 *
 * @author <a href="mailto:joed@opennms.org">Johan Edstrom</a>
 * @author Seth
 */
public class JiraTicketerPlugin implements Plugin {
    private static final Logger LOG = LoggerFactory.getLogger(JiraTicketerPlugin.class);

    protected final JiraRestClientFactory clientFactory = new AsynchronousJiraRestClientFactory();

    protected JiraRestClient getConnection() {
        try {
            URI jiraUri = new URL(getProperties().getProperty("jira.host")).toURI();
            String username = getProperties().getProperty("jira.username");
            if (username == null || "".equals(username)) {
                return clientFactory.create(jiraUri, new AnonymousAuthenticationHandler());
            } else {
                return clientFactory.createWithBasicHttpAuthentication(jiraUri, getProperties().getProperty("jira.username"), getProperties().getProperty("jira.password"));
            }
        } catch (MalformedURLException e) {
            LOG.error("Failed to parse URL: {}", getProperties().getProperty("jira.host"));
        } catch (URISyntaxException e) {
            LOG.error("Failed to parse URI: {}", getProperties().getProperty("jira.host"));
        }
        return null;
    }

    /**
     * Implementation of TicketerPlugin API call to retrieve a Jira trouble ticket.
     *
     * @return an OpenNMS
     * @throws PluginException
     */
    @Override
    public Ticket get(String ticketId) throws PluginException {
        JiraRestClient jira = getConnection();
        if (jira == null) {
            return null;
        }

        // w00t
        Issue issue;
        try {
            issue = jira.getIssueClient().getIssue(ticketId).get();
        } catch (InterruptedException|ExecutionException e) {
            throw new PluginException("Failed to get issue with id: " + ticketId, e);
        }

        if (issue != null) {
            Ticket ticket = new Ticket();

            ticket.setId(issue.getKey());
            ticket.setModificationTimestamp(String.valueOf(issue.getUpdateDate().toDate().getTime()));
            ticket.setSummary(issue.getSummary());
            ticket.setDetails(issue.getDescription());
            ticket.setState(getStateFromStatusName(issue.getStatus().getName()));

            return ticket;
        } else {
            return null;
        }
    }

    /**
     * Convenience method for converting a string representation of
     * the OpenNMS enumerated ticket states.
     *
     * @param stateIdString
     * @return the converted <code>org.opennms.api.integration.ticketing.Ticket.State</code>
     */
    private static Ticket.State getStateFromStatusName(String ticketStatusName) {
        // Mapping of property key names to ticket states
        // The values for the properties at these keys should contain
        // a comma-separated list of known JIRA status names that map
        // the respective ticket states
        Map<Ticket.State, String> ticketStateToPropNameMap = new HashMap<>();
        ticketStateToPropNameMap.put(Ticket.State.OPEN, "jira.status.open");
        ticketStateToPropNameMap.put(Ticket.State.CLOSED, "jira.status.closed");
        ticketStateToPropNameMap.put(Ticket.State.CANCELLED, "jira.status.cancelled");

        Properties jiraProperties = getProperties();
        for (Entry<Ticket.State, String> entry : ticketStateToPropNameMap.entrySet()) {
            // Grab the value for the given property and all of the names to the set
            Set<String> knownStateIds = new HashSet<>();
            String stateIdsProp = jiraProperties.getProperty(entry.getValue());
            if (stateIdsProp != null) {
                knownStateIds.addAll(Arrays.asList(stateIdsProp.split(",")));
            }

            // If there's a match, return the current ticket state
            if (knownStateIds.contains(ticketStatusName)) {
                return entry.getKey();
            }
        }

        // No match, default to open
        return Ticket.State.OPEN;
    }

    /**
     * Retrieves the properties defined in the jira.properties file.
     *
     * @return a <code>java.util.Properties object containing jira plugin defined properties
     */
    private static Properties getProperties() {
        File home = new File(System.getProperty("opennms.home"));
        File etc = new File(home, "etc");
        File config = new File(etc, "jira.properties");

        Properties props = new Properties();

        try (InputStream in = new FileInputStream(config)) {
            props.load(in);
        } catch (IOException e) {
            LOG.error("Unable to load {} ignoring.", config, e);
        }

        LOG.debug("Loaded user: {}", props.getProperty("jira.username"));
        LOG.debug("Loaded type: {}", props.getProperty("jira.type"));

        return props;
    }

    /*
    * (non-Javadoc)
    * @see org.opennms.api.integration.ticketing.Plugin#saveOrUpdate(org.opennms.api.integration.ticketing.Ticket)
    */
    @Override
    public void saveOrUpdate(Ticket ticket) throws PluginException {

        JiraRestClient jira = getConnection();

        if (ticket.getId() == null || ticket.getId().equals("")) {
            // If we can't find a ticket with the specified ID then create one.
            IssueInputBuilder builder = new IssueInputBuilder(getProperties().getProperty("jira.project"), Long.valueOf(getProperties().getProperty("jira.type").trim()));
            builder.setReporterName(getProperties().getProperty("jira.username"));
            builder.setSummary(ticket.getSummary());
            builder.setDescription(ticket.getDetails());
            builder.setDueDate(new DateTime(Calendar.getInstance()));

            BasicIssue createdIssue;
            try {
                createdIssue = jira.getIssueClient().createIssue(builder.build()).get();
            } catch (InterruptedException | ExecutionException e) {
                throw new PluginException("Failed to create issue.", e);
            }
            LOG.info("created ticket " + createdIssue);

            ticket.setId(createdIssue.getKey());

        } else {
            // Otherwise update the existing ticket
            LOG.info("Received ticket: {}", ticket.getId());

            Issue issue;
            try {
                issue = jira.getIssueClient().getIssue(ticket.getId()).get();
            } catch (InterruptedException | ExecutionException e) {
                throw new PluginException("Failed to get issue with id:" + ticket.getId(), e);
            }

            Iterable<Transition> transitions;
            try {
                transitions = jira.getIssueClient().getTransitions(issue).get();
            } catch (InterruptedException | ExecutionException e) {
                throw new PluginException("Failed to get transitions for issue with id:" + issue.getId(), e);
            }

            if (Ticket.State.CLOSED.equals(ticket.getState())) {
                Comment comment = Comment.valueOf("Issue resolved by OpenNMS.");
                for (Transition transition : transitions) {
                    if (getProperties().getProperty("jira.resolve").equals(transition.getName())) {
                        LOG.info("Resolving ticket {}", ticket.getId());
                        // Resolve the issue
                        try {
                            jira.getIssueClient().transition(issue, new TransitionInput(transition.getId(), comment)).get();
                        } catch (InterruptedException | ExecutionException e) {
                            throw new PluginException("Failed to get resolve issue with id:" + issue.getId(), e);
                        }
                        return;
                    }
                }
                LOG.warn("Could not resolve ticket {}, no '{}' operation available.", ticket.getId(), getProperties().getProperty("jira.resolve"));
            } else if (Ticket.State.OPEN.equals(ticket.getState())) {
                Comment comment = Comment.valueOf("Issue reopened by OpenNMS.");
                for (Transition transition : transitions) {
                    if (getProperties().getProperty("jira.reopen").equals(transition.getName())) {
                        LOG.info("Reopening ticket {}", ticket.getId());
                        // Resolve the issue
                        try {
                            jira.getIssueClient().transition(issue, new TransitionInput(transition.getId(), comment)).get();
                        } catch (InterruptedException | ExecutionException e) {
                            throw new PluginException("Failed to reopen issue with id:" + issue.getId(), e);
                        }
                        return;
                    }
                }
                LOG.warn("Could not reopen ticket {}, no '{}' operation available.", ticket.getId(), getProperties().getProperty("jira.reopen"));
            }
        }
    }
}
