/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.events.api.model;

import org.junit.Test;
import org.opennms.core.test.xml.XmlTest;
import org.opennms.netmgt.xml.event.*;

import java.util.Collections;
import java.util.Date;

/**
 * A test class to verify mapping an immutability properties of '{@link ImmutableEvent}'.
 */
public class ImmutableEventTest {

    @Test
    public void test() {
        Event event = createEvent();

        // Mutable to Immutable
        IEvent immutableEvent = ImmutableMapper.fromMutableEvent(event);

        // Attempt to add to immutable list.
        try {
            immutableEvent.getParmCollection().add(ImmutableParm.newBuilder().build());
        } catch (Exception e) {
            // Expected...
        }

        try {
            immutableEvent.getAutoactionCollection().add(ImmutableAutoAction.newBuilder().build());
        } catch (Exception e) {
            // Expected...
        }

        try {
            immutableEvent.getOperactionCollection().add(ImmutableOperAction.newBuilder().build());
        } catch (Exception e) {
            // Expected...
        }

        try {
            immutableEvent.getLoggroupCollection().add("");
        } catch (Exception e) {
            // Expected...
        }

        try {
            immutableEvent.getForwardCollection().add(ImmutableForward.newBuilder().build());
        } catch (Exception e) {
            // Expected...
        }

        try {
            immutableEvent.getScriptCollection().add(ImmutableScript.newBuilder().build());
        } catch (Exception e) {
            // Expected...
        }

        // Immutable to Mutable
        Event convertedEvent = Event.copyFrom(immutableEvent);

        String expectedXml = XmlTest.marshalToXmlWithJaxb(event);
        String convertedXml = XmlTest.marshalToXmlWithJaxb(convertedEvent);
        XmlTest.assertXmlEquals(expectedXml, convertedXml);
    }

    @Test
    public void testSimple() {
        Event event = new Event();

        // The following must have values due to the implementation of their getters.
        event.setParmCollection(Collections.emptyList());

        // Mutable to Immutable
        IEvent immutableEvent = ImmutableMapper.fromMutableEvent(event);

        // Immutable to Mutable
        Event convertedEvent = Event.copyFrom(immutableEvent);

        String expectedXml = XmlTest.marshalToXmlWithJaxb(event);
        String convertedXml = XmlTest.marshalToXmlWithJaxb(convertedEvent);
        XmlTest.assertXmlEquals(expectedXml, convertedXml);
    }

    private static Event createEvent() {
        Event event = new Event();
        event.setUuid("test-uuid");
        event.setDbid(100);
        event.setDistPoller("test-dist-poller");
        event.setCreationTime(new Date());
        event.setMasterStation("test-master-station");

        Maskelement maskElement = new Maskelement();
        maskElement.setMename("test-me");
        maskElement.getMevalueCollection().add("test-me-val1");
        Mask mask = new Mask();
        mask.getMaskelementCollection().add(maskElement);
        event.setMask(mask);

        event.setUei("test-uei");
        event.setSource("test-source");
        event.setNodeid(100L);
        event.setTime(new Date());
        event.setHost("test-host");
        event.setInterfaceAddress(null);
        event.setInterface(null);
        event.setSnmphost("test-snmp-host");

        Snmp snmp = new Snmp();
        snmp.setId("test-snmp-id");
        snmp.setVersion("test-version");
        snmp.setTimeStamp(0L);
        snmp.setSpecific(0);
        snmp.setGeneric(0);
        event.setSnmp(snmp);

        Parm parm = new Parm();
        parm.setParmName("test-parm");
        Value value = new Value();
        value.setContent("test-parm-value");
        parm.setValue(value);
        event.setParmCollection(Collections.singletonList(parm));

        event.setDescr("test-descr");

        Logmsg logmsg = new Logmsg();
        logmsg.setContent("test-content");
        event.setLogmsg(logmsg);

        event.setSeverity("test-severity");
        event.setPathoutage("test-pathoutage");

        Correlation correlation = new Correlation();
        correlation.setPath("test-path");
        event.setCorrelation(correlation);

        event.setOperinstruct("test-operinstruct");

        Autoaction autoaction = new Autoaction();
        autoaction.setContent("test");
        event.getAutoactionCollection().add(autoaction);

        Operaction operaction = new Operaction();
        operaction.setContent("test");
        event.getOperactionCollection().add(operaction);

        Autoacknowledge autoacknowledge = new Autoacknowledge();
        autoacknowledge.setContent("test");
        event.setAutoacknowledge(autoacknowledge);

        event.getLoggroupCollection().add("test");

        Tticket tticket = new Tticket();
        tticket.setContent("test");
        event.setTticket(tticket);

        Forward forward = new Forward();
        forward.setContent("test");
        event.getForwardCollection().add(forward);

        Script script = new Script();
        script.setContent("test");
        event.getScriptCollection().add(script);

        event.setIfIndex(100);
        event.setIfAlias("test-ifalias");
        event.setMouseovertext("test");

        AlarmData alarmData = new AlarmData();
        alarmData.setReductionKey("test");
        alarmData.setAlarmType(0);
        alarmData.setAutoClean(false);
        alarmData.setX733ProbableCause(0);
        ManagedObject managedObject = new ManagedObject();
        managedObject.setType("test-type");
        alarmData.setManagedObject(managedObject);
        UpdateField updateField = new UpdateField();
        updateField.setFieldName("test");
        alarmData.getUpdateFieldList().add(updateField);
        event.setAlarmData(alarmData);

        return event;
    }
}
