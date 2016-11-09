/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.alarmd.api;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;
import org.opennms.core.utils.StringUtils;
import org.opennms.netmgt.alarmd.api.NorthboundAlarm.AlarmType;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.model.TroubleTicketState;

public class NorthboundAlarmTest extends XmlTestNoCastor<NorthboundAlarm> {

    public NorthboundAlarmTest(NorthboundAlarm sampleObject, Object sampleXml) {
        super(sampleObject, sampleXml, null);
    }

    @Override
    protected String getSchemaFile() {
        return "target/classes/xsds/northbound-alarm.xsd";
    }

    @Parameters
    public static Collection<Object[]> data() throws Exception {
        return Arrays.asList(new Object[][] {
                {
                    getSampleAlarm(),
                    "<northbound-alarm xmlns=\"http://xmlns.opennms.org/xsd/alarms\" id=\"1\">\n" +
                     "<uei>some-uei</uei>\n" +
                     "<node-id>99</node-id>\n" +
                     "<node-label>some-node-label</node-label>\n" +
                     "<node-sysobjectid>.1.3.6</node-sysobjectid>\n" +
                     "<node-foreignsource>fs</node-foreignsource>\n" +
                     "<node-foreignid>fid</node-foreignid>\n" +
                     "<ack-time>" + StringUtils.iso8601LocalOffsetString(new Date(1)) + "</ack-time>\n" +
                     "<ack-user>admin</ack-user>\n" +
                     "<alarm-type>PROBLEM</alarm-type>\n" +
                     "<app-dn>some-app-dn</app-dn>\n" +
                     "<clear-key>some-clear-key</clear-key>\n" +
                     "<count>1</count>\n" +
                     "<description>some description</description>\n" +
                     "<first-occurrence>" + StringUtils.iso8601LocalOffsetString(new Date(2)) + "</first-occurrence>\n" +
                     "<ip-address>127.0.0.127</ip-address>\n" +
                     "<last-occurrence>" + StringUtils.iso8601LocalOffsetString(new Date(4)) + "</last-occurrence>\n" +
                     "<log-messsage>logmsg</log-messsage>\n" +
                     "<object-instance>instance</object-instance>\n" +
                     "<object-type>type</object-type>\n" +
                     "<operator-instructions>instructions here</operator-instructions>\n" +
                     "<oss-key>key</oss-key>\n" +
                     "<oss-state>state</oss-state>\n" +
                     "<alarm-key>key</alarm-key>\n" +
                     "<service>service</service>\n" +
                     "<severity>CRITICAL</severity>\n" +
                     "<suppressed>" + StringUtils.iso8601LocalOffsetString(new Date(5)) + "</suppressed>\n" +
                     "<suppressed-until>" + StringUtils.iso8601LocalOffsetString(new Date(6)) + "</suppressed-until>\n" +
                     "<suppressed-by>me</suppressed-by>\n" +
                     "<ticket-id>NMS-8068</ticket-id>\n" +
                     "<ticket-state>OPEN</ticket-state>\n" +
                     "<x733-type>type</x733-type>\n" +
                     "<x733-cause>1</x733-cause>\n" +
                     "<parameters/>\n" +
                     "<preserved>true</preserved>\n" +
                    "</northbound-alarm>"
                }
        });
    }

    private static NorthboundAlarm getSampleAlarm() {
        NorthboundAlarm alarm = new  NorthboundAlarm();
        alarm.setId(1);
        alarm.setUei("some-uei");
        alarm.setNodeId(99);
        alarm.setNodeLabel("some-node-label");
        alarm.setNodeSysObjectId(".1.3.6");
        alarm.setForeignSource("fs");
        alarm.setForeignId("fid");
        alarm.setAckTime(new Date(1));
        alarm.setAckUser("admin");
        alarm.setAlarmType(AlarmType.PROBLEM);
        alarm.setAppDn("some-app-dn");
        alarm.setClearKey("some-clear-key");
        alarm.setCount(1);
        alarm.setDesc("some description");
        alarm.setFirstOccurrence(new Date(2));
        alarm.setIpAddr("127.0.0.127");
        alarm.setLastOccurrence(new Date(4));
        alarm.setLogMsg("logmsg");
        alarm.setObjectInstance("instance");
        alarm.setObjectType("type");
        alarm.setOperInst("instructions here");
        alarm.setOssKey("key");
        alarm.setOssState("state");
        alarm.setAlarmKey("key");
        alarm.setService("service");
        alarm.setSeverity(OnmsSeverity.CRITICAL);
        alarm.setSuppressed(new Date(5));
        alarm.setSuppressedUntil(new Date(6));
        alarm.setSuppressedBy("me");
        alarm.setTicketId("NMS-8068");
        alarm.setTicketState(TroubleTicketState.OPEN);
        alarm.setx733Type("type");
        alarm.setx733Cause(1);
        alarm.setPreserved(true);
        return alarm;
    }
}
