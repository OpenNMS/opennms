/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
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

package org.opennms.web.controller.alarm;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.opennms.core.utils.WebSecurityUtils;
import org.opennms.netmgt.dao.api.AlarmRepository;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsFilterFavorite;
import org.opennms.web.alarm.AcknowledgeType;
import org.opennms.web.alarm.AlarmQueryParms;
import org.opennms.web.alarm.AlarmUtil;
import org.opennms.web.alarm.SortStyle;
import org.opennms.web.alarm.filter.AlarmCriteria;
import org.opennms.web.alert.AlertType;
import org.opennms.web.filter.Filter;
import org.opennms.web.filter.FilterUtil;
import org.opennms.web.filter.NormalizedQueryParameters;
import org.opennms.web.services.FilterFavoriteService;
import org.opennms.web.tags.AlertTag;
import org.opennms.web.tags.filters.AlarmFilterCallback;
import org.opennms.web.tags.filters.FilterCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

/**
 * A controller that handles querying the event table by using filters to create an
 * event list and and then forwards that event list to a JSP for display.
 *
 * @author <A HREF="mailto:larry@opennms.org">Lawrence Karnowski </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 */
public class AlarmFilterController extends MultiActionController implements InitializingBean {

    private static final Logger LOG = LoggerFactory.getLogger(AlarmFilterController.class);

    private static final int DEFAULT_MULTIPLE = 0;
    private static final int DEFAULT_SHORT_LIMIT = 20;
    private static final int DEFAULT_LONG_LIMIT = 10;
    private static final AcknowledgeType DEFAULT_ACKNOWLEDGE_TYPE = AcknowledgeType.UNACKNOWLEDGED;
    private static final SortStyle DEFAULT_SORT_STYLE = SortStyle.ID;

    @Autowired
    private AlarmRepository m_webAlarmRepository;

    @Autowired
    private FilterFavoriteService favoriteService;

    private FilterCallback m_callback;

    @Override
    @Transactional
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        return super.handleRequest(request, response);
    }

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
        modelAndView.setViewName("alarm/list");
        return modelAndView;
    }

    // index view
    public ModelAndView index(HttpServletRequest request, HttpServletResponse response) throws Exception {
        List<OnmsFilterFavorite> userFilterList = favoriteService.getFavorites(request.getRemoteUser(), OnmsFilterFavorite.Page.ALARM);
        ModelAndView modelAndView = new ModelAndView("alarm/index");
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
                    OnmsFilterFavorite.Page.ALARM);
            if (favorite != null) {
                ModelAndView successView = list(request, favorite); // success
                //Commented out per request. Left it in, in case we wanted it back later
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

    private String getDisplay(HttpServletRequest request) {
        // handle the display parameter
        String displayString = request.getParameter("display");
        String display = null;
        if (displayString != null) {
            String temp = WebSecurityUtils.sanitizeString(displayString);
            if (temp != null) {
                display = temp;
            }
        }
        return display;
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
        // handle the style sort parameter
        String sortStyleString = request.getParameter("sortby");
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
        // handle the acknowledgment type parameter
        String ackTypeString = request.getParameter("acktype");
        AcknowledgeType ackType = DEFAULT_ACKNOWLEDGE_TYPE;

        // set default ack type to both if alarm flashing enabled in opennms.properties
        String unAckFlashStr = System.getProperty("opennms.alarmlist.unackflash");
        boolean unAckFlash = (unAckFlashStr == null) ? false : "true".equals(unAckFlashStr.trim());
        if (unAckFlash) ackType = AcknowledgeType.BOTH;

        if (ackTypeString != null) {
            AcknowledgeType temp = AcknowledgeType.getAcknowledgeType(ackTypeString);
            if (temp != null) {
                ackType = temp;
            }
        }
        return ackType;
    }

    private AlarmQueryParms createAlarmQueryParms(HttpServletRequest request, List<Filter> filterList, AcknowledgeType ackType) {
        AlarmQueryParms parms = new AlarmQueryParms();
        parms.ackType = ackType;
        parms.display = getDisplay(request);
        parms.filters = filterList;
        parms.limit = getLimit(request);
        parms.multiple = getMultiple(request);
        parms.sortStyle = getSortStyle(request);
        return parms;
    }

    private ModelAndView createListModelAndView(HttpServletRequest request, List<Filter> filterList, AcknowledgeType ackType) {
        final AlarmQueryParms parms = createAlarmQueryParms(request, filterList, ackType);
        AlarmCriteria queryCriteria = new AlarmCriteria(parms);
        AlarmCriteria countCriteria = new AlarmCriteria(filterList, ackType);

        final OnmsAlarm[] alarms = m_webAlarmRepository.getMatchingAlarms(AlarmUtil.getOnmsCriteria(queryCriteria));
        final long alarmCount = m_webAlarmRepository.countMatchingAlarms(AlarmUtil.getOnmsCriteria(countCriteria));

        final ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("alarms", alarms);
        modelAndView.addObject("alarmCount", alarmCount);
        modelAndView.addObject("parms", new NormalizedQueryParameters(parms));
        modelAndView.addObject("callback", getFilterCallback());
        modelAndView.addObject("favorites", favoriteService.getFavorites(request.getRemoteUser(), OnmsFilterFavorite.Page.ALARM).toArray());
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
            m_callback = new AlarmFilterCallback(getServletContext());
        }
        return m_callback;
    }

    @Override
    public void afterPropertiesSet() {
        Assert.notNull(DEFAULT_SHORT_LIMIT, "property defaultShortLimit must be set to a value greater than 0");
        Assert.isTrue(DEFAULT_SHORT_LIMIT > 0, "property defaultShortLimit must be set to a value greater than 0");
        Assert.notNull(DEFAULT_LONG_LIMIT, "property defaultLongLimit must be set to a value greater than 0");
        Assert.isTrue(DEFAULT_LONG_LIMIT > 0, "property defaultLongLimit must be set to a value greater than 0");
        Assert.notNull(m_webAlarmRepository, "webAlarmRepository must be set");
    }
}
