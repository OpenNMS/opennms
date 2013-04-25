/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
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

package org.opennms.web.controller.ncs;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.core.utils.WebSecurityUtils;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.web.alarm.AcknowledgeType;
import org.opennms.web.alarm.AlarmQueryParms;
import org.opennms.web.alarm.AlarmUtil;
import org.opennms.web.alarm.SortStyle;
import org.opennms.web.alarm.WebAlarmRepository;
import org.opennms.web.alarm.filter.AlarmCriteria;
import org.opennms.web.filter.Filter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("alarm/ncs-alarms.htm")
public class NCSAlarmController {
    
    public static final int DEFAULT_MULTIPLE = 0;
    
    private String m_successView = "alarm/ncs-alarms";
    private Integer m_defaultShortLimit = 1000;
    private Integer m_defaultLongLimit = 2000;
    private AcknowledgeType m_defaultAcknowledgeType = AcknowledgeType.UNACKNOWLEDGED;
    private SortStyle m_defaultSortStyle = SortStyle.ID;
    
    @Autowired
    WebAlarmRepository m_webAlarmRepository;
    
    @Autowired
    ServletContext m_servletContext;
     
    @RequestMapping(method = RequestMethod.GET)
    public ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String display = request.getParameter("display");

        // handle the style sort parameter
        String sortStyleString = request.getParameter("sortby");
        SortStyle sortStyle = m_defaultSortStyle;
        if (sortStyleString != null) {
            SortStyle temp = SortStyle.getSortStyle(sortStyleString);
            if (temp != null) {
                sortStyle = temp;
            }
        }

        // handle the acknowledgment type parameter
        String ackTypeString = request.getParameter("acktype");
        AcknowledgeType ackType = m_defaultAcknowledgeType;
        if (ackTypeString != null) {
            AcknowledgeType temp = AcknowledgeType.getAcknowledgeType(ackTypeString);
            if (temp != null) {
                ackType = temp;
            }
        }

        // handle the filter parameters
        String[] filterStrings = request.getParameterValues("filter");
        List<Filter> filterList = new ArrayList<Filter>();
        if (filterStrings != null) {
            for (int i = 0; i < filterStrings.length; i++) {
                Filter filter = AlarmUtil.getFilter(filterStrings[i], getServletContext());
                if (filter != null) {
                    filterList.add(filter);
                }
            }
        }else {
            filterList.add(AlarmUtil.getFilter("parmmatchany=componentType=Service", getServletContext()));
        }

        // handle the optional limit parameter
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

        // handle the optional multiple parameter
        String multipleString = request.getParameter("multiple");
        int multiple = DEFAULT_MULTIPLE;
        if (multipleString != null) {
            try {
                multiple = Math.max(0, WebSecurityUtils.safeParseInt(multipleString));
            } catch (NumberFormatException e) {
            } 
        }

        // put the parameters in a convenient struct
        
        Filter[] filters = filterList.toArray(new Filter[0]);
        
        AlarmQueryParms parms = new AlarmQueryParms();
        parms.ackType = ackType;
        parms.display = display;
        parms.filters = filterList;
        parms.limit = limit;
        parms.multiple =  multiple; 
        parms.sortStyle = sortStyle;
        
        AlarmCriteria queryCriteria = new AlarmCriteria(filters, sortStyle, ackType, limit, limit * multiple);
        AlarmCriteria countCriteria = new AlarmCriteria(ackType, filters);

        OnmsAlarm[] alarms = m_webAlarmRepository.getMatchingAlarms(queryCriteria);
        
        // get the total alarm count
        int alarmCount = m_webAlarmRepository.countMatchingAlarms(countCriteria);
        
        ModelAndView modelAndView = new ModelAndView(getSuccessView());
        modelAndView.addObject("alarms", alarms);
        modelAndView.addObject("alarmCount", alarmCount);
        modelAndView.addObject("parms", parms);
        return modelAndView;
    }

    private String getSuccessView() {
        return m_successView;
    }

    private ServletContext getServletContext() {
        return m_servletContext;
    }

    private Integer getDefaultShortLimit() {
        return m_defaultShortLimit;
    }

    private Integer getDefaultLongLimit() {
        return m_defaultLongLimit;
    }
}
