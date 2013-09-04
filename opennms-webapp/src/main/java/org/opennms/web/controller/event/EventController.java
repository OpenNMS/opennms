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
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.core.utils.WebSecurityUtils;
import org.opennms.netmgt.model.OnmsFilter;
import org.opennms.web.event.AcknowledgeType;
import org.opennms.web.event.Event;
import org.opennms.web.event.EventQueryParms;
import org.opennms.web.event.EventUtil;
import org.opennms.web.event.SortStyle;
import org.opennms.web.event.WebEventRepository;
import org.opennms.web.event.filter.EventCriteria;
import org.opennms.web.event.filter.EventIdFilter;
import org.opennms.web.filter.Filter;
import org.opennms.web.services.FilterService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

// TODO MVR modify javadoc
/**
 * A controller that handles querying the event table by using filters to create an
 * event list and and then forwards that event list to a JSP for display.
 *
 * @author <A HREF="mailto:larry@opennms.org">Lawrence Karnowski</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 * @author <A HREF="mailto:larry@opennms.org">Lawrence Karnowski</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 * @version $Id: $
 * @since 1.8.1
 */
public class EventController extends MultiActionController implements InitializingBean {
	
	public static final int DEFAULT_MULTIPLE = 0;
	
	@Autowired
    private FilterService filterService;
	
	@Autowired
	private FilterController filterController;
	
	@Autowired
	private WebEventRepository m_webEventRepository;
	
    private Integer m_defaultShortLimit;

    private Integer m_defaultLongLimit;
    
    private AcknowledgeType m_defaultEventType = AcknowledgeType.UNACKNOWLEDGED;

    private SortStyle m_defaultSortStyle = SortStyle.ID;

    
    private boolean m_showEventCount = false;

    public EventController() {
        super();
        m_showEventCount = Boolean.getBoolean("opennms.eventlist.showCount");
    }

    // TODO MVR modify javadoc
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
    	List<Filter> filterList = EventUtil.getFilterList(request.getParameterValues("filter"), getServletContext());
    	AcknowledgeType ackType = getAcknowledgeType(request);
    	ModelAndView modelAndView = createModelAndView(request, filterList, ackType);
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
    	List<OnmsFilter> userFilterList = filterService.getFilters(request.getRemoteUser(), OnmsFilter.Page.EVENT);
        ModelAndView modelAndView = new ModelAndView("event/index");
        modelAndView.addObject("filters", userFilterList);
        return modelAndView;
    }
    
    public ModelAndView createFilter(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	ModelAndView modelAndView = filterController.create(request,  response);
    	ModelAndView listModelAndView = list(request, response);
    	listModelAndView.addObject("filter.create.error", modelAndView.getModel().get("filter.create.error"));
    	return listModelAndView;
    }
    
    public ModelAndView deleteFilter(HttpServletRequest request, HttpServletResponse response) throws Exception {
    	ModelAndView filterModelAndView = filterController.delete(request, response);
    	ModelAndView listModelAndView = list(request, response);
    	listModelAndView.addObject("filter.delete.error", filterModelAndView.getModel().get("filter.delete.error"));
//    	listModelAndView.getModel().remove("filterId");
//    	listModelAndView.getModel().remove("filterName");
    	return listModelAndView;
    }
    
    private ModelAndView createModelAndView(HttpServletRequest request, Filter singleFilter) {
    	List<Filter> filterList = new ArrayList<Filter>();
    	filterList.add(singleFilter);
        return createModelAndView(request, filterList, null);
    }
    
    private String getDisplay(HttpServletRequest request) {
    	return request.getParameter("display");
    }
    
    private int getLimit(HttpServletRequest request) {
    	final String display = getDisplay(request);
        final String limitString = request.getParameter("limit");
    	int limit = "long".equals(display) ? getDefaultLongLimit() : getDefaultShortLimit();
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
    	SortStyle sortStyle = m_defaultSortStyle;
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
    	 AcknowledgeType ackType = m_defaultEventType;
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
        parms.limit = getLimit(request);;
        parms.multiple =  getMultiple(request);
        parms.sortStyle = getSortStyle(request);	
        return parms;
    }
    
    private ModelAndView createModelAndView(HttpServletRequest request, List<Filter> filterList, AcknowledgeType ackType) {
    	final EventQueryParms parms = createEventQueryParms(request, filterList, ackType);
        final EventCriteria queryCriteria = new EventCriteria(parms);
        final Event[] events = m_webEventRepository.getMatchingEvents(queryCriteria);
        
        final ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("events", events);
        modelAndView.addObject("parms", parms);
        
        if (m_showEventCount) {
            EventCriteria countCriteria = new EventCriteria(filterList, ackType);
            modelAndView.addObject("eventCount", m_webEventRepository.countMatchingEvents(countCriteria));
        } else {
            modelAndView.addObject("eventCount", Integer.valueOf(-1));
        }
        return modelAndView;
	}

    private Integer getDefaultShortLimit() {
        return m_defaultShortLimit;
    }

    public void setDefaultShortLimit(Integer limit) {
        m_defaultShortLimit = limit;
    }

    private Integer getDefaultLongLimit() {
        return m_defaultLongLimit;
    }

    public void setDefaultLongLimit(Integer limit) {
        m_defaultLongLimit = limit;
    }

    @Override
    public void afterPropertiesSet() {
        Assert.notNull(m_defaultShortLimit, "property defaultShortLimit must be set to a value greater than 0");
        Assert.isTrue(m_defaultShortLimit > 0, "property defaultShortLimit must be set to a value greater than 0");
        Assert.notNull(m_defaultLongLimit, "property defaultLongLimit must be set to a value greater than 0");
        Assert.isTrue(m_defaultLongLimit > 0, "property defaultLongLimit must be set to a value greater than 0");
        Assert.notNull(m_webEventRepository, "webEventRepository must be set");
        Assert.notNull(filterService, "filterService must be set");
        Assert.notNull(filterController, "filterController must be set");
    }

    public AcknowledgeType getDefaultAcknowledgeType() {
        return m_defaultEventType;
    }

    public void setDefaultAcknowledgeType(AcknowledgeType defaultAcknowledgeType) {
        m_defaultEventType = defaultAcknowledgeType;
    }

    public SortStyle getDefaultSortStyle() {
        return m_defaultSortStyle;
    }

    public void setDefaultSortStyle(SortStyle defaultSortStyle) {
        m_defaultSortStyle = defaultSortStyle;
    }

}
