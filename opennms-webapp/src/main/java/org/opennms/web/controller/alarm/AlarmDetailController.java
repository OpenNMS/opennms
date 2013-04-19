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

package org.opennms.web.controller.alarm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletException;

import org.opennms.core.utils.WebSecurityUtils;
import org.opennms.netmgt.model.OnmsAcknowledgment;
import org.opennms.web.alarm.Alarm;
import org.opennms.web.alarm.WebAlarmRepository;
import org.opennms.web.event.AcknowledgeType;
import org.opennms.web.event.Event;
import org.opennms.web.event.EventQueryParms;
import org.opennms.web.event.EventUtil;
import org.opennms.web.event.SortStyle;
import org.opennms.web.event.WebEventRepository;
import org.opennms.web.event.filter.EventCriteria;
import org.opennms.web.filter.Filter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import org.springframework.web.servlet.view.RedirectView;

/**
 * <p>
 * AlarmDetailController class.
 * </p>
 * 
 * @author Ronny Trommer <ronny@opennms.org>
 */
public class AlarmDetailController extends MultiActionController {

	/** Constant <code>DEFAULT_MULTIPLE=0</code> */
	public static final int DEFAULT_MULTIPLE = 0;

	/** To hold successView page name */
	private String m_successView;

	/** To hold defaultShortLimit */
	private Integer m_defaultShortLimit;

	/**
	 * OpenNMS default sort style
	 */
	private SortStyle m_defaultSortStyle = SortStyle.ID;

	/**
	 * OpenNMS default acknowledgment type
	 */
	private AcknowledgeType m_defaultAcknowledgeType = AcknowledgeType.UNACKNOWLEDGED;

	/**
	 * OpenNMS alarm repository
	 */
	private WebAlarmRepository m_webAlarmRepository;

	/**
	 * OpenNMS event repository
	 */
	private WebEventRepository m_webEventRepository;

	/**
	 * Alarm to display
	 */
	private Alarm m_alarm;

	/**
	 * Logging
	 */
	private Logger logger = LoggerFactory.getLogger("OpenNMS.WEB."+ AlarmDetailController.class.getName());

	/** To hold EventCount status */
	private boolean m_showEventCount = false;

	/**
	 * <p>
	 * Constructor for AlarmDetailController.
	 * </p>
	 */
	public AlarmDetailController() {
		super();
		m_showEventCount = Boolean.getBoolean("opennms.eventlist.showCount");
	}

	/**
	 * <p>
	 * setWebAlarmRepository
	 * </p>
	 * 
	 * @param webAlarmRepository
	 *            a {@link org.opennms.web.alarm.WebAlarmRepository} object.
	 */
	public void setWebAlarmRepository(WebAlarmRepository webAlarmRepository) {
		m_webAlarmRepository = webAlarmRepository;
	}

	/**
	 * <p>
	 * setWebEventRepository
	 * </p>
	 * 
	 * @param WebEventRepository
	 *            a {@link org.opennms.web.alarm.WebEventRepository} object.
	 */
	public void setWebEventRepository(WebEventRepository webEventRepository) {
		m_webEventRepository = webEventRepository;
	}

	/**
	 * <p>
	 * getDefaultShortLimit
	 * </p>
	 * 
	 * @return limit a {@link java.lang.Integer} object.
	 */
	private Integer getDefaultShortLimit() {
		return m_defaultShortLimit;
	}

	/**
	 * <p>
	 * setDefaultShortLimit
	 * </p>
	 * 
	 * @param limit
	 *            a {@link java.lang.Integer} object.
	 */
	public void setDefaultShortLimit(Integer limit) {
		m_defaultShortLimit = limit;
	}

	/**
	 * <p>
	 * getSuccessView
	 * </p>
	 * 
	 * @return successView a {@link java.lang.String} object.
	 */
	private String getSuccessView() {
		return m_successView;
	}

	/**
	 * <p>
	 * setSuccessView
	 * </p>
	 * 
	 * @param successView
	 *            a {@link java.lang.String} object.
	 */
	public void setSuccessView(String successView) {
		m_successView = successView;
	}

	/**
	 * <p>
	 * getDefaultSortStyle
	 * </p>
	 * 
	 * @return a {@link org.opennms.web.alarm.SortStyle} object.
	 */
	public SortStyle getDefaultSortStyle() {
		return m_defaultSortStyle;
	}

	/**
	 * <p>
	 * setDefaultSortStyle
	 * </p>
	 * 
	 * @param defaultSortStyle
	 *            a {@link org.opennms.web.alarm.SortStyle} object.
	 */
	public void setDefaultSortStyle(SortStyle defaultSortStyle) {
		m_defaultSortStyle = defaultSortStyle;
	}

	/**
	 * <p>
	 * getDefaultAcknowledgeType
	 * </p>
	 * 
	 * @return a {@link org.opennms.web.alarm.AcknowledgeType} object.
	 */
	public AcknowledgeType getDefaultAcknowledgeType() {
		return m_defaultAcknowledgeType;
	}

	/**
	 * <p>
	 * setDefaultAcknowledgeType
	 * </p>
	 * 
	 * @param defaultAcknowledgeType
	 *            a {@link org.opennms.web.alarm.AcknowledgeType} object.
	 */
	public void setDefaultAcknowledgeType(AcknowledgeType defaultAcknowledgeType) {
		m_defaultAcknowledgeType = defaultAcknowledgeType;
	}

	/**
	 * <p>
	 * afterPropertiesSet
	 * </p>
	 * 
	 * @throws java.lang.Exception
	 *             if any.
	 */
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(m_defaultShortLimit,"property defaultShortLimit must be set to a value greater than 0");
		Assert.isTrue(m_defaultShortLimit > 0,"property defaultShortLimit must be set to a value greater than 0");
		Assert.notNull(m_successView, "property successView must be set");
		Assert.notNull(m_webAlarmRepository, "webAlarmRepository must be set");
		Assert.notNull(m_webEventRepository, "webEventRepository must be set");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request,HttpServletResponse response) throws Exception {
		logger.debug("AlarmDetailController handleRequestInternal called");
		return super.handleRequestInternal(request, response);
	}

	/**
	 * {@inheritDoc} Display alarm detail page
	 */
	public ModelAndView detail(HttpServletRequest httpServletRequest,HttpServletResponse httpServletResponse) throws Exception {

		// handle the id parameter
		String alarmIdString = httpServletRequest.getParameter("id");
		List<OnmsAcknowledgment> acknowledgments = null;
		if (alarmIdString != null) {
			int alarmId = Integer.parseInt(alarmIdString);
			try {
				acknowledgments = m_webAlarmRepository.getAcknowledgments(alarmId);
				// Get alarm by ID
				m_alarm = m_webAlarmRepository.getAlarm(alarmId);
				logger.debug("Alarm retrieved: '{}'", m_alarm.toString());
			} catch (NumberFormatException e) {
				logger.error("Could not parse alarm ID '{}' to integer.",alarmIdString);
			} catch (Exception e) {
				logger.error("Could not retrieve alarm from webAlarmRepository for ID='{}'",alarmIdString);
			}
		}

		// handle the limit parameter
		String limitString = httpServletRequest.getParameter("limit");
		int limit = getDefaultShortLimit();
		if (limitString != null) {
			try {
				int newlimit = WebSecurityUtils.safeParseInt(limitString);
				if (newlimit > 0) {
					limit = newlimit;
				}
			} catch (Exception e) {
				logger.error("Could not parse limit value '{}' to integer.",limitString);
			}
		}

		// handle the multiple parameter
		String multipleString = httpServletRequest.getParameter("multiple");
		int multiple = DEFAULT_MULTIPLE;
		if (multipleString != null) {
			try {
				multiple = Math.max(0,WebSecurityUtils.safeParseInt(multipleString));
			} catch (Exception e) {
				logger.error("Could not parse multiple value '{}' to integer.",multipleString);
			}
		}

		// handle the sort style parameter
		String sortStyleString = httpServletRequest.getParameter("sortby");
		SortStyle sortStyle = m_defaultSortStyle;
		if (sortStyleString != null) {
			try {
				sortStyle = SortStyle.getSortStyle(sortStyleString);
			} catch (Exception e) {
				logger.error("Could not retrieve sort id for this '{}'.",sortStyleString);
			}
		}

		// handle the acknowledged type parameter
		String ackTypeString = httpServletRequest.getParameter("acktype");
		AcknowledgeType ackType = m_defaultAcknowledgeType;
		if (ackTypeString != null) {
			try {
				ackType = AcknowledgeType.getAcknowledgeType(ackTypeString);
			} catch (Exception e) {
				logger.error("Could not retrieve acknowledge type for this '{}'.",ackTypeString);
			}
		}

		// handle the filter parameters
		List<Filter> filterList = new ArrayList<Filter>();
		String[] filterStrings = httpServletRequest.getParameterValues("filter");
		if (filterStrings != null) {
			for (String filterString : filterStrings) {
				Filter filter = EventUtil.getFilter(filterString,getServletContext());
				if (filter != null) {
					filterList.add(filter);
				}
			}
		}

		// handle the display parameter
		String display = httpServletRequest.getParameter("display");

		Filter[] filters = filterList.toArray(new Filter[0]);
		EventQueryParms parms = new EventQueryParms();
		parms.ackType = ackType;
		parms.display = display;
		parms.filters = filterList;
		parms.limit = limit;
		parms.multiple = multiple;
		parms.sortStyle = sortStyle;

		// Get the events by event criteria
		Event[] events = null;
		HashMap<Integer, List<OnmsAcknowledgment>> alarmsAcknowledgments = new HashMap<Integer, List<OnmsAcknowledgment>>();
		
		if (m_alarm != null) {
			
			EventCriteria queryCriteria = new EventCriteria(filters, sortStyle,ackType, limit, limit * multiple);
			try{
				events = m_webEventRepository.getMatchingEvents(queryCriteria);
				logger.debug("Events retrieved: '{}'", events.toString());
			} catch(Exception e){
				logger.error("Could not retrieve events for this queryCriteria '{}'.",queryCriteria);
			}
			
			// Get the acknowledgments for an alarm
			for (Event event : events) {
				try {
					alarmsAcknowledgments.put(event.getAlarmId(),m_webAlarmRepository.getAcknowledgments(event.getAlarmId()));
					logger.debug("Acknowledgments retrieved: '{}'",alarmsAcknowledgments.toString());
				} catch (Exception e) {
					logger.error("Could not retrieve alarm acknowledgments for this alarm id '{}'.",event.getAlarmId());
				}
			}
		}

		// Return to view WEB-INF/jsp/alarm/detail.jsp
		ModelAndView mv = new ModelAndView(getSuccessView());
		mv.addObject("alarm", m_alarm);
		mv.addObject("events", events);
		mv.addObject("parms", parms);
		mv.addObject("alarmsAcknowledgments", alarmsAcknowledgments);
		mv.addObject("acknowledgments", acknowledgments);

		// Get the total events count
		if (m_showEventCount) {
			EventCriteria countCriteria = new EventCriteria(ackType, filters);
			mv.addObject("eventCount",m_webEventRepository.countMatchingEvents(countCriteria));
		} else {
			mv.addObject("eventCount", Integer.valueOf(-1));
		}

		return mv;
	}

    public ModelAndView clearSticky(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
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

    public ModelAndView saveSticky(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
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

    public ModelAndView clearJournal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
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

    public ModelAndView saveJournal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception {
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
