//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2005 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2009 Apr 2: Converted to be a Spring Controller rather than a servlet
// 2007 Jul 24: Add serialVersionUID. - dj@opennms.org
// 2005 Apr 18: This file created from AcknowledgeEventServlet.java
//
// Original Code Base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//

package org.opennms.web.controller.alarm;

import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.web.MissingParameterException;
import org.opennms.web.WebSecurityUtils;
import org.opennms.web.alarm.WebAlarmRepository;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.view.RedirectView;

/**
 * This servlet receives an HTTP POST with a list of alarms to acknowledge or
 * unacknowledge, and then it redirects the client to a URL for display. The
 * target URL is configurable in the servlet config (web.xml file).
 * 
 * @author <A HREF="mailto:larry@opennms.org">Lawrence Karnowski </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 */
public class AcknowledgeAlarmController extends AbstractController implements InitializingBean {
    private static final long serialVersionUID = 1L;

    public final static String ACKNOWLEDGE_ACTION = "1";

    public final static String UNACKNOWLEDGE_ACTION = "2";
    
    private WebAlarmRepository m_webAlarmRepository;
    
    private String m_redirectView;
    
    public void setRedirectView(String redirectView) {
        m_redirectView = redirectView;
    }
    
    public void setWebAlarmRepository(WebAlarmRepository webAlarmRepository) {
        m_webAlarmRepository = webAlarmRepository;
    }

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(m_redirectView, "redirectView must be set");
        Assert.notNull(m_webAlarmRepository, "webAlarmRepository must be set");
    }

    /**
     * Acknowledge the alarms specified in the POST and then redirect the client
     * to an appropriate URL for display.
     */
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {

        // required parameter
        String[] alarmIdStrings = request.getParameterValues("alarm");
        String action = request.getParameter("actionCode");

        if (alarmIdStrings == null) {
            throw new MissingParameterException("alarm", new String[] { "alarm", "actionCode" });
        }

        if (action == null) {
            throw new MissingParameterException("actionCode", new String[] { "alarm", "actionCode" });
        }
        
        
        // convert the alarm id strings to ints
        int[] alarmIds = new int[alarmIdStrings.length];
        for (int i = 0; i < alarmIds.length; i++) {
            alarmIds[i] = WebSecurityUtils.safeParseInt(alarmIdStrings[i]);
        }

        if (action.equals(ACKNOWLEDGE_ACTION)) {
            m_webAlarmRepository.acknowledgeAlarms(alarmIds, request.getRemoteUser(), new Date());
        } else if (action.equals(UNACKNOWLEDGE_ACTION)) {
            m_webAlarmRepository.unacknowledgeAlarms(alarmIds);
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
