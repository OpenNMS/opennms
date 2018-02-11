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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.opennms.api.integration.ticketing.Plugin;
import org.opennms.api.integration.ticketing.PluginException;
import org.opennms.api.integration.ticketing.Ticket;
import org.opennms.netmgt.ticketer.jira.cache.Cache;
import org.opennms.netmgt.ticketer.jira.cache.TimeoutRefreshPolicy;
import org.opennms.netmgt.ticketer.jira.fieldmapper.FieldMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.BasicIssue;
import com.atlassian.jira.rest.client.api.domain.CimFieldInfo;
import com.atlassian.jira.rest.client.api.domain.CimProject;
import com.atlassian.jira.rest.client.api.domain.Comment;
import com.atlassian.jira.rest.client.api.domain.FieldSchema;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.Transition;
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder;
import com.atlassian.jira.rest.client.api.domain.input.TransitionInput;
import com.google.common.base.Strings;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

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

    // In order to correctly map (custom) fields, we have to know the meta data, which does not change very often,
    // therefore we cache it.
    private final Cache<List<CimProject>> fieldInfoCache;

    private final LoadingCache<FieldSchema, FieldMapper> fieldMapFunctionCache;

    public JiraTicketerPlugin() {
        Long cacheReloadTime = getConfig().getCacheReloadTime();
        if (cacheReloadTime == null || cacheReloadTime < 0) {
            LOG.warn("Cache Reload time was set to {} ms. Negative or null values are not supported. Setting to 5 minutes.", cacheReloadTime);
            cacheReloadTime = TimeUnit.MILLISECONDS.convert(5, TimeUnit.MINUTES);
        }
        fieldMapFunctionCache = CacheBuilder.newBuilder()
                .maximumSize(100)
                .expireAfterWrite(cacheReloadTime, TimeUnit.MILLISECONDS)
                .build(new CacheLoader<FieldSchema, FieldMapper>() {
                    @Override
                    public FieldMapper load(FieldSchema schema) throws Exception {
                        final FieldMapperRegistry fieldMapperRegistry = new FieldMapperRegistry(getConfig().getProperties());
                        return fieldMapperRegistry.lookup(schema);
                    }
                });
        fieldInfoCache = new Cache<>(
                () -> {
                    try (JiraRestClient client = getConnection()) {
                        return JiraClientUtils.getIssueMetaData(
                                client,
                                "projects.issuetypes.fields",
                                getConfig().getIssueTypeId(),
                                getConfig().getProjectKey());
                    }
                },
                new TimeoutRefreshPolicy(cacheReloadTime, TimeUnit.NANOSECONDS)
        );
    }

    protected JiraRestClient getConnection() throws PluginException {
        Config config = getConfig();
        return JiraConnectionFactory.createConnection(config.getHost(), config.getUsername(), config.getPassword());
    }

    /**
     * Implementation of TicketerPlugin API call to retrieve a Jira trouble ticket.
     *
     * @return an OpenNMS
     * @throws PluginException
     */
    @Override
    public Ticket get(String ticketId) throws PluginException {
        try (JiraRestClient client = getConnection()) {
            return getInternal(ticketId, client);
        } catch (IOException e) {
            throw new PluginException(e);
        }
    }

    private Ticket getInternal(String ticketId, JiraRestClient jira) throws PluginException {
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
     * @param ticketStatusName
     * @return the converted <code>org.opennms.api.integration.ticketing.Ticket.State</code>
     */
    private static Ticket.State getStateFromStatusName(String ticketStatusName) {
        // Mapping of property key names to ticket states
        // The values for the properties at these keys should contain
        // a comma-separated list of known JIRA status names that map
        // the respective ticket states
        Map<Ticket.State, List<String>> ticketStateToJiraStatusMap = new HashMap<>();
        ticketStateToJiraStatusMap.put(Ticket.State.OPEN, getConfig().getOpenStatus());
        ticketStateToJiraStatusMap.put(Ticket.State.CLOSED, getConfig().getCloseStatus());
        ticketStateToJiraStatusMap.put(Ticket.State.CANCELLED, getConfig().getCancelStatus());

        for (Entry<Ticket.State, List<String>> entry : ticketStateToJiraStatusMap.entrySet()) {
            // Grab the value for the given property and all of the names to the set
            Set<String> knownStateIds = Sets.newHashSet(entry.getValue());

            // If there's a match, return the current ticket state
            if (knownStateIds.contains(ticketStatusName)) {
                return entry.getKey();
            }
        }

        // No match, default to open
        return Ticket.State.OPEN;
    }

    public static Config getConfig() {
        return new Config(getProperties());
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
        try (JiraRestClient client = getConnection()) {
            saveOrUpdateInternal(ticket, client);
        } catch (IOException e) {
            throw new PluginException(e);
        }
    }

    private void saveOrUpdateInternal(Ticket ticket, JiraRestClient jira) throws PluginException {
        Config config = getConfig();
        if (ticket.getId() == null || ticket.getId().equals("")) {
            // If we can't find a ticket with the specified ID then create one.
            IssueInputBuilder builder = new IssueInputBuilder(
                    config.getProjectKey(),
                    config.getIssueTypeId());
            builder.setReporterName(config.getUsername());
            builder.setSummary(ticket.getSummary());
            builder.setDescription(ticket.getDetails());

           populateFields(ticket, builder);

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
                    if (config.getResolveTransitionName().equals(transition.getName())) {
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
                LOG.warn("Could not resolve ticket {}, no '{}' operation available.", ticket.getId(), getConfig().getResolveTransitionName());
            } else if (Ticket.State.OPEN.equals(ticket.getState())) {
                Comment comment = Comment.valueOf("Issue reopened by OpenNMS.");
                for (Transition transition : transitions) {
                    if (getConfig().getReopentransitionName().equals(transition.getName())) {
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
                LOG.warn("Could not reopen ticket {}, no '{}' operation available.", ticket.getId(), getConfig().getReopentransitionName());
            }
        }
    }

    /**
     * Convenient method to populate additional fields in the {@link IssueInputBuilder}.
     * The fields are read from {@link Ticket#getAttributes()}.
     *
     * @param ticket The ticket to read the attributes from.
     * @param builder The builder to set additional fields.
     */
    private void populateFields(Ticket ticket, IssueInputBuilder builder) {
        // Only convert additional attributes to field values, if available
        if (!ticket.hasAttributes()) {
            return;
        }
        final List<String> populatedFields = Lists.newArrayList(); // List of fields already populated
        final Collection<CimFieldInfo> fields = getFields();
        for (Entry<String, String> eachEntry : ticket.getAttributes().entrySet()) {
            if (!Strings.isNullOrEmpty(eachEntry.getValue())) { // ignore null or empty values
                // Find a field representation in jira
                for (CimFieldInfo eachField : fields) {
                    if (eachEntry.getKey().equals(eachField.getId())) {
                        try {
                            final String attributeValue = eachEntry.getValue();
                            final Object mappedFieldValue = fieldMapFunctionCache.get(eachField.getSchema()).mapToFieldValue(eachField.getId(), eachField.getSchema(), attributeValue);
                            builder.setFieldValue(eachField.getId(), mappedFieldValue);
                            populatedFields.add(eachField.getId());
                            break; // we found a representation, now continue with next attribute
                        } catch (Exception ex) {
                            LOG.error("Could not convert attribute (id={}, value={}) to jira field value. Ignoring attribute.", eachField.getId(), eachEntry.getValue(), ex);
                        }
                    }
                }
            }
        }
        // Inform about not found attributes
        if (populatedFields.size() != ticket.getAttributes().size()) {
            for (String eachKey : ticket.getAttributes().keySet()) {
                if (!populatedFields.contains(eachKey)) {
                    LOG.warn("Ticket attribute '{}' is defined, but was not mapped to a (custom) field in JIRA. Attribute is skipped.", eachKey);
                }
            }
        }

        // Inform if required attribute has not been set
        final List<CimFieldInfo> requiredFieldsNotSet = fields.stream().filter(CimFieldInfo::isRequired).filter(f -> !populatedFields.contains(f)).collect(Collectors.toList());
        if (!requiredFieldsNotSet.isEmpty()) {
            final String missingFields = requiredFieldsNotSet.stream().map(f -> String.format("id: %s, name: %s", f.getId(), f.getName())).collect(Collectors.joining(", "));
            LOG.warn("Not all required (custom) jira fields have been set. The following are unset: {}", missingFields);
        }
    }

    private Collection<CimFieldInfo> getFields() {
        try {
            return JiraClientUtils.getFields(fieldInfoCache.get());
        } catch (Exception ex) {
            LOG.error("Error while retrieving (custom) field definitions from JIRA.", ex);
            return new ArrayList<>();
        }
    }
}
