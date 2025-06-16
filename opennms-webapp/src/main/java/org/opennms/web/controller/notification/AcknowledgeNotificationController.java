/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.web.controller.notification;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.core.utils.WebSecurityUtils;
import org.opennms.web.controller.RedirectRestricter;
import org.opennms.web.filter.Filter;
import org.opennms.web.notification.Notification;
import org.opennms.web.notification.WebNotificationRepository;
import org.opennms.web.notification.filter.NotificationCriteria;
import org.opennms.web.notification.filter.NotificationIdListFilter;
import org.opennms.web.servlet.MissingParameterException;
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

    private WebNotificationRepository m_webNotificationRepository;
    
    private String m_redirectView;

    private RedirectRestricter redirectRestricter = RedirectRestricter.builder()
            .allowRedirect("notification/detail.jsp")
            .build();
    
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
    @Override
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
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String[] required = { "notices" };

        String[] noticeIdStrings = request.getParameterValues("notices");
        if (noticeIdStrings == null) {
            throw new MissingParameterException("notices", required);
        }

        String currentUser = request.getParameter("curUser");
        if (currentUser == null) {
            currentUser = request.getRemoteUser();
        }

        List<Integer> noticeIds = new ArrayList<>();
        for (String noticeIdString : noticeIdStrings) {
            noticeIds.add(WebSecurityUtils.safeParseInt(noticeIdString));
        }
        List<Filter> filters = new ArrayList<>();
        filters.add(new NotificationIdListFilter(noticeIds.toArray(new Integer[0])));
        NotificationCriteria criteria = new NotificationCriteria(filters.toArray(new Filter[0]));
        m_webNotificationRepository.acknowledgeMatchingNotification(currentUser, new Date(), criteria);

        Notification[] notices = m_webNotificationRepository.getMatchingNotifications(criteria);
        request.setAttribute("notices", notices);

        String redirectParms = request.getParameter("redirectParms");
        String redirect = redirectRestricter.getRedirectOrNull(request.getParameter("redirect"));
        String viewName;
        if (redirect != null) {
            viewName = redirect;
        } else {
            viewName = (redirectParms == null || "".equals(redirectParms) || "null".equals(redirectParms) ? m_redirectView : m_redirectView + "?" + redirectParms);
        }
        RedirectView view = new RedirectView(viewName, true);
        return new ModelAndView(view);

    }
}
