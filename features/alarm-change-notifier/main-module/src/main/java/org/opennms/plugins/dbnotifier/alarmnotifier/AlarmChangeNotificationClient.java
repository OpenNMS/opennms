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

package org.opennms.plugins.dbnotifier.alarmnotifier;


import java.sql.Timestamp;
import java.util.Calendar;

import javax.xml.bind.DatatypeConverter;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.opennms.netmgt.events.api.EventProxy;
import org.opennms.netmgt.events.api.EventProxyException;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.core.network.IPAddress;
import org.opennms.plugins.com.impossibl.postgres.jdbc.TimestampUtils;
import org.opennms.plugins.dbnotifier.DbNotification;
import org.opennms.plugins.dbnotifier.NotificationClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * sends alarm change events into opennms
 * 
 * @author admin
 *
 */
public class AlarmChangeNotificationClient implements NotificationClient {
	private static final Logger LOG = LoggerFactory
			.getLogger(AlarmChangeNotificationClient.class);

	public static final String EVENT_SOURCE_NAME = "AlarmChangeNotifier";

	protected static final String TYPE_JSON = "json";
	protected static final String ENCODING_TEXT = "text";

	EventProxy eventProxy = null;

	public EventProxy getEventProxy() {
		return eventProxy;
	}

	public void setEventProxy(EventProxy eventProxy) {
		this.eventProxy = eventProxy;
	}

	@Override
	public void sendDbNotification(DbNotification dbNotification) {
		try{
			String payload = dbNotification.getPayload();

			JSONObject newJsonObject=null;
			JSONObject oldJsonObject=null;

			try {
				JSONParser parser = new JSONParser();
				Object obj;
				obj = parser.parse(payload);
				JSONArray jsonArray = (JSONArray) obj;
				LOG.debug("payload jsonArray.toString():" + jsonArray.toString());
				newJsonObject = (JSONObject) jsonArray.get(0);
				oldJsonObject = (JSONObject) jsonArray.get(1);

				newJsonObject = jsonAlarmTimeNormaliser(newJsonObject);
				oldJsonObject = jsonAlarmTimeNormaliser(oldJsonObject);

			} catch (ParseException e1) {
				throw new RuntimeException("cannot parse notification payload to json object. payload="+ payload, e1);
			}

			if ( newJsonObject.isEmpty() && (! oldJsonObject.isEmpty()) ){
				// received an alarm delete
				// ignore alarm type 2
				if(! "2".equals(oldJsonObject.get("alarmtype").toString())) {
					if (LOG.isDebugEnabled()) LOG.debug("alarm deleted alarmid="+oldJsonObject.get("alarmid"));
					EventBuilder eb= jsonAlarmToEventBuilder(oldJsonObject, 
							new EventBuilder( AlarmChangeEventConstants.ALARM_DELETED_EVENT, EVENT_SOURCE_NAME));

					//copy in all values as json in params
					eb.addParam(AlarmChangeEventConstants.OLD_ALARM_VALUES_PARAM,oldJsonObject.toString(), TYPE_JSON, ENCODING_TEXT);
					eb.addParam(AlarmChangeEventConstants.NEW_ALARM_VALUES_PARAM,newJsonObject.toString(), TYPE_JSON, ENCODING_TEXT);

					sendEvent(eb.getEvent());
				}

			} else if ( (! newJsonObject.isEmpty()) && oldJsonObject.isEmpty()){
				// received an alarm create

				// ignore alarm type 2
				if(! "2".equals(newJsonObject.get("alarmtype").toString())) {
					if (LOG.isDebugEnabled()) LOG.debug("alarm created alarmid="+newJsonObject.get("alarmid"));
					EventBuilder eb= jsonAlarmToEventBuilder(newJsonObject, 
							new EventBuilder( AlarmChangeEventConstants.ALARM_CREATED_EVENT, EVENT_SOURCE_NAME));

					//copy in all values as json in params
					eb.addParam(AlarmChangeEventConstants.OLD_ALARM_VALUES_PARAM,oldJsonObject.toString(), TYPE_JSON, ENCODING_TEXT);
					eb.addParam(AlarmChangeEventConstants.NEW_ALARM_VALUES_PARAM,newJsonObject.toString(), TYPE_JSON, ENCODING_TEXT);

					// set initial severity to new alarm severity
					if (newJsonObject.get("severity")!=null) {
						try{
							String newseverity= newJsonObject.get("severity").toString();
							Integer newsvty= Integer.valueOf(newseverity);
							eb.addParam(AlarmChangeEventConstants.INITIAL_SEVERITY_PARAM,newsvty.toString());
						} catch (Exception e){
							LOG.error("problem parsing initial severity for new alarm event newJsonObject="+newJsonObject,e);
						}
					}
					sendEvent(eb.getEvent());
				}
			} else {
				// received an alarm change notification
				// alarm has changed check for changes and send appropriate notifications

				// ignore alarm type 2
				if(! "2".equals(newJsonObject.get("alarmtype").toString())) {

					// ignore event count and automation changes if these are only change in alarm
					// TODO need database trigger to also ignore these changes
					JSONObject newobj = new JSONObject(newJsonObject);
					JSONObject oldobj = new JSONObject(oldJsonObject);
					newobj.remove("lasteventtime");
					oldobj.remove("lasteventtime");
					newobj.remove("lasteventid");
					oldobj.remove("lasteventid");
					newobj.remove("counter");
					oldobj.remove("counter");
					newobj.remove("firstautomationtime");
					oldobj.remove("firstautomationtime");
					newobj.remove("lastautomationtime");
					oldobj.remove("lastautomationtime");

					if (! newobj.toString().equals(oldobj.toString())){
						// changes other than event count

						// severity changed notification
						String oldseverity= (oldJsonObject.get("severity")==null) ? null : oldJsonObject.get("severity").toString();
						String newseverity= (newJsonObject.get("severity")==null) ? null : newJsonObject.get("severity").toString();

						if (newseverity !=null && ! newseverity.equals(oldseverity)){
							
							// check if alarm cleared
							EventBuilder eb=null;
							if("2".equals(newseverity)){
								if (LOG.isDebugEnabled()) LOG.debug("alarm cleared alarmid="+oldJsonObject.get("alarmid")
										+" old severity="+oldseverity+" new severity="+newseverity);
								eb= jsonAlarmToEventBuilder(newJsonObject, 
										new EventBuilder( AlarmChangeEventConstants.ALARM_CLEARED_EVENT, EVENT_SOURCE_NAME));
							} else {
								// if just severity changed
								if (LOG.isDebugEnabled()) LOG.debug("alarm severity changed alarmid="+oldJsonObject.get("alarmid")
										+" old severity="+oldseverity+" new severity="+newseverity);
								eb= jsonAlarmToEventBuilder(newJsonObject, 
										new EventBuilder( AlarmChangeEventConstants.ALARM_SEVERITY_CHANGED_EVENT, EVENT_SOURCE_NAME));
							}
							
							eb.addParam(AlarmChangeEventConstants.OLDSEVERITY_PARAM,oldseverity);

							//copy in all values as json in params
							eb.addParam(AlarmChangeEventConstants.OLD_ALARM_VALUES_PARAM,oldJsonObject.toString(), TYPE_JSON, ENCODING_TEXT);
							eb.addParam(AlarmChangeEventConstants.NEW_ALARM_VALUES_PARAM,newJsonObject.toString(), TYPE_JSON, ENCODING_TEXT);

							sendEvent(eb.getEvent());
						}

						// alarm acknowledged / unacknowledged notifications  
						String oldalarmacktime= (oldJsonObject.get("alarmacktime")==null) ? null : oldJsonObject.get("alarmacktime").toString();
						String newalarmacktime= (newJsonObject.get("alarmacktime")==null) ? null : newJsonObject.get("alarmacktime").toString();
						if(oldalarmacktime==null && newalarmacktime !=null) {
							//alarm acknowledged notification
							if (LOG.isDebugEnabled()) LOG.debug("alarm acknowleged alarmid="+newJsonObject.get("alarmid"));

							EventBuilder eb= jsonAlarmToEventBuilder(newJsonObject, 
									new EventBuilder(AlarmChangeEventConstants.ALARM_ACKNOWLEDGED_EVENT, EVENT_SOURCE_NAME));

							//copy in all values as json in params
							eb.addParam(AlarmChangeEventConstants.OLD_ALARM_VALUES_PARAM,oldJsonObject.toString(), TYPE_JSON, ENCODING_TEXT);
							eb.addParam(AlarmChangeEventConstants.NEW_ALARM_VALUES_PARAM,newJsonObject.toString(), TYPE_JSON, ENCODING_TEXT);

							sendEvent(eb.getEvent());

						} else {
							if(oldalarmacktime!=null && newalarmacktime ==null) {

								//alarm unacknowledged notification
								if (LOG.isDebugEnabled()) LOG.debug("alarm unacknowleged alarmid="+newJsonObject.get("alarmid"));
								//TODO issue unacknowledged doesn't have a user because only user and time in alarm field
								EventBuilder eb= jsonAlarmToEventBuilder(newJsonObject, 
										new EventBuilder(AlarmChangeEventConstants.ALARM_UNACKNOWLEDGED_EVENT, EVENT_SOURCE_NAME));

								//copy in all values as json in params
								eb.addParam(AlarmChangeEventConstants.OLD_ALARM_VALUES_PARAM,oldJsonObject.toString(), TYPE_JSON, ENCODING_TEXT);
								eb.addParam(AlarmChangeEventConstants.NEW_ALARM_VALUES_PARAM,newJsonObject.toString(), TYPE_JSON, ENCODING_TEXT);

								sendEvent(eb.getEvent());
							}
						}

						// alarm suppressed / unsuppressed notifications 
						String newsuppresseduntil= (newJsonObject.get("suppresseduntil")==null) ? null : newJsonObject.get("suppresseduntil").toString();
						String oldsuppresseduntil= (oldJsonObject.get("suppresseduntil")==null) ? null : oldJsonObject.get("suppresseduntil").toString();

						if (newsuppresseduntil!=null && ! newsuppresseduntil.equals(oldsuppresseduntil)) {
							//alarm suppressed notification
							if (LOG.isDebugEnabled()) LOG.debug("alarm suppressed alarmid="+newJsonObject.get("alarmid"));

							EventBuilder eb= jsonAlarmToEventBuilder(newJsonObject, 
									new EventBuilder(AlarmChangeEventConstants.ALARM_SUPPRESSED_EVENT, EVENT_SOURCE_NAME));

							//copy in all values as json in params
							eb.addParam(AlarmChangeEventConstants.OLD_ALARM_VALUES_PARAM,oldJsonObject.toString(), TYPE_JSON, ENCODING_TEXT);
							eb.addParam(AlarmChangeEventConstants.NEW_ALARM_VALUES_PARAM,newJsonObject.toString(), TYPE_JSON, ENCODING_TEXT);

							sendEvent(eb.getEvent());

						} else {
							if(oldsuppresseduntil!=null && newsuppresseduntil ==null) {

								//alarm unsuppressed notification
								if (LOG.isDebugEnabled()) LOG.debug("alarm unsuppressed alarmid="+newJsonObject.get("alarmid"));
								//TODO issue unsuppress doesn't have a user because only user and time in alarm field
								EventBuilder eb= jsonAlarmToEventBuilder(newJsonObject, 
										new EventBuilder(
												AlarmChangeEventConstants.ALARM_UNSUPPRESSED_EVENT,
												EVENT_SOURCE_NAME));

								//copy in all values as json in params
								eb.addParam(AlarmChangeEventConstants.OLD_ALARM_VALUES_PARAM,oldJsonObject.toString(), TYPE_JSON, ENCODING_TEXT);
								eb.addParam(AlarmChangeEventConstants.NEW_ALARM_VALUES_PARAM,newJsonObject.toString(), TYPE_JSON, ENCODING_TEXT);

								sendEvent(eb.getEvent());
							}
						}

						// trouble ticket state changed notification
						String oldtticketid= (oldJsonObject.get("tticketid")==null) ? null : oldJsonObject.get("tticketid").toString();
						String newtticketid= (newJsonObject.get("tticketid")==null) ? null : newJsonObject.get("tticketid").toString();
						String oldtticketstate= (oldJsonObject.get("tticketstate")==null) ? null : oldJsonObject.get("tticketstate").toString();
						String newtticketstate= (newJsonObject.get("tticketstate")==null) ? null : newJsonObject.get("tticketstate").toString();

						if ( (oldtticketid==null && newtticketid !=null)
								|| (oldtticketid !=null && ! newtticketid.equals(oldtticketid))
								|| (oldtticketstate ==null &&  newtticketstate!=null) 
								|| (oldtticketstate !=null && ! newtticketstate.equals(oldtticketstate)) ) {
							if (LOG.isDebugEnabled()) LOG.debug("trouble ticket state changed for alarmid="+oldJsonObject.get("alarmid")
									+" oldtticketid="+oldtticketid
									+" newtticketid="+newtticketid 
									+" oldtticketstate="+oldtticketstate
									+" newtticketstate="+newtticketstate);
							EventBuilder eb= jsonAlarmToEventBuilder(newJsonObject, 
									new EventBuilder( AlarmChangeEventConstants.ALARM_TROUBLETICKET_STATE_CHANGE_EVENT, EVENT_SOURCE_NAME));
							
							eb.addParam(AlarmChangeEventConstants.OLDTICKETID_PARAM,oldtticketid);
							eb.addParam(AlarmChangeEventConstants.TTICKETID_PARAM,newtticketid);
							eb.addParam(AlarmChangeEventConstants.OLDTTICKETSTATE_PARAM,oldtticketstate);
							eb.addParam(AlarmChangeEventConstants.TTICKETSTATE_PARAM,newtticketstate);

							//copy in all values as json in params
							eb.addParam(AlarmChangeEventConstants.OLD_ALARM_VALUES_PARAM,oldJsonObject.toString(), TYPE_JSON, ENCODING_TEXT);
							eb.addParam(AlarmChangeEventConstants.NEW_ALARM_VALUES_PARAM,newJsonObject.toString(), TYPE_JSON, ENCODING_TEXT);

							sendEvent(eb.getEvent());
						}

						// alarm sticky note changed notification
						String oldstickymemo= (oldJsonObject.get("stickymemo")==null) ? null : oldJsonObject.get("stickymemo").toString();
						String newstickymemo= (newJsonObject.get("stickymemo")==null) ? null : newJsonObject.get("stickymemo").toString();

						if ( (newstickymemo!=null && ! newstickymemo.equals(oldstickymemo)) ) {
							if (LOG.isDebugEnabled()) LOG.debug("Sticky memo added for alarmid="+oldJsonObject.get("alarmid")
									+" newstickymemo="+newstickymemo);
							EventBuilder eb= jsonAlarmToEventBuilder(newJsonObject, 
									new EventBuilder(AlarmChangeEventConstants.ALARM_STICKYMEMO_ADD_EVENT, EVENT_SOURCE_NAME));
							eb.addParam(AlarmChangeEventConstants.STICKYMEMO_PARAM,newstickymemo);

							//copy in all values as json in params
							eb.addParam(AlarmChangeEventConstants.OLD_ALARM_VALUES_PARAM,oldJsonObject.toString(), TYPE_JSON, ENCODING_TEXT);
							eb.addParam(AlarmChangeEventConstants.NEW_ALARM_VALUES_PARAM,newJsonObject.toString(), TYPE_JSON, ENCODING_TEXT);

							sendEvent(eb.getEvent());
						}

						// send alarm changed event for any other changes not captured above
						newobj.remove("severity");
						oldobj.remove("severity");
						newobj.remove("alarmacktime");
						oldobj.remove("alarmacktime");
						newobj.remove("alarmackuser");
						oldobj.remove("alarmackuser");
						newobj.remove("suppresseduntil");
						oldobj.remove("suppresseduntil");
						newobj.remove("suppresseduser");
						oldobj.remove("suppresseduser");
						newobj.remove("tticketid");
						oldobj.remove("tticketid");
						newobj.remove("tticketstate");
						oldobj.remove("tticketstate");
						newobj.remove("stickymemo");
						oldobj.remove("stickymemo");
						
						if (! newobj.toString().equals(oldobj.toString())) {

							EventBuilder eb= jsonAlarmToEventBuilder(oldJsonObject, 
									new EventBuilder(AlarmChangeEventConstants.ALARM_CHANGED_EVENT, EVENT_SOURCE_NAME));

							//copy in all values as json in params
							eb.addParam(AlarmChangeEventConstants.OLD_ALARM_VALUES_PARAM,oldJsonObject.toString(), TYPE_JSON, ENCODING_TEXT);
							eb.addParam(AlarmChangeEventConstants.NEW_ALARM_VALUES_PARAM,newJsonObject.toString(), TYPE_JSON, ENCODING_TEXT);

							sendEvent(eb.getEvent());
						}

					}

				}
			}

		} catch (Exception e){
			LOG.error("problem creating opennms alarm change event from database notification", e);
		}

	}

	private EventBuilder jsonAlarmToEventBuilder(JSONObject jsonObject, EventBuilder eb){
		//copy generic alarm details as paramaters
		String alarmId= (jsonObject.get("alarmid")==null) ? null : jsonObject.get("alarmid").toString();
		String severity= (jsonObject.get("severity")==null) ? null : jsonObject.get("severity").toString();
		String logmsg= (jsonObject.get("logmsg")==null) ? null : jsonObject.get("logmsg").toString();
		String eventuei= (jsonObject.get("eventuei")==null) ? null : jsonObject.get("eventuei").toString();
		String reductionkey= (jsonObject.get("reductionkey")==null) ? null : jsonObject.get("reductionkey").toString();
		String clearkey= (jsonObject.get("clearkey")==null) ? null : jsonObject.get("clearkey").toString();
		String alarmtype= (jsonObject.get("alarmtype")==null) ? null : jsonObject.get("alarmtype").toString();	
		String alarmacktime= (jsonObject.get("alarmacktime")==null) ? null : jsonObject.get("alarmacktime").toString();
		String alarmackuser= (jsonObject.get("alarmackuser")==null) ? null : jsonObject.get("alarmackuser").toString();
		String suppressedtime= (jsonObject.get("suppressedtime")==null) ? null : jsonObject.get("suppressedtime").toString();
		String suppresseduntil= (jsonObject.get("suppresseduntil")==null) ? null : jsonObject.get("suppresseduntil").toString();
		String suppresseduser= (jsonObject.get("suppresseduser")==null) ? null : jsonObject.get("suppresseduser").toString();

		// Node / service identity - copy into event corresponding event fields
		String nodeid= (jsonObject.get("nodeid")==null) ? null : jsonObject.get("nodeid").toString();
		String ipaddr= (jsonObject.get("ipaddr")==null) ? null : jsonObject.get("ipaddr").toString();
		String ifindex= (jsonObject.get("ifindex")==null) ? null : jsonObject.get("ifindex").toString();
		String applicationdn= (jsonObject.get("applicationdn")==null) ? null : jsonObject.get("applicationdn").toString();			
		String serviceid= (jsonObject.get("serviceid")==null) ? null : jsonObject.get("serviceid").toString();
		String systemid= (jsonObject.get("systemid")==null) ? null : jsonObject.get("systemid").toString();

		eb.addParam(AlarmChangeEventConstants.ALARMID_PARAM, alarmId );
		eb.addParam(AlarmChangeEventConstants.ALARM_SEVERITY_PARAM, severity );
		eb.addParam(AlarmChangeEventConstants.LOGMSG_PARAM, logmsg );
		eb.addParam(AlarmChangeEventConstants.CLEARKEY_PARAM, clearkey );
		eb.addParam(AlarmChangeEventConstants.ALARMTYPE_PARAM, alarmtype );
		eb.addParam(AlarmChangeEventConstants.ALARM_ACK_TIME_PARAM, alarmacktime );
		eb.addParam(AlarmChangeEventConstants.ALARM_ACK_USER_PARAM, alarmackuser );
		eb.addParam(AlarmChangeEventConstants.SUPPRESSEDTIME_PARAM, suppressedtime );
		eb.addParam(AlarmChangeEventConstants.SUPPRESSEDUNTIL_PARAM, suppresseduntil );
		eb.addParam(AlarmChangeEventConstants.SUPPRESSEDUSER_PARAM, suppresseduser );
		eb.addParam(AlarmChangeEventConstants.EVENTUEI_PARAM, eventuei );
		eb.addParam(AlarmChangeEventConstants.REDUCTIONKEY_PARAM, reductionkey );

		if(nodeid!=null) eb.setNodeid(Long.parseLong(nodeid));
		if (ipaddr!= null) try {
			IPAddress ipaddress = new IPAddress(ipaddr);
			eb.setInterface(ipaddress.toInetAddress());
		} catch (Exception e){
			LOG.error("cannot parse json object ipaddr="+ipaddr,e);
		}
		if (ifindex!=null) eb.setIfIndex(Integer.parseInt(ifindex));

		eb.addParam(AlarmChangeEventConstants.APPLICATIONDN_PARAM,applicationdn);
		eb.addParam(AlarmChangeEventConstants.SERVICEID_PARAM,serviceid);
		eb.addParam(AlarmChangeEventConstants.SYSTEMID_PARAM,systemid);

		return eb;
	}



	private void sendEvent(Event e){
		LOG.debug("sending event to opennms. event.tostring():" + e.toString());
		try {
			if (eventProxy != null) {
				eventProxy.send(e);
			} else {
				LOG.error("OpenNMS event proxy not set - cannot send events to opennms");
			}
		} catch (EventProxyException ex) {
			throw new RuntimeException(
					"event proxy problem sending AlarmChangeNotificationEvent to OpenNMS:",
					ex);
		}
	}

	/**
	 * concerts all time values into a normalised time in alarm json object from database
	 * example timestamps to translate
	 *  {
	 *    "suppressedtime":"2016-08-04 16:11:16.01+01",
	 *    "suppresseduntil":"2016-08-04 16:11:16.01+01",
	 *    "lasteventtime":"2016-08-04 16:11:16.01+01",
	 *    "alarmacktime":"2016-08-04 07:34:04.617+01",
	 *    "firsteventtime":"2016-08-04 16:11:16.01+01",
	 *    "firstautomationtime":"2016-08-04 16:12:03.272205+01",
	 *    "lastautomationtime":"2016-08-04 16:12:03.272205+01"
	 *  }
	 * @param jsonObject
	 * @return
	 */
	public JSONObject jsonAlarmTimeNormaliser(JSONObject jsonObject){

		if(jsonObject.isEmpty()) return jsonObject;

		String suppressedtime= (jsonObject.get("suppressedtime")==null) ? null : timeNormaliser(jsonObject.get("suppressedtime").toString());
		if (suppressedtime!=null) jsonObject.put("suppressedtime", suppressedtime);

		String suppresseduntil= (jsonObject.get("suppresseduntil")==null) ? null : timeNormaliser(jsonObject.get("suppresseduntil").toString());
		if (suppresseduntil!=null) jsonObject.put("suppresseduntil", suppresseduntil);

		String lasteventtime= (jsonObject.get("lasteventtime")==null) ? null : timeNormaliser(jsonObject.get("lasteventtime").toString());
		if (lasteventtime!=null) jsonObject.put("lasteventtime", lasteventtime);

		String alarmacktime= (jsonObject.get("alarmacktime")==null) ? null : timeNormaliser(jsonObject.get("alarmacktime").toString());
		if (alarmacktime!=null) jsonObject.put("alarmacktime", alarmacktime);

		String firsteventtime= (jsonObject.get("firsteventtime")==null) ? null : timeNormaliser(jsonObject.get("firsteventtime").toString());
		if (firsteventtime!=null) jsonObject.put("firsteventtime", firsteventtime);

		String firstautomationtime= (jsonObject.get("firstautomationtime")==null) ? null : timeNormaliser(jsonObject.get("firstautomationtime").toString());
		if (firstautomationtime!=null) jsonObject.put("firstautomationtime", firstautomationtime);

		String lastautomationtime= (jsonObject.get("lastautomationtime")==null) ? null : timeNormaliser(jsonObject.get("lastautomationtime").toString());
		if (lastautomationtime!=null) jsonObject.put("lastautomationtime", lastautomationtime);

		return jsonObject;
	}

	/**
	 * converts postgres json time format to normalised time format for matching to Elasticsearch 
	 * date_optional_time or strict_date_optional_time
	 * (A generic ISO datetime parser where the date is mandatory and the time is optional
	 * see https://www.elastic.co/guide/en/elasticsearch/reference/current/mapping-date-format.html#strict-date-time) 
	 * @param dbTimeStr
	 * @return
	 */
	public String timeNormaliser(String dbTimeStr){
		String normalisedTimeStr=null;

		Calendar alarmCreationCal=null;
		TimestampUtils timestampUtils= new TimestampUtils();
		Timestamp timestamp;
		try {
			timestamp = timestampUtils.toTimestamp(null, dbTimeStr);
			// using DatatypeConverter.printDateTime
			alarmCreationCal=Calendar.getInstance();
			alarmCreationCal.setTime(timestamp);
			normalisedTimeStr=  DatatypeConverter.printDateTime(alarmCreationCal);

			//alternative using simple date format
			//final String TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSX";
			//SimpleDateFormat simpleDateFormat = new SimpleDateFormat(TIME_FORMAT); 
			//timeStr=  simpleDateFormat.format(timestamp);

		} catch (Exception e) {
			LOG.error("cannot parse database json time string dbTimeStr"+dbTimeStr, e);
		}
		return normalisedTimeStr;

	}


	@Override
	public void init() {
		LOG.debug("initialising AlarmChangeNotificationClient");
		if (eventProxy == null)
			LOG.debug("OpenNMS event proxy not set - cannot send events to opennms");
	}

	@Override
	public void destroy() {
	}

}
