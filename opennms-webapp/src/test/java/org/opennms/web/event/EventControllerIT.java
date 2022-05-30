/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

package org.opennms.web.event;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.dao.api.AlarmRepository;
import org.opennms.netmgt.dao.hibernate.AlarmRepositoryHibernate;
import org.opennms.netmgt.model.*;
import org.opennms.web.alarm.AlarmQueryParms;
import org.opennms.web.alarm.AlarmUtil;
import org.opennms.web.alarm.filter.AlarmCriteria;
import org.opennms.web.alarm.filter.AlarmTextFilter;
import org.opennms.web.controller.alarm.AlarmFilterController;
import org.opennms.web.controller.event.EventController;
import org.opennms.web.event.filter.EventCriteria;
import org.opennms.web.event.filter.EventTextFilter;
import org.opennms.web.filter.Filter;
import org.opennms.web.services.FilterFavoriteService;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * The Test Class for GraphResultsController.
 *
 * @author Alejandro Galue <agalue@opennms.org>
 */
public class EventControllerIT  {

    /** The controller. */
    private EventController eventController;

    private WebEventRepository m_webEventRepository;

    private FilterFavoriteService favoriteService;

    private AlarmFilterController alarmFilterController;

    private AlarmRepository m_webAlarmRepository;


    @Before
    public void setUp() {
        //Event controller/filter settings
        m_webEventRepository = mock(DaoWebEventRepository.class);
        favoriteService = mock(FilterFavoriteService.class);
        eventController = new EventController();
        eventController.setWebEventRepository(m_webEventRepository);
        eventController.setFavoriteService(favoriteService);
        eventController.setServletContext(new MockServletContext("file:src/main/webapp"));
        eventController.afterPropertiesSet();

        //Alarm controller/filter settings
        m_webAlarmRepository = mock(AlarmRepositoryHibernate.class);
        alarmFilterController = new AlarmFilterController();
        alarmFilterController.setServletContext(new MockServletContext("file:src/main/webapp"));
        alarmFilterController.setWebAlarmRepository(m_webAlarmRepository);
        alarmFilterController.setFavoriteService(favoriteService);
        alarmFilterController.afterPropertiesSet();
    }

    @After
    public void tearDown() throws Exception {
        //verifyNoMoreInteractions(m_webEventRepository);
    }

    /**
     * Test matching.
     *
     * @throws Exception the exception
     */
    @Test
    public void testEventsAdvancedSearch() throws Exception {

        final MockHttpServletResponse response = new MockHttpServletResponse();
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setQueryString("systemId=Any&amp;nodelocation=Any&amp;limit=10&amp;sortby=id&amp;filter=eventtext%3Dtest");

        List<Filter> filterList = new ArrayList();
        filterList.add(new EventTextFilter("eventtext=test"));

        EventQueryParms parms = new EventQueryParms();
        parms.ackType = AcknowledgeType.UNACKNOWLEDGED;
        parms.filters = filterList;
        parms.limit = 10;
        parms.sortStyle = SortStyle.ID;
        parms.display = "Y";
        parms.multiple = 0;

        final EventCriteria eventCriteria = new EventCriteria(parms);
        Event[] events = {mapOnmsEventToEvent(getOnmsEvents().get(0))};

        when(m_webEventRepository.getMatchingEvents(any())).thenReturn(events);

        ModelAndView mv = eventController.list(request,response);
        Event[] ev = (Event[])mv.getModelMap().get("events");
        Assert.assertEquals(1, ev.length);
        Assert.assertEquals("test", ev[0].parms.get("user"));
    }

    /**
     * Test matching.
     *
     * @throws Exception the exception
     */
    @Test
    public void testAlarmsAdvancedSearch() throws Exception {

        final MockHttpServletResponse response = new MockHttpServletResponse();
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setQueryString("afterfirsteventtimedate=29&amp;afterfirsteventtimeampm=pm&amp;beforelasteventtimeampm=pm&amp;beforelasteventtimeyear=2022&amp;beforelasteventtimehour=12&amp;afterlasteventtimeyear=2022&amp;beforefirsteventtimeyear=2022&amp;beforefirsteventtimedate=29&amp;beforefirsteventtimemonth=4&amp;afterlasteventtimedate=29&amp;limit=10&amp;afterfirsteventtimeminute=22&amp;afterfirsteventtimeyear=2022&amp;afterlasteventtimemonth=4&amp;beforelasteventtimeminute=22&amp;situation=any&amp;afterlasteventtimeminute=22&amp;afterfirsteventtimemonth=4&amp;beforelasteventtimemonth=4&amp;beforefirsteventtimehour=12&amp;beforefirsteventtimeampm=pm&amp;afterlasteventtimeampm=pm&amp;afterlasteventtimehour=12&amp;afterfirsteventtimehour=12&amp;beforefirsteventtimeminute=22&amp;sortby=id&amp;beforelasteventtimedate=29&amp;filter=alarmtext%3Dtest");

        List<Filter> filterList = new ArrayList();
        filterList.add(new AlarmTextFilter("alarmtext=test"));

        AlarmQueryParms alarmQueryParms = new AlarmQueryParms();
        alarmQueryParms.ackType = org.opennms.web.alarm.AcknowledgeType.UNACKNOWLEDGED;
        alarmQueryParms.filters = filterList;
        alarmQueryParms.limit = 20;
        alarmQueryParms.sortStyle = org.opennms.web.alarm.SortStyle.ID;
        alarmQueryParms.display = "Y";
        alarmQueryParms.multiple = 0;

        AlarmCriteria alarmCriteria = new AlarmCriteria(alarmQueryParms);

        when(m_webAlarmRepository.getMatchingAlarms(any())).thenReturn(getAlarms());
        when(m_webAlarmRepository.countMatchingAlarms(AlarmUtil.getOnmsCriteria(alarmCriteria))).thenReturn(1);

        ModelAndView mv = alarmFilterController.list(request,response);
        OnmsAlarm[] av = (OnmsAlarm[])mv.getModelMap().get("alarms");
        Assert.assertEquals(1, av.length);
        Assert.assertEquals("Normal", av[0].getSeverity().getLabel());
    }

    private List<OnmsEvent> getOnmsEvents(){
        OnmsEvent event1 = new OnmsEvent();
        event1.setId(1);
        event1.setEventCreateTime(new Date());
        event1.setEventDescr("test");
        event1.setEventHost("localhost");
        event1.setEventLog("Y");
        event1.setEventDisplay("Y");
        event1.setEventLogGroup("event dao test log group");
        event1.setEventLogMsg("test");
        event1.setEventSeverity(OnmsSeverity.CRITICAL.getId());
        event1.setEventSource("EventDaoTest1");
        event1.setEventTime(new Date());
        event1.setEventUei("uei://org/opennms/test/EventDaoTest1");
        event1.setEventParameters(Lists.newArrayList(
                new OnmsEventParameter(event1, "user", "test", "string"),
                new OnmsEventParameter(event1, "ds", "(memAvailReal + memCached) / memTotalReal * 100.0", "string"),
                new OnmsEventParameter(event1, "description", "test", "string"),
                new OnmsEventParameter(event1, "value", "4.7", "string"),
                new OnmsEventParameter(event1, "instance", "node1", "string"),
                new OnmsEventParameter(event1, "instanceLabel", "node1", "string"),
                new OnmsEventParameter(event1, "resourceId", "node[70].nodeSnmp[]", "string"),
                new OnmsEventParameter(event1, "threshold", "5.0", "string"),
                new OnmsEventParameter(event1, "trigger", "2", "string"),
                new OnmsEventParameter(event1, "rearm", "10.0", "string")));


        List<OnmsEvent> onmsEventList = new ArrayList<>();
        onmsEventList.add(event1);
        return onmsEventList;
    }

    private OnmsAlarm[] getAlarms() {
        OnmsAlarm alarm = new OnmsAlarm();
        alarm.setUei("uei.opennms.org/vendor/Juniper/traps/jnxVpnIfUp");
        alarm.setLastEvent(getOnmsEvents().get(0));
        alarm.setSeverityId(3);
        alarm.setCounter(100);
        alarm.setLastEvent(getOnmsEvents().get(0));
        alarm.setLogMsg("test");
        OnmsAlarm[] alarms = {alarm};
        return alarms;
    }


    private Event mapOnmsEventToEvent(OnmsEvent onmsEvent){
        Event event = new Event();
        event.acknowledgeTime = onmsEvent.getEventAckTime();
        event.acknowledgeUser = onmsEvent.getEventAckUser();
        event.alarmId = onmsEvent.getAlarm() != null ? onmsEvent.getAlarm().getId() : 0;
        event.autoAction = onmsEvent.getEventAutoAction();
        event.createTime = onmsEvent.getEventCreateTime();
        event.description = onmsEvent.getEventDescr();
        event.dpName = onmsEvent.getDistPoller() != null ? onmsEvent.getDistPoller().getId() : "";
        event.eventDisplay = true;
        event.forward = onmsEvent.getEventForward();
        event.host = onmsEvent.getEventHost();
        event.ipAddr = onmsEvent.getIpAddr() == null ? null : InetAddressUtils.toIpAddrString(onmsEvent.getIpAddr());
        event.logGroup = onmsEvent.getEventLogGroup();
        event.logMessage = onmsEvent.getEventLogMsg();
        event.mouseOverText = onmsEvent.getEventMouseOverText();
        event.notification = onmsEvent.getEventNotification();
        event.operatorAction = onmsEvent.getEventOperAction();
        event.operatorActionMenuText = onmsEvent.getEventOperActionMenuText();
        event.operatorInstruction = onmsEvent.getEventOperInstruct();
        event.parms = onmsEvent.getEventParameters()==null ? Maps.newLinkedHashMap() : onmsEvent.getEventParameters().stream().collect(Collectors.toMap(OnmsEventParameter::getName, OnmsEventParameter::getValue, (u, v) -> u, LinkedHashMap::new));
        event.serviceID = onmsEvent.getServiceType() != null ? onmsEvent.getServiceType().getId() : 0;
        event.serviceName = onmsEvent.getServiceType() != null ? onmsEvent.getServiceType().getName() : "";
        event.severity = OnmsSeverity.get(onmsEvent.getEventSeverity());
        event.snmp = onmsEvent.getEventSnmp();
        event.snmphost = onmsEvent.getEventSnmpHost();
        event.time = onmsEvent.getEventTime();
        event.troubleTicket = onmsEvent.getEventTTicket();
        event.troubleTicketState = onmsEvent.getEventTTicketState();
        event.uei = onmsEvent.getEventUei();
        if (onmsEvent.getDistPoller() != null) {
            event.location = onmsEvent.getDistPoller().getLocation();
            event.systemId = onmsEvent.getDistPoller().getId();
        }
        return event;
    }
}
