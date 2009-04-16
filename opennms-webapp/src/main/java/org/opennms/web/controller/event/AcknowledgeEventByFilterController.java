package org.opennms.web.controller.event;

import java.util.ArrayList;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.web.MissingParameterException;
import org.opennms.web.event.AcknowledgeType;
import org.opennms.web.event.EventUtil;
import org.opennms.web.event.WebEventRepository;
import org.opennms.web.event.filter.EventCriteria;
import org.opennms.web.filter.Filter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.view.RedirectView;

public class AcknowledgeEventByFilterController extends AbstractController implements InitializingBean {
    private static final long serialVersionUID = 1L;
    
    private WebEventRepository m_webEventRepository;
    
    private String m_redirectView;
    
    public void setRedirectView(String redirectView) {
        m_redirectView = redirectView;
    }
    
    public void setWebEventRepository(WebEventRepository webEventRepository) {
        m_webEventRepository = webEventRepository;
    }

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(m_redirectView, "redirectView must be set");
        Assert.notNull(m_webEventRepository, "webEventRepository must be set");
    }

    /**
     * Acknowledge the events specified in the POST and then redirect the client
     * to an appropriate URL for display.
     */
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
            Filter filter = EventUtil.getFilter(filterString);
            if (filter != null) {
                filterArray.add(filter);
            }
        }

        Filter[] filters = filterArray.toArray(new Filter[filterArray.size()]);
        
        EventCriteria criteria = new EventCriteria(filters);

        if (action.equals(AcknowledgeType.ACKNOWLEDGED.getShortName()) || action.equals(AcknowledgeType.BOTH.getShortName())) {
            m_webEventRepository.acknowledgeMatchingEvents(request.getRemoteUser(), new Date(), criteria);
        } else if (action.equals(AcknowledgeType.UNACKNOWLEDGED.getShortName()) || action.equals(AcknowledgeType.BOTH.getShortName())) {
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