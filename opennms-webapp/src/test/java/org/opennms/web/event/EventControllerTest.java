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
package org.opennms.web.event;

import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.dao.api.AlarmRepository;
import org.opennms.netmgt.dao.hibernate.AlarmRepositoryHibernate;
import org.opennms.netmgt.model.*;
import org.opennms.web.alarm.AlarmQueryParms;
import org.opennms.web.alarm.AlarmUtil;
import org.opennms.web.alarm.filter.AlarmCriteria;
import org.opennms.web.alarm.filter.AlarmTextFilter;
import org.opennms.web.controller.alarm.AlarmFilterController;
import org.opennms.web.controller.event.EventController;
import org.opennms.web.event.filter.*;
import org.opennms.web.filter.Filter;
import org.opennms.web.services.FilterFavoriteService;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.servlet.ModelAndView;

import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class EventControllerTest  {

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

    /**
     * Test matching.
     *
     * @throws Exception the exception
     */
    @Test
    public void testEventsAdvancedSearch() throws Exception {

        final MockHttpServletResponse response = new MockHttpServletResponse();
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setQueryString("systemId=Any&amp;nodelocation=Any&amp;limit=10&amp;sortby=id&amp;filter=eventtext%3Dtest&amp;filter=severity%3D4&amp;filter=eventId%3D1");

        List<Filter> filterList = new ArrayList();
        filterList.add(new EventTextFilter("eventtext=test"));
        filterList.add(new EventIdFilter(1));
        filterList.add(new EventIdFilter(2));
        filterList.add(new EventIdFilter(3));
        filterList.add(new SeverityFilter(2));
        filterList.add(new SeverityFilter(1));
        filterList.add(new SeverityFilter(3));




        EventQueryParms parms = new EventQueryParms();
        parms.ackType = AcknowledgeType.UNACKNOWLEDGED;
        parms.filters = filterList;
        parms.limit = 10;
        parms.sortStyle = SortStyle.ID;
        parms.display = "Y";
        parms.multiple = 0;

        final EventCriteria eventCriteria = new EventCriteria(parms);
        when(m_webEventRepository.getMatchingEvents(any())).thenReturn(getEvents().toArray(Event[]::new));

        ModelAndView mv = eventController.list(request,response);
        Event[] ev = (Event[])mv.getModelMap().get("events");
        Assert.assertEquals(3, ev.length);
        Assert.assertEquals("admin", ev[0].parms.get("user"));
        Assert.assertEquals(OnmsSeverity.WARNING,ev[1].getSeverity());
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
        request.setQueryString("afterfirsteventtimedate=29&amp;afterfirsteventtimeampm=pm&amp;beforelasteventtimeampm=pm&amp;beforelasteventtimeyear=2022&amp;beforelasteventtimehour=12&amp;afterlasteventtimeyear=2022&amp;beforefirsteventtimeyear=2022&amp;beforefirsteventtimedate=29&amp;beforefirsteventtimemonth=4&amp;afterlasteventtimedate=29&amp;limit=10&amp;afterfirsteventtimeminute=22&amp;afterfirsteventtimeyear=2022&amp;afterlasteventtimemonth=4&amp;beforelasteventtimeminute=22&amp;situation=any&amp;afterlasteventtimeminute=22&amp;afterfirsteventtimemonth=4&amp;beforelasteventtimemonth=4&amp;beforefirsteventtimehour=12&amp;beforefirsteventtimeampm=pm&amp;afterlasteventtimeampm=pm&amp;afterlasteventtimehour=12&amp;afterfirsteventtimehour=12&amp;beforefirsteventtimeminute=22&amp;sortby=id&amp;beforelasteventtimedate=29&amp;filter=alarmtext%3Dtest&amp;filter=nodenamelike%3DMyNode&amp;filter=severity%3D3");

        List<Filter> filterList = new ArrayList();
        filterList.add(new AlarmTextFilter("alarmtext=test"));
        filterList.add(new SeverityFilter(3));
        filterList.add(new NodeNameLikeFilter("MyNode"));

        AlarmQueryParms alarmQueryParms = new AlarmQueryParms();
        alarmQueryParms.ackType = org.opennms.web.alarm.AcknowledgeType.UNACKNOWLEDGED;
        alarmQueryParms.filters = filterList;
        alarmQueryParms.limit = 20;
        alarmQueryParms.sortStyle = org.opennms.web.alarm.SortStyle.ID;
        alarmQueryParms.display = "Y";
        alarmQueryParms.multiple = 0;

        AlarmCriteria alarmCriteria = new AlarmCriteria(alarmQueryParms);

        when(m_webAlarmRepository.getMatchingAlarms(any())).thenReturn(getAlarms());
        when(m_webAlarmRepository.countMatchingAlarms(AlarmUtil.getOnmsCriteria(alarmCriteria))).thenReturn(2);

        ModelAndView mv = alarmFilterController.list(request,response);
        OnmsAlarm[] av = (OnmsAlarm[])mv.getModelMap().get("alarms");
        Assert.assertEquals(2, av.length);
        Assert.assertEquals("Normal", av[0].getSeverity().getLabel());
    }

    private List<OnmsEvent> getOnmsEvents(){
        List<OnmsEvent> onmsEventList = new ArrayList<>();
        OnmsEvent event4 = new OnmsEvent();
        event4.setId(1);
        event4.setEventCreateTime(new Date());
        event4.setEventDescr("test");
        event4.setEventHost("localhost");
        event4.setEventLog("Y");
        event4.setEventDisplay("Y");
        event4.setEventLogGroup("event dao test log group");
        event4.setEventLogMsg("test");
        event4.setEventSeverity(OnmsSeverity.CRITICAL.getId());
        event4.setEventSource("EventDaoTest1");
        event4.setEventTime(new Date());
        event4.setEventUei("uei://org/opennms/test/EventDaoTest1");
        OnmsNode node1 = new OnmsNode();
        node1.setId(100);
        node1.setLabel("MyNode");
        event4.setNode(node1);
        event4.setEventParameters(Lists.newArrayList(
                new OnmsEventParameter(event4, "user", "test", "string"),
                new OnmsEventParameter(event4, "ds", "(memAvailReal + memCached) / memTotalReal * 100.0", "string"),
                new OnmsEventParameter(event4, "description", "test", "string"),
                new OnmsEventParameter(event4, "value", "4.7", "string"),
                new OnmsEventParameter(event4, "instance", "node1", "string"),
                new OnmsEventParameter(event4, "instanceLabel", "node1", "string"),
                new OnmsEventParameter(event4, "resourceId", "node[70].nodeSnmp[]", "string"),
                new OnmsEventParameter(event4, "threshold", "5.0", "string"),
                new OnmsEventParameter(event4, "trigger", "2", "string"),
                new OnmsEventParameter(event4, "rearm", "10.0", "string")));
        onmsEventList.add(event4);

        OnmsEvent event5 = new OnmsEvent();
        event5.setId(1);
        event5.setEventCreateTime(new Date());
        event5.setEventDescr("test2");
        event5.setEventHost("localhost");
        event5.setEventLog("Y");
        event5.setEventDisplay("Y");
        event5.setEventLogGroup("event dao test log group");
        event5.setEventLogMsg("test");
        event5.setEventSeverity(OnmsSeverity.NORMAL.getId());
        event5.setEventSource("EventDaoTest1");
        event5.setEventTime(new Date());
        event5.setEventUei("uei://org/opennms/test/EventDaoTest2");
        OnmsNode node2 = new OnmsNode();
        node2.setId(101);
        node2.setLabel("MyNode1");

        event4.setNode(node2);
        event5.setEventParameters(Lists.newArrayList(
                new OnmsEventParameter(event5, "user", "test", "string"),
                new OnmsEventParameter(event5, "ds", "(memAvailReal + memCached) / memTotalReal * 100.0", "string"),
                new OnmsEventParameter(event5, "description", "test", "string"),
                new OnmsEventParameter(event5, "value", "4.7", "string"),
                new OnmsEventParameter(event5, "instance", "node1", "string"),
                new OnmsEventParameter(event5, "instanceLabel", "node1", "string"),
                new OnmsEventParameter(event5, "resourceId", "node[70].nodeSnmp[]", "string"),
                new OnmsEventParameter(event5, "threshold", "7.0", "string"),
                new OnmsEventParameter(event5, "trigger", "2", "string"),
                new OnmsEventParameter(event5, "rearm", "11.0", "string")));

        onmsEventList.add(event5);
        return onmsEventList;
    }

    private OnmsAlarm[] getAlarms() {
        OnmsAlarm alarm1 = new OnmsAlarm();
        alarm1.setUei("uei.opennms.org/vendor/Juniper/traps/jnxVpnIfUp");
        alarm1.setLastEvent(getOnmsEvents().get(0));
        alarm1.setSeverityId(3);
        alarm1.setCounter(100);
        alarm1.setLastEvent(getOnmsEvents().get(0));
        alarm1.setLogMsg("testalarm1");

        OnmsAlarm alarm2 = new OnmsAlarm();
        alarm2.setUei("uei.opennms.org/vendor/Juniper/traps/xyz");
        alarm2.setLastEvent(getOnmsEvents().get(1));
        alarm2.setSeverityId(2);
        alarm2.setCounter(101);
        alarm2.setLastEvent(getOnmsEvents().get(1));
        alarm2.setLogMsg("testalarm2");
        OnmsAlarm[] alarms = {alarm1, alarm2};
        return alarms;
    }


    private List<Event> getEvents(){
        Event event1 = new Event();
        Event event2 = new Event();
        Event event3 = new Event();
        List<Event> eventList = new ArrayList<>();

        //creating event object
        event1.acknowledgeTime = new Date();
        event1.acknowledgeUser = "testAdmin";
        event1.alarmId = 1;
        event1.createTime = new Date();
        event1.description = "Test event";
        event1.dpName = "test dp";
        event1.eventDisplay = true;
        event1.host = "localhost";
        event1.ipAddr = "127.0.0.1";
        event1.logGroup = "Any";
        event1.logMessage = "Testing log message test";
        event1.mouseOverText = "testing mouse over";
        event1.notification = "Show test notification";
        event1.operatorAction = "Action";
        event1.operatorActionMenuText = "Action Menu";
        event1.operatorInstruction = "Instruction setting";

        Map<String, String> params = new HashMap<>();
        params.put("trigger", "3.0");
        params.put("description", "test desc");
        params.put("label", "test label");
        params.put("user", "testAdmin");
        params.put("threshold", "9.0");
        params.put("resourceId", "11");
        params.put("logmessage", "test log message");

        event1.parms = params;
        event1.serviceID = 1;
        event1.serviceName = "Test Service";
        event1.severity = OnmsSeverity.NORMAL;
        event1.snmp = "test SNMP";
        event1.snmphost = "Remote Host";
        event1.time = new Date();
        event1.uei = "uei://org/opennms/test/EventConterollerTest";
        eventList.add(event1);

        //creating event object
        event2.acknowledgeTime = new Date();
        event2.acknowledgeUser = "testAdmin";
        event2.alarmId = 2;
        event2.createTime = new Date();
        event2.description = "Test event";
        event2.dpName = "test dp";
        event2.eventDisplay = true;
        event2.host = "localhost";
        event2.ipAddr = "127.0.0.1";
        event2.logGroup = "Any";
        event2.logMessage = "Testing log message test";
        event2.mouseOverText = "testing mouse over";
        event2.notification = "Show test notification";
        event2.operatorAction = "Action";
        event2.operatorActionMenuText = "Action Menu";
        event2.operatorInstruction = "Instruction setting";

        params.clear();
        params.put("trigger", "7.0");
        params.put("description", "test desc");
        params.put("label", "test label");
        params.put("user", "admin");
        params.put("threshold", "7.0");
        params.put("resourceId", "15");
        params.put("logmessage", "test log message");

        event2.parms = params;
        event2.serviceID = 1;
        event2.serviceName = "Test Service";
        event2.severity = OnmsSeverity.WARNING;
        event2.snmp = "test SNMP";
        event2.snmphost = "Remote Host";
        event2.time = new Date();
        event2.uei = "uei://org/opennms/test/EventConterollerTest2";
        eventList.add(event2);

        //creating event object
        event3.acknowledgeTime = new Date();
        event3.acknowledgeUser = "testAdmin";
        event3.alarmId = 3;
        event3.createTime = new Date();
        event3.description = "Test event";
        event3.dpName = "test dp";
        event3.eventDisplay = true;
        event3.host = "localhost";
        event3.ipAddr = "127.0.0.1";
        event3.logGroup = "Any";
        event3.logMessage = "Testing log message test";
        event3.mouseOverText = "testing mouse over";
        event3.notification = "Show test notification";
        event3.operatorAction = "Action";
        event3.operatorActionMenuText = "Action Menu";
        event3.operatorInstruction = "Instruction setting";

        params.clear();
        params.put("trigger", "5.0");
        params.put("description", "test desc");
        params.put("label", "test label");
        params.put("user", "admin");
        params.put("threshold", "10.0");
        params.put("resourceId", "12");
        params.put("logmessage", "test log message");

        event3.parms = params;
        event3.serviceID = 3;
        event3.serviceName = "Test Service";
        event3.severity = OnmsSeverity.MINOR;
        event3.snmp = "test SNMP";
        event3.snmphost = "Remote Host";
        event3.time = new Date();
        event3.uei = "uei://org/opennms/test/EventConterollerTest3";
        eventList.add(event3);

        return eventList;
    }
}
