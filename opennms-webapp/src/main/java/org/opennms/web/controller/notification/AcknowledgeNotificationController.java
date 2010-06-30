/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc. All rights reserved.
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

package org.opennms.web.controller.notification;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.web.MissingParameterException;
import org.opennms.web.WebSecurityUtils;
import org.opennms.web.filter.Filter;
import org.opennms.web.notification.Notification;
import org.opennms.web.notification.WebNotificationRepository;
import org.opennms.web.notification.filter.NotificationCriteria;
import org.opennms.web.notification.filter.NotificationIdListFilter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.view.RedirectView;

/**
 * <p>AcknowledgeNotificationController class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class AcknowledgeNotificationController extends AbstractController implements InitializingBean {

    private static final long serialVersionUID = 1L;

    private WebNotificationRepository m_webNotificationRepository;
    
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
     * <p>setWebNotificationRepository</p>
     *
     * @param webNotificationRepository a {@link org.opennms.web.notification.WebNotificationRepository} object.
     */
    public void setWebNotificationRepository(WebNotificationRepository webNotificationRepository) {
        m_webNotificationRepository = webNotificationRepository;
    }

    /**
     * <p>afterPropertiesSet</p>
     *
     * @throws java.lang.Exception if any.
     */
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(m_redirectView, "redirectView must be set");
        Assert.notNull(m_webNotificationRepository, "webNotificationRepository must be set");
    }

    /**
     * {@inheritDoc}
     *
     * Acknowledge the notifications specified in the POST and then redirect the client
     * to an appropriate URL for display.
     */
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String[] required = { "notices", "curUser" };

        String[] noticeIdStrings = request.getParameterValues("notices");
        String currentUser = request.getParameter("curUser");

        if (noticeIdStrings == null) {
            throw new MissingParameterException("notices", required);
        }
        if (currentUser == null) {
            throw new MissingParameterException("curUser", required);
        }

        List<Integer> noticeIds = new ArrayList<Integer>();
        for (String noticeIdString : noticeIdStrings) {
            noticeIds.add(WebSecurityUtils.safeParseInt(noticeIdString));
        }
        List<Filter> filters = new ArrayList<Filter>();
        filters.add(new NotificationIdListFilter(noticeIds.toArray(new Integer[0])));
        NotificationCriteria criteria = new NotificationCriteria(filters.toArray(new Filter[0]));
        m_webNotificationRepository.acknowledgeMatchingNotification(currentUser, new Date(), criteria);

        Notification[] notices = m_webNotificationRepository.getMatchingNotifications(criteria);
        request.setAttribute("notices", notices);

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
