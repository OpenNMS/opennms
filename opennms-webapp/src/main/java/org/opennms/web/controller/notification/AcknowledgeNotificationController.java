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

public class AcknowledgeNotificationController extends AbstractController implements InitializingBean {

    private static final long serialVersionUID = 1L;

    private WebNotificationRepository m_webNotificationRepository;
    
    private String m_redirectView;
    
    public void setRedirectView(String redirectView) {
        m_redirectView = redirectView;
    }
    
    public void setWebNotificationRepository(WebNotificationRepository webNotificationRepository) {
        m_webNotificationRepository = webNotificationRepository;
    }

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(m_redirectView, "redirectView must be set");
        Assert.notNull(m_webNotificationRepository, "webNotificationRepository must be set");
    }

    /**
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
