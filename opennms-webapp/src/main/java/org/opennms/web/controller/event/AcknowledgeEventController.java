/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
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

package org.opennms.web.controller.event;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.core.utils.WebSecurityUtils;
import org.opennms.web.event.AcknowledgeType;
import org.opennms.web.event.WebEventRepository;
import org.opennms.web.event.filter.EventCriteria;
import org.opennms.web.event.filter.EventIdListFilter;
import org.opennms.web.filter.Filter;
import org.opennms.web.servlet.MissingParameterException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.view.RedirectView;

/**
 * This servlet receives an HTTP POST with a list of events to acknowledge or
 * unacknowledge, and then it redirects the client to a URL for display. The
 * target URL is configurable in the servlet config (web.xml file).
 *
 * @author <a href="mailto:ranger@opennms.org">Benjamin Reed</a>
 * @author <a href="http://www.opennms.org/">OpenNMS</a>
 * @since 1.8.1
 */
public class AcknowledgeEventController extends AbstractController implements InitializingBean {
	
	private static final Logger LOG = LoggerFactory.getLogger(AcknowledgeEventController.class);

    private WebEventRepository m_webEventRepository;
    
    private String m_redirectView;
    
    /**
     * <p>setRedirectView</p>
     *
     * @param redirectView a {@link java.lang.String} object.
     */
    public void setRedirectView(String redirectView) {
        m_redirectView = redirectView;
    }
    
    /**
     * <p>setWebEventRepository</p>
     *
     * @param webEventRepository a {@link org.opennms.web.event.WebEventRepository} object.
     */
    public void setWebEventRepository(WebEventRepository webEventRepository) {
        m_webEventRepository = webEventRepository;
    }

    /**
     * <p>afterPropertiesSet</p>
     *
     * @throws java.lang.Exception if any.
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(m_redirectView, "redirectView must be set");
        Assert.notNull(m_webEventRepository, "webEventRepository must be set");
    }

    /**
     * {@inheritDoc}
     *
     * Acknowledge the events specified in the POST and then redirect the client
     * to an appropriate URL for display.
     */
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {

        // required parameter
        String[] eventIdStrings = request.getParameterValues("event");
        String action = request.getParameter("actionCode");

        if (eventIdStrings == null) {
            throw new MissingParameterException("event", new String[] { "event", "actionCode" });
        }

        if (action == null) {
            throw new MissingParameterException("actionCode", new String[] { "event", "actionCode" });
        }
        
        List<Filter> filters = new ArrayList<Filter>();
        filters.add(new EventIdListFilter(WebSecurityUtils.safeParseInt(eventIdStrings)));
        EventCriteria criteria = new EventCriteria(filters.toArray(new Filter[0]));

        LOG.debug("criteria = {}, action = {}", criteria, action);
        if (action.equals(AcknowledgeType.ACKNOWLEDGED.getShortName())) {
            m_webEventRepository.acknowledgeMatchingEvents(request.getRemoteUser(), new Date(), criteria);
        } else if (action.equals(AcknowledgeType.UNACKNOWLEDGED.getShortName())) {
            m_webEventRepository.unacknowledgeMatchingEvents(criteria);
        } else {
            throw new ServletException("Unknown acknowledge action: " + action);
        }

        String redirectParms = request.getParameter("redirectParms");
        String redirect = request.getParameter("redirect");
        String viewName;
        if (redirect != null) {
            viewName = redirect;
        } else {
            viewName = (redirectParms == null || redirectParms=="" || redirectParms=="null" ? m_redirectView : m_redirectView + "?" + redirectParms);
        }
        RedirectView view = new RedirectView(viewName, true);
        return new ModelAndView(view);

    }


}
