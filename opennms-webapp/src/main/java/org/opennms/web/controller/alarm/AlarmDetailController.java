/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
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

import java.util.Collections;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.netmgt.dao.api.AlarmRepository;
import org.opennms.netmgt.model.OnmsAcknowledgment;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.web.alarm.AlarmIdNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.web.servlet.ModelAndView;
import org.opennms.web.servlet.XssRequestWrapper;

import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import org.springframework.web.servlet.view.RedirectView;

/**
 * <p>AlarmDetailController class.</p>
 *
 * @author Ronny Trommer <ronny@opennms.org>
 */
public class AlarmDetailController extends MultiActionController {

    /**
     * OpenNMS alarm repository
     */
    private AlarmRepository m_webAlarmRepository;

    /**
     * Logging
     */
    private Logger logger = LoggerFactory.getLogger(AlarmDetailController.class);

    /**
     * <p>setWebAlarmRepository</p>
     *
     * @param webAlarmRepository a {@link org.opennms.netmgt.dao.api.AlarmRepository}
     * object.
     */
    public void setAlarmRepository(AlarmRepository webAlarmRepository) {
        m_webAlarmRepository = webAlarmRepository;
    }

    /**
     * <p>afterPropertiesSet</p>
     *
     * @throws java.lang.Exception if any.
     */
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(m_webAlarmRepository, "webAlarmRepository must be set");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        logger.debug("AlarmDetailController handleRequestInternal called");
        return super.handleRequestInternal(request, response);
    }

    /**
     * {@inheritDoc}
     *
     * Display alarm detail page
     */
    public ModelAndView detail(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {

        OnmsAlarm m_alarm = null;
        XssRequestWrapper safeRequest = new XssRequestWrapper(httpServletRequest);
        String alarmIdString = "";
        List<OnmsAcknowledgment> acknowledgments = Collections.emptyList();

        // Try to parse alarm ID as string to integer
        try {
            alarmIdString = safeRequest.getParameter("id");
            int alarmId = Integer.parseInt(alarmIdString);
            acknowledgments = m_webAlarmRepository.getAcknowledgments(alarmId);

            // Get alarm by ID
            m_alarm = m_webAlarmRepository.getAlarm(alarmId);
            logger.debug("Alarm retrieved: '{}'", m_alarm.toString());
        } catch (NumberFormatException e) {
            logger.error("Could not parse alarm ID '{}' to integer.", safeRequest.getParameter("id"));
        } catch (Throwable e) {
            logger.error("Could not retrieve alarm from webAlarmRepository for ID='{}'", alarmIdString);
        }

        if (m_alarm == null) {
            throw new AlarmIdNotFoundException("Could not find alarm with ID: " + alarmIdString, alarmIdString);
        }

        // return to view WEB-INF/jsp/alarm/detail.jsp
        ModelAndView mv = new ModelAndView("alarm/detail");
        mv.addObject("alarm", m_alarm);
        mv.addObject("alarmId", alarmIdString);
        mv.addObject("acknowledgments", acknowledgments);
        return mv;
    }

    public ModelAndView removeStickyMemo(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
        int alarmId;
        String alarmIdString = "";

        // Try to parse alarm ID from string to integer
        try {
            alarmIdString = httpServletRequest.getParameter("alarmId");
            alarmId = Integer.parseInt(alarmIdString);
            m_webAlarmRepository.removeStickyMemo(alarmId);

            return new ModelAndView(new RedirectView("detail.htm", true), "id", alarmId);
        } catch (NumberFormatException e) {
            logger.error("Could not parse alarm ID '{}' to integer.", httpServletRequest.getParameter("alarmId"));
            throw new ServletException("Could not parse alarm ID " + httpServletRequest.getParameter("alarmId") + " to integer.");
        }
    }

    public ModelAndView saveStickyMemo(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
        int alarmId;
        String alarmIdString = "";

        // Try to parse alarm ID from string to integer
        try {
            alarmIdString = httpServletRequest.getParameter("alarmId");
            alarmId = Integer.parseInt(alarmIdString);
            String stickyMemoBody = httpServletRequest.getParameter("stickyMemoBody");
            m_webAlarmRepository.updateStickyMemo(alarmId, stickyMemoBody, httpServletRequest.getRemoteUser());
            return new ModelAndView(new RedirectView("detail.htm", true), "id", alarmId);
        } catch (NumberFormatException e) {
            logger.error("Could not parse alarm ID '{}' to integer.", httpServletRequest.getParameter("alarmId"));
            throw new ServletException("Could not parse alarm ID " + httpServletRequest.getParameter("alarmId") + " to integer.");
        }
    }

    public ModelAndView removeJournalMemo(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
        int alarmId;
        String alarmIdString = "";

        // Try to parse alarm ID from string to integer
        try {
            alarmIdString = httpServletRequest.getParameter("alarmId");
            alarmId = Integer.parseInt(alarmIdString);
            m_webAlarmRepository.removeReductionKeyMemo(alarmId);

            return new ModelAndView(new RedirectView("detail.htm", true), "id", alarmId);
        } catch (NumberFormatException e) {
            logger.error("Could not parse alarm ID '{}' to integer.", httpServletRequest.getParameter("alarmId"));
            throw new ServletException("Could not parse alarm ID " + httpServletRequest.getParameter("alarmId") + " to integer.");
        }
    }

    public ModelAndView saveJournalMemo(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
        int alarmId;
        String alarmIdString = "";

        // Try to parse alarm ID from string to integer
        try {
            alarmIdString = httpServletRequest.getParameter("alarmId");
            alarmId = Integer.parseInt(alarmIdString);
            String journalMemoBody = httpServletRequest.getParameter("journalMemoBody");
            m_webAlarmRepository.updateReductionKeyMemo(alarmId, journalMemoBody, httpServletRequest.getRemoteUser());

            return new ModelAndView(new RedirectView("detail.htm", true), "id", alarmId);
        } catch (NumberFormatException e) {
            logger.error("Could not parse alarm ID '{}' to integer.", httpServletRequest.getParameter("alarmId"));
            throw new ServletException("Could not parse alarm ID " + httpServletRequest.getParameter("alarmId") + " to integer.");
        }
    }
}
