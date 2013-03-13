/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2013 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2013 The OpenNMS Group, Inc.
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

package org.opennms.web.controller.alarm;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.netmgt.model.alarm.AlarmSummary;
import org.opennms.web.alarm.WebAlarmRepository;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

/**
 * A controller that handles querying the alarm table to create the front-page
 * alarm summary box.
 *
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class AlarmBoxController extends AbstractController implements InitializingBean {
    public static final int ROWS = 16;

    private WebAlarmRepository m_webAlarmRepository;
    private String m_successView;

    /** {@inheritDoc} */
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        int rows = Integer.getInteger("opennms.nodesWithProblems.count", ROWS);
        final String parm = request.getParameter("alarmCount");
        if (parm != null) {
            try {
                rows = Integer.valueOf(parm);
            } catch (NumberFormatException e) {
                // ignore, and let it fall back to the defaults
            }
        }
        List<AlarmSummary> summaries = m_webAlarmRepository.getCurrentNodeAlarmSummaries();
        int moreCount = summaries.size() - rows;

        ModelAndView modelAndView = new ModelAndView(getSuccessView());
        
        if (rows == 0 || summaries.size() < rows) {
            modelAndView.addObject("summaries", summaries);
        } else {
            modelAndView.addObject("summaries", summaries.subList(0, rows));
        }
        modelAndView.addObject("moreCount", moreCount);
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
     * @param webAlarmRepository a {@link org.opennms.web.alarm.WebAlarmRepository} object.
     */
    public void setWebAlarmRepository(WebAlarmRepository webAlarmRepository) {
        m_webAlarmRepository = webAlarmRepository;
    }

    /**
     * <p>afterPropertiesSet</p>
     */
    public void afterPropertiesSet() {
        Assert.notNull(m_successView, "property successView must be set");
        Assert.notNull(m_webAlarmRepository, "webAlarmRepository must be set");
    }

}
