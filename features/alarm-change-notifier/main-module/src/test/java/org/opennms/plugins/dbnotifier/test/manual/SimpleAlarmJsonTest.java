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

package org.opennms.plugins.dbnotifier.test.manual;

import org.junit.Test;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class SimpleAlarmJsonTest {

	@Test
	public void test() {

		// contents of alarm to json in postgres
		String testString1="{\"alarmid\":285,\"eventuei\":\"uei.opennms.org/application/generic/piAlarmRaise\",\"nodeid\":null,\"ipaddr\":\"127.0.0.1\",\"serviceid\":null,\"reductionkey\":\"uei.opennms.org/application/generic/piAlarmRaise:0:127.0.0.1:1\",\"alarmtype\":1,\"counter\":2,\"severity\":5,\"lasteventid\":4160,\"firsteventtime\":\"2016-07-04 16:19:14+01\",\"lasteventtime\":\"2016-07-04 16:24:04+01\",\"firstautomationtime\":null,\"lastautomationtime\":null,\"description\":\"Generic Raspberry Pi Alarm Raise 1\",\"logmsg\":\"Generic Raspberry Pi Alarm Raise 1\",\"operinstruct\":\"\",\"tticketid\":null,\"tticketstate\":null,\"mouseovertext\":null,\"suppresseduntil\":\"2016-07-04 16:19:14+01\",\"suppresseduser\":null,\"suppressedtime\":\"2016-07-04 16:19:14+01\",\"alarmackuser\":null,\"alarmacktime\":null,\"managedobjectinstance\":null,\"managedobjecttype\":null,\"applicationdn\":null,\"ossprimarykey\":null,\"x733alarmtype\":null,\"x733probablecause\":0,\"qosalarmstate\":null,\"clearkey\":null,\"ifindex\":null,\"eventparms\":\"PiIoId=1(string,text)\",\"stickymemo\":null,\"systemid\":\"00000000-0000-0000-0000-000000000000\"}";
		String testString2="{\"alarmid\":285,\"eventuei\":\"uei.opennms.org/application/generic/piAlarmRaise\",\"nodeid\":null,\"ipaddr\":\"127.0.0.1\",\"serviceid\":null,\"reductionkey\":\"uei.opennms.org/application/generic/piAlarmRaise:0:127.0.0.1:1\",\"alarmtype\":1,\"counter\":2,\"severity\":5,\"lasteventid\":4160,\"firsteventtime\":\"2016-07-04 16:19:14+01\",\"lasteventtime\":\"2016-07-04 16:24:04+01\",\"firstautomationtime\":null,\"lastautomationtime\":null,\"description\":\"Generic Raspberry Pi Alarm Raise 1\",\"logmsg\":\"Generic Raspberry Pi Alarm Raise 1\",\"operinstruct\":\"\",\"tticketid\":null,\"tticketstate\":null,\"mouseovertext\":null,\"suppresseduntil\":\"2016-07-04 16:19:14+01\",\"suppresseduser\":null,\"suppressedtime\":\"2016-07-04 16:19:14+01\",\"alarmackuser\":\"admin\",\"alarmacktime\":\"2016-07-05 13:38:51.753+01\",\"managedobjectinstance\":null,\"managedobjecttype\":null,\"applicationdn\":null,\"ossprimarykey\":null,\"x733alarmtype\":null,\"x733probablecause\":0,\"qosalarmstate\":null,\"clearkey\":null,\"ifindex\":null,\"eventparms\":\"PiIoId=1(string,text)\",\"stickymemo\":null,\"systemid\":\"00000000-0000-0000-0000-000000000000\"}";

		// "suppressedtime":"2016-07-04 16:19:14+01","systemid":"00000000-0000-0000-0000-000000000000","suppresseduntil":"2016-07-04 16:19:14+01","description":"Generic Raspberry Pi Alarm Raise 1","mouseovertext":null,"x733probablecause":0,"lasteventid":4184,"lasteventtime":"2016-07-05 12:00:02+01","managedobjectinstance":null,"alarmacktime":"2016-07-05 13:38:51.753+01","qosalarmstate":null,"ipaddr":"127.0.0.1","alarmackuser":"admin","nodeid":null,"firsteventtime":"2016-07-04 16:19:14+01","severity":5,"ifindex":null,"alarmtype":1,"x733alarmtype":null,"logmsg":"Generic Raspberry Pi Alarm Raise 1","tticketid":null,"firstautomationtime":null,"clearkey":null,"managedobjecttype":null,"eventuei":"uei.opennms.org\/application\/generic\/piAlarmRaise","counter":3,"applicationdn":null,"operinstruct":"","ossprimarykey":null,"stickymemo":null,"tticketstate":null,"alarmid":285,"serviceid":null,"reductionkey":"uei.opennms.org\/application\/generic\/piAlarmRaise:0:127.0.0.1:1","suppresseduser":null,"lastautomationtime":null,"eventparms":"PiIoId=1(string,text)"
		//String testString = "[{},{}]";

		String testString = "["+testString1+","+testString2+"]";

		JSONParser parser = new JSONParser();

		try {
			System.out.println(this.getClass().getName()+" start of test");

			System.out.println("testString:"+testString);

			Object obj = parser.parse(testString);

			JSONArray jsonArray = (JSONArray) obj;

			System.out.println("jsonArray.toString():"+jsonArray.toString());

			JSONObject newJsonObject = (JSONObject) jsonArray.get(0);

			JSONObject oldJsonObject = (JSONObject) jsonArray.get(1);

			if (newJsonObject.isEmpty()) {
				System.out.println("newJsonObject is empty");
			} else {
				System.out.println("newJsonObject:");
				for (Object key : oldJsonObject.keySet()) {
					//based on you key types
					String keyStr = (String)key;
					Object keyvalue = oldJsonObject.get(keyStr);

					//Print key and value
					System.out.println("   key: "+ keyStr + " value: " + keyvalue);
				}

				/*
				 *    Types of alarm notifications
				 *    Alarm Creation - Entering the Alarm Active State
				 *    Alarm Severity Escalation or De-escalation
				 *    Alarm Sticky Memo change
				 *    Alarm Acknowledgement state change
				 *    Alarm Trouble Ticket Change Event
				 *    Alarm Clear/Deletion - Entering the Alarm Inactive state
				 */
				
				/*    
				 *    // copy as parameter
				 *    key: alarmid value: 285
				 */
				String alarmId= (newJsonObject.get("alarmid")==null) ? null : newJsonObject.get("alarmid").toString();

				/*    generic alarm details
				 *    // copy as paramater
				 *    key: logmsg value: Generic Raspberry Pi Alarm Raise 1
				 *    key: description value: Generic Raspberry Pi Alarm Raise 1
				 *    Key: mouseovertext value: null
				 *    key: operinstruct value:
				 */
				String logmsg= (newJsonObject.get("logmsg")==null) ? null : newJsonObject.get("logmsg").toString();
				String description= (newJsonObject.get("description")==null) ? null : newJsonObject.get("description").toString();
				String mouseovertext= (newJsonObject.get("mouseovertext")==null) ? null : newJsonObject.get("mouseovertext").toString();
				String operinstruct= (newJsonObject.get("operinstruct")==null) ? null : newJsonObject.get("operinstruct").toString();

				/*
				 *    // alarm automation parameters
				 *    //copy as paramater
				 *    key: eventuei value: uei.opennms.org/application/generic/piAlarmRaise
				 *    key: reductionkey value: uei.opennms.org/application/generic/piAlarmRaise:0:127.0.0.1:
				 *    key: clearkey value: null
				 *    key: alarmtype value: 1
				 */
				String eventuei= (newJsonObject.get("eventuei")==null) ? null : newJsonObject.get("eventuei").toString();
				String reductionkey= (newJsonObject.get("reductionkey")==null) ? null : newJsonObject.get("reductionkey").toString();
				String clearkey= (newJsonObject.get("clearkey")==null) ? null : newJsonObject.get("clearkey").toString();
				String alarmtype= (newJsonObject.get("alarmtype")==null) ? null : newJsonObject.get("alarmtype").toString();

				/*
				 *    // Node / service identity - copy into event corresponding event fields
				 *    // copy as field into event field
				 *    key: nodeid value: null
				 *    key: ipaddr value: 127.0.0.1
				 *    key: ifindex value: null
				 *    key: applicationdn value: null
				 *    key: serviceid value: null
				 *    key: systemid value: 00000000-0000-0000-0000-000000000000
				 */
				String nodeid= (newJsonObject.get("nodeid")==null) ? null : newJsonObject.get("nodeid").toString();
				String ipaddr= (newJsonObject.get("ipaddr")==null) ? null : newJsonObject.get("ipaddr").toString();
				String ifindex= (newJsonObject.get("ifindex")==null) ? null : newJsonObject.get("ifindex").toString();
				String applicationdn= (newJsonObject.get("applicationdn")==null) ? null : newJsonObject.get("applicationdn").toString();
				String serviceid= (newJsonObject.get("serviceid")==null) ? null : newJsonObject.get("serviceid").toString();
				String systemid= (newJsonObject.get("systemid")==null) ? null : newJsonObject.get("systemid").toString();


				/*
				 *    // use to generate alarm ack state change event
				 *    // copy as parameter
				 *    key: alarmacktime value: 2016-07-05 13:38:51.753+01
				 *    key: alarmackuser value: admin
				 */
				String alarmacktime= (newJsonObject.get("alarmacktime")==null) ? null : newJsonObject.get("alarmacktime").toString();
				String alarmackuser= (newJsonObject.get("alarmackuser")==null) ? null : newJsonObject.get("alarmackuser").toString();

				/*
				 *    // use to generate Alarm Severity Escalation or De-escalation
				 *    // copy as parameter
				 *    key: severity value: 5
				 */
				String severity= (newJsonObject.get("severity")==null) ? null : newJsonObject.get("severity").toString();

				/*
				 *    // use to generate Alarm Trouble Ticket Change Event
				 *    // copy as parameter
				 *    key: tticketid value: null
				 *    key: tticketstate value: null
				 */
				String tticketid= (newJsonObject.get("tticketid")==null) ? null : newJsonObject.get("tticketid").toString();
				String tticketstate= (newJsonObject.get("tticketstate")==null) ? null : newJsonObject.get("tticketstate").toString();

				/*
				 *    // use to generate Alarm Sticky Note change notification
				 *    // copy as paramater - need to resolve
				 *    key: stickymemo value: null
				 */
				String stickymemo= (newJsonObject.get("stickymemo")==null) ? null : newJsonObject.get("stickymemo").toString();


				/*
				 *    // alarm suppressed notification
				 *    //copy as paramater
				 *    key: suppressedtime value: 2016-07-04 16:19:14+01
				 *    key: suppresseduntil value: 2016-07-04 16:19:14+01
				 *    key: suppresseduser value: null
				 */
				String suppressedtime= (newJsonObject.get("suppressedtime")==null) ? null : newJsonObject.get("suppressedtime").toString();
				String suppresseduntil= (newJsonObject.get("suppresseduntil")==null) ? null : newJsonObject.get("suppresseduntil").toString();
				String suppresseduser= (newJsonObject.get("suppresseduser")==null) ? null : newJsonObject.get("suppresseduser").toString();

				/*    
				 *    // alarm delete event - used to store final value of alarm
				 *    (note alarm delete event when alarm deleted)
				 *    // copy as paramater
				 *    key: firsteventtime value: 2016-07-04 16:19:14+01
				 *    key: lasteventtime value: 2016-07-04 16:24:04+01
				 *    key: lasteventid value: 4160
				 */
				String firsteventtime= (newJsonObject.get("firsteventtime")==null) ? null : newJsonObject.get("firsteventtime").toString();
				String lasteventtime= (newJsonObject.get("lasteventtime")==null) ? null : newJsonObject.get("lasteventtime").toString();
				String lasteventid= (newJsonObject.get("lasteventid")==null) ? null : newJsonObject.get("lasteventid").toString();
				
				/*
				 *    key: counter value: 2
				 *    key: lastautomationtime value: null
				 *    key: firstautomationtime value: null
				 *    key: eventparms value: PiIoId=1(string,text)
				 */
				String counter= (newJsonObject.get("counter")==null) ? null : newJsonObject.get("counter").toString();
				String lastautomationtime= (newJsonObject.get("lastautomationtime")==null) ? null : newJsonObject.get("lastautomationtime").toString();
				String firstautomationtime= (newJsonObject.get("firstautomationtime")==null) ? null : newJsonObject.get("firstautomationtime").toString();
				String eventparms= (newJsonObject.get("eventparms")==null) ? null : newJsonObject.get("eventparms").toString();
				
				/*    // used for ossj
				 *    key: managedobjecttype value: null
				 *    key: managedobjectinstance value: null
				 *    key: x733alarmtype value: null
				 *    key: x733probablecause value: 0
				 *    key: ossprimarykey value: null
				 *    key: qosalarmstate value: null
				 */
				String managedobjecttype= (newJsonObject.get("managedobjecttype")==null) ? null : newJsonObject.get("managedobjecttype").toString();
				String managedobjectinstance= (newJsonObject.get("lmanagedobjectinstance")==null) ? null : newJsonObject.get("managedobjectinstance").toString();
				String x733alarmtype= (newJsonObject.get("x733alarmtype")==null) ? null : newJsonObject.get("x733alarmtype").toString();
				String x733probablecause= (newJsonObject.get("x733probablecause")==null) ? null : newJsonObject.get("x733probablecause").toString();
				String ossprimarykey= (newJsonObject.get("ossprimarykey")==null) ? null : newJsonObject.get("ossprimarykey").toString();
				String qosalarmstate= (newJsonObject.get("qosalarmstate")==null) ? null : newJsonObject.get("qosalarmstate").toString();
				
			}

			if (oldJsonObject.isEmpty()) {
				System.out.println("oldJsonObject is empty");
			} else {
				System.out.println("oldJsonObject:");
				for (Object key : oldJsonObject.keySet()) {
					//based on you key types
					String keyStr = (String)key;
					Object keyvalue = oldJsonObject.get(keyStr);

					//Print key and value
					System.out.println("   key: "+ keyStr + " value: " + keyvalue);
				}
			}

		} catch (ParseException e) {
			e.printStackTrace();
			throw new RuntimeException("test failed parsing of alarm",e);
		}

		System.out.println(this.getClass().getName()+" end of test");

	}
}





