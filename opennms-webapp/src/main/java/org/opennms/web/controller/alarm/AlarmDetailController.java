/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.web.controller.alarm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.WebSecurityUtils;
import org.opennms.netmgt.dao.api.AlarmRepository;
import org.opennms.netmgt.model.OnmsAcknowledgment;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.web.alarm.AlarmIdNotFoundException;
import org.opennms.web.event.Event;
import org.opennms.web.event.SortStyle;
import org.opennms.web.event.WebEventRepository;
import org.opennms.web.event.filter.AlarmIDFilter;
import org.opennms.web.event.filter.EventCriteria;
import org.opennms.web.filter.Filter;
import org.opennms.web.servlet.XssRequestWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.util.Assert;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import org.springframework.web.servlet.view.RedirectView;

/**
 * <p>AlarmDetailController class.</p>
 *
 * @author Ronny Trommer <ronny@opennms.org>
 */
public class AlarmDetailController extends MultiActionController {
    private static final int DEFAULT_SHORT_LIMIT = 20;
    private static final int DEFAULT_MULTIPLE = 0;

    private static final Logger logger = LoggerFactory.getLogger(AlarmDetailController.class);

    /**
     * OpenNMS alarm repository
     */
    private AlarmRepository m_webAlarmRepository;

    /**
     * OpenNMS event repository
     */
    private WebEventRepository m_webEventRepository;

    private Integer m_defaultShortLimit = DEFAULT_SHORT_LIMIT;

    public void setAlarmRepository(AlarmRepository repository) {
        m_webAlarmRepository = repository;
    }
    public void setWebEventRepository(final WebEventRepository repository) {
        m_webEventRepository = repository;
    }
    public void setDefaultShortLimit(final Integer limit) { m_defaultShortLimit = limit; }

    /**
     * <p>afterPropertiesSet</p>
     *
     * @throws java.lang.Exception if any.
     */
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(m_webAlarmRepository, "webAlarmRepository must be set");
        Assert.notNull(m_webEventRepository, "webEventRepository must be set");
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

        OnmsAlarm alarm = null;
        XssRequestWrapper safeRequest = new XssRequestWrapper(httpServletRequest);
        String alarmIdString = "";
        List<OnmsAcknowledgment> acknowledgments = Collections.emptyList();

        // Try to parse alarm ID as string to integer
        try {
            alarmIdString = safeRequest.getParameter("id");
            int alarmId = Integer.parseInt(alarmIdString);
            acknowledgments = m_webAlarmRepository.getAcknowledgments(alarmId);

            // Get alarm by ID
            alarm = m_webAlarmRepository.getAlarm(alarmId);
            logger.debug("Alarm retrieved: '{}'", alarm.toString());
        } catch (NumberFormatException e) {
            logger.error("Could not parse alarm ID '{}' to integer.", safeRequest.getParameter("id"));
        } catch (Throwable e) {
            logger.error("Could not retrieve alarm from webAlarmRepository for ID='{}'", alarmIdString);
        }

        if (alarm == null) {
            throw new AlarmIdNotFoundException("Could not find alarm with ID: " + alarmIdString, alarmIdString);
        }

        // return to view WEB-INF/jsp/alarm/detail.jsp
        ModelAndView mv = new ModelAndView("alarm/detail");
        mv.addObject("alarm", alarm);
        mv.addObject("alarmId", alarmIdString);
        mv.addObject("acknowledgments", acknowledgments);
        mv.addObject("related", this.getRelatedEvents(alarm, httpServletRequest));
        return mv;
    }

    private void checkRole(HttpServletRequest httpServletRequest) throws ServletException {
        final Authentication authentication = (Authentication) httpServletRequest.getUserPrincipal();
        final boolean isAdmin = authentication.getAuthorities().stream().anyMatch(g -> Objects.equals(org.opennms.web.api.Authentication.ROLE_ADMIN, g.getAuthority()));
        final boolean isReadOnly = authentication.getAuthorities().stream().anyMatch(g -> Objects.equals(org.opennms.web.api.Authentication.ROLE_READONLY, g.getAuthority()));
        if (!isAdmin && isReadOnly) {
            throw new ServletException("User '" + authentication.getName() + "', is a read-only user!");
        }
    }

    public ModelAndView removeStickyMemo(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
        int alarmId;
        String alarmIdString = "";

        checkRole(httpServletRequest);

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

        checkRole(httpServletRequest);

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

        checkRole(httpServletRequest);

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

        checkRole(httpServletRequest);

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

    private List<RelatedEvent> getRelatedEvents(final OnmsAlarm alarm, final HttpServletRequest request) {
        Assert.notNull(alarm);
 
        final List<RelatedEvent> relatedEvents = new ArrayList<>();

        final List<Filter> filters = new ArrayList<>(List.of(new AlarmIDFilter(alarm.getId())));

        SortStyle sortStyle = SortStyle.ID;
        final String sortStyleString = request.getParameter("sortby");
        if (sortStyleString != null) {
            try {
                sortStyle = SortStyle.getSortStyle(sortStyleString);
            } catch (final IllegalArgumentException e) {
                logger.error("Unable to determine SortStyle for '{}'", sortStyleString, e);
            }
        }

        int limit = m_defaultShortLimit;
        final String limitString = request.getParameter("limit");
        if (limitString != null) {
            try {
                int newLimit = WebSecurityUtils.safeParseInt(limitString);
                if (newLimit > 0) {
                    limit = newLimit;
                }
            } catch (final NumberFormatException e) {
                logger.error("Unable to parse query limit '{}'", limitString, e);
            }
        }

        int multiple = DEFAULT_MULTIPLE;
        final String multipleString = request.getParameter("multiple");
        if (multipleString != null) {
            try {
                multiple = Math.max(0, WebSecurityUtils.safeParseInt(multipleString));
            } catch (final NumberFormatException e) {
                logger.error("Unable to parse query multiple '{}'", multipleString, e);
            }
        }

        final EventCriteria queryCriteria = new EventCriteria(filters, sortStyle, null, limit, limit * multiple);
        try {
            for (final Event event : m_webEventRepository.getMatchingEvents(queryCriteria)) {
                relatedEvents.add(new RelatedEvent(event.getId(), event.getAlarmId(), event.getCreateTime(), event.getSeverity(), event.getUei(), event.getLogMessage()));
            }
        } catch (final Exception e) {
            logger.error("Could not retrieve events for queryCriteria '{}'.", queryCriteria);
        }
        return relatedEvents;
    }
}
