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

package org.opennms.web.controller.alarm;

import java.util.ArrayList;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.netmgt.dao.api.AlarmRepository;
import org.opennms.netmgt.model.OnmsCriteria;
import org.opennms.web.alarm.AcknowledgeType;
import org.opennms.web.alarm.AlarmUtil;
import org.opennms.web.alarm.filter.AlarmCriteria;
import org.opennms.web.filter.Filter;
import org.opennms.web.servlet.MissingParameterException;
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
 * @author <A HREF="mailto:larry@opennms.org">Lawrence Karnowski </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @since 1.8.1
 */
public class AcknowledgeAlarmByFilterController extends AbstractController implements InitializingBean {
    private AlarmRepository m_webAlarmRepository;
    
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
     * <p>setWebAlarmRepository</p>
     *
     * @param webAlarmRepository a {@link org.opennms.netmgt.dao.api.AlarmRepository} object.
     */
    public void setAlarmRepository(AlarmRepository webAlarmRepository) {
        m_webAlarmRepository = webAlarmRepository;
    }

    /**
     * <p>afterPropertiesSet</p>
     *
     * @throws java.lang.Exception if any.
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(m_redirectView, "redirectView must be set");
        Assert.notNull(m_webAlarmRepository, "webAlarmRepository must be set");
    }

    /**
     * {@inheritDoc}
     *
     * Acknowledge the events specified in the POST and then redirect the client
     * to an appropriate URL for display.
     */
    @Override
    public ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        // required parameter
        String[] filterStrings = request.getParameterValues("filter");
        String action = request.getParameter("actionCode");

        if (filterStrings == null) {
            filterStrings = new String[0];
        }

        if (action == null) {
            throw new MissingParameterException("actionCode", new String[] { "filter", "actionCode" });
        }

        // handle the filter parameters
        ArrayList<Filter> filterArray = new ArrayList<Filter>();
        for (String filterString : filterStrings) {
            Filter filter = AlarmUtil.getFilter(filterString, getServletContext());
            if (filter != null) {
                filterArray.add(filter);
            }
        }

        Filter[] filters = filterArray.toArray(new Filter[filterArray.size()]);
        
        OnmsCriteria criteria = AlarmUtil.getOnmsCriteria(new AlarmCriteria(filters));

        if (action.equals(AcknowledgeType.ACKNOWLEDGED.getShortName())) {
            m_webAlarmRepository.acknowledgeMatchingAlarms(request.getRemoteUser(), new Date(), criteria);
        } else if (action.equals(AcknowledgeType.UNACKNOWLEDGED.getShortName())) {
            m_webAlarmRepository.unacknowledgeMatchingAlarms(criteria, request.getRemoteUser());
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
