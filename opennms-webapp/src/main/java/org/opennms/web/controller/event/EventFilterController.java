//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2009 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2009 Apr 15: convert event filter servlet to a controller
//
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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

package org.opennms.web.controller.event;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.web.WebSecurityUtils;
import org.opennms.web.event.AcknowledgeType;
import org.opennms.web.event.Event;
import org.opennms.web.event.EventQueryParms;
import org.opennms.web.event.EventUtil;
import org.opennms.web.event.SortStyle;
import org.opennms.web.event.WebEventRepository;
import org.opennms.web.event.filter.EventCriteria;
import org.opennms.web.event.filter.EventIdFilter;
import org.opennms.web.filter.Filter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

/**
 * A controller that handles querying the event table by using filters to create an
 * event list and and then forwards that event list to a JSP for display.
 * 
 * @author <A HREF="mailto:larry@opennms.org">Lawrence Karnowski</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 */
public class EventFilterController extends AbstractController implements InitializingBean {
    public static final int DEFAULT_MULTIPLE = 0;

    private String m_successView;

    private Integer m_defaultShortLimit;

    private Integer m_defaultLongLimit;
    
    private AcknowledgeType m_defaultEventType = AcknowledgeType.UNACKNOWLEDGED;

    private SortStyle m_defaultSortStyle = SortStyle.ID;

    private WebEventRepository m_webEventRepository;
    
    private boolean m_showEventCount = false;

    public EventFilterController() {
        super();
        m_showEventCount = Boolean.getBoolean("opennms.eventlist.showCount");
    }

    /**
     * Parses the query string to determine what types of event filters to use
     * (for example, what to filter on or sort by), then does the database query
     * and then forwards the results to a JSP for display.
     * 
     * <p>
     * Sets the <em>events</em> and <em>parms</em> request attributes for
     * the forwardee JSP (or whatever gets called).
     * </p>
     */
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        List<Filter> filterList = new ArrayList<Filter>();
        AcknowledgeType ackType = m_defaultEventType;

        String display = request.getParameter("display");

        String limitString = request.getParameter("limit");
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

        String multipleString = request.getParameter("multiple");
        int multiple = DEFAULT_MULTIPLE;
        if (multipleString != null) {
            try {
                multiple = Math.max(0, WebSecurityUtils.safeParseInt(multipleString));
            } catch (NumberFormatException e) {
            }
        }

        String sortStyleString = request.getParameter("sortby");
        SortStyle sortStyle = m_defaultSortStyle;
        if (sortStyleString != null) {
            SortStyle temp = SortStyle.getSortStyle(sortStyleString);
            if (temp != null) {
                sortStyle = temp;
            }
        }

        String idString = request.getParameter("id");
        if (idString != null) {
            // asking for a specific ID; only filter should be event ID
            filterList.add(new EventIdFilter(WebSecurityUtils.safeParseInt(idString)));
            ackType = null;
        } else {
            // otherwise, apply filters/acktype/etc.

            String ackTypeString = request.getParameter("acktype");
            if (ackTypeString != null) {
                AcknowledgeType temp = AcknowledgeType.getAcknowledgeType(ackTypeString);
                if (temp != null) {
                    ackType = temp;
                }
            }

            String[] filterStrings = request.getParameterValues("filter");
            if (filterStrings != null) {
                for (String filterString : filterStrings) {
                    Filter filter = EventUtil.getFilter(filterString);
                    if (filter != null) {
                        filterList.add(filter);
                    }
                }
            }

        }

        Filter[] filters = filterList.toArray(new Filter[0]);
        
        EventQueryParms parms = new EventQueryParms();
        parms.ackType = ackType;
        parms.display = display;
        parms.filters = filterList;
        parms.limit = limit;
        parms.multiple =  multiple;
        parms.sortStyle = sortStyle;
        
        EventCriteria queryCriteria = new EventCriteria(filters, sortStyle, ackType, limit, limit * multiple);

        Event[] events = m_webEventRepository.getMatchingEvents(queryCriteria);
        
        ModelAndView modelAndView = new ModelAndView(getSuccessView());
        modelAndView.addObject("events", events);
        modelAndView.addObject("parms", parms);
        
        if (m_showEventCount) {
            EventCriteria countCriteria = new EventCriteria(ackType, filters);
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

    private String getSuccessView() {
        return m_successView;
    }

    public void setSuccessView(String successView) {
        m_successView = successView;
    }
    
    public void setWebEventRepository(WebEventRepository webEventRepository) {
        m_webEventRepository = webEventRepository;
    }

    public void afterPropertiesSet() {
        Assert.notNull(m_defaultShortLimit, "property defaultShortLimit must be set to a value greater than 0");
        Assert.isTrue(m_defaultShortLimit > 0, "property defaultShortLimit must be set to a value greater than 0");
        Assert.notNull(m_defaultLongLimit, "property defaultLongLimit must be set to a value greater than 0");
        Assert.isTrue(m_defaultLongLimit > 0, "property defaultLongLimit must be set to a value greater than 0");
        Assert.notNull(m_successView, "property successView must be set");
        Assert.notNull(m_webEventRepository, "webEventRepository must be set");
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
