/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2016 The OpenNMS Group, Inc.
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

import org.opennms.netmgt.dao.api.AlarmRepository;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.web.alarm.AcknowledgeType;
import org.opennms.web.alarm.AlarmUtil;
import org.opennms.web.alarm.SortStyle;
import org.opennms.web.alarm.filter.AlarmCriteria;
import org.opennms.web.alarm.filter.PartialUEIFilter;
import org.opennms.web.filter.Filter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * A controller that handles querying threshold alarms from the alarm table to create the front-page
 * threshold alarm box.
 *
 * @author <a href="mailto:ronny@opennms.org">Ronny Trommer</a>
 */
public class ThresholdBoxController extends AbstractController implements InitializingBean {
    public static final int DEFAULT_ROWS = 16;

    private static final String UEI_DEFAULT_FILTER = "uei.opennms.org/threshold/";
    private static final String UEI_FILTER_PROPERTY = "opennms.thresholdAlarm.uei.filter";
    private static final String THRESHOLD_ALARM_COUNT_PROPERTY = "opennms.thresholdAlarm.count";

    private AlarmRepository m_webAlarmRepository;
    private String m_successView;
    private AlarmCriteria m_alarmCriteria;

    /**
     * {@inheritDoc}
     */
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {

        int rows = Integer.getInteger(THRESHOLD_ALARM_COUNT_PROPERTY, DEFAULT_ROWS);

        String ueiFilter = System.getProperty(UEI_FILTER_PROPERTY, UEI_DEFAULT_FILTER);

        Filter[] filters = new Filter[1];
        filters[0] = new PartialUEIFilter(ueiFilter);

        // Query one alarm more than listed, to show link for all other threshold alarms
        m_alarmCriteria = new AlarmCriteria(filters, SortStyle.ID, AcknowledgeType.UNACKNOWLEDGED, rows + 1, 0);

        ArrayList<OnmsAlarm> thresholdAlarmList = new ArrayList<OnmsAlarm>(Arrays.asList(m_webAlarmRepository.getMatchingAlarms(AlarmUtil.getOnmsCriteria(m_alarmCriteria))));

        int moreCount = thresholdAlarmList.size() - rows;

        ModelAndView modelAndView = new ModelAndView(getSuccessView());

        if (rows == 0 || thresholdAlarmList.size() < rows) {
            modelAndView.addObject("thresholdAlarmList", thresholdAlarmList);
        } else {
            modelAndView.addObject("thresholdAlarmList", thresholdAlarmList.subList(0, rows));
        }
        modelAndView.addObject("moreCount", moreCount);
        modelAndView.addObject("ueiFilter",ueiFilter);
        return modelAndView;
    }

    private String getSuccessView() {
        return m_successView;
    }

    /**
     * <p>setSuccessView</p>
     *
     * @param successView a {@link java.lang.String} object.
     */
    public void setSuccessView(String successView) {
        m_successView = successView;
    }

    /**
     * <p>setWebAlarmRepository</p>
     *
     * @param webAlarmRepository a {@link org.opennms.netmgt.dao.api.AlarmRepository} object.
     */
    public void setAlarmRepository(AlarmRepository webAlarmRepository) {
        m_webAlarmRepository = webAlarmRepository;
    }

    /**
     * <p>afterPropertiesSet</p>
     */
    @Override
    public void afterPropertiesSet() {
        Assert.notNull(m_successView, "property m_successView must be set");
        Assert.notNull(m_webAlarmRepository, "m_webAlarmRepository must be set");
    }
}
