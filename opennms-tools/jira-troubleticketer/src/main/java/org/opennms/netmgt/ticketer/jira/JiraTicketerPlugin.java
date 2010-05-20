/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007 The OpenNMS Group, Inc. All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.ticketer.jira;

import com.atlassian.jira.rpc.soap.client.JiraSoapService;
import com.atlassian.jira.rpc.soap.client.JiraSoapServiceService;
import com.atlassian.jira.rpc.soap.client.JiraSoapServiceServiceLocator;
import com.atlassian.jira.rpc.soap.client.RemoteComment;
import com.atlassian.jira.rpc.soap.client.RemoteFieldValue;
import com.atlassian.jira.rpc.soap.client.RemoteIssue;
import org.apache.commons.io.IOUtils;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.api.integration.ticketing.Ticket;
import org.opennms.api.integration.ticketing.Plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.Properties;
import java.net.URL;
import java.net.MalformedURLException;


/**
 * OpenNMS Trouble Ticket Plugin API implementation for Jira
 *
 * @author <a href="mailto:joed@opennms.org">Johan Edstrom</a>
 */

/*
* This class uses the Codehaus Swizzle-Jira library
* to manipulate issues via the RPC gateway.
* @author joed@opennms.org
*
*/

public class JiraTicketerPlugin implements Plugin {

    /*
    * @returns JiraConnection
     */
    private class JiraConnection {
        public JiraSoapService jira = null;
        public String token = null;

        public JiraSoapService getJira() {
            return jira;
        }

        public void setJira(JiraSoapService jira) {
            this.jira = jira;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }
    }

    JiraConnection getConnection() {
        JiraConnection jira = new JiraConnection();
        JiraSoapServiceService jiraSoapServiceGetter = new JiraSoapServiceServiceLocator();


        URL jiraUrl = null;

        try {
            jiraUrl = new URL(getProperties().getProperty("jira.host"));
        } catch (MalformedURLException e) {
            log().error("Failed to parse url " + jiraUrl);
        }

        try {
            jira.setJira(jiraSoapServiceGetter.getJirasoapserviceV2(jiraUrl));
        } catch (Exception e) {
            log().error("Failed initialzing JiraConnection" + e);
        }
	log().debug("Jira factory: " + jiraSoapServiceGetter.getJirasoapserviceV2Address());
        try {
            jira.setToken(jira.getJira().login(getProperties().getProperty("jira.username"), getProperties().getProperty("jira.password")));
        } catch (Exception e) {
            log().error("Login failure: " + e);
        }

        return jira;
    }

    /**
     * Implementation of TicketerPlugin API call to retrieve a Jira trouble ticket.
     *
     * @return an OpenNMS
     */

    public Ticket get(String ticketId) {
        JiraConnection jira = getConnection();

        RemoteIssue issue = new RemoteIssue();

        // w00t
        try {
            issue = jira.getJira().getIssue(jira.getToken(), ticketId);
        } catch (RemoteException e) {
            log().error("Error fetching issue: " + ticketId + " " + e);
        }

        Ticket ticket = new Ticket();
        if (issue != null) {

            ticket.setId(issue.getKey());
            ticket.setModificationTimestamp(String.valueOf(issue.getUpdated().getTime()));
            ticket.setSummary(issue.getSummary());

            RemoteComment[] comments = null;
            try {
                comments = jira.getJira().getComments(jira.getToken(), ticketId);
            } catch (RemoteException e) {
                log().error("Error retreiving remote comments " + e);
            }

            String allComments = "";
            if (comments != null) {
                for (RemoteComment comment : comments) {
                    allComments = allComments + "\n" + comment.getAuthor() + "\n" + comment.getAuthor();
                }
            }

            ticket.setDetails(allComments);
            ticket.setState(getStateFromId(issue.getStatus().toUpperCase()));

        }
        return ticket;
    }

    private int openNMSToJira(Ticket.State state) {

        switch (state) {

            case OPEN:
                return 1;
            case CANCELLED:
                return 10033;
            case CLOSED:
                //Resolved
                return 5;
            default:
                return 1;

        }
    }

    /**
     * Convenience method for converting a string representation of
     * the OpenNMS enumerated ticket states.
     *
     * @param stateIdString
     * @return the converted <code>org.opennms.api.integration.ticketing.Ticket.State</code>
     */
    private Ticket.State getStateFromId(String stateIdString) {
        if (stateIdString == null) {
            return Ticket.State.OPEN;
        }
        int stateId = Integer.parseInt(stateIdString);
        switch (stateId) {
            case 1:
                return Ticket.State.OPEN;
            case 2:
                return Ticket.State.OPEN;
            case 3:
                return Ticket.State.OPEN;
            case 4:
                return Ticket.State.OPEN;
            case 5:
                return Ticket.State.CLOSED;
            case 6:
                return Ticket.State.CANCELLED;
            case 7:
                return Ticket.State.CANCELLED;
            default:
                return Ticket.State.OPEN;

        }
    }

    /**
     * Retrieves the properties defined in the jira.properties file.
     *
     * @return a <code>java.util.Properties object containing jira plugin defined properties
     */

    private Properties getProperties() {
        File home = new File(System.getProperty("opennms.home"));
        File etc = new File(home, "etc");
        File config = new File(etc, "jira.properties");

        Properties props = new Properties();

        InputStream in = null;
        try {
            in = new FileInputStream(config);
            props.load(in);
        } catch (IOException e) {
            log().error("Unable to load " + config + " ignoring.", e);
        } finally {
            IOUtils.closeQuietly(in);
        }

	log().debug("Loaded user: " + props.getProperty("jira.username"));
	log().debug("Loaded type: " + props.getProperty("jira.type"));

        return props;

    }

    /*
    * (non-Javadoc)
    * @see org.opennms.api.integration.ticketing.Plugin#saveOrUpdate(org.opennms.api.integration.ticketing.Ticket)
    */
    public void saveOrUpdate(Ticket ticket) {

        JiraConnection jira = getConnection();
        RemoteIssue issue = new RemoteIssue();
        issue.setProject(getProperties().getProperty("jira.project"));
        issue.setReporter(getProperties().getProperty("jira.username"));
        issue.setType(getProperties().getProperty("jira.type").trim());
        issue.setSummary(ticket.getSummary());
        issue.setDescription(ticket.getSummary());
        issue.setDuedate(Calendar.getInstance());

        if (ticket.getId() == null || ticket.getId().equals("")) {
            try {
                RemoteIssue addedIssue = jira.getJira().createIssue(jira.getToken(), issue);
                ticket.setId(addedIssue.getKey());
                RemoteComment comment = new RemoteComment();
                comment.setBody(ticket.getDetails());
                comment.setAuthor(getProperties().getProperty("jira.username"));
                jira.getJira().addComment(jira.getToken(), ticket.getId(), comment);
                log().error("Ticket ID: " + addedIssue.getKey());
            } catch (Exception e) {
                log().error("Error: Could not create a Jira issue id " + e);
            }
        } else {
            log().info("Received ticket: " + ticket.getId());

            try {
                issue = jira.getJira().getIssue(jira.getToken(), ticket.getId());
            } catch (RemoteException e) {
                log().error("Error: could not retrive remote issue");
            }

            if ("closed".equals(ticket.getState().toString().toLowerCase())) {
                log().info("Closing ticket " + ticket.getId());
                RemoteComment comment = new RemoteComment();
                comment.setBody("Issue resolved by OpenNMS");
                try {
                    jira.getJira().addComment(jira.getToken(), issue.getKey(), comment);
                } catch (RemoteException e) {
                    log().error("Could not append comments " + e);
                }

                RemoteFieldValue[] resolution = new RemoteFieldValue[0];
                try {
                    jira.getJira().progressWorkflowAction(jira.getToken(), ticket.getId(), String.valueOf(openNMSToJira(ticket.getState())), resolution);
                    ticket.setState(Ticket.State.CLOSED);
                } catch (RemoteException e) {
                    log().error("Error: Could not resolve ticket");
                }
            }

            // This case is most likely never used.
            if ("open".equals(ticket.getState().toString().toLowerCase())) {
                log().info("Re-Opening ticket " + ticket.getId());
                RemoteFieldValue[] resolution = new RemoteFieldValue[0];
                try {
                    jira.getJira().progressWorkflowAction(jira.getToken(), ticket.getId(), String.valueOf(openNMSToJira(ticket.getState())), resolution);
                    ticket.setState(Ticket.State.OPEN);
                } catch (RemoteException e) {
                    log().error("Error: Could not re-open ticket");
                }
            }

        }

    }

    /**
     * Convenience logging.
     *
     * @return a log4j Category for this class
     */
    private Category log() {
        return ThreadCategory.getInstance(getClass());
    }

}
