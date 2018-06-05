/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
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

package org.opennms.web.controller.event;

import org.apache.commons.lang.StringUtils;
import org.opennms.core.utils.WebSecurityUtils;
import org.opennms.netmgt.model.OnmsFilterFavorite;
import org.opennms.web.alert.AlertType;
import org.opennms.web.event.*;
import org.opennms.web.event.filter.EventCriteria;
import org.opennms.web.event.filter.EventIdFilter;
import org.opennms.web.event.filter.EventIdListFilter;
import org.opennms.web.filter.Filter;
import org.opennms.web.filter.FilterUtil;
import org.opennms.web.filter.NormalizedQueryParameters;
import org.opennms.web.services.FilterFavoriteService;
import org.opennms.web.servlet.MissingParameterException;
import org.opennms.web.tags.AlertTag;
import org.opennms.web.tags.filters.EventFilterCallback;
import org.opennms.web.tags.filters.FilterCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * A controller that handles all event actions (e.g. querying the event table by using filters to create an
 * event list and and then forwards that event list to a JSP for display).
 *
 * @author <A HREF="mailto:larry@opennms.org">Lawrence Karnowski</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 * @author <A HREF="mailto:larry@opennms.org">Lawrence Karnowski</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 * @version $Id: $
 * @since 1.8.1
 */
public class EventController extends MultiActionController implements InitializingBean {

    private static final Logger LOG = LoggerFactory.getLogger(EventController.class);

	private static final int DEFAULT_MULTIPLE = 0;

    private static final int DEFAULT_SHORT_LIMIT = 20;

    private static final int DEFAULT_LONG_LIMIT = 10;

    private static final AcknowledgeType DEFAULT_ACKNOWLEDGE_TYPE = AcknowledgeType.UNACKNOWLEDGED;

    private static final SortStyle DEFAULT_SORT_STYLE = SortStyle.ID;

    private FilterCallback m_callback;

	@Autowired
    private FilterFavoriteService favoriteService;

	@Autowired
	private WebEventRepository m_webEventRepository;

    private boolean m_showEventCount = false;

    public EventController() {
        super();
        m_showEventCount = Boolean.getBoolean("opennms.eventlist.showCount");
    }

    @Override
    @Transactional
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        return super.handleRequest(request, response);
    }

    /**
     * {@inheritDoc}
     *
     * Parses the query string to determine what types of event filters to use
     * (for example, what to filter on or sort by), then does the database query
     * and then forwards the results to a JSP for display.
     *
     * <p>
     * Sets the <em>events</em> and <em>parms</em> request attributes for
     * the forwardee JSP (or whatever gets called).
     * </p>
     */
    public ModelAndView list(HttpServletRequest request, HttpServletResponse response) throws Exception {
        OnmsFilterFavorite favorite = getFavorite(
                request.getParameter("favoriteId"),
                request.getRemoteUser(),
                request.getParameterValues("filter"));
        return list(request, favorite);
    }

    private ModelAndView list(HttpServletRequest request, OnmsFilterFavorite favorite) {
        AcknowledgeType ackType = getAcknowledgeType(request);
        ModelAndView modelAndView = createListModelAndView(request, getFilterCallback().parse(request.getParameterValues("filter")), ackType);
        modelAndView.addObject("favorite", favorite);
        modelAndView.setViewName("event/list");
        return modelAndView;
    }
    
    public ModelAndView detail(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	String idString = request.getParameter("id");
    	// asking for a specific ID; only filter should be event ID
    	ModelAndView modelAndView = createModelAndView(request, new EventIdFilter(WebSecurityUtils.safeParseInt(idString)));
    	modelAndView.setViewName("event/detail");
    	return modelAndView;
    }
    
    // index view
    public ModelAndView index(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	List<OnmsFilterFavorite> userFilterList = favoriteService.getFavorites(request.getRemoteUser(), OnmsFilterFavorite.Page.EVENT);
        ModelAndView modelAndView = new ModelAndView("event/index");
        modelAndView.addObject("favorites", userFilterList.toArray());
        modelAndView.addObject("callback", getFilterCallback());
        return modelAndView;
    }

    public ModelAndView createFavorite(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String error = null;
        try {
            OnmsFilterFavorite favorite = favoriteService.createFavorite(
                    request.getRemoteUser(),
                    request.getParameter("favoriteName"),
                    FilterUtil.toFilterURL(request.getParameterValues("filter")),
                    OnmsFilterFavorite.Page.EVENT);
            if (favorite != null) {
                ModelAndView successView = list(request, favorite); // success
                //Comment out as per request
                //AlertTag.addAlertToRequest(successView, "Favorite was created successfully", AlertType.SUCCESS);
                return successView;
            }
            error = "An error occured while creating the favorite";
        } catch (FilterFavoriteService.FilterFavoriteException ex) {
            error = ex.getMessage();
        }
        ModelAndView errorView = list(request, (OnmsFilterFavorite) null);
        AlertTag.addAlertToRequest(errorView, error, AlertType.ERROR);
        return errorView;
    }

    public ModelAndView deleteFavorite(HttpServletRequest request, HttpServletResponse response) throws Exception {
        // delete
        String favoriteId = request.getParameter("favoriteId");
        boolean success = favoriteService.deleteFavorite(favoriteId, request.getRemoteUser());

        ModelAndView resultView = list(request, (OnmsFilterFavorite) null);
        resultView.addObject("favorite", null); // we deleted the favorite
        if (!StringUtils.isEmpty(request.getParameter("redirect"))) {
            resultView.setViewName(request.getParameter("redirect")); // change to redirect View
        }

        if (!success) {
            AlertTag.addAlertToRequest(resultView, "Favorite couldn't be deleted.", AlertType.ERROR);
        } else {
            AlertTag.addAlertToRequest(resultView, "Favorite deleted successfully.", AlertType.SUCCESS);
        }
        return resultView;
    }

    /**
     * Acknowledge the events specified in the POST and then redirect the client
     * to an appropriate URL for display.
     */
    public ModelAndView acknowledge(HttpServletRequest request, HttpServletResponse response) throws Exception {

        // required parameter
        String[] eventIdStrings = request.getParameterValues("event");
        String action = request.getParameter("actionCode");

        if (eventIdStrings == null) {
            throw new MissingParameterException("event", new String[] { "event", "actionCode" });
        }

        if (action == null) {
            throw new MissingParameterException("actionCode", new String[] { "event", "actionCode" });
        }

        List<Filter> filters = new ArrayList<>();
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

        return getRedirectView(request);
    }

    /**
     * Acknowledge the events specified in the POST and then redirect the client
     * to an appropriate URL for display.
     */
    public ModelAndView acknowledgeByFilter(HttpServletRequest request, HttpServletResponse response) throws Exception {
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
        ArrayList<Filter> filterArray = new ArrayList<>();
        for (String filterString : filterStrings) {
            Filter filter = EventUtil.getFilter(filterString, getServletContext());
            if (filter != null) {
                filterArray.add(filter);
            }
        }

        Filter[] filters = filterArray.toArray(new Filter[filterArray.size()]);

        EventCriteria criteria = new EventCriteria(filters);

        if (action.equals(AcknowledgeType.ACKNOWLEDGED.getShortName())) {
            m_webEventRepository.acknowledgeMatchingEvents(request.getRemoteUser(), new Date(), criteria);
        } else if (action.equals(AcknowledgeType.UNACKNOWLEDGED.getShortName())) {
            m_webEventRepository.unacknowledgeMatchingEvents(criteria);
        } else {
            throw new ServletException("Unknown acknowledge action: " + action);
        }
        return getRedirectView(request);
    }

    private ModelAndView getRedirectView(HttpServletRequest request) {
        String redirectParms = request.getParameter("redirectParms");
        String redirect = request.getParameter("redirect");
        String viewName;
        if (redirect != null) {
            viewName = redirect;
        } else {
            viewName = (redirectParms == null || "".equals(redirectParms) || "null".equals(redirectParms) ? "/event/list" : "/event/list" + "?" + redirectParms);
        }
        RedirectView redirectView = new RedirectView(viewName);
        return new ModelAndView(redirectView);
    }

    private String getDisplay(HttpServletRequest request) {
    	return request.getParameter("display");
    }
    
    private int getLimit(HttpServletRequest request) {
    	final String display = getDisplay(request);
        final String limitString = request.getParameter("limit");
    	int limit = "long".equals(display) ? DEFAULT_LONG_LIMIT : DEFAULT_SHORT_LIMIT;
        if (limitString != null) {
            try {
                int newlimit = WebSecurityUtils.safeParseInt(limitString);
                if (newlimit > 0) {
                    limit = newlimit;
                }
            } catch (NumberFormatException e) {
                // do nothing, the default is already set
            }
        }
        return limit;
    }
    
    private int getMultiple(HttpServletRequest request) {
    	final String multipleString = request.getParameter("multiple");
    	int multiple = DEFAULT_MULTIPLE;
        if (multipleString != null) {
            try {
                multiple = Math.max(0, WebSecurityUtils.safeParseInt(multipleString));
            } catch (NumberFormatException e) {
            }
        }
        return multiple;
    }
    
    private SortStyle getSortStyle(HttpServletRequest request) {
    	final String sortStyleString = request.getParameter("sortby");
    	SortStyle sortStyle = DEFAULT_SORT_STYLE;
        if (sortStyleString != null) {
            SortStyle temp = SortStyle.getSortStyle(sortStyleString);
            if (temp != null) {
                sortStyle = temp;
            }
        }
        return sortStyle;
    }
    
    private AcknowledgeType getAcknowledgeType(HttpServletRequest request) {
    	 String ackTypeString = request.getParameter("acktype");
    	 AcknowledgeType ackType = DEFAULT_ACKNOWLEDGE_TYPE;
    	 // otherwise, apply filters/acktype/etc.
         if (ackTypeString != null) {
             AcknowledgeType temp = AcknowledgeType.getAcknowledgeType(ackTypeString);
             if (temp != null) {
                 ackType = temp;
             }
         }
         return ackType;
    }
    
    private EventQueryParms createEventQueryParms(HttpServletRequest request, List<Filter> filterList, AcknowledgeType ackType) {
    	EventQueryParms parms = new EventQueryParms();
        parms.ackType = ackType;
        parms.display = getDisplay(request);
        parms.filters = filterList;
        parms.limit = getLimit(request);
        parms.multiple =  getMultiple(request);
        parms.sortStyle = getSortStyle(request);	
        return parms;
    }

    private ModelAndView createModelAndView(HttpServletRequest request, Filter singleFilter) {
        List<Filter> filterList = new ArrayList<>();
        filterList.add(singleFilter);
        return createListModelAndView(request, filterList, null);
    }

    private ModelAndView createListModelAndView(HttpServletRequest request, List<Filter> filterList, AcknowledgeType ackType) {
    	final EventQueryParms parms = createEventQueryParms(request, filterList, ackType);
        final EventCriteria queryCriteria = new EventCriteria(parms);
        final Event[] events = m_webEventRepository.getMatchingEvents(queryCriteria);
        
        final ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("events", events);
        modelAndView.addObject("parms", new NormalizedQueryParameters(parms));
        modelAndView.addObject("callback", getFilterCallback());
        modelAndView.addObject("favorites", favoriteService.getFavorites(request.getRemoteUser(), OnmsFilterFavorite.Page.EVENT).toArray());

        if (m_showEventCount) {
            EventCriteria countCriteria = new EventCriteria(filterList, ackType);
            modelAndView.addObject("eventCount", m_webEventRepository.countMatchingEvents(countCriteria));
        } else {
            modelAndView.addObject("eventCount", Integer.valueOf(-1));
        }
        return modelAndView;
	}

    private OnmsFilterFavorite getFavorite(String favoriteId, String username, String[] filters) {
        if (favoriteId != null) {
        	return favoriteService.getFavorite(favoriteId, username, getFilterCallback().toFilterString(filters));
        }
        return null;
    }

    private FilterCallback getFilterCallback() {
        if (m_callback == null) {
            m_callback = new EventFilterCallback(getServletContext());
        }
        return m_callback;
    }

    @Override
    public void afterPropertiesSet() {
        Assert.notNull(DEFAULT_SHORT_LIMIT, "property defaultShortLimit must be set to a value greater than 0");
        Assert.isTrue(DEFAULT_SHORT_LIMIT > 0, "property defaultShortLimit must be set to a value greater than 0");
        Assert.notNull(DEFAULT_LONG_LIMIT, "property defaultLongLimit must be set to a value greater than 0");
        Assert.isTrue(DEFAULT_LONG_LIMIT > 0, "property defaultLongLimit must be set to a value greater than 0");
        Assert.notNull(m_webEventRepository, "webEventRepository must be set");
        Assert.notNull(favoriteService, "favoriteService must be set");
    }
}
