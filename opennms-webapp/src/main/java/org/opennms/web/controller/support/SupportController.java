/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.web.controller.support;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.opennms.core.resource.Vault;
import org.opennms.netmgt.rt.CustomField;
import org.opennms.netmgt.rt.RTQueue;
import org.opennms.netmgt.rt.RTTicket;
import org.opennms.netmgt.rt.RTUser;
import org.opennms.netmgt.rt.RequestTracker;
import org.opennms.netmgt.rt.RequestTrackerException;
import org.opennms.systemreport.SystemReport;
import org.opennms.systemreport.SystemReportPlugin;
import org.opennms.systemreport.formatters.FtpSystemReportFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

/**
 * SystemReportController
 *
 * @author ranger
 * @since 1.8.6
 */
public class SupportController extends AbstractController implements InitializingBean {
	
	private static final Logger LOG = LoggerFactory.getLogger(SupportController.class);

    private SystemReport m_systemReport = null;
    private SupportRtConfigDao m_configDao = null;

    /** {@inheritDoc} */
    @Override
    protected ModelAndView handleRequestInternal(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        final String operation = request.getParameter("operation");
        final HttpSession session = request.getSession(true);

        SupportResults results = new SupportResults();
        results.setSuccess(false);
        results.setNeedsLogin(false);

        LOG.debug("operation = {}", operation);
        if ("login".equals(operation)) {
            results = login(request);
        } else if (session.getAttribute("requestTracker") == null) {
            results.setNeedsLogin(true);
        } else if ("logout".equals(operation)) {
            results = logout(request);
        } else if ("createTicket".equals(operation)) {
            results = createTicket(request);
        }

        RequestTracker rt = (RequestTracker)session.getAttribute("requestTracker");

        if (results.getNeedsLogin() == false) {
            if (results.getQueue() == null) {
                final Long queueId = m_configDao.getQueueId();
                if (queueId != null) {
                    RTQueue queue = rt.getQueue(queueId);
                    results.setQueue(queue.getName());
                }
            }
            results.setUsername(rt.getUsername());
            results.setLatestTickets(rt.getTicketsForQueue(results.getQueue(), 5));
        }

        results.setRTUrl(m_configDao.getBaseURL());
        return new ModelAndView("/support/index", "results", results);
    }

    private SupportResults createTicket(final HttpServletRequest request) {
        final HttpSession session = request.getSession();

        final RequestTracker rt = (RequestTracker)session.getAttribute("requestTracker");

        // get ticket and user information
        final String subject = request.getParameter("subject").trim();
        String body = request.getParameter("text").trim();
        final String includeReport = request.getParameter("include-report");
        final boolean report  = Boolean.parseBoolean(includeReport);
        LOG.debug("include report?: {} (parsed as {})", includeReport, new Boolean(report));

        final RTUser user = rt.getUserInfo(rt.getUsername());

        String email = user.getEmail();
        if (email == null || "".equals(email)) {
            email = m_configDao.getRequestor();
        }

        final Long queueId = m_configDao.getQueueId();
        RTQueue queue = new RTQueue(0, "Unknown");
        try {
            queue = rt.getQueue(queueId);
        } catch (final RequestTrackerException e) {
            LOG.warn("Unable to determine queue for queue ID {}", queueId.toString());
        }

        // create report if necessary
        if (report) {
            final FtpSystemReportFormatter formatter = new FtpSystemReportFormatter();
            final String url = m_configDao.getFtpBaseURL() + "/" + queue.getName() + "-" + user.getUsername() + "-" + UUID.randomUUID() + ".zip";
            formatter.setOutput(url);

            formatter.begin();
            for (final SystemReportPlugin plugin : m_systemReport.getPlugins()) {
                if (plugin.getName().equals("Logs")) continue;
                
                formatter.write(plugin);
            }
            formatter.end();

            body = body.concat("\n\nSystem report is available at: " + url + "\n");
        }

        
        final SupportResults results = new SupportResults();
        results.setNeedsLogin(false);
        results.setUsername(rt.getUsername());
        results.setQueue(queue.getName());

        List<CustomField> customFields = new ArrayList<CustomField>();
        customFields.add(new CustomField(m_configDao.getVersionFieldName(), "Version " + Vault.getProperty("version.display"), false));
        customFields.add(new CustomField(m_configDao.getOSFieldName(), System.getProperty("os.name")+" "+System.getProperty("os.version")+" ("+System.getProperty("os.arch")+")", false));
        
        final RTTicket ticket = new RTTicket(queue.getName(), email, subject, body, customFields);
        try {
            final long id = rt.createTicket(ticket);
            results.setSuccess(true);
            results.setMessage("New ticket created: <a href=\"" + m_configDao.getBaseURL() + "/Ticket/Display.html?id=" + id + "\">" + id + "</a>");
        } catch (final RequestTrackerException e) {
            LOG.warn("Unable to create ticket {}", ticket, e);
            results.setSuccess(false);
            results.setMessage("Unable to create ticket: " + e.getLocalizedMessage());
        }
        return results;
    }

    private SupportResults login(final HttpServletRequest request) {
        final String username = request.getParameter("username").trim();
        final String password = request.getParameter("password").trim();

        final RequestTracker rt = new RequestTracker(m_configDao.getBaseURL(), username, password, m_configDao.getTimeout(), m_configDao.getRetry());
        LOG.debug("tracker = {}", rt);

        final SupportResults results = new SupportResults();
        results.setUsername(username);

        RTQueue queue = null;

        try {
            
            // First, check if the currently configured queue exists
            Long queueId = m_configDao.getQueueId();
            if (queueId != null) {
                queue = rt.getQueue(queueId);
            }

            // If not, try to find a default queue
            if (queue == null || !queue.isAccessible()) {
                queue = rt.getFirstPublicQueueForUser(username);
                LOG.warn("If more than one queue was found for user {}, the first was used.  ({})", username, queue);

                m_configDao.setQueueId(queue.getId());
            } else {
                LOG.debug("Existing queue found in support.properties ({}), will not overwrite.", m_configDao.getQueueId().toString());
            }

            m_configDao.setUsername(username);
            m_configDao.setPassword(password);
            m_configDao.save();
            
            final HttpSession session = request.getSession(true);
            session.setAttribute("username", username);
            session.setAttribute("password", password);
            session.setAttribute("requestTracker", rt);

            results.setSuccess(true);
            results.setNeedsLogin(false);
            results.setMessage("Logged in successfully.");
            results.setQueue(queue.getName());
            return results;
        } catch (final Exception e) {
            LOG.warn("Unable to log in user {}", username, e);
            results.setSuccess(false);
            results.setNeedsLogin(true);
            results.setMessage("Unable to log in: " + e.getLocalizedMessage());
            return results;
        }
    }
    
    private SupportResults logout(final HttpServletRequest request) {
        final HttpSession session = request.getSession(true);
        final RequestTracker rt = (RequestTracker)session.getAttribute("requestTracker");
        session.setAttribute("requestTracker", null);

        m_configDao.setUsername(null);
        m_configDao.setPassword(null);
        
        final SupportResults results = new SupportResults();
        try {
            m_configDao.save();
            results.setSuccess(true);
            results.setNeedsLogin(true);
        } catch (final IOException e) {
            LOG.warn("Unable to remove username/password from support.properties.", e);
            results.setSuccess(false);
            results.setUsername(rt.getUsername());
            results.setMessage("Unable to remove username/password from support.properties.");
        }
        return results;
    }

    public void setRtConfigDao(final SupportRtConfigDao dao) {
        m_configDao = dao;
    }
    
    public void setSystemReport(final SystemReport systemReport) {
        m_systemReport = systemReport;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(m_configDao);
        Assert.notNull(m_systemReport);
    }
}
